package cloud.ciky.repository;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * @Author: ciky
 * @Description: 文件接口
 * @DateTime: 2025/4/12 11:45
 **/
@Component
public interface FileRepository {

    /**
     * 保存文件,记录chatId与文件映射关系
     * @param chatId    回话ID
     * @param resource  文件
     */
    boolean save(String chatId, Resource resource);

    /**
     * 根据chatId获取文件
     * @param chatId    会话ID
     */
    Resource getFile(String chatId);
}
