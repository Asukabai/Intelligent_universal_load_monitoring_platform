package com.ss.springboot.config;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Order(1)
public class UrlFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String originalUri = request.getRequestURI();

        // 判断是否是静态资源请求，这里简单通过后缀判断，可根据实际情况完善
        if (originalUri.endsWith(".html") || originalUri.endsWith(".css") || originalUri.endsWith(".js")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String[] split = originalUri.split("/");
            if (split.length < 5) {
                throw new Exception("URL格式错误，缺少安全参数或业务路径");
            }
            String token = split[1];
            String sign= split[2];

            if (!token.equals("springboot1")) {
                throw new Exception("安全参数1错误");
            }
            if (!sign.equals("server")) {
                throw new Exception("安全参数2错误");
            }
            StringBuilder newPath = new StringBuilder();
            for(int i=3;i<split.length;i++){
                newPath.append("/").append(split[i]);
            }
            String newUrl = newPath.toString();

            HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request){
                @Override
                public String getRequestURI() {
                    return newUrl;
                }
            };

            filterChain.doFilter(wrappedRequest, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("安全验证失败：" + e.getMessage());
        }
    }
}
