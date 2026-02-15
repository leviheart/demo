package tw_six.demo.entity; // 定义路线实体类所在的包，属于数据模型层

import jakarta.persistence.Entity; // JPA注解，标记此类为数据库实体
import jakarta.persistence.GeneratedValue; // JPA注解，指定主键生成策略
import jakarta.persistence.GenerationType; // JPA枚举，定义主键生成方式
import jakarta.persistence.Id; // JPA注解，标记字段为主键
import lombok.Data; // Lombok注解，自动生成标准方法

/**
 * 路线实体类 - 地图导航系统路线规划数据模型
 * 
 * 文件关联说明：
 * 1. 与RouteRepository关联：通过JPA实现路线信息的CRUD操作
 * 2. 与RouteService关联：作为路线规划业务逻辑处理的核心数据对象
 * 3. 与MapController关联：作为REST接口接收起点终点参数并返回路线信息
 * 4. 与数据库表关联：通过@Entity注解映射到route表
 * 5. 与前端页面关联：通过API接口向前端提供路线规划结果
 * 
 * 作用说明：
 * - 定义路线规划数据结构，包含起点、终点、距离和路线描述
 * - 通过JPA实现持久化存储，支持历史路线的缓存和复用
 * - 为地图导航系统提供智能路线规划的数据模型支撑
 */
@Entity // 标记此类为JPA实体，Hibernate会自动创建route数据表
@Data // Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
public class Route { // 路线实体类定义
    @Id // 标记此字段为主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 设置主键自增策略
    private Long id; // 路线记录的唯一标识符
    
    private String startPoint; // 起点位置描述
    private String endPoint; // 终点位置描述
    private Double distance; // 路线距离，单位公里
    private String routeInfo; // 路线详细信息描述
}