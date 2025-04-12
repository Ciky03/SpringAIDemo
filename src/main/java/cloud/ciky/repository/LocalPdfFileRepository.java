package cloud.ciky.repository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.groovy.json.internal.IO;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.lang.module.FindException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Properties;

/**
 * @Author: ciky
 * @Description: 文件接口(本地磁盘存储)
 * @DateTime: 2025/4/12 11:49
 **/
@Slf4j
@Component
@RequiredArgsConstructor
    public class LocalPdfFileRepository implements FileRepository{

    //持久化向量库
    private final VectorStore vectorStore;

    // 会话id与文件名的映射关系
        // properties是键值对集合(自带持久化存储能力)
    private final Properties chatFiles = new Properties();

    /**
     * 保存文件方法
     */
    @Override
    public boolean save(String chatId, Resource resource) {
        //1.保存到本地磁盘(或保存到其他oss服务)
        String filename = resource.getFilename();
        File target = new File(Objects.requireNonNull(filename));
        if(!target.exists()){
            try {
                Files.copy(resource.getInputStream(), target.toPath());
            }catch (IOException e){
                log.error("Failed to save PDF resource",e);
                return false;
            }
        }

        //2.保存映射关系
        chatFiles.setProperty(chatId,filename);
        return false;
    }

    /**
     * 获取文件方法
     */
    @Override
    public Resource getFile(String chatId) {
        return new FileSystemResource(chatFiles.getProperty(chatId));
    }

    /**
     * 初始化方法:读取持久化文件
     */
    @PostConstruct
    private void init(){
        //1.读取持久化文件:会话ID与文件的映射关系(chat-pdf.properties)
        FileSystemResource pdfResource = new FileSystemResource("chat-pdf.properties");
        if(pdfResource.exists()){
            try {
                chatFiles.load(new BufferedReader(new InputStreamReader(pdfResource.getInputStream())));
            }catch (IOException e){
                throw new RuntimeException(e);
            }
        }

        //2.读取持久化文件: 向量库持久化(chat-pdf.json)
        FileSystemResource vectorResource = new FileSystemResource("chat-pdf.json");
        if(vectorResource.exists()){
            SimpleVectorStore simpleVectorStore = (SimpleVectorStore) vectorStore;
            simpleVectorStore.load(vectorResource);
         }
    }

    /**
     * 销毁方法:写入文件持久化
     */
    @PreDestroy
    private void persistent(){
        try {
            chatFiles.store(new FileWriter("chat-pdf.properties"), LocalDateTime.now().toString());
            SimpleVectorStore simpleVectorStore = (SimpleVectorStore) vectorStore;
            simpleVectorStore.save(new File("chat-pdf.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
