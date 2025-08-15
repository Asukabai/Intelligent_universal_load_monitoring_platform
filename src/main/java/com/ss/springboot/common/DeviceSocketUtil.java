package com.ss.springboot.common;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class DeviceSocketUtil {
    @Resource
    private DeviceConfig deviceConfig;  // 注入配置类

    // 重载sendCommand方法，支持指定板号
    public String sendCommand(int boardId, String command) {
        String host = deviceConfig.getIpByBoardId(boardId);
        int port = deviceConfig.getCommonPort();
        return sendCommand(host, port, command);
    }

    // 通信方法，修改返回方法返回具体错误信息
    private String sendCommand(String host, int port, String command) {
        // 最多重试2次（首次失败后重试）
        for (int retry = 0; retry < 2; retry++) {
            StringBuilder response = new StringBuilder();
            try (Socket socket = new Socket()) {
                try {
                    socket.connect(new InetSocketAddress(host, port), 1500);
                } catch (SocketTimeoutException e) {
                    String error = "连接设备超时（1.5秒）：" + host + ":" + port;
                    log.warn("第{}次重试失败：{}", retry + 1, error);
                    if (retry == 1) return "ERROR:" + error; // 最后一次重试失败才返回
                    continue; // 重试
                } catch (IOException e) {
                    String error = "连接设备失败：" + e.getMessage();
                    log.error("第{}次重试失败：{}", retry + 1, error);
                    if (retry == 1) return "ERROR:" + error;
                    continue;
                }

                try (OutputStream out = socket.getOutputStream();
                     BufferedReader in = new BufferedReader(
                             new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

                    out.write(command.getBytes(StandardCharsets.UTF_8));
                    out.flush();
                    socket.shutdownOutput();
                    socket.setSoTimeout(2000);

                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    // 成功读取响应，直接返回
                    log.info("与设备通信完成，响应内容：{}", response);
                    return response.toString();

                } catch (SocketException e) {
                    // 针对“连接重置”错误进行重试
                    String error = "与设备通信时连接被重置：" + e.getMessage();
                    log.warn("第{}次重试失败：{}", retry + 1, error);
                    if (retry == 1) return "ERROR:" + error;
                    // 重试前短暂等待（给设备恢复时间）
                    Thread.sleep(500);
                } catch (Exception e) {
                    // 其他错误处理（略）
                    String error = "通信异常：" + e.getMessage();
                    log.error("第{}次重试失败：{}", retry + 1, error);
                    if (retry == 1) return "ERROR:" + error;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "ERROR:重试被中断";
            } catch (Exception e) {
                log.error("Socket资源释放异常", e);
                return "ERROR:资源释放失败";
            }
        }
        return "ERROR:超过最大重试次数";
    }
}