package com.itheima.reggie.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import com.itheima.reggie.common.JacksonObjectMapper;
import com.itheima.reggie.interceptor.MyInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.List;

//为了能使resource目录下的静态资源文件在路径上直接访问
//需要对resources下的文件进行放行
//或者放在static文件夹下
@Slf4j
@Configuration
@EnableSwagger2
@EnableKnife4j
public class WebMvcConfig extends WebMvcConfigurationSupport {
    /**
     * 设置静态资源映射
     * @param registry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始进行静态资源映射");
        // 对所有/backend/**开头的请求，添加映射到resources下的backend文件夹下
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * 拓展mvc框架的消息转换器（实现json转换为java对象）
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("拓展消息转换器...");
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        //设置对象转换器，底层使用Jaskson将java对象转为json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将上面的消息转换器对象追加到mvc框架的转换器集合中
        converters.add(0,messageConverter);//0表示放在最前面，最优先使用
        super.extendMessageConverters(converters);
    }

//    //拦截器配置
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        String[] urls = new String[]{ //放行的请求
//                "/employee/login",
//                "/employee/logout",
//                "/backend/**", // 静态资源不用处理都放行
//                "/front/**",
//                "/common/**",
//                "/user/sendMsg", //移动端发送短信
//                "/user/login"   //移动端登录
//        };
//        registry.addInterceptor(new MyInterceptor()) //拦截器注册对象
//                .addPathPatterns("/**") //指定要拦截的请求
//                .excludePathPatterns(urls); //排除请求
//
//    }

    //生成接口文件，根据basePackage包下每一个类生成一个接口
    //可以用来后期维护和测试
    @Bean
    public Docket createRestApi() {
        //文档类型
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.itheima.reggie.controller"))
                .paths(PathSelectors.any())
                .build();
    }
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("瑞吉外卖")
                .version("1.0")
                .description("瑞吉外卖接口文档")
                .build();
    }

}
