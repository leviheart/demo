package tw_six.demo.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import tw_six.demo.entity.FenceAlert;
import tw_six.demo.service.FenceAlertService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 告警推送服务 - WebSocket告警通知核心服务
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 负责将各类告警信息通过WebSocket实时推送给所有订阅的客户端。
 * 支持围栏告警、超速告警、设备异常等多种告警类型。
 * 
 * 【告警推送流程】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                        告警推送流程                                      │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │                                                                         │
 * │  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐             │
 * │  │ 告警触发      │───▶│ 创建告警数据  │───▶│ 封装消息     │             │
 * │  │ (围栏/超速)   │    │              │    │              │             │
 * │  └──────────────┘    └──────────────┘    └──────────────┘             │
 * │                                                   │                     │
 * │                                                   ▼                     │
 * │  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐             │
 * │  │ 前端显示      │◀───│ WebSocket    │◀───│ 推送到告警   │             │
 * │  │ 告警通知      │    │ 通道         │    │ 通道         │             │
 * │  └──────────────┘    └──────────────┘    └──────────────┘             │
 * │                                                                         │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【推送通道】
 * - /topic/alerts: 广播所有告警消息
 * 
 * 【告警类型】
 * 1. 围栏告警: 车辆进入/离开监控围栏
 * 2. 超速告警: 车辆速度超过限制值
 * 3. 设备告警: 设备离线、电量低等
 * 4. 紧急告警: 司机触发的紧急情况
 * 
 * 【告警级别处理】
 * - INFO: 普通通知，显示5秒后自动消失
 * - WARNING: 警告通知，显示10秒后自动消失
 * - ERROR: 错误通知，需要手动关闭
 * - CRITICAL: 严重告警，需要手动关闭并发出声音提示
 * 
 * 【使用示例】
 * ```java
 * // 推送围栏告警
 * alertPushService.pushGeofenceAlert("京A10001", "仓库围栏", 
 *     AlertData.AlertType.GEOFENCE_EXIT, 32.0650, 118.7500);
 * 
 * // 推送超速告警
 * alertPushService.pushOverspeedAlert("京A10001", 85.0, 60.0, 32.0650, 118.7500);
 * ```
 * 
 * 【前端对接】
 * ```javascript
 * stompClient.subscribe('/topic/alerts', (message) => {
 *   const alert = JSON.parse(message.body);
 *   // 显示告警通知
 *   showNotification(alert.data);
 *   // 播放告警声音（严重告警）
 *   if (alert.data.alertLevel === 'CRITICAL') {
 *     playAlertSound();
 *   }
 * });
 * ```
 * 
 * 【关联文件】
 * - WebSocketConfig.java: WebSocket配置
 * - AlertData.java: 告警数据模型
 * - FenceAlertService.java: 围栏告警服务
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Service
public class AlertPushService {

    /** WebSocket消息发送模板 */
    private final SimpMessagingTemplate messagingTemplate;
    
    /** 围栏告警服务 */
    private final FenceAlertService fenceAlertService;
    
    /** 时间格式化器 */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /** 告警ID生成器 - 用于生成唯一告警ID */
    private final AtomicLong alertIdGenerator = new AtomicLong(0);
    
    /** 今日日期 - 用于告警ID生成 */
    private String todayDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

    /**
     * 构造函数 - 依赖注入
     * 
     * @param messagingTemplate WebSocket消息发送模板
     * @param fenceAlertService 围栏告警服务
     */
    @Autowired
    public AlertPushService(SimpMessagingTemplate messagingTemplate, 
            FenceAlertService fenceAlertService) {
        this.messagingTemplate = messagingTemplate;
        this.fenceAlertService = fenceAlertService;
    }

    /**
     * 推送围栏告警
     * 
     * 当车辆进入或离开围栏时触发此方法。
     * 同时将告警记录保存到数据库。
     * 
     * @param carName 车辆名称
     * @param fenceName 围栏名称
     * @param alertType 告警类型（进入/离开）
     * @param latitude 纬度
     * @param longitude 经度
     */
    public void pushGeofenceAlert(String carName, String fenceName, 
            AlertData.AlertType alertType, Double latitude, Double longitude) {
        // 生成告警ID
        String alertId = generateAlertId();
        
        // 创建告警数据
        AlertData alertData = AlertData.geofenceAlert(
            alertId, alertType, carName, fenceName, latitude, longitude
        );
        
        // 保存到数据库
        saveFenceAlert(alertData);
        
        // 推送WebSocket消息
        WebSocketMessage message = WebSocketMessage.alert(alertData, alertData.getMessage());
        messagingTemplate.convertAndSend("/topic/alerts", message);
        
        // 记录日志
        System.out.println(String.format("[告警推送] %s - %s", alertType, alertData.getMessage()));
    }

    /**
     * 推送超速告警
     * 
     * 当车辆速度超过限制值时触发此方法。
     * 
     * @param carName 车辆名称
     * @param currentSpeed 当前速度
     * @param speedLimit 速度限制
     * @param latitude 纬度
     * @param longitude 经度
     */
    public void pushOverspeedAlert(String carName, Double currentSpeed, 
            Double speedLimit, Double latitude, Double longitude) {
        // 生成告警ID
        String alertId = generateAlertId();
        
        // 创建告警数据
        AlertData alertData = AlertData.overspeedAlert(
            alertId, carName, currentSpeed, speedLimit, latitude, longitude
        );
        
        // 推送WebSocket消息
        WebSocketMessage message = WebSocketMessage.alert(alertData, alertData.getMessage());
        messagingTemplate.convertAndSend("/topic/alerts", message);
        
        // 记录日志
        System.out.println(String.format("[超速告警] %s - 当前速度: %.1f km/h, 限速: %.1f km/h", 
            carName, currentSpeed, speedLimit));
    }

    /**
     * 推送设备离线告警
     * 
     * @param carName 车辆名称
     * @param offlineMinutes 离线时长（分钟）
     */
    public void pushDeviceOfflineAlert(String carName, int offlineMinutes) {
        String alertId = generateAlertId();
        
        AlertData alertData = new AlertData();
        alertData.setAlertId(alertId);
        alertData.setAlertType(AlertData.AlertType.DEVICE_OFFLINE);
        alertData.setAlertLevel(AlertData.AlertLevel.WARNING);
        alertData.setCarName(carName);
        alertData.setMessage(String.format("车辆 %s 设备已离线 %d 分钟", carName, offlineMinutes));
        
        WebSocketMessage message = WebSocketMessage.alert(alertData, alertData.getMessage());
        messagingTemplate.convertAndSend("/topic/alerts", message);
    }

    /**
     * 推送紧急告警
     * 
     * @param carName 车辆名称
     * @param latitude 纬度
     * @param longitude 经度
     * @param description 紧急情况描述
     */
    public void pushEmergencyAlert(String carName, Double latitude, 
            Double longitude, String description) {
        String alertId = generateAlertId();
        
        AlertData alertData = new AlertData();
        alertData.setAlertId(alertId);
        alertData.setAlertType(AlertData.AlertType.EMERGENCY);
        alertData.setAlertLevel(AlertData.AlertLevel.CRITICAL);
        alertData.setCarName(carName);
        alertData.setLatitude(latitude);
        alertData.setLongitude(longitude);
        alertData.setMessage(String.format("【紧急】车辆 %s: %s", carName, description));
        
        WebSocketMessage message = WebSocketMessage.alert(alertData, alertData.getMessage());
        messagingTemplate.convertAndSend("/topic/alerts", message);
        
        // 紧急告警额外记录
        System.err.println(String.format("[紧急告警] %s", alertData.getMessage()));
    }

    /**
     * 推送系统消息
     * 
     * @param message 系统消息内容
     */
    public void pushSystemMessage(String message) {
        WebSocketMessage wsMessage = WebSocketMessage.system(message);
        messagingTemplate.convertAndSend("/topic/alerts", wsMessage);
    }

    /**
     * 生成唯一告警ID
     * 格式: ALT-日期-序号（如: ALT-20240115-001）
     * 
     * @return 告警ID
     */
    private String generateAlertId() {
        // 检查日期是否变更
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        if (!today.equals(todayDate)) {
            todayDate = today;
            alertIdGenerator.set(0);
        }
        
        long sequence = alertIdGenerator.incrementAndGet();
        return String.format("ALT-%s-%03d", todayDate, sequence);
    }

    /**
     * 保存围栏告警到数据库
     * 
     * @param alertData 告警数据
     */
    private void saveFenceAlert(AlertData alertData) {
        try {
            FenceAlert fenceAlert = new FenceAlert();
            fenceAlert.setCarName(alertData.getCarName());
            fenceAlert.setAlertType(alertData.getAlertType().name());
            fenceAlert.setAlertTime(LocalDateTime.now());
            fenceAlert.setLatitude(alertData.getLatitude());
            fenceAlert.setLongitude(alertData.getLongitude());
            fenceAlert.setIsHandled(false);
            
            fenceAlertService.createAlert(fenceAlert);
        } catch (Exception e) {
            System.err.println("保存告警记录失败: " + e.getMessage());
        }
    }
}
