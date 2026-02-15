package tw_six.demo.controller; // 定义主页控制器所在的包，属于控制层

import org.springframework.web.bind.annotation.GetMapping; // Spring MVC注解，处理GET请求
import org.springframework.web.bind.annotation.RestController; // Spring MVC注解，标记为REST控制器

/**
 * 主页控制器 - Web控制层组件
 * 
 * 文件关联说明：
 * 1. 与Spring MVC框架关联：通过@RestController注解集成到Web框架
 * 2. 与HTTP协议关联：处理浏览器访问根路径的GET请求
 * 3. 与应用启动关联：作为应用的欢迎页面入口
 * 
 * 作用说明：
 * - 处理应用根路径(/)的HTTP请求
 * - 提供简单的欢迎信息响应
 * - 作为Spring Boot应用的基本Web入口点
 */
@RestController // 标记为REST控制器，返回数据而非视图页面
public class HomeController { // 主页控制器类定义
    
    /**
     * 处理根路径GET请求
     * 当用户访问 http://localhost:8080/ 时调用此方法
     * 
     * @return 欢迎信息字符串
     */
    @GetMapping("/") // 映射GET请求到根路径
    public String home() { // 控制器方法：处理主页请求
        return "Welcome to Spring Boot Demo Application!"; // 返回欢迎信息
    }
}