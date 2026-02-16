package tw_six.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 车辆分组实体类 - 用于对车辆进行分类管理
 * 
 * 【功能概述】
 * 车辆分组功能允许管理员将车辆按部门、用途、区域等维度进行分类，
 * 便于批量管理和监控不同类型的车辆。
 * 
 * 【数据库映射】
 * - 表名：vehicle_groups
 * - 主键：id（自增长）
 * - 关联表：car_locations（分组下的车辆）
 * 
 * 【业务场景】
 * 1. 部门分组：按公司部门划分车辆（如：销售部、物流部、管理层）
 * 2. 区域分组：按服务区域划分车辆（如：东区、西区、北区）
 * 3. 用途分组：按车辆用途划分（如：货运车、商务车、工程车）
 * 4. 权限控制：不同用户只能查看特定分组的车辆
 * 
 * 【关联关系】
 * - 一对多：VehicleGroup -> CarLocation（一个分组包含多辆车）
 * 
 * 【使用示例】
 * VehicleGroup group = new VehicleGroup();
 * group.setGroupName("物流部车辆");
 * group.setDescription("负责公司货物运输的车辆");
 * group.setStatus("active");
 */
@Entity
@Table(name = "vehicle_groups")
public class VehicleGroup {
    
    /**
     * 主键ID - 数据库自动生成的唯一标识符
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 分组名称 - 分组的显示名称
     * 示例："物流部车辆"、"销售团队"、"管理层专车"
     * 用于在界面上展示和识别分组
     */
    @Column(name = "group_name")
    private String groupName;
    
    /**
     * 分组描述 - 对分组用途的详细说明
     * 示例："负责华东区域货物运输的车辆，共15辆"
     */
    private String description;
    
    /**
     * 分组状态 - 控制分组是否生效
     * - "active"：启用 - 分组正常使用
     * - "inactive"：禁用 - 暂停使用该分组
     */
    private String status;
    
    /**
     * 创建时间 - 分组记录的创建时间
     * 自动记录分组创建的时间戳
     */
    @Column(name = "created_time")
    private LocalDateTime createdTime;
    
    /**
     * 分组车辆列表 - 该分组下的所有车辆
     * 使用一对多关系映射，级联操作
     * 注意：使用懒加载（LAZY）优化性能
     */
    @OneToMany(mappedBy = "vehicleGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CarLocation> carLocations;
    
    /**
     * 默认构造函数
     */
    public VehicleGroup() {}
    
    // ==================== Getter/Setter 方法 ====================
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    
    public List<CarLocation> getCarLocations() { return carLocations; }
    public void setCarLocations(List<CarLocation> carLocations) { this.carLocations = carLocations; }
}
