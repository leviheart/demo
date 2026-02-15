package tw_six.demo.repository; // 定义路线数据访问接口所在的包

import org.springframework.data.jpa.repository.JpaRepository; // Spring Data JPA核心接口
import org.springframework.stereotype.Repository; // Spring注解，标记为数据访问组件
import tw_six.demo.entity.Route; // 引入路线实体类

/**
 * 路线数据访问接口 - 地图导航系统路线规划数据访问层组件
 * 
 * 文件关联说明：
 * 1. 与Route实体类关联：泛型参数指定操作Route实体
 * 2. 与RouteService关联：通过依赖注入为路线规划业务提供数据访问能力
 * 3. 与数据库表关联：继承JpaRepository自动映射到route表
 * 4. 与Spring容器关联：通过@Repository注解注册为Spring Bean
 * 5. 与MapController关联：间接为路线规划REST接口提供数据支持
 * 
 * 作用说明：
 * - 继承JpaRepository获得标准CRUD操作
 * - 定义自定义查询方法findByStartPointAndEndPoint用于路线缓存查询
 * - 作为路线数据访问的核心接口，提供路线信息的持久化能力
 * - 支持智能路线规划和历史路线复用功能
 */
@Repository // 标记为Spring数据访问组件，纳入Spring容器管理
public interface RouteRepository extends JpaRepository<Route, Long> { // 继承JPA标准接口
    // JpaRepository<Route, Long> 泛型说明：
    // Route：操作的路线实体类类型
    // Long：实体主键的数据类型
    
    /**
     * 自定义查询方法：根据起点和终点查找已有路线
     * 用于路线缓存机制，避免重复计算相同路径
     * Spring Data JPA会自动根据方法名生成对应的SQL查询
     * 
     * @param startPoint 起点位置
     * @param endPoint 终点位置
     * @return 匹配的路线对象，如果不存在则返回null
     */
    Route findByStartPointAndEndPoint(String startPoint, String endPoint); // 根据起终点查询路线
}