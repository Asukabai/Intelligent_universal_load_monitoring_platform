package com.ss.springboot.common;



// 请求上下文工具类
public class RequestContext {
    private static final ThreadLocal<Integer> REQ_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> SEEDER = new ThreadLocal<>();
    private static final ThreadLocal<String> SEEDEE = new ThreadLocal<>();

    public static void setreqID(Integer reqID) {
        REQ_ID.set(reqID);
    }

    public static Integer getreqID() {
        return REQ_ID.get();
    }

    public static void setSender(String sender) {
        SEEDER.set(sender);
    }

    public static String getSender() {
        return SEEDER.get();
    }

    public static void setSendee(String sendee) {
        SEEDEE.set(sendee);
    }

    public static String getSendee() {
        return SEEDEE.get();
    }

    public static void clear() {
        REQ_ID.remove();
        SEEDER.remove();
        SEEDEE.remove();
    }
}


