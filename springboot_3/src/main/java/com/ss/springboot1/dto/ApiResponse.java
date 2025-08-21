package com.ss.springboot1.dto;


import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse{
    private Integer reqID;
    private Integer respID;
    private String sendee;
    private Integer result;
    private String msg;
    private ObjectNode respData;

}