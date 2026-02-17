package tw_six.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 围栏告警实体类 - 记录车辆触发围栏告警的详细信息
 * 
 * 【功能概述】
 * 当车辆进入或离开地理围栏区域时，系统自动创建告警记录。
 * 该实体类存储告警的完整信息，包括触发车辆、位置、时间、处理状态等。
 * 
 * 【数据库映射】
 * - 表名：fence_alerts
 * - 主键：id（自增长）
 * - 外键：fence_id（关联geo_fences表）
 * 
 * 【告警流程】
 * 1. 车辆位置更新时，系统检查是否触发围栏
 * 2. 如触发围栏，自动创建FenceAlert记录
 * 3. 告警显示在监控界面，等待处理
 * 4. 管理员处理后更新处理状态
 * 
 * 【业务场景】
 * 1. 安全监控：记录车辆越界行为
 * 2. 事件追溯：事后查询历史告警记录
 * 3. 统计分析：分析告警频率和区域分布
 * 4. 责任追踪：记录告警处理人员和时间
 * 
 * 【状态说明】
 * - isHandled=false：待处理，新告警等待管理员关注
 * - isHandled=true：已处理，管理员已确认并处理
 * 
 * 【使用示例】
 * // 系统自动创建告警（由GeoFenceService调用）
 * FenceAlert alert = new FenceAlert();
 * alert.setGeoFence(fence);
 * alert.setCarName("苏A12345");
 * alert.setAlertType("entry");
 * alert.setIsHandled(false);
 */
@Entity
@Table(name = "fence_alerts")
public class FenceAlert {
    
    /**
     * 主键ID - 数据库自动生成的唯一标识符
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 关联围栏 - 触发告警的地理围栏
     * 通过外键fence_id关联geo_fences表
     * 用于追溯告警是由哪个围栏触发的
     */
    @ManyToOne
    @JoinColumn(name = "fence_id")
    private GeoFence geoFence;
    
    /**
     * 车牌号码 - 触发告警的车辆标识
     * 记录是哪辆车触发了围栏告警
     */
    @Column(name = "car_name")
    private String carName;
    
    /**
     * 告警位置纬度 - 触发告警时的车辆纬度
     * 记录车辆触发告警时的精确位置
     */
    private Double latitude;
    
    /**
     * 告警位置经度 - 触发告警时的车辆经度
     * 记录车辆触发告警时的精确位置
     */
    private Double longitude;
    
    /**
     * 告警类型 - 表示是进入还是离开围栏
     * - "entry"：进入围栏触发
     * - "exit"：离开围栏触发
     */
    @Column(name = "alert_type")
    private String alertType;
    
    /**
     * 告警严重程度 - 表示告警的紧急程度
     * - "high"：高严重程度，需要立即处理
     * - "medium"：中等严重程度
     * - "low"：低严重程度
     */
    @Column(name = "severity")
    private String severity;
    
    /**
     * 告警时间 - 告警触发的时间
     * 与created_time相同，用于兼容数据库字段
     */
    @Column(name = "alert_time")
    private LocalDateTime alertTime;
    
    /**
     * 处理状态 - 标识告警是否已被处理
     * - false：待处理，需要管理员关注
     * - true：已处理，管理员已确认
     */
    @Column(name = "is_handled")
    private Boolean isHandled;
    
    /**
     * 处理时间 - 管理员处理告警的时间
     * 只有在isHandled=true时才有值
     */
    @Column(name = "handle_time")
    private LocalDateTime handleTime;
    
    /**
     * 处理人员 - 处理该告警的管理员
     * 记录是谁确认处理了这条告警
     */
    private String handler;
    
    /**
     * 创建时间 - 告警产生的时间
     * 自动记录告警触发的时间戳
     */
    @Column(name = "created_time")
    private LocalDateTime createdTime;
    
    /**
     * 默认构造函数
     */
    public FenceAlert() {}
    
    /**
     * 实体持久化前的回调方法
     * 自动设置告警时间和创建时间
     */
    @PrePersist
    protected void onCreate() {
        if (alertTime == null) {
            alertTime = LocalDateTime.now();
        }
        if (createdTime == null) {
            createdTime = LocalDateTime.now();
        }
    }
    
    // ==================== Getter/Setter 方法 ====================
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public GeoFence getGeoFence() { return geoFence; }
    public void setGeoFence(GeoFence geoFence) { this.geoFence = geoFence; }
    
    public String getCarName() { return carName; }
    public void setCarName(String carName) { this.carName = carName; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    
    public LocalDateTime getAlertTime() { return alertTime; }
    public void setAlertTime(LocalDateTime alertTime) { this.alertTime = alertTime; }
    
    public Boolean getIsHandled() { return isHandled; }
    public void setIsHandled(Boolean isHandled) { this.isHandled = isHandled; }
    
    public LocalDateTime getHandleTime() { return handleTime; }
    public void setHandleTime(LocalDateTime handleTime) { this.handleTime = handleTime; }
    
    public String getHandler() { return handler; }
    public void setHandler(String handler) { this.handler = handler; }
    
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
}
