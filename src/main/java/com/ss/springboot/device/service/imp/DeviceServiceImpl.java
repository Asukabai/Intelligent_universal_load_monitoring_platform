package com.ss.springboot.device.service.imp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ss.springboot.common.DeviceConfig;
import com.ss.springboot.common.DeviceSocketUtil;
import com.ss.springboot.dto.DeviceReqData;
import com.ss.springboot.device.service.DeviceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class DeviceServiceImpl implements DeviceService {

    @Resource
    private DeviceSocketUtil socketUtil;
    @Resource
    private DeviceConfig deviceConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 新增读写锁
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();    // 读锁（轮询用）
    private final ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();  // 写锁（设置用）
    // 缓存12个通道的状态（key:通道号，value:动态字段Map）
    private final Map<Integer, Map<String, Object>> statusCache = new ConcurrentHashMap<>();

    // 初始化缓存
    @PostConstruct
    public void init() {
        for (int i = 1; i <= 12; i++) {
            statusCache.put(i, new HashMap<>()); // 初始化空Map
        }
    }

    //  轮询任务（读操作）使用读锁
    @Scheduled(fixedRate = 3000)
    public void pollAllChannels() throws JsonProcessingException {
        log.info("开始轮询所有通道状态...");
        //
        for (int channel = 1; channel <= 4; channel++) {
            try {
                int boardId = deviceConfig.getBoardIdByChannel(channel);
                int boardInnerChannel = deviceConfig.getBoardInnerChannel(channel);
                String command = "{\"reqID\":1,\"sender\":\"8888\",\"sendee\":\"dev_id\",\"method\":\"get_state_ch\",\"reqData\":{\"channel\":" + boardInnerChannel+ "}}" + "\r\n";
                log.info("通道号：{}，板号: {} 通道号: {}",channel, boardId, boardInnerChannel);
                // 加读锁
                readLock.lock();
                // 发送指令获取设备响应
                String response = socketUtil.sendCommand(boardId, command);
                if(response == null ||response.startsWith("ERROR:")) {
                    statusCache.put(channel, new HashMap<>());
                    log.warn("通道{}轮询响应为空，板号: {} 通道号: {}", channel, boardId, boardInnerChannel);
                    continue;
                }
                Map<String, Object> statusMap = parseResponse(channel, response);
                statusCache.put(channel, statusMap);
                log.info("通道号：{}轮询状态更新完成,板号: {} 通道号: {}", channel,boardId, boardInnerChannel);
            } finally {
                // 释放读锁
                readLock.unlock();
            }
        }
    }


    // 解析设备响应
    private Map<String, Object> parseResponse(int globalChannel, String response) throws JsonProcessingException {
        Map<String, Object> statusMap = new HashMap<>();
        // 1. 存入全局通道号
        statusMap.put("globalChannel", globalChannel);

        JsonNode rootNode = objectMapper.readTree(response);

        // 2. 提取顶层字段（非respData内的信息）
        statusMap.put("reqID", rootNode.get("reqID") != null ? rootNode.get("reqID").asInt() : null);
        statusMap.put("respID", rootNode.get("respID") != null ? rootNode.get("respID").asInt() : null);
        statusMap.put("result", rootNode.get("result") != null ? rootNode.get("result").asInt() : null);
        statusMap.put("msg", rootNode.get("msg") != null ? rootNode.get("msg").asText() : null);
        statusMap.put("sender", rootNode.get("sender") != null ? rootNode.get("sender").asText() : null);
        statusMap.put("sendee", rootNode.get("sendee") != null ? rootNode.get("sendee").asText() : null);
        statusMap.put("updateTime", new Date().toString()); // 服务器时间

        // 3. 提取respData内的所有字段
        JsonNode respDataNode = rootNode.get("respData");
        if (respDataNode != null) {
            Iterator<Map.Entry<String, JsonNode>> respFields = respDataNode.fields();
            while (respFields.hasNext()) {
                Map.Entry<String, JsonNode> field = respFields.next();
                String key = field.getKey();
                JsonNode valueNode = field.getValue();

                // 按类型转换并存储
                if (valueNode.isNumber()) {
                    statusMap.put(key, valueNode.numberValue());
                } else if (valueNode.isTextual()) {
                    statusMap.put(key, valueNode.textValue());
                } else if (valueNode.isBoolean()) {
                    statusMap.put(key, valueNode.booleanValue());
                } else if (valueNode.isArray()) {
                    List<Object> arrayList = new ArrayList<>();
                    for (JsonNode arrayItem : valueNode) {
                        if (arrayItem.isNumber()) {
                            arrayList.add(arrayItem.numberValue());
                        } else if (arrayItem.isTextual()) {
                            arrayList.add(arrayItem.textValue());
                        } else {
                            arrayList.add(arrayItem.toString());
                        }
                    }
                    statusMap.put(key, arrayList);
                } else {
                    statusMap.put(key, valueNode.toString());
                }
            }
        }

        return statusMap;
    }

    // 查询状态
    @Override
    public Map<String, Object> getChannelStatus(int channel) {
        if (channel < 1 || channel > 12) {
            throw new IllegalArgumentException("通道号必须为1-12");
        }
        // 返回一个新的Map副本，避免外部修改缓存
        return new HashMap<>(statusCache.get(channel));
    }

    //设置电流
    @Override
    public String setCurrent(DeviceReqData deviceReqData) throws JsonProcessingException {
        log.info("开始处理设置电流请求：{}", deviceReqData.toString());
        writeLock.lock();
        try {
            int channel=deviceReqData.getChannel();
            int boardInnerChannel = deviceConfig.getBoardInnerChannel(channel);
            String cmd = "{\"reqID\":1,\"sender\":\"8888\",\"sendee\":\"dev_id\",\"method\":\"set_dianliu\",\"reqData\":{\"channel\":" + boardInnerChannel + ",\"enable\":" + deviceReqData.getEnable() + "}}" + "\r\n";
            int boardId = deviceConfig.getBoardIdByChannel(channel);
            String result =  socketUtil.sendCommand(boardId, cmd);
            // 调用公共方法更新缓存
            updateCacheAfterSet(boardId, boardInnerChannel, channel, result);
            return result;
        } finally {
            // 释放写锁
            writeLock.unlock();
        }
    }

    //设置输出
    @Override
    public String setPut(DeviceReqData deviceReqData) throws JsonProcessingException {
        log.info("开始处理设置输出请求：{}", deviceReqData.toString());
        writeLock.lock();
        try {
            int channel=deviceReqData.getChannel();
            int boardInnerChannel = deviceConfig.getBoardInnerChannel(channel);
            String cmd = "{\"reqID\":1,\"sender\":\"8888\",\"sendee\":\"dev_id\",\"method\":\"set_shuchu\",\"reqData\":{\"channel\":" + boardInnerChannel + ",\"enable\":" + deviceReqData.getEnable() + "}}" + "\r\n";
            int boardId = deviceConfig.getBoardIdByChannel(channel);
            String result =  socketUtil.sendCommand(boardId, cmd);
            // 调用公共方法更新缓存
            updateCacheAfterSet(boardId, boardInnerChannel, channel, result);
            return result;
        }  finally {
            // 释放写锁
            writeLock.unlock();
        }
    }

    //设置使能
    @Override
    public String setEnable(DeviceReqData deviceReqData) throws JsonProcessingException {
        log.info("开始处理设置使能请求：{}", deviceReqData.toString());
        writeLock.lock();
        try {
            Integer channel=deviceReqData.getChannel();
            int boardInnerChannel = deviceConfig.getBoardInnerChannel(channel);
            String cmd = "{\"reqID\":1,\"sender\":\"8888\",\"sendee\":\"dev_id\",\"method\":\"set_shineng\",\"reqData\":{\"channel\":" + boardInnerChannel + ",\"enable\":" + deviceReqData.getEnable() + "}}" + "\r\n";
            int boardId = deviceConfig.getBoardIdByChannel(deviceReqData.getChannel());
            String result =  socketUtil.sendCommand(boardId, cmd);
            // 调用公共方法更新缓存
            updateCacheAfterSet(boardId, boardInnerChannel, channel, result);
            return result;
        } finally {
            // 释放写锁
            writeLock.unlock();
        }
    }

    //获取12个通道的状态
    @Override
    public List<Map<String, Object>> getAllChannelStatus() {
        readLock.lock();
        try {
            // 转换缓存的Map为有序的List（按通道号1-12排序）
            return IntStream.rangeClosed(1, 12)
                    .mapToObj(channel ->
                            new HashMap<>(statusCache.get(channel)))
                    .collect(Collectors.toList());
        } finally {
            readLock.unlock();
        }
    }

    private void updateCacheAfterSet(int boardId, int boardInnerChannel, int channel, String result) throws JsonProcessingException {
        if (!result.startsWith("ERROR:")) {
            String queryCmd = "{\"reqID\":1,\"sender\":\"8888\",\"sendee\":\"dev_id\",\"method\":\"get_state_ch\",\"reqData\":{\"channel\":" + boardInnerChannel + "}}" + "\r\n";
            String response = socketUtil.sendCommand(boardId, queryCmd);
            System.out.println();
            if (!response.startsWith("ERROR:")) {
                Map<String, Object> statusMap = parseResponse(channel, response);
                statusCache.put(channel, statusMap); // 立即更新缓存
            }
        }
    }
}