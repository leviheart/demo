package tw_six.demo.repository; // 定义汽车位置数据访问接口所在的包

import org.springframework.data.jpa.repository.JpaRepository; // Spring Data JPA核心接口
import org.springframework.stereotype.Repository; // Spring注解，标记为数据访问组件
import tw_six.demo.entity.CarLocation; // 引入汽车位置实体类
import java.util.List; // Java集合框架，用于返回列表数据

/**
 * 汽车位置数据访问接口 - 地图导航系统数据访问层组件
 * 
 * 文件关联说明：
 * 1. 与CarLocation实体类关联：泛型参数指定操作CarLocation实体
 * 2. 与CarLocationService关联：通过依赖注入为车辆位置业务提供数据访问能力
 * 3. 与数据库表关联：继承JpaRepository自动映射到car_location表
 * 4. 与Spring容器关联：通过@Repository注解注册为Spring Bean
 * 5. 与MapController关联：间接为REST接口提供车辆位置数据支持
 * 
 * 作用说明：
 * - 继承JpaRepository获得标准CRUD操作
 * - 定义自定义查询方法findByStatus用于筛选特定状态的车辆
 * - 作为车辆位置数据访问的核心接口，提供位置信息的持久化能力
 * - 支持实时车辆位置更新和状态查询功能
 */
@Repository // 标记为Spring数据访问组件，纳入Spring容器管理
public interface CarLocationRepository extends JpaRepository<CarLocation, Long> { // 继承JPA标准接口
    // JpaRepository<CarLocation, Long> 泛型说明：
    // CarLocation：操作的汽车位置实体类类型
    // Long：实体主键的数据类型
    
    /**
     * 自定义查询方法：根据车辆状态查找车辆位置
     * Spring Data JPA会自动根据方法名生成对应的SQL查询
     * 
     * @param status 车辆状态参数（active/inactive/maintenance）
     * @return 符合状态条件的车辆位置列表
     */
    List<CarLocation> findByStatus(String status); // 根据状态查询车辆位置列表
}