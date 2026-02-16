package tw_six.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 地理围栏实体类 - 用于定义虚拟地理边界区域
 * 
 * 【功能概述】
 * 地理围栏是车辆监控系统的核心安全功能，通过在地图上划定虚拟边界，
 * 当车辆进入或离开该区域时自动触发告警，实现对车辆活动的自动监控。
 * 
 * 【数据库映射】
 * - 表名：geo_fences
 * - 主键：id（自增长）
 * - 关联表：fence_alerts（围栏告警记录）
 * 
 * 【围栏类型】
 * 目前支持圆形围栏（circle），通过中心点坐标和半径定义区域范围。
 * 未来可扩展支持多边形围栏、线路围栏等类型。
 * 
 * 【告警类型说明】
 * - entry：进入告警 - 车辆进入围栏区域时触发
 * - exit：离开告警 - 车辆离开围栏区域时触发
 * - both：双向告警 - 进入和离开都触发告警
 * 
 * 【业务场景】
 * 1. 区域监控：监控车辆是否在指定区域内活动
 * 2. 越界告警：车辆未经授权离开指定区域
 * 3. 入侵检测：未授权车辆进入敏感区域
 * 4. 路线管控：确保车辆按照规定路线行驶
 * 
 * 【使用示例】
 * GeoFence fence = new GeoFence();
 * fence.setFenceName("仓库区域");
 * fence.setCenterLatitude(32.0617);
 * fence.setCenterLongitude(118.7634);
 * fence.setRadius(500.0);  // 500米半径
 * fence.setAlertType("both");  // 进出都告警
 */
@Entity
@Table(name = "geo_fences")
public class GeoFence {
    
    /**
     * 主键ID - 数据库自动生成的唯一标识符
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 围栏名称 - 围栏的显示名称
     * 示例："仓库区域"、"公司园区"、"禁行区域"
     * 用于在界面上展示和识别围栏
     */
    @Column(name = "fence_name")
    private String fenceName;
    
    /**
     * 中心点纬度 - 圆形围栏圆心的纬度坐标
     * 用于计算车辆与围栏中心的距离
     */
    @Column(name = "center_latitude")
    private Double centerLatitude;
    
    /**
     * 中心点经度 - 圆形围栏圆心的经度坐标
     * 用于计算车辆与围栏中心的距离
     */
    @Column(name = "center_longitude")
    private Double centerLongitude;
    
    /**
     * 围栏半径 - 圆形围栏的半径（单位：米）
     * 定义围栏的覆盖范围
     * 示例：500.0 表示以中心点为圆心，500米为半径的圆形区域
     */
    private Double radius;
    
    /**
     * 围栏类型 - 定义围栏的形状类型
     * 当前支持：
     * - "circle"：圆形围栏（通过中心点和半径定义）
     * 预留扩展：
     * - "polygon"：多边形围栏
     * - "polyline"：线路围栏
     */
    @Column(name = "fence_type")
    private String fenceType;
    
    /**
     * 告警类型 - 定义触发告警的条件
     * 可选值：
     * - "entry"：仅进入时告警
     * - "exit"：仅离开时告警
     * - "both"：进入和离开都告警
     */
    @Column(name = "alert_type")
    private String alertType;
    
    /**
     * 围栏状态 - 控制围栏是否生效
     * - "active"：启用 - 围栏监控功能正常工作
     * - "inactive"：禁用 - 暂停围栏监控功能
     */
    private String status;
    
    /**
     * 围栏描述 - 对围栏用途的详细说明
     * 示例："公司主仓库周边500米范围，用于监控货物车辆进出"
     */
    private String description;
    
    /**
     * 创建时间 - 围栏记录的创建时间
     * 自动记录围栏创建的时间戳
     */
    @Column(name = "created_time")
    private LocalDateTime createdTime;
    
    /**
     * 默认构造函数
     */
    public GeoFence() {}
    
    // ==================== Getter/Setter 方法 ====================
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFenceName() { return fenceName; }
    public void setFenceName(String fenceName) { this.fenceName = fenceName; }
    
    public Double getCenterLatitude() { return centerLatitude; }
    public void setCenterLatitude(Double centerLatitude) { this.centerLatitude = centerLatitude; }
    
    public Double getCenterLongitude() { return centerLongitude; }
    public void setCenterLongitude(Double centerLongitude) { this.centerLongitude = centerLongitude; }
    
    public Double getRadius() { return radius; }
    public void setRadius(Double radius) { this.radius = radius; }
    
    public String getFenceType() { return fenceType; }
    public void setFenceType(String fenceType) { this.fenceType = fenceType; }
    
    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
}
