package org.example.reggie.config;

import lombok.extern.slf4j.Slf4j;
import org.example.reggie.common.JacksonObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

//WebMvc配置类
@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    /**
     * 设置静态资源映射（默认情况下，不能加载到 backend和front中的静态页面）
     * @param registry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始进行资源映射...");
        //将请求路径中有backend的请求映射到 backend目录下
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        //将请求路径中有front的请求映射到 front目录下
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }

    /**
     * 扩展消息转换器，使用自定义的对象转换器
     * @param converters：消息转换器对象列表
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("启用扩展消息转换器");
        //创建消息转换器
        MappingJackson2HttpMessageConverter messageConverter=new MappingJackson2HttpMessageConverter();
        //设置具体的对象映射器
        //使用自定义的对象映射器
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //通过设置索引，将自定义的消息映射器放在集合的最前面，确保使用自定义的对象转换器
        converters.add(0,messageConverter);
    }

}
