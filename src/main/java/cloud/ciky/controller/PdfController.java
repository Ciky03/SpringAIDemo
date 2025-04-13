package cloud.ciky.controller;

import cloud.ciky.entity.vo.Result;
import cloud.ciky.repository.ChatHistoryRepository;
import cloud.ciky.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Objects;

/**
 * @Author: ciky
 * @Description: 文件操作接口
 * @DateTime: 2025/4/12 17:29
 **/
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai/pdf")
public class PdfController {

    private final FileRepository fileRepository;

    private final VectorStore vectorStore;

    private final ChatHistoryRepository chatHistoryRepository;

    @Autowired
    @Qualifier("pdfChatClient")
    private  ChatClient pdfChatClient;

    /**
     * 上传文件
     */
    @RequestMapping("/upload/{chatId}")
    public Result uploadPdf(@PathVariable("chatId") String chatId, @RequestParam("file")MultipartFile file){
        try {
            //1.校验文件是否为PDF格式
            if(!Objects.equals(file.getContentType(),"application/pdf")){
                return Result.fail("只能上传PDF文件");
            }

            //2.保存文件
            boolean success = fileRepository.save(chatId, file.getResource());
            if(!success){
                return Result.fail("文件保存失败");
            }

            //3.写入向量库
            this.writeToVectorStore(file.getResource());
            return Result.ok();
        }catch (Exception e){
            log.error("Failed to upload PDF file",e);
            return Result.fail("上传文件失败");
        }
    }

    /**
     * 文件下载
     */
    @GetMapping("/file/{chatId}")
    public ResponseEntity<Resource> downloadPdf(@PathVariable String chatId) throws IOException {
        //1.读取文件
        Resource resource = fileRepository.getFile(chatId);
        if(!resource.exists()){
            return ResponseEntity.notFound().build();
        }

        //2.文件名编码,写入响应头
        String filename = URLEncoder.encode(Objects.requireNonNull(resource.getFilename()), "UTF-8");

        //3.返回文件
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition","attachment; filename=\""+filename+"\"")
                .body(resource);
        /*
        *  APPLICATION_OCTET_STREAM:表示这是一个二进制流文件
        *  Content-Disposition: attachment; [filename=value]
        *  attachment:表示下载文件 (inline:表示在浏览器中打开)
        *  filename=value:表示下载的文件名
        */
    }

    @RequestMapping(value = "/chat",produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestParam("prompt") String prompt, @RequestParam("chatId") String chatId){
        //1.找到会话文件
        Resource file = fileRepository.getFile(chatId);
        if(!file.exists()){
            //文件不存在,不会答
            throw new RuntimeException("会话文件不存在");
        }
        //2.保存会话ID
        chatHistoryRepository.save("pdf",chatId);
        //3.请求模型
        return pdfChatClient.prompt()
                .user(prompt)   //传入user提示词
                .advisors(a -> a.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY,chatId))   //添加会话id到AdvisorContext
                .advisors(a -> a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, "file_name == '"+ file.getFilename()+"'"))  //添加文件名过滤器
                .stream()   //获取流式响应
                .content();
    }




    /** 提取方法: 写入向量库  */
    private void writeToVectorStore(Resource resource){
        //1.创建PDF的读取器
        PagePdfDocumentReader reader = new PagePdfDocumentReader(
                resource, // 文件源
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())  //文本格式化器
                        .withPagesPerDocument(1) // 每1页PDF作为一个Document
                        .build()
        );
        //2.读取PDF文档,拆分为Document
        List<Document> documents = reader.read();
        //3.写入向量库
        vectorStore.add(documents);
    }
}
