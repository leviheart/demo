package tw_six.demo.entity; // 定义汽车位置实体类所在的包，属于数据模型层

import jakarta.persistence.Entity; // JPA注解，标记此类为数据库实体
import jakarta.persistence.GeneratedValue; // JPA注解，指定主键生成策略
import jakarta.persistence.GenerationType; // JPA枚举，定义主键生成方式
import jakarta.persistence.Id; // JPA注解，标记字段为主键
import lombok.Data; // Lombok注解，自动生成标准方法

/**
 * 汽车位置实体类 - 地图导航系统核心数据模型
 * 
 * 文件关联说明：
 * 1. 与CarLocationRepository关联：通过JPA实现车辆位置的CRUD操作
 * 2. 与CarLocationService关联：作为车辆位置业务逻辑处理的核心数据对象
 * 3. 与MapController关联：作为REST接口接收和返回的车辆位置数据载体
 * 4. 与数据库表关联：通过@Entity注解映射到car_location表
 * 5. 与前端页面关联：通过API接口向前端提供实时车辆位置数据
 * 
 * 作用说明：
 * - 定义汽车位置数据结构，包含经纬度坐标和状态信息
 * - 通过JPA实现持久化存储，支持车辆位置的实时更新
 * - 为地图导航系统提供基础的位置数据模型支撑
 */
@Entity // 标记此类为JPA实体，Hibernate会自动创建car_location数据表
@Data // Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
public class CarLocation { // 汽车位置实体类定义
    @Id // 标记此字段为主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 设置主键自增策略
    private Long id; // 车辆位置记录的唯一标识符
    
    private Double latitude; // 纬度坐标，使用Double类型保证精度
    private Double longitude; // 经度坐标，使用Double类型保证精度
    private String carName; // 车牌号码，用于标识具体车辆
    private String status; // 车辆状态，如active(运行中)、inactive(停止)、maintenance(维修中)
}