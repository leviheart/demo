package tw_six.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tw_six.demo.common.ApiResponse;
import tw_six.demo.entity.VehicleGroup;
import tw_six.demo.service.VehicleGroupService;
import java.util.List;
import java.util.Optional;

/**
 * 车辆分组控制器 - RESTful API接口层
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第一章：控制器层概述】
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * 1.1 什么是控制器（Controller）？
 * ─────────────────────────────────────────────────────────────────────────────
 * 控制器是MVC架构中的"C"（Controller），负责：
 * - 接收HTTP请求：监听特定的URL路径和HTTP方法
 * - 解析请求参数：从URL路径、查询参数、请求体中提取数据
 * - 调用业务逻辑：将请求转发给Service层处理
 * - 构建响应结果：将处理结果封装为HTTP响应返回给客户端
 * 
 * 控制器就像是"餐厅服务员"：
 * - 接待客人（接收请求）
 * - 记录点单（解析参数）
 * - 传达厨房（调用Service）
 * - 上菜结账（返回响应）
 * 
 * 1.2 为什么需要控制器层？
 * ─────────────────────────────────────────────────────────────────────────────
 * 【职责分离原则】
 * 如果没有控制器层，直接在Service中处理HTTP请求：
 * ❌ Service层需要了解HTTP协议细节（请求头、响应码等）
 * ❌ 业务逻辑与Web层耦合，难以复用
 * ❌ 测试困难，需要模拟HTTP环境
 * 
 * 有了控制器层：
 * ✅ Controller专注HTTP协议处理（参数解析、响应封装）
 * ✅ Service专注业务逻辑（可被多个Controller复用）
 * ✅ 各层职责清晰，便于维护和测试
 * 
 * 1.3 三层架构数据流向
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 *   ┌─────────────┐    HTTP请求     ┌─────────────────┐
 *   │   前端/客户端  │ ──────────────▶│   Controller    │
 *   └─────────────┘                 └────────┬────────┘
 *                                            │ 调用
 *                                            ▼
 *   ┌─────────────┐    返回结果     ┌─────────────────┐
 *   │   数据库      │ ◀──────────────│     Service     │
 *   └─────────────┘                 └────────┬────────┘
 *                                            │ 调用
 *                                            ▼
 *                                   ┌─────────────────┐
 *                                   │   Repository    │
 *                                   └─────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第二章：核心注解详解】
 * ═══════════════════════════════════════════════════════════════════════════════
 */

/**
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ @RestController 注解详解                                                    │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * 2.1 @RestController 是什么？
 * ─────────────────────────────────────────────────────────────────────────────
 * @RestController 是一个组合注解，等价于：
 *   @Controller + @ResponseBody
 * 
 * 【源码定义】
 * @Target(ElementType.TYPE)      // 只能用在类上
 * @Retention(RetentionPolicy.RUNTIME)  // 运行时保留
 * @Documented
 * @Controller                    // 标记为Spring MVC控制器
 * @ResponseBody                  // 方法返回值直接作为响应体
 * public @interface RestController { ... }
 * 
 * 2.2 为什么使用 @RestController？
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * 【传统 @Controller 的写法】
 * @Controller
 * public class UserController {
 *     
 *     @RequestMapping("/user")
 *     @ResponseBody  // 每个方法都要加这个注解
 *     public User getUser() {
 *         return new User();
 *     }
 * }
 * 
 * 【使用 @RestController 的写法】
 * @RestController  // 类上加一个注解就够了
 * public class UserController {
 *     
 *     @RequestMapping("/user")  // 方法上不需要 @ResponseBody
 *     public User getUser() {
 *         return new User();  // 自动序列化为JSON
 *     }
 * }
 * 
 * 2.3 @ResponseBody 的作用
 * ─────────────────────────────────────────────────────────────────────────────
 * - 默认情况下，Controller方法返回的是"视图名称"（如JSP、Thymeleaf模板名）
 * - 加上@ResponseBody后，返回值直接写入HTTP响应体
 * - Spring Boot使用Jackson库将对象序列化为JSON格式
 * 
 * 【序列化过程】
 * Java对象 ──▶ Jackson ObjectMapper ──▶ JSON字符串 ──▶ HTTP响应体
 * 
 * 例如：
 * User对象 {name: "张三", age: 25}
 * 转换为：
 * {"name":"张三","age":25}
 * 
 * 2.4 RESTful风格的含义
 * ─────────────────────────────────────────────────────────────────────────────
 * REST = Representational State Transfer（表述性状态转移）
 * 
 * 【REST原则】
 * 1. 资源（Resource）：一切皆资源，用URI标识
 *    例如：/api/vehicle-groups 表示"车辆分组"资源集合
 * 
 * 2. 统一接口（Uniform Interface）：使用标准HTTP方法
 *    - GET：获取资源
 *    - POST：创建资源
 *    - PUT：更新资源
 *    - DELETE：删除资源
 * 
 * 3. 无状态（Stateless）：每个请求包含所有必要信息
 *    服务器不保存客户端状态，便于水平扩展
 * 
 * 4. 表述（Representation）：资源可以有多种表示形式
 *    JSON、XML、HTML等，通过Content-Type协商
 */
@RestController

/**
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ @RequestMapping 注解详解                                                    │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * 2.5 @RequestMapping 是什么？
 * ─────────────────────────────────────────────────────────────────────────────
 * @RequestMapping 用于将HTTP请求映射到控制器方法。
 * 
 * 【基本属性】
 * ┌────────────────┬──────────────────────────────────────────────────────────┐
 * │ 属性            │ 说明                                                      │
 * ├────────────────┼──────────────────────────────────────────────────────────┤
 * │ value / path   │ 请求路径，如 "/api/users"                                  │
 * │ method         │ HTTP方法，如 GET、POST、PUT、DELETE                        │
 * │ params         │ 请求参数条件，如 "id=123"                                  │
 * │ headers        │ 请求头条件，如 "content-type=application/json"            │
 * │ consumes       │ 请求内容类型，如 "application/json"                        │
 * │ produces       │ 响应内容类型，如 "application/json"                        │
 * └────────────────┴──────────────────────────────────────────────────────────┘
 * 
 * 【使用示例】
 * // 完整写法
 * @RequestMapping(value = "/users", method = RequestMethod.GET)
 * 
 * // 多个路径
 * @RequestMapping({"/users", "/members"})
 * 
 * // 多个方法
 * @RequestMapping(value = "/users", method = {RequestMethod.GET, RequestMethod.POST})
 * 
 * 2.6 为什么用类级别的 @RequestMapping？
 * ─────────────────────────────────────────────────────────────────────────────
 * 【好处】
 * 1. 统一路径前缀：所有方法共享基础路径
 * 2. 便于维护：修改路径只需改一处
 * 3. 路径分组：相关接口集中在同一前缀下
 * 
 * 【路径组合规则】
 * 类级别：@RequestMapping("/api/vehicle-groups")
 * 方法级别：@GetMapping("/{id}")
 * 最终路径：/api/vehicle-groups/{id}
 * 
 * 2.7 路径变量的命名规则
 * ─────────────────────────────────────────────────────────────────────────────
 * 【推荐命名规范】
 * - 使用kebab-case（短横线分隔）：/vehicle-groups（推荐）
 * - 避免camelCase：/vehicleGroups（不推荐）
 * - 避免下划线：/vehicle_groups（不推荐）
 * - 使用复数形式表示资源集合：/groups 而非 /group
 * 
 * 【RESTful路径设计】
 * GET    /api/vehicle-groups        # 获取所有分组
 * POST   /api/vehicle-groups        # 创建新分组
 * GET    /api/vehicle-groups/{id}   # 获取指定分组
 * PUT    /api/vehicle-groups/{id}   # 更新指定分组
 * DELETE /api/vehicle-groups/{id}   # 删除指定分组
 */
@RequestMapping("/api/vehicle-groups")
public class VehicleGroupController {
    
    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 依赖注入：控制器如何获取Service实例                                       │
     * └────────────────────────────────────────────────────────────────────────┘
     * 
     * 2.8 为什么使用final字段？
     * ─────────────────────────────────────────────────────────────────────────
     * private final VehicleGroupService vehicleGroupService;
     * 
     * 【final关键字的作用】
     * 1. 不可变：字段一旦赋值就不能修改
     * 2. 线程安全：final字段在构造函数完成后对所有线程可见
     * 3. 编译检查：如果忘记在构造函数中初始化，编译器会报错
     * 
     * 【为什么推荐final？】
     * - 依赖注入后不应该再改变
     * - 防止意外修改依赖
     * - 明确表达"这是必需依赖"的语义
     */
    private final VehicleGroupService vehicleGroupService;
    
    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ @Autowired 注解详解                                                     │
     * └────────────────────────────────────────────────────────────────────────┘
     * 
     * 2.9 @Autowired 是什么？
     * ─────────────────────────────────────────────────────────────────────────
     * @Autowired 是Spring的依赖注入注解，用于自动装配Bean。
     * 
     * 【工作原理】
     * 1. Spring容器启动时扫描所有@Bean和@Component
     * 2. 创建所有Bean实例并放入IoC容器
     * 3. 扫描@Autowired注解，按类型查找匹配的Bean
     * 4. 通过反射将Bean注入到目标位置
     * 
     * 【装配方式】
     * ┌──────────────┬──────────────────────────────────────────────────────────┐
     * │ 方式          │ 说明                                                      │
     * ├──────────────┼──────────────────────────────────────────────────────────┤
     * │ 按类型（默认）  │ 根据字段类型查找Bean，最常用                                 │
     * │ 按名称        │ 使用@Qualifier指定Bean名称                                 │
     * │ 按构造函数     │ 推荐方式，保证依赖不可变                                     │
     * └──────────────┴──────────────────────────────────────────────────────────┘
     * 
     * 2.10 构造函数注入 vs 字段注入 vs Setter注入
     * ─────────────────────────────────────────────────────────────────────────
     * 
     * 【方式一：字段注入（不推荐）】
     * @Autowired
     * private VehicleGroupService vehicleGroupService;
     * 
     * 缺点：
     * - 无法使用final字段
     * - 难以进行单元测试（需要反射或Mock框架）
     * - 隐藏了类的依赖关系
     * 
     * 【方式二：Setter注入（可选依赖时使用）】
     * private VehicleGroupService vehicleGroupService;
     * 
     * @Autowired
     * public void setVehicleGroupService(VehicleGroupService service) {
     *     this.vehicleGroupService = service;
     * }
     * 
     * 适用场景：
     * - 可选依赖（依赖可能为null）
     * - 需要运行时更换依赖
     * 
     * 【方式三：构造函数注入（强烈推荐）】
     * private final VehicleGroupService vehicleGroupService;
     * 
     * @Autowired
     * public VehicleGroupController(VehicleGroupService vehicleGroupService) {
     *     this.vehicleGroupService = vehicleGroupService;
     * }
     * 
     * 优点：
     * ✅ 可以使用final字段，保证不可变
     * ✅ 依赖关系明确，一眼看出类需要哪些依赖
     * ✅ 便于单元测试，可以直接new对象传入Mock
     * ✅ 保证对象创建时所有依赖都已注入
     * 
     * 【Spring 4.3+ 的优化】
     * 如果类只有一个构造函数，@Autowired可以省略：
     * public VehicleGroupController(VehicleGroupService vehicleGroupService) {
     *     this.vehicleGroupService = vehicleGroupService;
     * }
     * Spring会自动使用这个构造函数进行注入。
     */
    @Autowired
    public VehicleGroupController(VehicleGroupService vehicleGroupService) {
        this.vehicleGroupService = vehicleGroupService;
    }
    
    /**
     * ══════════════════════════════════════════════════════════════════════════
     * 【第三章：HTTP方法映射注解】
     * ══════════════════════════════════════════════════════════════════════════
     */
    
    /**
     * 创建新分组
     * 
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ @PostMapping 注解详解                                                   │
     * └────────────────────────────────────────────────────────────────────────┘
     * 
     * 3.1 @PostMapping 是什么？
     * ─────────────────────────────────────────────────────────────────────────
     * @PostMapping 是 @RequestMapping(method = RequestMethod.POST) 的快捷方式。
     * 
     * 【等价写法】
     * @PostMapping("/api/vehicle-groups")
     * 等价于
     * @RequestMapping(value = "/api/vehicle-groups", method = RequestMethod.POST)
     * 
     * 【HTTP方法快捷注解】
     * ┌─────────────────┬───────────────────────────────────────────────────────┐
     * │ 注解             │ 说明                                                   │
     * ├─────────────────┼───────────────────────────────────────────────────────┤
     * │ @GetMapping     │ 查询资源，幂等、安全                                      │
     * │ @PostMapping    │ 创建资源，非幂等                                         │
     * │ @PutMapping     │ 更新资源（整体更新），幂等                                  │
     * │ @DeleteMapping  │ 删除资源，幂等                                           │
     * │ @PatchMapping   │ 部分更新资源，非幂等                                      │
     * └─────────────────┴───────────────────────────────────────────────────────┘
     * 
     * 3.2 幂等性（Idempotency）解释
     * ─────────────────────────────────────────────────────────────────────────
     * 【幂等性定义】
     * 同一个请求执行一次和执行多次，对资源产生的影响是相同的。
     * 
     * 【幂等操作】
     * GET：查询不会改变资源状态，天然幂等
     * PUT：整体更新，多次更新结果相同，幂等
     * DELETE：删除同一资源多次，结果相同，幂等
     * 
     * 【非幂等操作】
     * POST：每次创建都会产生新资源，非幂等
     *   例如：POST /orders 创建订单，调用3次会创建3个订单
     * 
     * 【实际意义】
     * - 网络超时重试：幂等操作可以安全重试
     * - 分布式系统：幂等性保证消息处理的正确性
     * 
     * 3.3 @RequestBody 注解详解
     * ─────────────────────────────────────────────────────────────────────────
     * @RequestBody 用于将HTTP请求体绑定到方法参数。
     * 
     * 【工作流程】
     * 1. 客户端发送JSON格式的请求体
     * 2. Spring使用HttpMessageConverter解析请求体
     * 3. Jackson将JSON反序列化为Java对象
     * 4. 将对象注入到方法参数
     * 
     * 【请求示例】
     * POST /api/vehicle-groups
     * Content-Type: application/json
     * 
     * {
     *   "groupName": "华北区车队",
     *   "description": "负责华北区域的配送车辆",
     *   "vehicles": ["京A12345", "京B67890"],
     *   "status": "ACTIVE"
     * }
     * 
     * 【反序列化过程】
     * JSON字符串 ──▶ Jackson ObjectMapper ──▶ VehicleGroup对象
     * 
     * 【注意事项】
     * - 需要Content-Type: application/json
     * - JSON字段名要与Java属性名匹配（或使用@JsonProperty）
     * - 必须有无参构造函数（或使用@JsonCreator）
     * 
     * @param group 分组对象（JSON格式请求体）
     * @return 创建成功的分组对象
     */
    @PostMapping
    public ApiResponse<VehicleGroup> createGroup(@RequestBody VehicleGroup group) {
        /**
         * ┌────────────────────────────────────────────────────────────────────┐
         * │ 异常处理策略                                                         │
         * └────────────────────────────────────────────────────────────────────┘
         * 
         * 3.4 控制器层的异常处理
         * ─────────────────────────────────────────────────────────────────────
         * 
         * 【当前方式：try-catch】
         * 优点：简单直观，适合简单场景
         * 缺点：代码重复，每个方法都要写try-catch
         * 
         * 【推荐方式：全局异常处理器】
         * 使用 @ControllerAdvice + @ExceptionHandler：
         * 
         * @ControllerAdvice
         * public class GlobalExceptionHandler {
         *     
         *     @ExceptionHandler(IllegalArgumentException.class)
         *     public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException e) {
         *         return ApiResponse.error(400, e.getMessage());
         *     }
         *     
         *     @ExceptionHandler(RuntimeException.class)
         *     public ApiResponse<Void> handleRuntime(RuntimeException e) {
         *         return ApiResponse.error(500, "服务器内部错误");
         *     }
         * }
         * 
         * 这样控制器方法可以简化为：
         * @PostMapping
         * public ApiResponse<VehicleGroup> createGroup(@RequestBody VehicleGroup group) {
         *     VehicleGroup createdGroup = vehicleGroupService.createGroup(group);
         *     return ApiResponse.success("分组创建成功", createdGroup);
         * }
         * 
         * 异常会被全局处理器自动捕获并转换为统一响应格式。
         */
        try {
            VehicleGroup createdGroup = vehicleGroupService.createGroup(group);
            return ApiResponse.success("分组创建成功", createdGroup);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
    
    /**
     * 更新分组信息
     * 
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ @PutMapping 和 @PathVariable 注解详解                                    │
     * └────────────────────────────────────────────────────────────────────────┘
     * 
     * 3.5 @PutMapping 注解
     * ─────────────────────────────────────────────────────────────────────────
     * @PutMapping 用于映射HTTP PUT请求，通常用于更新资源。
     * 
     * 【PUT vs POST 的区别】
     * ┌─────────────┬───────────────────────────────────────────────────────────┐
     * │ 方面         │ PUT                        │ POST                         │
     * ├─────────────┼───────────────────────────────────────────────────────────┤
     * │ 语义         │ 更新已有资源                 │ 创建新资源                    │
     * │ 幂等性       │ 幂等                        │ 非幂等                        │
     * │ URL         │ 通常包含资源ID              │ 通常不包含ID                  │
     * │ 响应         │ 返回更新后的资源             │ 返回创建的资源（含新ID）        │
     * └─────────────┴───────────────────────────────────────────────────────────┘
     * 
     * 【PUT vs PATCH 的区别】
     * PUT：整体更新，需要提供资源的完整数据
     * PATCH：部分更新，只需提供要修改的字段
     * 
     * 例如更新分组：
     * PUT /api/vehicle-groups/1
     * { "groupName": "新名称", "description": "新描述", "vehicles": [...], "status": "ACTIVE" }
     * 
     * PATCH /api/vehicle-groups/1
     * { "groupName": "新名称" }  // 只更新名称
     * 
     * 3.6 @PathVariable 注解详解
     * ─────────────────────────────────────────────────────────────────────────
     * @PathVariable 用于从URL路径中提取变量值。
     * 
     * 【基本用法】
     * @PutMapping("/{id}")
     * public ApiResponse<VehicleGroup> updateGroup(@PathVariable Long id, ...)
     * 
     * 请求：PUT /api/vehicle-groups/123
     * 结果：id = 123
     * 
     * 【路径变量 vs 请求参数】
     * ┌─────────────────┬─────────────────────────────────────────────────────┐
     * │ 类型             │ 示例                                    │ 用途        │
     * ├─────────────────┼─────────────────────────────────────────────────────┤
     * │ 路径变量          │ /users/{id}                             │ 标识资源     │
     * │ 请求参数          │ /users?page=1&size=10                   │ 过滤/分页   │
     * └─────────────────┴─────────────────────────────────────────────────────┘
     * 
     * 【路径变量的高级用法】
     * 
     * // 1. 多个路径变量
     * @GetMapping("/users/{userId}/orders/{orderId}")
     * public Order getOrder(@PathVariable Long userId, @PathVariable Long orderId)
     * 
     * // 2. 正则表达式约束
     * @GetMapping("/{id:\\d+}")  // 只匹配数字
     * public User getById(@PathVariable Long id)
     * 
     * // 3. 可选路径变量（Spring 4.3.3+）
     * @GetMapping({"/users", "/users/{id}"})
     * public List<User> getUsers(@PathVariable(required = false) Long id)
     * 
     * // 4. 不同名称
     * @GetMapping("/{groupId}")
     * public Group getGroup(@PathVariable("groupId") Long id)  // URL变量名与方法参数名不同
     * 
     * @param id    分组ID（从URL路径提取）
     * @param group 更新后的分组对象（从请求体解析）
     * @return 更新后的分组对象
     */
    @PutMapping("/{id}")
    public ApiResponse<VehicleGroup> updateGroup(@PathVariable Long id, @RequestBody VehicleGroup group) {
        try {
            VehicleGroup updatedGroup = vehicleGroupService.updateGroup(id, group);
            return ApiResponse.success("分组更新成功", updatedGroup);
        } catch (RuntimeException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }
    
    /**
     * 删除分组
     * 
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ @DeleteMapping 注解详解                                                 │
     * └────────────────────────────────────────────────────────────────────────┘
     * 
     * 3.7 @DeleteMapping 注解
     * ─────────────────────────────────────────────────────────────────────────
     * @DeleteMapping 用于映射HTTP DELETE请求，用于删除资源。
     * 
     * 【DELETE请求的特点】
     * - 幂等性：删除同一资源多次，结果相同
     * - 通常不需要请求体
     * - 成功响应：200 OK 或 204 No Content
     * 
     * 【RESTful删除的最佳实践】
     * 
     * 1. 物理删除 vs 软删除
     *    物理删除：从数据库真正删除记录
     *    软删除：设置deleted标志位，记录仍保留
     * 
     *    软删除的优点：
     *    - 可以恢复误删数据
     *    - 保留历史记录用于审计
     *    - 维护数据完整性（外键关联）
     * 
     * 2. 删除前检查
     *    - 是否存在关联数据？
     *    - 用户是否有权限删除？
     *    - 是否需要级联删除？
     * 
     * 3. 响应格式
     *    成功：返回被删除的资源或确认消息
     *    失败：返回错误原因（资源不存在/权限不足等）
     * 
     * @param id 要删除的分组ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteGroup(@PathVariable Long id) {
        try {
            vehicleGroupService.deleteGroup(id);
            return ApiResponse.success("分组删除成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }
    
    /**
     * 获取所有分组
     * 
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ @GetMapping 注解详解                                                    │
     * └────────────────────────────────────────────────────────────────────────┘
     * 
     * 3.8 @GetMapping 注解
     * ─────────────────────────────────────────────────────────────────────────
     * @GetMapping 用于映射HTTP GET请求，通常用于查询资源。
     * 
     * 【GET请求的特点】
     * - 安全性：不应该修改服务器状态
     * - 幂等性：多次请求结果相同
     * - 可缓存：响应可以被缓存
     * - 参数传递：通过URL查询参数
     * 
     * 【查询参数的获取方式】
     * 
     * // 方式1：@RequestParam
     * @GetMapping("/users")
     * public List<User> getUsers(
     *     @RequestParam(required = false) String name,
     *     @RequestParam(defaultValue = "0") int page,
     *     @RequestParam(defaultValue = "10") int size
     * )
     * 
     * // 方式2：@RequestParam Map
     * @GetMapping("/search")
     * public List<User> search(@RequestParam Map<String, String> params)
     * 
     * // 方式3：POJO绑定
     * @GetMapping("/search")
     * public List<User> search(UserQuery query)  // 自动绑定查询参数到对象
     * 
     * 【分页查询的最佳实践】
     * 
     * @GetMapping
     * public Page<VehicleGroup> getAllGroups(
     *     @RequestParam(defaultValue = "0") int page,
     *     @RequestParam(defaultValue = "10") int size,
     *     @RequestParam(defaultValue = "id") String sort,
     *     @RequestParam(defaultValue = "asc") String direction
     * ) {
     *     Pageable pageable = PageRequest.of(
     *         page, 
     *         size, 
     *         direction.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
     *         sort
     *     );
     *     return vehicleGroupService.getAllGroups(pageable);
     * }
     * 
     * @return 所有分组列表
     */
    @GetMapping
    public ApiResponse<List<VehicleGroup>> getAllGroups() {
        List<VehicleGroup> groups = vehicleGroupService.getAllGroups();
        return ApiResponse.success(groups);
    }
    
    /**
     * 获取启用的分组
     * 
     * 3.9 路径设计原则
     * ─────────────────────────────────────────────────────────────────────────
     * 
     * 【路径层级设计】
     * /api/vehicle-groups          # 资源集合
     * /api/vehicle-groups/active   # 集合的子资源/过滤条件
     * /api/vehicle-groups/{id}     # 具体资源
     * 
     * 【路径冲突问题】
     * 问题：/api/vehicle-groups/active 和 /api/vehicle-groups/{id} 会冲突吗？
     * 答案：不会，Spring会优先匹配具体路径，再匹配路径变量。
     * 
     * 匹配顺序：
     * 1. /api/vehicle-groups/active → getActiveGroups()  ✅ 精确匹配
     * 2. /api/vehicle-groups/123    → getGroupById()     ✅ 路径变量匹配
     * 
     * 【设计建议】
     * - 使用名词表示资源，动词通过HTTP方法表达
     * - 避免在URL中使用动词：/getActiveGroups ❌
     * - 使用查询参数表示过滤：/groups?status=active ✅
     * 
     * @return 启用状态的分组列表
     */
    @GetMapping("/active")
    public ApiResponse<List<VehicleGroup>> getActiveGroups() {
        List<VehicleGroup> groups = vehicleGroupService.getActiveGroups();
        return ApiResponse.success(groups);
    }
    
    /**
     * 根据ID获取分组详情
     * 
     * 3.10 Optional类的使用
     * ─────────────────────────────────────────────────────────────────────────
     * 
     * 【为什么Service返回Optional？】
     * - 明确表示"结果可能为空"
     * - 强制调用者处理空值情况
     * - 避免NullPointerException
     * 
     * 【处理Optional的方式】
     * 
     * // 方式1：isPresent() + get()
     * Optional<VehicleGroup> group = service.getGroupById(id);
     * if (group.isPresent()) {
     *     return group.get();
     * } else {
     *     throw new NotFoundException();
     * }
     * 
     * // 方式2：orElse() 提供默认值
     * VehicleGroup group = service.getGroupById(id).orElse(new VehicleGroup());
     * 
     * // 方式3：orElseThrow() 抛出异常
     * VehicleGroup group = service.getGroupById(id)
     *     .orElseThrow(() -> new NotFoundException("分组不存在"));
     * 
     * // 方式4：orElseGet() 延迟计算
     * VehicleGroup group = service.getGroupById(id)
     *     .orElseGet(() -> createDefaultGroup());
     * 
     * @param id 分组ID
     * @return 分组详细信息
     */
    @GetMapping("/{id}")
    public ApiResponse<VehicleGroup> getGroupById(@PathVariable Long id) {
        Optional<VehicleGroup> group = vehicleGroupService.getGroupById(id);
        if (group.isPresent()) {
            return ApiResponse.success(group.get());
        } else {
            return ApiResponse.error(404, "分组不存在");
        }
    }
    
    /**
     * 根据名称获取分组
     * 
     * @param groupName 分组名称
     * @return 匹配的分组对象
     */
    @GetMapping("/name/{groupName}")
    public ApiResponse<VehicleGroup> getGroupByName(@PathVariable String groupName) {
        Optional<VehicleGroup> group = vehicleGroupService.getGroupByName(groupName);
        if (group.isPresent()) {
            return ApiResponse.success(group.get());
        } else {
            return ApiResponse.error(404, "分组不存在");
        }
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第四章：统一响应格式 ApiResponse】
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * 4.1 为什么需要统一响应格式？
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * 【不统一的响应格式】
 * // 成功时直接返回数据
 * return user;
 * 
 * // 失败时抛出异常
 * throw new RuntimeException("用户不存在");
 * 
 * 问题：
 * - 前端无法统一处理响应
 * - 错误信息格式不一致
 * - HTTP状态码与业务状态混淆
 * 
 * 【统一响应格式】
 * {
 *   "code": 200,           // 业务状态码
 *   "message": "操作成功",   // 提示信息
 *   "data": { ... }        // 业务数据
 * }
 * 
 * 优点：
 * - 前端可以统一处理响应
 * - 清晰区分成功/失败
 * - 便于添加额外信息（时间戳、追踪ID等）
 * 
 * 4.2 ApiResponse 设计示例
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * public class ApiResponse<T> {
 *     private int code;        // 状态码：200成功，400客户端错误，500服务器错误
 *     private String message;  // 提示信息
 *     private T data;          // 泛型数据，可以是任意类型
 *     private long timestamp;  // 时间戳
 *     
 *     // 成功响应
 *     public static <T> ApiResponse<T> success(T data) {
 *         return new ApiResponse<>(200, "操作成功", data);
 *     }
 *     
 *     // 成功响应（带消息）
 *     public static <T> ApiResponse<T> success(String message, T data) {
 *         return new ApiResponse<>(200, message, data);
 *     }
 *     
 *     // 错误响应
 *     public static <T> ApiResponse<T> error(int code, String message) {
 *         return new ApiResponse<>(code, message, null);
 *     }
 * }
 * 
 * 4.3 HTTP状态码 vs 业务状态码
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * 【HTTP状态码】
 * 由服务器自动设置，表示HTTP请求的处理结果。
 * 
 * 常用状态码：
 * ┌─────┬─────────────────────────────────────────────────────────────────────┐
 * │ 状态码│ 含义                                                                │
 * ├─────┼─────────────────────────────────────────────────────────────────────┤
 * │ 200 │ OK - 请求成功                                                        │
 * │ 201 │ Created - 资源创建成功                                               │
 * │ 204 │ No Content - 成功但无返回内容                                         │
 * │ 400 │ Bad Request - 请求参数错误                                           │
 * │ 401 │ Unauthorized - 未认证                                                │
 * │ 403 │ Forbidden - 无权限                                                   │
 * │ 404 │ Not Found - 资源不存在                                               │
 * │ 500 │ Internal Server Error - 服务器内部错误                                │
 * └─────┴─────────────────────────────────────────────────────────────────────┘
 * 
 * 【业务状态码】
 * 在响应体中自定义，表示业务处理结果。
 * 
 * 例如：
 * code = 200：操作成功
 * code = 1001：用户名已存在
 * code = 1002：密码错误
 * code = 1003：账户被锁定
 * 
 * 【最佳实践】
 * - HTTP状态码表示请求是否成功到达服务器并被理解
 * - 业务状态码表示业务逻辑是否执行成功
 * - 两者结合使用，提供更完整的错误信息
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第五章：RESTful API设计最佳实践】
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * 5.1 资源命名规范
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * 【使用名词复数】
 * ✅ /users          用户集合
 * ✅ /users/{id}     具体用户
 * ❌ /getUser        不要使用动词
 * ❌ /user           使用复数形式
 * 
 * 【使用kebab-case】
 * ✅ /vehicle-groups
 * ❌ /vehicleGroups
 * ❌ /vehicle_groups
 * 
 * 【表达层级关系】
 * /users/{userId}/orders           用户的订单
 * /users/{userId}/orders/{orderId} 用户的具体订单
 * 
 * 5.2 HTTP方法的正确使用
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * ┌──────────┬────────────────────────────────────────────────────────────────┐
 * │ 方法      │ 使用场景                                                        │
 * ├──────────┼────────────────────────────────────────────────────────────────┤
 * │ GET      │ 获取资源，不修改服务器状态，可缓存                                 │
 * │ POST     │ 创建资源，非幂等                                                 │
 * │ PUT      │ 整体更新资源，幂等                                               │
 * │ PATCH    │ 部分更新资源，非幂等                                             │
 * │ DELETE   │ 删除资源，幂等                                                   │
 * └──────────┴────────────────────────────────────────────────────────────────┘
 * 
 * 5.3 版本控制
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * 【URL版本控制】
 * /api/v1/vehicle-groups
 * /api/v2/vehicle-groups
 * 
 * 【请求头版本控制】
 * Accept: application/vnd.myapi.v1+json
 * 
 * 【推荐使用URL版本控制】
 * - 简单直观
 * - 便于测试和文档
 * - 便于缓存代理处理
 * 
 * 5.4 分页、过滤、排序
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * GET /api/vehicle-groups?page=1&size=20&sort=name,asc&status=ACTIVE
 * 
 * 参数说明：
 * - page: 页码（从0或1开始）
 * - size: 每页数量
 * - sort: 排序字段和方向
 * - status: 过滤条件
 * 
 * 5.5 响应状态码选择
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * 操作         │ 成功响应           │ 失败响应
 * ─────────────┼────────────────────┼────────────────────
 * 创建资源      │ 201 Created        │ 400 Bad Request
 * 查询资源      │ 200 OK             │ 404 Not Found
 * 更新资源      │ 200 OK             │ 400/404
 * 删除资源      │ 204 No Content     │ 404 Not Found
 * 列表查询      │ 200 OK             │ 400 Bad Request
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第六章：控制器层总结】
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * 6.1 控制器的职责边界
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * 【应该做的】
 * ✅ 接收HTTP请求，解析参数
 * ✅ 调用Service层处理业务
 * ✅ 将结果封装为统一响应格式
 * ✅ 处理HTTP相关的异常（参数校验失败等）
 * 
 * 【不应该做的】
 * ❌ 包含业务逻辑
 * ❌ 直接操作数据库
 * ❌ 复杂的数据转换
 * ❌ 事务管理
 * 
 * 6.2 控制器设计原则
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * 1. 单一职责：每个控制器只负责一类资源
 * 2. 瘦控制器：控制器应该很"薄"，只做转发
 * 3. 统一响应：使用统一的响应格式
 * 4. 异常处理：使用全局异常处理器
 * 5. 参数校验：使用@Valid进行参数校验
 * 
 * 6.3 与其他层的关系
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * Controller ──调用──▶ Service ──调用──▶ Repository
 *     │                   │                  │
 *     ▼                   ▼                  ▼
 *  处理HTTP           处理业务逻辑         数据访问
 *  解析参数           事务管理            SQL执行
 *  封装响应           调用其他服务         CRUD操作
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */
