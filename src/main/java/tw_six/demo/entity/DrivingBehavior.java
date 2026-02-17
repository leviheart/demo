package tw_six.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 驾驶行为记录实体类 - 记录异常驾驶行为事件
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 存储检测到的异常驾驶行为，包括急加速、急刹车、超速、疲劳驾驶等。
 * 用于驾驶行为分析、安全评分、风险预警等场景。
 * 
 * 【行为类型说明】
 * ┌──────────────────┬───────────────────────────────────────────────────────┐
 * │ 行为类型          │ 触发条件                                              │
 * ├──────────────────┼───────────────────────────────────────────────────────┤
 * │ RAPID_ACCEL      │ 急加速：加速度 > 3 m/s²                               │
 * │ RAPID_BRAKE      │ 急刹车：减速度 > 3 m/s²                               │
 * │ OVERSPEED        │ 超速：速度超过限速值                                  │
 * │ FATIGUE          │ 疲劳驾驶：连续驾驶超过4小时                           │
 * │ SHARP_TURN       │ 急转弯：转弯速度过快                                  │
 * │ IDLE_LONG        │ 长时间怠速：怠速超过10分钟                            │
 * └──────────────────┴───────────────────────────────────────────────────────┘
 * 
 * 【风险等级】
 * - LOW: 低风险，一般提醒
 * - MEDIUM: 中风险，需要关注
 * - HIGH: 高风险，需要立即处理
 * - CRITICAL: 严重风险，紧急情况
 * 
 * 【业务场景】
 * 1. 安全监控：实时检测危险驾驶行为
 * 2. 驾驶评分：基于行为记录计算驾驶评分
 * 3. 风险预警：识别高风险驾驶员
 * 4. 培训建议：根据行为数据提供驾驶建议
 * 
 * 【关联文件】
 * - DrivingBehaviorService.java: 行为检测服务
 * - DrivingBehaviorController.java: API接口
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Entity
@Table(name = "driving_behaviors")
public class DrivingBehavior {

    /**
     * 主键ID - 数据库自动生成的唯一标识符
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 车牌号码 - 触发该行为的车辆标识
     */
    @Column(name = "car_name", nullable = false)
    private String carName;

    /**
     * 行为类型 - 标识具体的驾驶行为
     * @see BehaviorType
     */
    @Column(name = "behavior_type", nullable = false)
    private String behaviorType;

    /**
     * 风险等级 - 行为的危险程度
     * LOW, MEDIUM, HIGH, CRITICAL
     */
    @Column(name = "risk_level")
    private String riskLevel;

    /**
     * 行为发生位置 - 纬度
     */
    private Double latitude;

    /**
     * 行为发生位置 - 经度
     */
    private Double longitude;

    /**
     * 触发时的速度 - 单位：km/h
     */
    private Double speed;

    /**
     * 触发时的加速度/减速度 - 单位：m/s²
     * 正值表示加速，负值表示减速
     */
    private Double acceleration;

    /**
     * 行为发生时间
     */
    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    /**
     * 行为持续时长 - 单位：秒
     * 用于疲劳驾驶、长时间怠速等持续性事件
     */
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    /**
     * 行为描述 - 人类可读的描述信息
     */
    private String description;

    /**
     * 处理状态 - 是否已处理
     */
    @Column(name = "is_handled")
    private Boolean isHandled;

    /**
     * 创建时间 - 记录创建时间
     */
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    /**
     * 行为类型枚举
     */
    public enum BehaviorType {
        /** 急加速 */
        RAPID_ACCEL("急加速"),
        /** 急刹车 */
        RAPID_BRAKE("急刹车"),
        /** 超速 */
        OVERSPEED("超速"),
        /** 疲劳驾驶 */
        FATIGUE("疲劳驾驶"),
        /** 急转弯 */
        SHARP_TURN("急转弯"),
        /** 长时间怠速 */
        IDLE_LONG("长时间怠速");

        private final String description;

        BehaviorType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 风险等级枚举
     */
    public enum RiskLevel {
        LOW("低风险"),
        MEDIUM("中风险"),
        HIGH("高风险"),
        CRITICAL("严重");

        private final String description;

        RiskLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 默认构造函数
     */
    public DrivingBehavior() {
    }

    /**
     * 实体持久化前的回调方法
     */
    @PrePersist
    protected void onCreate() {
        if (eventTime == null) {
            eventTime = LocalDateTime.now();
        }
        if (createdTime == null) {
            createdTime = LocalDateTime.now();
        }
        if (isHandled == null) {
            isHandled = false;
        }
    }

    // ==================== Getter和Setter方法 ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCarName() { return carName; }
    public void setCarName(String carName) { this.carName = carName; }

    public String getBehaviorType() { return behaviorType; }
    public void setBehaviorType(String behaviorType) { this.behaviorType = behaviorType; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }

    public Double getAcceleration() { return acceleration; }
    public void setAcceleration(Double acceleration) { this.acceleration = acceleration; }

    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsHandled() { return isHandled; }
    public void setIsHandled(Boolean isHandled) { this.isHandled = isHandled; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
}
