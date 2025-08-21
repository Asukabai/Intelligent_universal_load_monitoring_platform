package com.ss.springboot1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeviceReqData {
    private Integer channel; // 通道号（1-12）
    private Integer enable;//状态（1=开启，0=关闭）
}
