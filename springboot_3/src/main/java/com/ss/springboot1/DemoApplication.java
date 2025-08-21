package com.ss.springboot1;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;


//@SpringBootApplication：核心注解，组合了：
//@Configuration：标记该类为配置类。
//@EnableAutoConfiguration：启用Spring Boot的自动配置机制。
//@ComponentScan：启用组件扫描，自动发现和注册Spring组件。
//@EnableAspectJAutoProxy()：启用面向切面编程（AOP）的支持。
//@EnableScheduling：启用定时任务功能。
@SpringBootApplication
@EnableAspectJAutoProxy()
@EnableScheduling
//public class DemoApplication {
//    public static void main(String[] args) {
//
//        SpringApplication.run(DemoApplication.class, args);
//    }
//}

//3.提供SpringBoot项目启动器（初始化项目内部Servite组件）
// 继承 SpringBootServletInitializer 是为了支持将应用打包为WAR文件并部署到外部Servlet容器（如Tomcat）。
public class DemoApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {

        SpringApplication.run(DemoApplication.class, args);
    }
//4.重写一个父类中的方法configure(),执行初始化项目启动类是谁（ DemoApplication）
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(DemoApplication.class);
    }
}
