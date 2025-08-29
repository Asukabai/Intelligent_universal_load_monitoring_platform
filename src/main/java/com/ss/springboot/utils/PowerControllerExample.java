package com.ss.springboot.utils;

public class PowerControllerExample {
    public static void main(String[] args) {
        // 创建电源控制器实例 (根据实际情况调整端口号)
        IT69360PowerController controller = new IT69360PowerController("COM3", 9600);
        
        try {
            System.out.println("尝试连接到串口: " + controller.checkPortStatus());
            // 连接设备
            if (controller.connect()) {
                System.out.println("成功连接到电源设备");
                
                // 获取设备标识
                String idn = controller.getDeviceIdentification();
                System.out.println("设备标识: " + idn);
                
                // 进入远程模式
                controller.enterRemoteMode();
                
                // 设置电压和电流
                controller.setVoltage(1.0);
                controller.setCurrent(0.1);
                
                // 设置保护值
//                controller.setVoltageProtection(13.0);
//                controller.setCurrentProtection(2.5);
                
                // 查询设置值
                System.out.println("设置电压: " + controller.getVoltage() + "V");
                System.out.println("设置电流: " + controller.getCurrent() + "A");
                
                // 开启输出
                controller.outputOn();
                
                // 查询工作状态
                String status = controller.getWorkingStatus();
                System.out.println("工作状态: " + status);
                
                // 延时5秒
                Thread.sleep(5000);
                
                // 关闭输出
                controller.outputOff();
            }
        } catch (Exception e) {
            System.err.println("操作电源时发生错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 断开连接
            controller.disconnect();
        }
    }
}
