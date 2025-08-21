package com.ss.springboot1.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ss.springboot1.common.RequestContext;
import com.ss.springboot1.common.RespIDGenerator;
import com.ss.springboot1.common.ResultCodeEnum;
import com.ss.springboot1.dto.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Method;

@RestControllerAdvice(basePackages = "com.ss.springboot1.book.controller")
public class BookResponseAdvice implements ResponseBodyAdvice<Object> {


    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 若控制器是 DeviceController，直接跳过封装
        return true;
    }


    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        // 1. 获取请求上下文（需确保非空，否则手动设置默认值）
        Integer reqID = RequestContext.getreqID();
        String sendee = RequestContext.getSendee();

        // 2. 生成 respID（根据方法名）
        Method method = returnType.getMethod();
        String methodName = method.getName();
        int respID = RespIDGenerator.next(methodName);

        // 3. 判定响应状态码
        ResultCodeEnum resultCode = determineResultCode(body, methodName);


        // 4. 将 body 转换为 Jackson 的 ObjectNode
        ObjectNode respData = convertToObjectNode(body);

        // 5. 封装统一响应
        return new ApiResponse(
                reqID,
                respID,
                sendee,
                resultCode.getCode(),
                resultCode.getMsg(),
                respData
        );
    }

    private ObjectNode convertToObjectNode(Object body) {
        if (body == null) {
            return objectMapper.createObjectNode(); // 空对象
        }
        if (body instanceof ObjectNode) {
            return (ObjectNode) body; // 已是 ObjectNode，直接返回
        }
        try {
            JsonNode jsonNode = objectMapper.valueToTree(body); // 转换为 JsonNode
            if (jsonNode.isObject()) {
                return (ObjectNode) jsonNode; // 是对象类型，强转
            } else {
                // 非对象类型（如基本类型、数组），包装为 {"data": value}
                ObjectNode wrapper = objectMapper.createObjectNode();
                wrapper.set("data", jsonNode); // 用 set 方法兼容所有 JsonNode 类型
                return wrapper;
            }
        } catch (Exception e) {
            // 捕获异常，明确错误原因
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("error", "转换响应体失败: " + e.getMessage());
            return errorNode;
        }
    }

    private ResultCodeEnum determineResultCode(Object result, String methodName) {
        // 原有逻辑保持不变
        if (result == null) {
            if (methodName.startsWith("insert")) return ResultCodeEnum.InsertFailed;
            if (methodName.startsWith("update")) return ResultCodeEnum.UpdateFailed;
            if (methodName.startsWith("delete")) return ResultCodeEnum.DeleteFailed;
            return ResultCodeEnum.NOT_FOUND;
        }
        if (result instanceof Integer && (Integer) result == -1) {
            if (methodName.startsWith("update")) return ResultCodeEnum.UpdateFailed;
            if (methodName.startsWith("delete")) return ResultCodeEnum.DeleteFailed;
        }
        return ResultCodeEnum.SUCCESS;
    }
}