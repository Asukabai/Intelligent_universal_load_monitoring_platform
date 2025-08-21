package com.ss.springboot1.common;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {

    SUCCESS(200, "成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    NOT_FOUND(404, "书籍不存在"),
    SYSTEM_ERROR(500, "系统异常"),
    InsertFailed(106,"添加失败"),
    HaveBook(105,"书籍存在,添加失败"),
    DeleteFailed(104,"删除失败"),
    UpdateFailed(103,"修改失败"),
    NotId(102,"id不存在"),

    NotFoundBook(101,"该书不存在");
    private final Integer code;
    private final String msg;

    ResultCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}