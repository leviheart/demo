package tw_six.demo.controller; // 定义控制器所在的包

import org.springframework.web.bind.annotation.RestController; // 导入REST控制器注解
import org.springframework.web.bind.annotation.GetMapping; // 导入GET请求映射注解
import org.springframework.web.bind.annotation.PostMapping; // 导入POST请求映射注解
import org.springframework.web.bind.annotation.RequestBody; // 导入请求体注解，用于接收JSON数据
import tw_six.demo.entity.User; // 导入User实体类
import tw_six.demo.service.UserService; // 导入UserService服务类

import java.util.List; // 导入List集合类

@RestController // 标记此类为REST控制器，处理HTTP请求
public class UserController { // 用户控制器类，处理用户相关的HTTP请求
    
    private final UserService userService; // 声明UserService实例变量，用于调用业务逻辑
    
    public UserController(UserService userService) { // 构造函数，通过依赖注入获取UserService实例
        this.userService = userService; // 将传入的userService赋值给成员变量
    }
    
    @GetMapping("/users") // 映射GET请求到/users路径
    public List<User> getUsers() { // 获取所有用户的方法
        return userService.getAllUsers(); // 调用服务层方法获取所有用户并返回
    }
    
    @PostMapping("/users") // 映射POST请求到/users路径
    public User createUser(@RequestBody User user) { // 创建用户的方法，@RequestBody将JSON转换为User对象
        return userService.saveUser(user); // 调用服务层方法保存用户并返回
    }
}