package com.itheima.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j  //注解（lombok提供的），可以直接使用log.info输出日志
@SpringBootApplication
@ServletComponentScan   //组件扫描（WebFilter）
@EnableTransactionManagement    // 开启事务管理
@EnableCaching  //开启SpringCache注解缓存功能
public class ReggieApplication {
    public static void main(String[] args){
        SpringApplication.run(ReggieApplication.class,args);
        log.info("项目启动成功！");
    }
}
