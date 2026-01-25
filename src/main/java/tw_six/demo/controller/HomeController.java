package tw_six.demo.controller; // 定义控制器所在的包

import org.springframework.web.bind.annotation.RestController; // 导入REST控制器注解
import org.springframework.web.bind.annotation.GetMapping; // 导入GET请求映射注解

@RestController // 标记此类为REST控制器，处理HTTP请求
public class HomeController { // 主页控制器类
    
    @GetMapping("/") // 映射GET请求到根路径"/"
    public String home() { // 返回主页内容的方法
        return "Hello World!"; // 返回字符串响应
    }
}