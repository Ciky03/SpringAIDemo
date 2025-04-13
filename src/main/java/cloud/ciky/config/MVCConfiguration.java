package cloud.ciky.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: ciky
 * @Description: MVC配置类
 * @DateTime: 2025/4/6 17:20
 **/
@Configuration
public class MVCConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedHeaders("*")
                .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
                .exposedHeaders("Content-Disposition");
    }
}
