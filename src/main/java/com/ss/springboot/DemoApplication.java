package com.ss.springboot;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

// 暂不需要数据库功能，可以在主应用类上添加排除数据库自动配置的注解：
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableScheduling // 添加此注解以启用定时任务
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
