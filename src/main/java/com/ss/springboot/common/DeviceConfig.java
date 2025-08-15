// com.ss.springboot1.common.DeviceConfig.java
package com.ss.springboot.common;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

/**
 * 设备配置类，管理多块板子的IP和端口
 */
@Component
public class DeviceConfig {
    // 端口号（所有板子共用）
    private static final int COMMON_PORT = 16861;
    // 板子IP映射（key:板号 1-3，value:IP地址）
    private static final Map<Integer, String> BOARD_IP_MAP = new HashMap<>();

    static {
        // 初始化IP配置（目前仅连接1号板，2、3号板预留）
        BOARD_IP_MAP.put(1, "172.18.104.149");  // 已连接的1号板
        BOARD_IP_MAP.put(2, "172.18.104.140");  // 预留2号板IP
        BOARD_IP_MAP.put(3, "172.18.104.141");  // 预留3号板IP
    }

    // 根据板号获取IP
    public String getIpByBoardId(int boardId) {
        return BOARD_IP_MAP.get(boardId);
    }

    // 获取共用端口
    public int getCommonPort() {
        return COMMON_PORT;
    }

    // 计算通道对应的板号（1-4→1号板，5-8→2号板，9-12→3号板）
    public int getBoardIdByChannel(int channel) {
        if (channel < 1 || channel > 12) {
            throw new IllegalArgumentException("通道号必须为1-12");
        }
        return (channel - 1) / 4 + 1;
    }

    public int getBoardInnerChannel(int channel){
        return  (channel- 1) % 4 + 1;
    }
}