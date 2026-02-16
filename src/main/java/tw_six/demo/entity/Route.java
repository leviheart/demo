package tw_six.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 路线规划实体类 - 存储车辆行驶路线规划结果
 * 
 * 【功能概述】
 * 该实体类用于存储从起点到终点的路线规划结果，包括距离、路线描述等信息。
 * 通过缓存已规划的路线，避免重复计算，提高系统响应速度。
 * 
 * 【数据库映射】
 * - 表名：routes
 * - 主键：id（自增长）
 * 
 * 【业务场景】
 * 1. 路线缓存：存储已计算的路线，避免重复调用地图API
 * 2. 历史查询：查询历史规划过的路线
 * 3. 距离统计：统计各路线的距离信息
 * 
 * 【使用示例】
 * Route route = new Route();
 * route.setStartPoint("南京");
 * route.setEndPoint("上海");
 * route.setDistance(300.5);  // 300.5公里
 * route.setRouteInfo("推荐路线：沪宁高速");
 */
@Entity
@Table(name = "routes")
public class Route {
    
    /**
     * 主键ID - 数据库自动生成的唯一标识符
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 起点位置 - 路线的起始地点
     * 示例："南京"、"上海市浦东新区"
     */
    @Column(name = "start_point")
    private String startPoint;
    
    /**
     * 终点位置 - 路线的目的地点
     * 示例："上海"、"北京市朝阳区"
     */
    @Column(name = "end_point")
    private String endPoint;
    
    /**
     * 路线距离 - 起点到终点的距离（单位：公里）
     * 由地图API计算得出
     */
    private Double distance;
    
    /**
     * 路线信息 - 路线的详细描述
     * 示例："从 南京 到 上海 的推荐路线，途经沪宁高速"
     */
    @Column(name = "route_info")
    private String routeInfo;
    
    /**
     * 创建时间 - 路线记录的创建时间
     * 自动记录路线规划的时间戳
     */
    @Column(name = "created_time")
    private LocalDateTime createdTime;
    
    /**
     * 默认构造函数
     */
    public Route() {}
    
    // ==================== Getter/Setter 方法 ====================
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getStartPoint() { return startPoint; }
    public void setStartPoint(String startPoint) { this.startPoint = startPoint; }
    
    public String getEndPoint() { return endPoint; }
    public void setEndPoint(String endPoint) { this.endPoint = endPoint; }
    
    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }
    
    public String getRouteInfo() { return routeInfo; }
    public void setRouteInfo(String routeInfo) { this.routeInfo = routeInfo; }
    
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
}
