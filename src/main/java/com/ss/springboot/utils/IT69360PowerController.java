package com.ss.springboot.utils;

import jssc.SerialPort;
import jssc.SerialPortException;
import java.io.IOException;

/**
 * IT69360电源RS232串口控制工具类 (使用JSSC库)
 */
public class IT69360PowerController {

    private SerialPort serialPort;
    private String portName;
    private int baudRate;

    /**
     * 构造函数
     * @param portName 串口名称 (如 "COM3")
     * @param baudRate 波特率 (通常为9600)
     */
    public IT69360PowerController(String portName, int baudRate) {
        this.portName = portName;
        this.baudRate = baudRate;
    }

    /**
     * 连接电源设备
     * @return 连接是否成功
     * @throws Exception 连接异常
     */
    public boolean connect() throws Exception {
        try {
            // 创建串口对象
            serialPort = new SerialPort(portName);

            // 打开串口并设置参数
            boolean isOpened = serialPort.openPort();
            if (!isOpened) {
                throw new Exception("无法打开串口: " + portName);
            }

            // 设置串口参数: 波特率, 数据位, 停止位, 校验位
            boolean isSet = serialPort.setParams(baudRate,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            // 添加更详细的错误信息
            if (!isSet) {
                // 尝试其他常见参数组合
                boolean alternativeSet = tryAlternativeParams();
                if (!alternativeSet) {
                    throw new Exception("无法设置串口参数: " + portName +
                            "，波特率: " + baudRate +
                            "，请检查串口是否被占用或设备是否连接正确");
                }
            }
            return true;
        } catch (SerialPortException e) {
            throw new Exception("连接串口时发生错误: " + e.getMessage() +
                    "，错误类型: " + e.getExceptionType(), e);
        }
    }

    /**
     * 尝试替代的串口参数设置
     * @return 是否设置成功
     */
    private boolean tryAlternativeParams() {
        try {
            // 尝试不同的停止位和校验位组合
            return serialPort.setParams(baudRate,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_EVEN) ||
                    serialPort.setParams(baudRate,
                            SerialPort.DATABITS_8,
                            SerialPort.STOPBITS_1,
                            SerialPort.PARITY_ODD) ||
                    serialPort.setParams(baudRate,
                            SerialPort.DATABITS_8,
                            SerialPort.STOPBITS_2,
                            SerialPort.PARITY_NONE);
        } catch (SerialPortException e) {
            return false;
        }
    }

    /**
     * 检查串口是否可用
     * @return 串口状态信息
     */
    public String checkPortStatus() {
        try {
            if (serialPort == null) {
                return "串口对象未初始化";
            }

            if (serialPort.isOpened()) {
                return "串口已打开";
            } else {
                return "串口未打开";
            }
        } catch (Exception e) {
            return "检查串口状态时发生错误: " + e.getMessage();
        }
    }


    /**
     * 断开连接
     */
    public void disconnect() {
        if (serialPort != null && serialPort.isOpened()) {
            try {
                serialPort.closePort();
            } catch (SerialPortException e) {
                // 记录日志或忽略关闭异常
            }
            serialPort = null;
        }
    }

    /**
     * 发送命令并获取响应
     * @param command 命令字符串
     * @return 设备响应
     * @throws IOException IO异常
     */
    private String sendCommand(String command) throws IOException {
        if (serialPort == null || !serialPort.isOpened()) {
            throw new IOException("串口未连接");
        }

        try {
            // 清空输入缓冲区
            serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);

            // 添加换行符
            String cmdWithNewLine = command + "\n";

            // 发送命令
            serialPort.writeString(cmdWithNewLine, "UTF-8");

            // 等待响应
            Thread.sleep(150);

            // 读取响应
            byte[] responseBytes = serialPort.readBytes();
            if (responseBytes != null) {
                String response = new String(responseBytes, "UTF-8");
                return response.trim();
            }
            return "";
        } catch (SerialPortException e) {
            throw new IOException("发送命令时发生错误: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("等待响应时被中断", e);
        }
    }

    /**
     * 获取设备标识
     * @return 设备标识信息
     * @throws IOException IO异常
     */
    public String getDeviceIdentification() throws IOException {
        return sendCommand("*IDN?");
    }

    /**
     * 进入远程模式
     * @throws IOException IO异常
     */
    public void enterRemoteMode() throws IOException {
        sendCommand("SYST:REM");
    }

    /**
     * 设置电流值
     * @param current 电流值(A)
     * @throws IOException IO异常
     */
    public void setCurrent(double current) throws IOException {
        sendCommand("CURR " + current);
    }

    /**
     * 查询当前电流设置
     * @return 当前电流值
     * @throws IOException IO异常
     */
    public String getCurrent() throws IOException {
        return sendCommand("CURR?");
    }

    /**
     * 设置电压值
     * @param voltage 电压值(V)
     * @throws IOException IO异常
     */
    public void setVoltage(double voltage) throws IOException {
        sendCommand("VOLT " + voltage);
    }

    /**
     * 查询当前电压设置
     * @return 当前电压值
     * @throws IOException IO异常
     */
    public String getVoltage() throws IOException {
        return sendCommand("VOLT?");
    }

    /**
     * 设置过流保护值
     * @param current 电流保护值(A)
     * @throws IOException IO异常
     */
    public void setCurrentProtection(double current) throws IOException {
        sendCommand("CURR:PROT " + current);
    }

    /**
     * 查询过流保护值
     * @return 过流保护值
     * @throws IOException IO异常
     */
    public String getCurrentProtection() throws IOException {
        return sendCommand("CURR:PROT?");
    }

    /**
     * 设置过压保护值
     * @param voltage 电压保护值(V)
     * @throws IOException IO异常
     */
    public void setVoltageProtection(double voltage) throws IOException {
        sendCommand("VOLT:PROT " + voltage);
    }

    /**
     * 查询过压保护值
     * @return 过压保护值
     * @throws IOException IO异常
     */
    public String getVoltageProtection() throws IOException {
        return sendCommand("VOLT:PROT?");
    }

    /**
     * 开启输出
     * @throws IOException IO异常
     */
    public void outputOn() throws IOException {
        sendCommand("OUTP ON");
    }

    /**
     * 关闭输出
     * @throws IOException IO异常
     */
    public void outputOff() throws IOException {
        sendCommand("OUTP OFF");
    }

    /**
     * 查询电源工作状态
     * @return 状态码 (0:关闭, 1:CV模式, 2:CC模式, 3:错误)
     * @throws IOException IO异常
     */
    public String getWorkingStatus() throws IOException {
        return sendCommand("STAT:QUES:COND?");
    }

    /**
     * 清除电压保护状态
     * @throws IOException IO异常
     */
    public void clearVoltageProtection() throws IOException {
        sendCommand("VOLT:PROT:CLE");
    }
}
