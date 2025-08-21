package com.ss.springboot1.config;

import com.ss.springboot1.common.RequestContext;
import com.ss.springboot1.common.RespIDGenerator;
import com.ss.springboot1.common.ResultCodeEnum;
import com.ss.springboot1.dto.ApiResponse;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.ss.springboot1.book.controller")
public class BookGlobalExceptionHandler {

    // 处理IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse handleIllegalArgumentException(IllegalArgumentException e) {
        log.error(e.getMessage());
        Integer reqID = RequestContext.getreqID();
        String sendee = RequestContext.getSendee();
        int respID = RespIDGenerator.next("error");
        ResultCodeEnum resultCode = mapExceptionToResultCode(e);
        return new ApiResponse(
                reqID,
                respID,
                sendee,
                resultCode.getCode(),
                resultCode.getMsg(),
                null
        );
    }

    // 处理通用异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse handleGeneralException(Exception e) {
        log.error(e.getMessage());
        e.printStackTrace();
        Integer reqID = RequestContext.getreqID();
        String sendee = RequestContext.getSendee();
        int respID = RespIDGenerator.next("error");
        log.error(e.getMessage());
                e.printStackTrace();
        return new ApiResponse(
                reqID,
                respID,
                sendee,
                ResultCodeEnum.SYSTEM_ERROR.getCode(),
                ResultCodeEnum.SYSTEM_ERROR.getMsg(),
                null
        );
    }

    // 保留原映射方法
    private ResultCodeEnum mapExceptionToResultCode(IllegalArgumentException e) {
        String message = e.getMessage();
        if (message.contains("书名已存在")) {
            return ResultCodeEnum.HaveBook;
        } else if (message.contains("id找不到")) {
            return ResultCodeEnum.NotId;
        } else if (message.contains("不存在")) {
            return ResultCodeEnum.NotFoundBook;
        } else {
            return ResultCodeEnum.PARAM_ERROR;
        }
    }
}