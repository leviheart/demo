package tw_six.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 车辆轨迹实体类 - 记录车辆的历史行驶轨迹点
 * 
 * 【功能概述】
 * 该实体类用于存储车辆行驶过程中的轨迹点数据，每个轨迹点包含位置、速度、方向等信息。
 * 通过连续的轨迹点可以还原车辆的完整行驶路径。
 * 
 * 【数据库映射】
 * - 表名：vehicle_tracks
 * - 主键：id（自增长）
 * 
 * 【业务场景】
 * 1. 轨迹回放：在地图上回放车辆的历史行驶路线
 * 2. 行程分析：分析车辆的行驶速度、行驶时间等
 * 3. 里程统计：通过轨迹点计算实际行驶里程
 * 4. 异常检测：检测超速、偏航等异常行为
 * 
 * 【数据采集】
 * 轨迹数据通常由车载GPS设备定时上报，上报频率可根据需求配置（如每10秒一次）。
 * 
 * 【使用示例】
 * VehicleTrack track = new VehicleTrack();
 * track.setCarName("苏A12345");
 * track.setLatitude(32.0617);
 * track.setLongitude(118.7634);
 * track.setSpeed(60.5);  // 时速60.5公里
 * track.setDirection(90.0);  // 向东行驶
 */
@Entity
@Table(name = "vehicle_tracks")
public class VehicleTrack {
    
    /**
     * 主键ID - 数据库自动生成的唯一标识符
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 车牌号码 - 产生该轨迹点的车辆标识
     */
    @Column(name = "car_name", nullable = false)
    private String carName;
    
    /**
     * 轨迹点纬度 - 该时刻车辆的GPS纬度
     */
    @Column(name = "latitude", nullable = false)
    private Double latitude;
    
    /**
     * 轨迹点经度 - 该时刻车辆的GPS经度
     */
    @Column(name = "longitude", nullable = false)
    private Double longitude;
    
    /**
     * 行驶速度 - 该时刻车辆的行驶速度（单位：公里/小时）
     * 用于速度监控和超速告警
     */
    @Column(name = "speed")
    private Double speed;
    
    /**
     * 行驶方向 - 车辆的行驶方向（单位：度）
     * 范围：0-360度
     * - 0度：正北
     * - 90度：正东
     * - 180度：正南
     * - 270度：正西
     */
    @Column(name = "direction")
    private Double direction;
    
    /**
     * 记录时间 - 该轨迹点产生的时间
     * 必填字段，用于轨迹排序和时间范围查询
     */
    @Column(name = "record_time", nullable = false)
    private LocalDateTime recordTime;
    
    /**
     * 轨迹状态 - 该轨迹点的附加状态信息
     * 可选值：
     * - "normal"：正常行驶
     * - "stopped"：停车中
     * - "speeding"：超速行驶
     */
    @Column(name = "status")
    private String status;
    
    /**
     * 默认构造函数
     */
    public VehicleTrack() {}
    
    /**
     * 实体持久化前的回调方法
     * 自动设置记录时间，如果未指定则使用当前时间
     */
    @PrePersist
    protected void onCreate() {
        if (recordTime == null) {
            recordTime = LocalDateTime.now();
        }
    }
    
    // ==================== Getter/Setter 方法 ====================
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCarName() { return carName; }
    public void setCarName(String carName) { this.carName = carName; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }
    
    public Double getDirection() { return direction; }
    public void setDirection(Double direction) { this.direction = direction; }
    
    public LocalDateTime getRecordTime() { return recordTime; }
    public void setRecordTime(LocalDateTime recordTime) { this.recordTime = recordTime; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
