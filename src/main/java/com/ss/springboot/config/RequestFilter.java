package com.ss.springboot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ss.springboot.common.RequestContext;
import com.ss.springboot.common.RespIDGenerator;
import com.ss.springboot.common.ResultCodeEnum;
import com.ss.springboot.dto.ApiRequest;
import com.ss.springboot.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(3) // 确保过滤器在其他处理之前执行
public class RequestFilter implements Filter {
    @Resource
    private  ObjectMapper objectMapper;
    @Resource
    private Validator validator;
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
//
//        String requestUri = httpRequest.getRequestURI();
//        // 定义需要跳过的接口（设置电流、使能、输出的接口）
//        boolean isSkipApi = requestUri.endsWith("/setCurrent")
//                || requestUri.endsWith("/setEnable")
//                || requestUri.endsWith("/setPut");
//
//        if (isSkipApi) {
//            // 直接放行，不经过ApiRequest解析和请求体重写
//            chain.doFilter(request, response);
//            return;
//        }
        // 检查是否需要跳过过滤
        String requestUri = httpRequest.getRequestURI();
        if (requestUri.startsWith("/device/")) {
            // 跳过过滤器，直接继续处理
            chain.doFilter(request, response);
            return;
        }
        // 仅处理JSON请求
        if (!isJsonRequest(httpRequest)) {
            // 非JSON请求直接继续
            chain.doFilter(request, response);
        } else {
            try {
                // 读取原始请求体
                String requestBody = readRequestBody(httpRequest);
                log.info("请求体内容: {}", requestBody);

                if (requestBody.isEmpty()) {
                    // 处理空请求体
                    chain.doFilter(request, response);
                } else {
                    // 解析统一请求格式
                    ApiRequest apiRequest = objectMapper.readValue(
                            requestBody, ApiRequest.class);

                    Set<ConstraintViolation<ApiRequest>> violations = validator.validate(apiRequest);
                    if (!violations.isEmpty()) {
                        // 验证失败，抛出异常（由全局异常处理器处理）
                        handleValidationFailure(httpResponse, violations);
                        return; // 中断过滤器链，阻止请求到达控制器
                    }
                    // 设置请求上下文
                    setRequestContext(apiRequest);
                    log.info("解析后获取的reqID: {}", apiRequest.getReqID());
                    // 创建包含reqData的新请求体
                    String newRequestBody = objectMapper.writeValueAsString(apiRequest.getReqData());
                    log.info("新的请求体"+newRequestBody);
                    byte[] newRequestBodyBytes = newRequestBody.getBytes(StandardCharsets.UTF_8);

                    // 创建可重复读取的请求包装器
                    HttpServletRequestWrapper requestWrapper = new RequestWrapper(httpRequest, newRequestBodyBytes);

                    // 继续过滤链，传递包装后的请求
                    chain.doFilter(requestWrapper, httpResponse);
                }
            } catch (Exception e) {
                log.error("处理请求时发生错误" + e.getMessage());
                // 发生异常时继续处理，由后续验证逻辑处理错误
                chain.doFilter(request, response);
            } finally {
                RequestContext.clear();
            }
        }
    }

    private void handleValidationFailure(HttpServletResponse httpResponse, Set<ConstraintViolation<ApiRequest>> violations) throws IOException {
        httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        httpResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Integer reqID = RequestContext.getreqID();
        if (reqID == null) {
            reqID = 999; // 空值填充999
        }
        String sendee = RequestContext.getSendee(); // 空值保持null
        int respID = RespIDGenerator.next("error");

        String errorMsg = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));

        ApiResponse errorResponse = new ApiResponse(
                reqID,
                respID,
                sendee,
                ResultCodeEnum.PARAM_ERROR.getCode(),
                "参数验证失败: " + errorMsg,
                null
        );
        objectMapper.writeValue(httpResponse.getWriter(), errorResponse);
    }

    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.contains("application/json");
    }

    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private void setRequestContext(ApiRequest apiRequest) {
        RequestContext.setreqID(apiRequest.getReqID());
        RequestContext.setSender(apiRequest.getSender());
        RequestContext.setSendee(apiRequest.getSendee());
    }

}