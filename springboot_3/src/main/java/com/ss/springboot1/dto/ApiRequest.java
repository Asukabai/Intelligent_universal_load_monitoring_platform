package com.ss.springboot1.dto;


import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiRequest {
    @NotNull(message = "reqID不能为空") // 非空验证
    @Positive(message = "reqID必须为正数") // 正数验证
    private Integer reqID;

    @NotBlank(message = "sender不能为空") // 非空字符串（不允许空白字符）
    private String sender;

    @NotBlank(message = "sendee不能为空")
    private String sendee;

    @NotBlank(message = "method不能为空")
    private String method;

    @NotNull(message = "reqData不能为空") // 非空验证（ObjectNode不能为null）
    private ObjectNode reqData;


}