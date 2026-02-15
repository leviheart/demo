package tw_six.demo.service; // 定义路线规划业务服务类所在的包

import lombok.RequiredArgsConstructor; // Lombok注解，生成必要的构造函数
import org.springframework.stereotype.Service; // Spring注解，标记为业务服务组件
import tw_six.demo.entity.Route; // 引入路线实体类
import tw_six.demo.repository.RouteRepository; // 引入路线数据访问接口

/**
 * 路线规划业务服务类 - 地图导航系统路线规划业务逻辑层组件
 * 
 * 文件关联说明：
 * 1. 与RouteRepository关联：通过构造函数注入获得路线数据访问能力
 * 2. 与Route实体类关联：处理路线对象的业务逻辑
 * 3. 与MapController关联：通过依赖注入为地图控制层提供路线规划业务服务
 * 4. 与Spring容器关联：通过@Service注解注册为Spring Bean
 * 
 * 作用说明：
 * - 封装路线规划相关的业务逻辑处理
 * - 实现智能路线缓存和复用机制
 * - 提供标准化的路线规划业务接口
 * - 支持地图导航系统的核心路线规划功能
 */
@Service // 标记为Spring业务服务组件，纳入Spring容器管理
@RequiredArgsConstructor // Lombok注解，为final字段生成构造函数，支持构造函数注入
public class RouteService { // 路线规划业务服务类定义
    
    // 使用final修饰确保依赖不可变性，通过构造函数注入
    private final RouteRepository routeRepository; // 路线数据访问接口依赖
    
    /**
     * 规划从起点到终点的路线
     * 实现路线缓存机制：先查找是否已有相同路线，避免重复计算
     * 
     * @param start 起点位置
     * @param end 终点位置
     * @return 路线规划结果对象
     */
    public Route planRoute(String start, String end) { // 业务方法：路线规划
        // 先查找是否已有相同路线缓存
        Route existingRoute = routeRepository.findByStartPointAndEndPoint(start, end);
        if (existingRoute != null) { // 如果找到缓存路线
            return existingRoute; // 直接返回缓存结果
        }
        
        // 创建新的路线对象
        Route newRoute = new Route();
        newRoute.setStartPoint(start); // 设置起点
        newRoute.setEndPoint(end); // 设置终点
        newRoute.setDistance(calculateDistance(start, end)); // 计算距离
        newRoute.setRouteInfo("从 " + start + " 到 " + end + " 的推荐路线"); // 设置路线描述
        
        // 保存新路线并返回
        return routeRepository.save(newRoute);
    }
    
    /**
     * 计算两点间距离的私有方法
     * TODO：实际项目中应集成真实的地图API进行精确计算
     * 
     * @param start 起点
     * @param end 终点
     * @return 计算得到的距离（公里）
     */
    private Double calculateDistance(String start, String end) { // 私有方法：距离计算
        // 示例：返回固定距离值，实际应调用地图API
        return 10.5; // 返回示例距离值
    }
}