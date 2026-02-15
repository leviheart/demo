package tw_six.demo.controller; // 定义地图视图控制器所在的包，属于控制层

import org.springframework.stereotype.Controller; // Spring MVC注解，标记为普通控制器
import org.springframework.web.bind.annotation.GetMapping; // Spring MVC注解，处理GET请求

/**
 * 地图视图控制器 - 汽车地图导航系统页面路由控制层组件
 * 
 * 文件关联说明：
 * 1. 与Thymeleaf模板引擎关联：通过@Controller注解配合视图解析器工作
 * 2. 与map.html模板关联：返回"map"字符串对应templates/map.html文件
 * 3. 与Spring MVC框架关联：处理浏览器访问/map路径的请求
 * 4. 与前端页面关联：提供地图导航系统主页面的访问入口
 * 
 * 作用说明：
 * - 处理地图页面的HTTP GET请求
 * - 通过视图解析器返回对应的HTML模板
 * - 作为传统服务端渲染架构的页面控制器
 * - 与REST API控制器配合，提供完整的Web应用功能
 */
@Controller // 标记为普通控制器，返回视图名称而非JSON数据
public class MapViewController { // 地图视图控制器类定义
    
    /**
     * 处理地图页面GET请求
     * 当用户访问 http://localhost:8080/map 时调用此方法
     * 
     * @return 视图名称"map"，对应templates/map.html模板文件
     */
    @GetMapping("/map") // 映射GET请求到/map路径
    public String mapView() { // 控制器方法：处理地图页面请求
        return "map"; // 返回视图名称，Spring会自动解析为templates/map.html
    }
}