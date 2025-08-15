package com.ss.springboot.dto;



import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.xml.transform.Source;

@Data
@AllArgsConstructor
public class ApiRequest implements Source {
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

    @Override
    public void setSystemId(String systemId) {
    }

    @Override
    public String getSystemId() {
        return null;
    }
}