package tw_six.demo.controller; // 定义用户控制器所在的包，属于控制层

import lombok.RequiredArgsConstructor; // Lombok注解，生成必要的构造函数
import org.springframework.web.bind.annotation.*; // Spring MVC注解集合
import tw_six.demo.entity.User; // 引入用户实体类
import tw_six.demo.service.UserService; // 引入用户业务服务类
import java.util.List; // Java集合框架，用于返回列表数据

/**
 * 用户控制器 - REST API控制层组件
 * 
 * 文件关联说明：
 * 1. 与UserService关联：通过构造函数注入获得用户业务服务能力
 * 2. 与User实体类关联：接收和返回User对象作为数据载体
 * 3. 与Spring MVC框架关联：通过@RestController注解集成到Web框架
 * 4. 与HTTP协议关联：处理各种HTTP请求方法（GET、POST、DELETE等）
 * 
 * 作用说明：
 * - 提供用户管理的RESTful API接口
 * - 处理HTTP请求并返回JSON格式响应
 * - 作为前后端分离架构的服务端接口层
 * - 协调业务逻辑层和HTTP协议之间的数据转换
 */
@RestController // 标记为REST控制器，自动将返回值序列化为JSON
@RequestMapping("/api/users") // 设置统一的URL前缀，所有方法路径都相对于/api/users
@RequiredArgsConstructor // Lombok注解，为final字段生成构造函数，支持构造函数注入
public class UserController { // 用户控制器类定义
    
    // 使用final修饰确保依赖不可变性，通过构造函数注入
    private final UserService userService; // 用户业务服务依赖
    
    /**
     * 获取所有用户列表
     * 对应HTTP GET /api/users 请求
     * 
     * @return 用户列表JSON数据
     */
    @GetMapping // 映射GET请求到/api/users路径
    public List<User> getAllUsers() { // 控制器方法：获取所有用户
        return userService.getAllUsers(); // 调用业务层方法获取用户列表
    }
    
    /**
     * 根据ID获取单个用户
     * 对应HTTP GET /api/users/{id} 请求
     * 
     * @param id 用户ID，从URL路径变量中提取
     * @return 用户对象JSON数据
     */
    @GetMapping("/{id}") // 映射GET请求到/api/users/{id}路径，{id}为路径变量
    public User getUserById(@PathVariable Long id) { // 控制器方法：根据ID获取用户
        return userService.getUserById(id); // 调用业务层方法查找用户
    }
    
    /**
     * 创建新用户
     * 对应HTTP POST /api/users 请求
     * 
     * @param user 从请求体中解析的用户对象
     * @return 保存后的用户对象JSON数据
     */
    @PostMapping // 映射POST请求到/api/users路径
    public User createUser(@RequestBody User user) { // 控制器方法：创建用户
        return userService.saveUser(user); // 调用业务层方法保存用户
    }
    
    /**
     * 删除用户
     * 对应HTTP DELETE /api/users/{id} 请求
     * 
     * @param id 用户ID，从URL路径变量中提取
     */
    @DeleteMapping("/{id}") // 映射DELETE请求到/api/users/{id}路径
    public void deleteUser(@PathVariable Long id) { // 控制器方法：删除用户
        userService.deleteUser(id); // 调用业务层方法删除用户
    }
}