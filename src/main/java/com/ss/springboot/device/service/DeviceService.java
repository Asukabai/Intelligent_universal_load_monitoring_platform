package com.ss.springboot.device.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.ss.springboot.dto.DeviceReqData;

import java.util.List;
import java.util.Map;

public interface DeviceService {

    Map<String, Object> getChannelStatus(int channel);
    //设置电流
    String setCurrent(DeviceReqData deviceReqData) throws JsonProcessingException;
    //设置输出
    String setPut(DeviceReqData deviceReqData) throws JsonProcessingException;
    // 设置使能
    String setEnable(DeviceReqData deviceReqData) throws JsonProcessingException;
    // 新增：批量获取所有通道状态
    List<Map<String, Object>> getAllChannelStatus();
}