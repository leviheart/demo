package tw_six.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 行驶统计实体类 - 存储车辆的日行驶统计数据
 * 
 * 【功能概述】
 * 该实体类用于存储车辆每日的行驶统计数据，包括里程、速度、油耗等信息。
 * 数据通常由系统定时任务在每天结束时汇总计算生成。
 * 
 * 【数据库映射】
 * - 表名：driving_statistics
 * - 主键：id（自增长）
 * 
 * 【业务场景】
 * 1. 里程统计：统计车辆每日行驶里程
 * 2. 速度分析：分析平均速度、最高速度等指标
 * 3. 油耗监控：估算车辆油耗情况
 * 4. 驾驶行为：统计急加速、急刹车等行为
 * 
 * 【数据来源】
 * 统计数据来源于vehicle_tracks表，通过定时任务汇总计算。
 * 
 * 【使用示例】
 * DrivingStatistics stats = new DrivingStatistics();
 * stats.setCarName("苏A12345");
 * stats.setRecordDate(LocalDate.now());
 * stats.setTotalMileage(150.5);  // 当日行驶150.5公里
 * stats.setAverageSpeed(45.0);  // 平均时速45公里
 */
@Entity
@Table(name = "driving_statistics")
public class DrivingStatistics {
    
    /**
     * 主键ID - 数据库自动生成的唯一标识符
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 车牌号码 - 统计数据对应的车辆
     */
    @Column(name = "car_name", nullable = false)
    private String carName;
    
    /**
     * 统计日期 - 该统计数据对应的日期
     * 格式：yyyy-MM-dd
     */
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;
    
    /**
     * 总行驶里程 - 当日总行驶距离（单位：公里）
     */
    @Column(name = "total_mileage")
    private Double totalMileage;
    
    /**
     * 平均速度 - 当日行驶的平均速度（单位：公里/小时）
     */
    @Column(name = "average_speed")
    private Double averageSpeed;
    
    /**
     * 最高速度 - 当日行驶的最高速度（单位：公里/小时）
     * 用于检测超速行为
     */
    @Column(name = "max_speed")
    private Double maxSpeed;
    
    /**
     * 行驶时间 - 当日总行驶时间（单位：分钟）
     */
    @Column(name = "driving_time_minutes")
    private Integer drivingTimeMinutes;
    
    /**
     * 油耗估算 - 当日估算油耗（单位：升）
     * 基于里程和车型估算
     */
    @Column(name = "fuel_consumption")
    private Double fuelConsumption;
    
    /**
     * 急加速次数 - 当日急加速行为次数
     * 用于评估驾驶行为
     */
    @Column(name = "harsh_acceleration_count")
    private Integer harshAccelerationCount;
    
    /**
     * 急刹车次数 - 当日急刹车行为次数
     * 用于评估驾驶行为
     */
    @Column(name = "harsh_braking_count")
    private Integer harshBrakingCount;
    
    /**
     * 默认构造函数
     */
    public DrivingStatistics() {}
    
    /**
     * 实体持久化前的回调方法
     * 自动设置统计日期，如果未指定则使用当前日期
     */
    @PrePersist
    protected void onCreate() {
        if (recordDate == null) {
            recordDate = LocalDate.now();
        }
    }
    
    // ==================== Getter/Setter 方法 ====================
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCarName() { return carName; }
    public void setCarName(String carName) { this.carName = carName; }
    
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    
    public Double getTotalMileage() { return totalMileage; }
    public void setTotalMileage(Double totalMileage) { this.totalMileage = totalMileage; }
    
    public Double getAverageSpeed() { return averageSpeed; }
    public void setAverageSpeed(Double averageSpeed) { this.averageSpeed = averageSpeed; }
    
    public Double getMaxSpeed() { return maxSpeed; }
    public void setMaxSpeed(Double maxSpeed) { this.maxSpeed = maxSpeed; }
    
    public Integer getDrivingTimeMinutes() { return drivingTimeMinutes; }
    public void setDrivingTimeMinutes(Integer drivingTimeMinutes) { this.drivingTimeMinutes = drivingTimeMinutes; }
    
    public Double getFuelConsumption() { return fuelConsumption; }
    public void setFuelConsumption(Double fuelConsumption) { this.fuelConsumption = fuelConsumption; }
    
    public Integer getHarshAccelerationCount() { return harshAccelerationCount; }
    public void setHarshAccelerationCount(Integer harshAccelerationCount) { this.harshAccelerationCount = harshAccelerationCount; }
    
    public Integer getHarshBrakingCount() { return harshBrakingCount; }
    public void setHarshBrakingCount(Integer harshBrakingCount) { this.harshBrakingCount = harshBrakingCount; }
}
