package com.ss.springboot.device.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.ss.springboot.dto.DeviceReqData;
import com.ss.springboot.device.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/device")
public class DeviceController {
    @Resource
    private DeviceService deviceService;

    //查询状态
    @GetMapping("/getStatus")
    public ResponseEntity<?> getStatus(@RequestParam int channel) {
        try {
            Map<String, Object> statusMap = deviceService.getChannelStatus(channel);
            return ResponseEntity.ok(statusMap);
        }catch (Exception e) {
            log.error("查询通道状态失败，通道号：{}", channel, e);
            return ResponseEntity.badRequest().body("通道号查询失败");
        }
    }

    //设置电流
    @PostMapping("/setCurrent")
    public ResponseEntity<String> setCurrent(@RequestBody DeviceReqData deviceReqData) {
        log.info("接收到设置电流请求");

        String result = null;
        try {
            result = deviceService.setCurrent(deviceReqData);

            if (result.startsWith("ERROR:")) {
                String errorMsg = result.substring(6);
                return ResponseEntity.badRequest().body(errorMsg);
            } else {
                return ResponseEntity.ok(result);
            }
        } catch (JsonProcessingException e) {
            log.error("设备响应格式错误，设置电流失败：", e);
            return ResponseEntity.badRequest().body("设备响应格式错误，请检查设备状态");
        } catch (RuntimeException e) {
            log.error("设置电流失败", e);
            return ResponseEntity.badRequest().body("操作失败");
        }
    }

    //设置输出
    @PostMapping("/setPut")
    public ResponseEntity<String> setPut(@RequestBody DeviceReqData deviceReqData) {
        log.info("接收到设置输出请求");
        try {
            String result = deviceService.setPut(deviceReqData);
            if (result.startsWith("ERROR:")) {
                String errorMsg = result.substring(6);
                return ResponseEntity.badRequest().body(errorMsg);
            } else {
                return ResponseEntity.ok(result);
            }
        } catch (JsonProcessingException e) {
            log.error("设备响应格式错误，设置输出失败：", e);
            return ResponseEntity.badRequest().body("设备响应格式错误，请检查设备状态");
        } catch (RuntimeException e) {
            log.error("设置输出失败", e);
            return ResponseEntity.badRequest().body("操作失败");
        }
    }

    //设置使能
    @PostMapping("/setEnable")
    public ResponseEntity<String> setEnable(@RequestBody DeviceReqData deviceReqData) {
        log.info("接收到设置使能请求");
        try {
            String result = deviceService.setEnable(deviceReqData);
            if (result.startsWith("ERROR:")) {
                String errorMsg = result.substring(6);
                return ResponseEntity.badRequest().body(errorMsg);
            } else {
                return ResponseEntity.ok(result);
            }
        } catch (JsonProcessingException e) {
            log.error("设备响应格式错误,设置使能失败：", e);
            return ResponseEntity.badRequest().body("设备响应格式错误，请检查设备状态");
        } catch (RuntimeException e) {
            log.error("设置使能失败", e);
            return ResponseEntity.badRequest().body("操作失败");
        }
    }

    //获取12个通道的状态
    @GetMapping("/getAllStatus")
    public ResponseEntity<?> getAllStatus() {
        return ResponseEntity.ok(deviceService.getAllChannelStatus());
    }
}