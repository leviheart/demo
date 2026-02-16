package tw_six.demo.entity;

import jakarta.persistence.*;

/**
 * 车辆位置实体类 - 用于存储和管理车辆的实时位置信息
 * 
 * 【功能概述】
 * 该实体类是智能车辆监控系统的核心数据模型之一，负责存储车辆的实时GPS位置数据。
 * 每条记录代表一个车辆在特定时刻的位置状态，包括经纬度坐标和运行状态。
 * 
 * 【数据库映射】
 * - 表名：car_locations
 * - 主键：id（自增长）
 * - 外键：group_id（关联vehicle_groups表，表示车辆所属分组）
 * 
 * 【业务场景】
 * 1. 实时定位：记录车辆的当前GPS坐标，用于地图展示
 * 2. 轨迹追踪：通过历史位置数据还原车辆行驶轨迹
 * 3. 状态监控：通过status字段区分车辆运行状态（运行中/停止/维修中）
 * 4. 分组管理：通过vehicleGroup关联实现车辆分组功能
 * 
 * 【关联关系】
 * - 多对一：CarLocation -> VehicleGroup（一辆车属于一个分组）
 * - 被关联：FenceAlert（位置数据触发围栏告警）
 * 
 * 【使用示例】
 * CarLocation location = new CarLocation();
 * location.setCarName("苏A12345");
 * location.setLatitude(32.0617);
 * location.setLongitude(118.7634);
 * location.setStatus("active");
 */
@Entity
@Table(name = "car_locations")
public class CarLocation {
    
    /**
     * 主键ID - 数据库自动生成的唯一标识符
     * 使用IDENTITY策略，由数据库自动递增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 车牌号码 - 车辆的唯一标识
     * 存储格式示例："苏A12345"、"沪B67890"
     * 用于在系统中识别和查找特定车辆
     */
    @Column(name = "car_name", nullable = false)
    private String carName;
    
    /**
     * 纬度坐标 - GPS定位的纬度值
     * 范围：-90.0 到 90.0
     * 示例：32.0617（南京市中心纬度）
     * 注意：正值表示北纬，负值表示南纬
     */
    @Column(name = "latitude", nullable = false)
    private Double latitude;
    
    /**
     * 经度坐标 - GPS定位的经度值
     * 范围：-180.0 到 180.0
     * 示例：118.7634（南京市中心经度）
     * 注意：正值表示东经，负值表示西经
     */
    @Column(name = "longitude", nullable = false)
    private Double longitude;
    
    /**
     * 车辆状态 - 表示当前车辆的运行状态
     * 可选值：
     * - "active"：运行中，车辆正在行驶
     * - "inactive"：停止，车辆已熄火停车
     * - "maintenance"：维修中，车辆正在保养或维修
     */
    @Column(name = "status")
    private String status;
    
    /**
     * 所属分组 - 车辆所属的分组信息
     * 使用懒加载（LAZY）优化性能，只在需要时查询分组详情
     * 通过group_id外键关联vehicle_groups表
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private VehicleGroup vehicleGroup;
    
    /**
     * 默认构造函数 - JPA要求的无参构造函数
     * 用于Hibernate实例化对象
     */
    public CarLocation() {}
    
    // ==================== Getter/Setter 方法 ====================
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCarName() { return carName; }
    public void setCarName(String carName) { this.carName = carName; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public VehicleGroup getVehicleGroup() { return vehicleGroup; }
    public void setVehicleGroup(VehicleGroup vehicleGroup) { this.vehicleGroup = vehicleGroup; }
}
