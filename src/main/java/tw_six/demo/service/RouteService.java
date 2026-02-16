package tw_six.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tw_six.demo.entity.Route;
import tw_six.demo.repository.RouteRepository;

/**
 * 路线服务类 - 业务逻辑层
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供路线规划的核心业务逻辑，支持路线查询、缓存和计算。
 * 实现了路线信息的持久化缓存，避免重复计算。
 * 
 * 【服务方法列表】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 方法名         │ 功能描述                         │ 返回值             │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ planRoute      │ 规划两点之间的路线               │ Route              │
 * │ calculateDistance │ 计算两点间距离（私有方法）    │ Double             │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【业务流程】
 * 1. 接收起点和终点坐标
 * 2. 首先查询数据库缓存
 * 3. 如果缓存命中则直接返回
 * 4. 如果缓存未命中则计算新路线
 * 5. 保存新路线到数据库并返回
 * 
 * 【关联文件】
 * - 控制器: tw_six.demo.controller.MapController
 * - 实体类: tw_six.demo.entity.Route
 * - 仓库层: tw_six.demo.repository.RouteRepository
 * 
 * 【扩展建议】
 * - 集成第三方地图API（高德、百度、Google）
 * - 添加多种路线策略（最短、最快、避开高速）
 * - 添加路况信息查询
 * - 实现路线缓存过期机制
 * 
 * 【坐标格式】
 * 当前使用字符串格式存储坐标，如："116.4074,39.9042"
 * 建议后续改为更结构化的格式
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Service
public class RouteService {
    
    private final RouteRepository routeRepository;
    
    @Autowired
    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }
    
    /**
     * 规划行车路线
     * 
     * 功能说明:
     * - 根据起点和终点规划最优路线
     * - 优先查询数据库缓存，提高响应速度
     * - 缓存未命中时创建新路线记录
     * 
     * 缓存策略:
     * - 使用起点+终点作为缓存键
     * - 相同起终点的路线只计算一次
     * - 后续请求直接从数据库读取
     * 
     * @param start 起点坐标，格式："经度,纬度"
     * @param end   终点坐标，格式："经度,纬度"
     * @return 路线对象，包含距离、路线信息等
     */
    public Route planRoute(String start, String end) {
        Route existingRoute = routeRepository.findByStartPointAndEndPoint(start, end);
        if (existingRoute != null) {
            return existingRoute;
        }
        
        Route newRoute = new Route();
        newRoute.setStartPoint(start);
        newRoute.setEndPoint(end);
        newRoute.setDistance(calculateDistance(start, end));
        newRoute.setRouteInfo("从 " + start + " 到 " + end + " 的推荐路线");
        
        return routeRepository.save(newRoute);
    }
    
    /**
     * 计算两点之间的距离
     * 
     * 功能说明:
     * - 计算起点和终点之间的直线距离
     * - 当前为简化实现，返回固定值
     * 
     * TODO: 实现真实的距离计算
     * - 可使用Haversine公式计算球面距离
     * - 或调用第三方API获取实际道路距离
     * 
     * @param start 起点坐标
     * @param end   终点坐标
     * @return 距离（单位：公里），当前返回固定值10.5
     */
    private Double calculateDistance(String start, String end) {
        return 10.5;
    }
}
