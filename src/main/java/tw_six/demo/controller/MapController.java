package tw_six.demo.controller; // 定义地图控制器所在的包，属于控制层

import lombok.RequiredArgsConstructor; // Lombok注解，生成必要的构造函数
import org.springframework.web.bind.annotation.*; // Spring MVC注解集合
import tw_six.demo.entity.CarLocation; // 引入汽车位置实体类
import tw_six.demo.entity.Route; // 引入路线实体类
import tw_six.demo.service.CarLocationService; // 引入汽车位置业务服务类
import tw_six.demo.service.RouteService; // 引入路线规划业务服务类
import java.util.List; // Java集合框架，用于返回列表数据

/**
 * 地图控制器 - 汽车地图导航系统REST API控制层组件
 * 
 * 文件关联说明：
 * 1. 与CarLocationService关联：通过构造函数注入获得车辆位置业务服务能力
 * 2. 与RouteService关联：通过构造函数注入获得路线规划业务服务能力
 * 3. 与CarLocation/Route实体类关联：接收和返回位置及路线对象
 * 4. 与Spring MVC框架关联：通过@RestController注解集成到Web框架
 * 5. 与前端页面关联：提供API接口供JavaScript调用
 * 
 * 作用说明：
 * - 提供汽车地图导航系统的RESTful API接口
 * - 处理车辆位置更新、路线规划等核心功能请求
 * - 作为前后端分离架构的服务端接口层
 * - 协调业务逻辑层和HTTP协议之间的数据转换
 */
@RestController // 标记为REST控制器，自动将返回值序列化为JSON
@RequestMapping("/api/map") // 设置统一的URL前缀，所有方法路径都相对于/api/map
@RequiredArgsConstructor // Lombok注解，为final字段生成构造函数，支持构造函数注入
public class MapController { // 地图控制器类定义
    
    // 使用final修饰确保依赖不可变性，通过构造函数注入
    private final CarLocationService carLocationService; // 车辆位置业务服务依赖
    private final RouteService routeService; // 路线规划业务服务依赖
    
    /**
     * 获取所有车辆位置信息
     * 对应HTTP GET /api/map/locations 请求
     * 
     * @return 车辆位置列表JSON数据
     */
    @GetMapping("/locations") // 映射GET请求到/api/map/locations路径
    public List<CarLocation> getAllLocations() { // 控制器方法：获取所有车辆位置
        return carLocationService.getAllLocations(); // 调用业务层方法获取位置列表
    }
    
    /**
     * 更新车辆位置信息
     * 对应HTTP POST /api/map/location 请求
     * 
     * @param location 从请求体中解析的车辆位置对象
     * @return 保存后的车辆位置对象JSON数据
     */
    @PostMapping("/location") // 映射POST请求到/api/map/location路径
    public CarLocation updateLocation(@RequestBody CarLocation location) { // 控制器方法：更新车辆位置
        return carLocationService.saveLocation(location); // 调用业务层方法保存位置
    }
    
    /**
     * 获取运行中的车辆列表
     * 对应HTTP GET /api/map/active-cars 请求
     * 
     * @return 运行中车辆列表JSON数据
     */
    @GetMapping("/active-cars") // 映射GET请求到/api/map/active-cars路径
    public List<CarLocation> getActiveCars() { // 控制器方法：获取运行中车辆
        return carLocationService.getActiveLocations(); // 调用业务层方法获取活跃车辆
    }
    
    /**
     * 规划路线
     * 对应HTTP GET /api/map/route 请求（修改为GET方法解决405错误）
     * 
     * @param start 起点参数，从请求参数中提取
     * @param end 终点参数，从请求参数中提取
     * @return 路线规划结果JSON数据
     */
    @GetMapping("/route") // 修改为GET请求以解决405 Method Not Allowed错误
    public Route planRoute(@RequestParam String start, @RequestParam String end) { // 控制器方法：路线规划
        return routeService.planRoute(start, end); // 调用业务层方法进行路线规划
    }
}