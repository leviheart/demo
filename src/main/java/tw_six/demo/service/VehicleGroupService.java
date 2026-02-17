package tw_six.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw_six.demo.entity.VehicleGroup;
import tw_six.demo.repository.VehicleGroupRepository;
import java.util.List;
import java.util.Optional;

/**
 * 车辆分组服务类 - Spring业务逻辑层核心示例
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第一章：什么是Service层？】
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Service层（业务逻辑层）是分层架构的核心，负责：
 * 1. 封装业务规则和业务流程
 * 2. 协调多个Repository完成复杂操作
 * 3. 提供事务管理边界
 * 4. 对外提供统一的业务接口
 * 
 * 三层架构：
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │                        Controller层（表现层）                               │
 * │   - 接收HTTP请求                                                          │
 * │   - 参数验证、格式转换                                                      │
 * │   - 调用Service层                                                          │
 * │   - 返回HTTP响应                                                           │
 * │                              │                                             │
 * │                              ▼                                             │
 * │                        Service层（业务逻辑层）                              │
 * │   - 实现业务规则                                                           │
 * │   - 事务管理                                                               │
 * │   - 调用Repository层                                                       │
 * │   - 处理业务异常                                                           │
 * │                              │                                             │
 * │                              ▼                                             │
 * │                        Repository层（数据访问层）                           │
 * │   - 数据库CRUD操作                                                         │
 * │   - 数据查询                                                               │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * 为什么需要Service层？
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ 没有Service层                      │ 有Service层                          │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │ Controller直接调用Repository        │ Controller调用Service                 │
 * │ 业务逻辑散落在Controller中           │ 业务逻辑集中在Service中               │
 * │ 无法复用业务逻辑                     │ 多个Controller可复用同一Service       │
 * │ 事务管理困难                        │ @Transactional统一管理事务            │
 * │ 代码难以测试                        │ Service层易于单元测试                 │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第二章：核心注解详解】
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * @Service - Spring服务组件注解
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ 作用：                                                                      │
 * │   1. 标记这是一个Spring管理的Service组件                                    │
 * │   2. Spring会自动扫描并创建Bean实例                                         │
 * │   3. 支持依赖注入（@Autowired）                                             │
 * │                                                                             │
 * │ @Service vs @Component vs @Repository：                                    │
 * │   @Component  - 通用组件注解，所有组件的父注解                              │
 * │   @Service    - 业务逻辑层，语义更明确                                      │
 * │   @Repository - 数据访问层，自动异常转换                                    │
 * │   @Controller - 表现层，用于Web请求处理                                     │
 * │                                                                             │
 * │ 虽然这四个注解功能相同，但使用不同的注解可以提高代码可读性                   │
 * │                                                                             │
 * │ Bean的默认名称：                                                            │
 * │   类名首字母小写：VehicleGroupService → vehicleGroupService                │
 * │   可以通过@Service("myService")指定自定义名称                               │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * @Transactional - 事务管理注解
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ 什么是事务？                                                                │
 * │   事务是一组操作，要么全部成功，要么全部失败                                │
 * │   经典例子：银行转账，A扣款和B收款必须同时成功或同时失败                     │
 * │                                                                             │
 * │ 事务的ACID特性：                                                            │
 * │   A - Atomicity（原子性）：事务不可分割                                    │
 * │   C - Consistency（一致性）：事务前后数据一致                              │
 * │   I - Isolation（隔离性）：并发事务互不干扰                                │
 * │   D - Durability（持久性）：事务提交后永久保存                             │
 * │                                                                             │
 * │ @Transactional的属性：                                                      │
 * │   propagation = Propagation.REQUIRED  - 传播行为（默认）                   │
 * │   isolation = Isolation.READ_COMMITTED - 隔离级别                          │
 * │   timeout = 30 - 超时时间（秒）                                            │
 * │   readOnly = true - 只读事务（优化查询性能）                               │
 * │   rollbackFor = Exception.class - 回滚异常类型                             │
 * │                                                                             │
 * │ 传播行为（Propagation）：                                                   │
 * │   REQUIRED  - 有事务就加入，没有就新建（默认）                              │
 * │   REQUIRES_NEW - 总是新建事务，挂起当前事务                                 │
 * │   SUPPORTS  - 有事务就加入，没有就非事务执行                                │
 * │   NOT_SUPPORTED - 非事务执行，挂起当前事务                                  │
 * │   MANDATORY - 必须在事务中调用，否则抛异常                                  │
 * │   NEVER - 必须非事务调用，否则抛异常                                        │
 * │   NESTED - 嵌套事务（如果支持）                                            │
 * │                                                                             │
 * │ 类级别vs方法级别：                                                          │
 * │   类级别：@Transactional - 所有public方法都有事务                          │
 * │   方法级别：可以覆盖类级别配置                                              │
 * │                                                                             │
 * │ 注意事项：                                                                  │
 * │   1. 只对public方法有效                                                    │
 * │   2. 同类方法调用不触发事务（绕过代理）                                      │
 * │   3. 默认只对RuntimeException回滚，需要rollbackFor指定Exception             │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第三章：依赖注入】
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * @Autowired - Spring依赖注入注解
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ 什么是依赖注入（DI）？                                                      │
 * │   传统方式：对象内部创建依赖对象                                            │
 * │   Service service = new Service();                                         │
 * │   Repository repo = new Repository(); // Service内部创建                   │
 * │                                                                             │
 * │   依赖注入：外部创建并注入依赖对象                                          │
 * │   @Autowired                                                               │
 * │   private Repository repo; // Spring自动注入                               │
 * │                                                                             │
 * │ 依赖注入的好处：                                                            │
 * │   1. 解耦：对象不需要知道依赖如何创建                                       │
 * │   2. 可测试：可以注入Mock对象进行单元测试                                   │
 * │   3. 灵活：可以轻松切换实现类                                               │
 * │                                                                             │
 * │ @Autowired的三种注入方式：                                                  │
 * │                                                                             │
 * │ 1. 字段注入（不推荐）：                                                     │
 * │    @Autowired                                                              │
 * │    private VehicleGroupRepository repository;                              │
 * │    缺点：无法设置final字段，难以单元测试                                    │
 * │                                                                             │
 * │ 2. Setter注入：                                                             │
 * │    private VehicleGroupRepository repository;                              │
 * │    @Autowired                                                              │
 * │    public void setRepository(VehicleGroupRepository repository) {          │
 * │        this.repository = repository;                                       │
 * │    }                                                                        │
 * │    优点：可以重新配置依赖                                                   │
 * │    缺点：可能忘记注入，对象处于不完整状态                                    │
 * │                                                                             │
 * │ 3. 构造函数注入（推荐）：                                                   │
 * │    private final VehicleGroupRepository repository;                        │
 * │    @Autowired                                                              │
 * │    public VehicleGroupService(VehicleGroupRepository repository) {         │
 * │        this.repository = repository;                                       │
 * │    }                                                                        │
 * │    优点：                                                                   │
 * │      - 依赖不可变（final）                                                  │
 * │      - 对象创建时就完整初始化                                               │
 * │      - 易于单元测试（可以直接new）                                          │
 * │      - 明确表达依赖关系                                                     │
 * │                                                                             │
 * │ Spring 4.3+：如果类只有一个构造函数，@Autowired可以省略                     │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * final关键字的作用：
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ private final VehicleGroupRepository vehicleGroupRepository;               │
 * │                                                                             │
 * │ 为什么用final？                                                             │
 * │   1. 不可变：初始化后不能被修改，保证依赖稳定                               │
 * │   2. 线程安全：final字段在构造函数完成后对其他线程可见                       │
 * │   3. 编译检查：必须通过构造函数初始化，否则编译错误                          │
 * │   4. 代码意图：明确表示这是一个必须的依赖                                   │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第四章：异常处理策略】
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Service层异常处理原则：
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ 1. 业务异常：抛出运行时异常（RuntimeException）                             │
 * │    - IllegalArgumentException：参数校验失败                                │
 * │    - IllegalStateException：状态不正确                                     │
 * │    - 自定义业务异常：更具体的业务错误                                       │
 * │                                                                             │
 * │ 2. 技术异常：让Spring处理                                                   │
 * │    - DataAccessException：数据库异常（Spring自动转换）                      │
 * │    - TransactionException：事务异常                                        │
 * │                                                                             │
 * │ 为什么抛RuntimeException而不是Checked Exception？                           │
 * │   - Checked Exception强制try-catch，代码冗余                               │
 * │   - RuntimeException可以向上传播到Controller层统一处理                      │
 * │   - Spring事务默认只对RuntimeException回滚                                  │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第五章：方法设计原则】
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Service方法设计最佳实践：
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ 1. 单一职责：一个方法只做一件事                                             │
 * │ 2. 命名清晰：方法名表达业务意图                                             │
 * │ 3. 参数验证：在方法入口验证参数                                             │
 * │ 4. 返回值明确：返回业务结果或抛出异常                                       │
 * │ 5. 事务边界：Service方法是事务的边界                                       │
 * │                                                                             │
 * │ 命名约定：                                                                  │
 * │   - get/find：查询操作，不修改数据                                         │
 * │   - create/save：创建新数据                                                │
 * │   - update/modify：更新现有数据                                            │
 * │   - delete/remove：删除数据                                                │
 * │   - process/handle：复杂业务处理                                           │
 * │   - validate/check：验证操作                                               │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */
@Service
@Transactional
public class VehicleGroupService {
    
    /**
     * 车辆分组数据访问对象
     * 
     * 为什么用final？
     * - 依赖不可变：确保注入后不会被修改
     * - 强制构造函数注入：final字段必须通过构造函数初始化
     * - 线程安全：final字段在构造完成后对其他线程可见
     */
    private final VehicleGroupRepository vehicleGroupRepository;
    
    /**
     * 构造函数注入 - Spring推荐的依赖注入方式
     * 
     * @Autowired可以省略吗？
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ Spring 4.3+：如果类只有一个构造函数，@Autowired可以省略                  │
     * │ 但保留它可以提高代码可读性，明确表示这是依赖注入点                        │
     └────────────────────────────────────────────────────────────────────────┘
     * 
     * @param vehicleGroupRepository 车辆分组Repository（Spring自动注入）
     */
    @Autowired
    public VehicleGroupService(VehicleGroupRepository vehicleGroupRepository) {
        this.vehicleGroupRepository = vehicleGroupRepository;
    }
    
    /**
     * 创建新分组 - 带业务校验的创建方法
     * 
     * 业务规则：
     * 1. 分组名称必须唯一
     * 2. 名称重复时抛出异常
     * 
     * 为什么在Service层校验而不是Controller层？
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ Controller层：只做参数格式校验（非空、格式等）                            │
     * │ Service层：做业务规则校验（唯一性、业务约束等）                          │
     * │                                                                             │
     * │ 好处：                                                                      │
     * │   - 业务规则集中在一处，便于维护                                           │
     * │   - 不同Controller可以复用同一校验逻辑                                     │
     * │   - Service层可以独立测试                                                 │
     └────────────────────────────────────────────────────────────────────────┘
     * 
     * @param group 分组对象（从前端接收的数据）
     * @return 创建成功的分组对象（包含数据库生成的ID）
     * @throws IllegalArgumentException 分组名称已存在时抛出
     */
    public VehicleGroup createGroup(VehicleGroup group) {
        // 业务规则校验：分组名称唯一性检查
        // existsByGroupName只执行COUNT，比findByGroupName性能更好
        if (vehicleGroupRepository.existsByGroupName(group.getGroupName())) {
            // 抛出IllegalArgumentException表示参数不合法
            // Spring会自动回滚事务
            throw new IllegalArgumentException("分组名称已存在: " + group.getGroupName());
        }
        
        // 保存到数据库
        // save方法会：
        // 1. 如果ID为null，执行INSERT
        // 2. 如果ID不为null，执行UPDATE
        // 3. 返回保存后的对象（包含生成的ID）
        return vehicleGroupRepository.save(group);
    }
    
    /**
     * 更新分组信息 - 先查询后更新的模式
     * 
     * 为什么不直接save？
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 直接save的问题：                                                         │
     * │   - 如果ID不存在，会创建新记录（可能不是期望行为）                        │
     * │   - 无法验证记录是否存在                                                 │
     * │   - 可能覆盖其他字段的值                                                 │
     * │                                                                             │
     * │ 先查询后更新的好处：                                                      │
     * │   - 确保记录存在                                                         │
     * │   - 可以选择性更新字段                                                   │
     * │   - 可以添加额外的业务逻辑                                               │
     └────────────────────────────────────────────────────────────────────────┘
     * 
     * orElseThrow的妙用：
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ vehicleGroupRepository.findById(id)                                     │
     * │     .orElseThrow(() -> new RuntimeException("分组不存在: " + id));      │
     * │                                                                             │
     * │ 等价于：                                                                  │
     * │ Optional<VehicleGroup> optional = repository.findById(id);              │
     * │ if (!optional.isPresent()) {                                             │
     * │     throw new RuntimeException("分组不存在: " + id);                     │
     * │ }                                                                        │
     * │ VehicleGroup group = optional.get();                                     │
     │ │                                                                             │
     * │ orElseThrow更简洁，是函数式编程的风格                                    │
     └────────────────────────────────────────────────────────────────────────┘
     * 
     * @param id 分组ID
     * @param groupDetails 更新后的分组信息
     * @return 更新后的分组对象
     * @throws RuntimeException 分组不存在时抛出
     */
    public VehicleGroup updateGroup(Long id, VehicleGroup groupDetails) {
        // 1. 查询现有分组
        VehicleGroup group = vehicleGroupRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("分组不存在: " + id));
        
        // 2. 更新字段
        // 这里可以添加业务逻辑，如：
        // - 检查是否有权限修改
        // - 记录修改日志
        // - 发送通知
        group.setGroupName(groupDetails.getGroupName());
        group.setDescription(groupDetails.getDescription());
        group.setStatus(groupDetails.getStatus());
        
        // 3. 保存更新
        // 因为ID不为null，JPA会执行UPDATE
        return vehicleGroupRepository.save(group);
    }
    
    /**
     * 删除分组 - 带存在性检查的删除
     * 
     * 为什么先检查再删除？
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 直接deleteById的问题：                                                   │
     * │   - 如果ID不存在，抛出EmptyResultDataAccessException                    │
     * │   - 异常信息不够友好                                                     │
     * │                                                                             │
     * │ 先检查再删除的好处：                                                      │
     * │   - 可以返回更友好的错误信息                                             │
     * │   - 可以添加删除前的业务检查（如分组下是否有车辆）                        │
     └────────────────────────────────────────────────────────────────────────┘
     * 
     * 级联删除说明：
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ VehicleGroup实体中定义了：                                               │
     * │ @OneToMany(mappedBy = "vehicleGroup", cascade = CascadeType.ALL)        │
     * │                                                                             │
     * │ 这意味着删除分组时，分组下的车辆也会被删除！                              │
     * │ 如果不想级联删除，应该：                                                  │
     * │   1. 移除cascade = CascadeType.ALL                                       │
     * │   2. 或者在删除前检查分组下是否有车辆                                    │
     └────────────────────────────────────────────────────────────────────────┘
     * 
     * @param id 分组ID
     * @throws RuntimeException 分组不存在时抛出
     */
    public void deleteGroup(Long id) {
        // 检查分组是否存在
        if (!vehicleGroupRepository.existsById(id)) {
            throw new RuntimeException("分组不存在: " + id);
        }
        
        // 执行删除
        // 生成SQL: DELETE FROM vehicle_groups WHERE id = ?
        vehicleGroupRepository.deleteById(id);
    }
    
    /**
     * 获取所有分组 - 简单查询方法
     * 
     * 为什么不缓存？
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 本项目是演示项目，数据量小，不需要缓存                                   │
     * │                                                                             │
     * │ 生产环境可以考虑：                                                        │
     * │   1. Spring Cache (@Cacheable)                                          │
     * │   2. Redis缓存                                                           │
     * │   3. 本地缓存 (Caffeine, Guava Cache)                                    │
     └────────────────────────────────────────────────────────────────────────┘
     * 
     * @return 所有分组列表（不会返回null）
     */
    public List<VehicleGroup> getAllGroups() {
        // findAll返回List，如果没有数据返回空列表而不是null
        // 这是Spring Data JPA的设计，避免NPE
        return vehicleGroupRepository.findAll();
    }
    
    /**
     * 获取启用的分组 - 按状态筛选
     * 
     * 使用场景：
     * - 下拉选择框：只显示启用的分组
     * - 车辆分配：只能分配到启用的分组
     * - 报表统计：只统计启用的分组
     * 
     * @return 启用状态的分组列表
     */
    public List<VehicleGroup> getActiveGroups() {
        // 调用Repository的命名查询方法
        // 生成SQL: SELECT * FROM vehicle_groups WHERE status = 'active'
        return vehicleGroupRepository.findByStatus("active");
    }
    
    /**
     * 根据ID获取分组 - 返回Optional
     * 
     * 为什么返回Optional而不是直接返回对象或null？
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 返回null的问题：                                                         │
     * │   VehicleGroup group = service.getGroupById(1L);                        │
     * │   group.getGroupName(); // 如果group为null，NPE！                        │
     * │                                                                             │
     * │ 返回Optional的好处：                                                      │
     * │   Optional<VehicleGroup> opt = service.getGroupById(1L);                │
     * │   opt.ifPresent(group -> ...);  // 安全处理                              │
     * │   VehicleGroup group = opt.orElse(defaultGroup);  // 提供默认值          │
     │ │                                                                             │
     * │ Optional强制调用者处理"值不存在"的情况                                   │
     └────────────────────────────────────────────────────────────────────────┘
     * 
     * @param id 分组ID
     * @return Optional包装的分组对象
     */
    public Optional<VehicleGroup> getGroupById(Long id) {
        return vehicleGroupRepository.findById(id);
    }
    
    /**
     * 根据名称获取分组 - 用于唯一性检查和快速查找
     * 
     * 使用场景：
     * - 创建前检查名称是否重复
     * - 按名称查找分组
     * - 导入数据时匹配现有分组
     * 
     * @param groupName 分组名称
     * @return Optional包装的分组对象
     */
    public Optional<VehicleGroup> getGroupByName(String groupName) {
        return vehicleGroupRepository.findByGroupName(groupName);
    }
}
