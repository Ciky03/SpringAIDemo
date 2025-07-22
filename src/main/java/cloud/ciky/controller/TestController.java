package cloud.ciky.controller;


import com.alibaba.nacos.api.model.v2.Result;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author ciky
 * @since 2025/7/22 17:31
 */
@RequestMapping("/test")
@RestController
public class TestController {

    @Autowired
    private VectorStore vectorStore1;

    @Autowired
    private ChatClient pdfChatClient;

    /**
     * 上传文件
     */
    @RequestMapping("/upload")
    public Result uploadPdf(@RequestParam("file") MultipartFile file){

        Resource resource = file.getResource();
        //2.创建PDF的读取器
        PagePdfDocumentReader reader = new PagePdfDocumentReader(
                resource, // 文件源
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())  //文本格式化器
                        .withPagesPerDocument(1) // 每1页PDF作为一个Document
                        .build()
        );
        //3.读取PDF文档,拆分为Document
        List<Document> documents = reader.read();
        //4.写入向量库
        vectorStore1.add(documents);
        return Result.success();
    }

    @RequestMapping("/chat")
    public Flux<String> chat(@RequestParam("prompt") String prompt){
        return pdfChatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }
}
