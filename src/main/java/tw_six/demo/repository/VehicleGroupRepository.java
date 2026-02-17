package tw_six.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tw_six.demo.entity.VehicleGroup;
import java.util.List;
import java.util.Optional;

/**
 * 车辆分组数据访问层 - Spring Data JPA Repository接口
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第一章：什么是Repository？】
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Repository（仓库）是领域驱动设计(DDD)中的概念，负责数据的持久化操作。
 * Spring Data JPA将其实现为接口，自动生成实现类。
 * 
 * 为什么使用Repository而不是直接写SQL？
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ 传统DAO方式                      │ Spring Data JPA方式                   │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │ 需要写接口和实现类               │ 只需要写接口                          │
 * │ 手写SQL语句                      │ 自动生成SQL                           │
 * │ 手动处理结果集                   │ 自动映射为对象                        │
 * │ 每个CRUD都要写                   │ 继承JpaRepository即可获得             │
 * │ 代码量大，易出错                 │ 代码量极少，不易出错                   │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第二章：核心注解】
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * @Repository - Spring的组件注解
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ 作用：                                                                      │
 * │   1. 标记这是一个数据访问层组件                                             │
 * │   2. Spring会自动扫描并创建Bean实例                                         │
 * │   3. 自动处理数据库异常转换（Checked → Unchecked）                          │
 * │                                                                             │
 * │ 为什么需要异常转换？                                                        │
 * │   - JDBC抛出的是Checked异常（SQLException）                                 │
 * │   - 强制try-catch或throws，代码冗余                                        │
 * │   - Spring转换为DataAccessException（Unchecked）                           │
 * │   - 无需强制处理，代码更简洁                                                │
 * │                                                                             │
 * │ 注意：                                                                      │
 * │   - 对于JpaRepository接口，@Repository可以省略                             │
 * │   - Spring Data会自动识别继承JpaRepository的接口                            │
 * │   - 但建议保留，提高代码可读性                                              │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第三章：JpaRepository接口详解】
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * JpaRepository<VehicleGroup, Long> 泛型参数说明：
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ 第一个泛型参数：VehicleGroup                                               │
 * │   - 实体类类型                                                              │
 * │   - 告诉JPA这个Repository操作哪个实体                                       │
 * │                                                                             │
 * │ 第二个泛型参数：Long                                                        │
 * │   - 主键类型                                                                │
 * │   - 必须与实体类中@Id字段的类型一致                                         │
 * │   - 用于findById、deleteById等方法                                          │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * JpaRepository继承体系：
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │                        Repository (标记接口)                               │
 * │                              │                                             │
 * │                              ▼                                             │
 * │                   CrudRepository (基本CRUD)                                │
 * │                     save, findById, findAll, delete                        │
 * │                              │                                             │
 * │                              ▼                                             │
 * │                  PagingAndSortingRepository (分页排序)                     │
 * │                     findAll(Pageable), findAll(Sort)                       │
 * │                              │                                             │
 * │                              ▼                                             │
 * │                      JpaRepository (JPA扩展)                               │
 * │                     flush, saveAndFlush, findAllById                       │
 * │                     deleteAllInBatch, getReferenceById                     │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * JpaRepository提供的常用方法：
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ 保存操作：                                                                  │
 * │   save(S entity)           - 保存/更新单个实体                             │
 * │   saveAll(Iterable<S>)     - 批量保存                                      │
 * │   saveAndFlush(S entity)   - 保存并立即刷新到数据库                         │
 * │                                                                             │
 * │ 查询操作：                                                                  │
 * │   findById(ID id)          - 按ID查询，返回Optional                        │
 * │   findAll()                - 查询所有                                      │
 * │   findAllById(Iterable<ID>)- 按ID列表查询                                  │
 * │   count()                  - 统计总数                                      │
 * │   existsById(ID id)        - 判断是否存在                                  │
 * │                                                                             │
 * │ 删除操作：                                                                  │
 * │   deleteById(ID id)        - 按ID删除                                      │
 * │   delete(T entity)         - 删除实体                                      │
 * │   deleteAll()              - 删除所有                                      │
 * │   deleteAllInBatch()       - 批量删除（性能更好）                           │
 * │                                                                             │
 * │ 其他操作：                                                                  │
 * │   flush()                  - 刷新到数据库                                  │
 * │   getReferenceById(ID id)  - 获取引用（懒加载代理）                         │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第四章：方法命名查询（Query Methods）】
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Spring Data JPA最强大的特性：根据方法名自动生成查询！
 * 
 * 命名规则：
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ 格式：findBy + 属性名 + [条件关键字] + [And/Or] + 属性名 + [条件关键字]    │
 * │                                                                             │
 * │ 示例：                                                                      │
 * │   findByGroupName()         → WHERE group_name = ?                         │
 * │   findByStatus()            → WHERE status = ?                             │
 * │   findByStatusAndGroupName() → WHERE status = ? AND group_name = ?         │
 * │   findByStatusOrGroupName()  → WHERE status = ? OR group_name = ?          │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * 常用条件关键字：
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ 关键字              │ 方法示例                    │ 生成的SQL              │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │ Is, Equals         │ findByName(name)           │ name = ?               │
 * │ IsNull             │ findByNameNull()           │ name IS NULL           │
 * │ IsNotNull          │ findByNameNotNull()        │ name IS NOT NULL       │
 * │ Like               │ findByNameLike(name)       │ name LIKE ?            │
 * │ NotLike            │ findByNameNotLike(name)    │ name NOT LIKE ?        │
 * │ StartingWith       │ findByNameStartingWith(n)  │ name LIKE n%           │
 * │ EndingWith         │ findByNameEndingWith(n)    │ name LIKE %n           │
 * │ Containing         │ findByNameContaining(n)    │ name LIKE %n%          │
 * │ In                 │ findByNameIn(names)        │ name IN (?, ?, ...)    │
 * │ NotIn              │ findByNameNotIn(names)     │ name NOT IN (...)      │
 * │ OrderBy            │ findByNameOrderByIdDesc()  │ ORDER BY id DESC       │
 * │ Not                │ findByNameNot(name)        │ name <> ?              │
 * │ Between            │ findByAgeBetween(a, b)     │ age BETWEEN a AND b    │
 * │ LessThan           │ findByAgeLessThan(age)     │ age < ?                │
 * │ LessThanEqual      │ findByAgeLessThanEqual(a)  │ age <= ?               │
 * │ GreaterThan        │ findByAgeGreaterThan(a)    │ age > ?                │
 * │ GreaterThanEqual   │ findByAgeGreaterThanEq(a)  │ age >= ?               │
 * │ Before             │ findByDateBefore(date)     │ date < ?               │
 * │ After              │ findByDateAfter(date)      │ date > ?               │
 * │ True               │ findByActiveTrue()         │ active = true          │
 * │ False              │ findByActiveFalse()        │ active = false         │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * 返回类型选择：
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ 返回类型           │ 使用场景                    │ 说明                   │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │ List<T>           │ 查询多条记录                │ 返回列表，可能为空      │
 * │ Optional<T>       │ 查询单条记录                │ 可能不存在，需要处理    │
 * │ T                 │ 查询单条记录（确定存在）     │ 不存在时抛异常          │
 * │ long/count()      │ 统计数量                    │ 返回基本类型long        │
 * │ boolean           │ 判断是否存在                │ existsBy...            │
 * │ Page<T>           │ 分页查询                    │ 需要Pageable参数        │
 * │ Slice<T>          │ 分页查询（不统计总数）       │ 性能更好               │
 * │ Stream<T>         │ 大数据量流式处理            │ 需要关闭流              │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第五章：Optional类详解】
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Optional<T> 是Java 8引入的容器类，用于优雅地处理可能为null的值。
 * 
 * 为什么使用Optional而不是直接返回null？
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ 返回null的问题：                                                            │
 * │   VehicleGroup group = repository.findByName("xxx");                       │
 * │   group.getGroupName();  // 如果group为null，NullPointerException!          │
 * │                                                                             │
 * │ Optional的优势：                                                            │
 * │   Optional<VehicleGroup> opt = repository.findByName("xxx");               │
 * │   opt.ifPresent(group -> group.getGroupName());  // 安全处理               │
 * │                                                                             │
 * │ Optional强制调用者处理"值不存在"的情况，避免NPE                             │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * Optional常用方法：
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ isPresent()        - 判断值是否存在                                        │
 * │ ifPresent(Consumer)- 如果存在则执行                                        │
 * │ get()              - 获取值（不存在抛NoSuchElementException）              │
 * │ orElse(T other)    - 不存在时返回默认值                                     │
 * │ orElseGet(Supplier)- 不存在时通过函数生成默认值                             │
 * │ orElseThrow()      - 不存在时抛异常                                        │
 * │ map(Function)      - 转换值                                                │
 * │ filter(Predicate)  - 过滤值                                                │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * 使用示例：
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ // 方式1：isPresent检查                                                    │
 * │ Optional<VehicleGroup> opt = repository.findByGroupName("物流部");         │
 * │ if (opt.isPresent()) {                                                     │
 * │     VehicleGroup group = opt.get();                                        │
 * │     System.out.println(group.getDescription());                            │
 * │ }                                                                          │
 * │                                                                             │
 * │ // 方式2：ifPresent（推荐）                                                 │
 * │ opt.ifPresent(group -> System.out.println(group.getDescription()));        │
 * │                                                                             │
 * │ // 方式3：orElse提供默认值                                                  │
 * │ VehicleGroup group = opt.orElse(new VehicleGroup());                       │
 * │                                                                             │
 * │ // 方式4：orElseThrow抛异常                                                 │
 * │ VehicleGroup group = opt.orElseThrow(() ->                                 │
 * │     new RuntimeException("分组不存在"));                                    │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第六章：自定义查询方法】
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * 如果方法命名无法满足需求，可以使用@Query自定义JPQL或SQL：
 * 
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ // JPQL查询（推荐）                                                         │
 * │ @Query("SELECT g FROM VehicleGroup g WHERE g.status = :status")            │
 * │ List<VehicleGroup> findByCustomStatus(@Param("status") String status);     │
 * │                                                                             │
 * │ // 原生SQL查询                                                              │
 * │ @Query(value = "SELECT * FROM vehicle_groups WHERE status = ?",            │
 * │         nativeQuery = true)                                                │
 * │ List<VehicleGroup> findByNativeQuery(String status);                       │
 * │                                                                             │
 * │ // 更新操作（需要@Modifying）                                               │
 * │ @Modifying                                                                  │
 * │ @Query("UPDATE VehicleGroup g SET g.status = :status WHERE g.id = :id")    │
 * │ int updateStatus(@Param("id") Long id, @Param("status") String status);    │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */
@Repository
public interface VehicleGroupRepository extends JpaRepository<VehicleGroup, Long> {
    
    /**
     * 按状态查询分组列表
     * 
     * 方法名解析过程：
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ findByStatus(String status)                                             │
     * │     │                                                                   │
     * │     ├── findBy - 查询关键字                                             │
     * │     │                                                                   │
     * │     └── Status - 实体类属性名（自动映射到数据库列status）                 │
     * │                                                                         │
     * │ 生成的SQL：                                                             │
     * │   SELECT * FROM vehicle_groups WHERE status = ?                        │
     │ │                                                                         │
     * │ 参数：status = "active" 或 "inactive"                                   │
     * └────────────────────────────────────────────────────────────────────────┘
     * 
     * 为什么返回List而不是Optional？
     * - 按状态查询可能返回多条记录
     * - List更合适表示"0到多条"的结果
     * - 如果没有匹配项，返回空列表而不是null
     * 
     * @param status 分组状态（"active"或"inactive"）
     * @return 匹配状态的分组列表（不会返回null）
     */
    List<VehicleGroup> findByStatus(String status);
    
    /**
     * 按分组名称查询
     * 
     * 方法名解析：
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ findByGroupName(String groupName)                                       │
     * │     │                                                                   │
     * │     ├── findBy - 查询关键字                                             │
     * │     │                                                                   │
     * │     └── GroupName - 实体类属性名（映射到group_name列）                   │
     * │                                                                         │
     * │ 生成的SQL：                                                             │
     * │   SELECT * FROM vehicle_groups WHERE group_name = ?                    │
     └────────────────────────────────────────────────────────────────────────┘
     * 
     * 为什么返回Optional而不是实体？
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 场景：分组名称是唯一的，最多返回一条记录                                  │
     * │                                                                         │
     * │ 返回Optional的好处：                                                    │
     * │ 1. 明确表示"可能不存在"                                                  │
     * │ 2. 强制调用者处理不存在的情况                                            │
     * │ 3. 避免NullPointerException                                             │
     * │                                                                         │
     * │ 调用示例：                                                              │
     * │ Optional<VehicleGroup> opt = repository.findByGroupName("物流部");      │
     * │ VehicleGroup group = opt.orElseThrow(() ->                              │
     * │     new IllegalArgumentException("分组不存在"));                         │
     └────────────────────────────────────────────────────────────────────────┘
     * 
     * @param groupName 分组名称
     * @return Optional包装的分组对象
     */
    Optional<VehicleGroup> findByGroupName(String groupName);
    
    /**
     * 检查分组名称是否已存在
     * 
     * 方法名解析：
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ existsByGroupName(String groupName)                                     │
     * │     │                                                                   │
     * │     ├── existsBy - 存在性检查关键字                                      │
     * │     │                                                                   │
     * │     └── GroupName - 实体类属性名                                        │
     * │                                                                         │
     * │ 生成的SQL：                                                             │
     * │   SELECT COUNT(*) > 0 FROM vehicle_groups WHERE group_name = ?         │
     * │   （数据库优化：只返回true/false，不返回具体数据）                        │
     └────────────────────────────────────────────────────────────────────────┘
     * 
     * 为什么用existsBy而不是findBy？
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ existsByGroupName()                                                     │
     * │   - 只返回boolean                                                       │
     * │   - 数据库只执行COUNT，不返回完整记录                                    │
     * │   - 性能更好                                                            │
     * │                                                                         │
     * │ findByGroupName().isPresent()                                           │
     * │   - 返回完整记录                                                        │
     * │   - 需要传输更多数据                                                    │
     * │   - 如果只需要判断存在，浪费资源                                         │
     └────────────────────────────────────────────────────────────────────────┘
     * 
     * 使用场景：
     * - 创建分组前检查名称是否重复
     * - 验证分组是否存在
     * 
     * @param groupName 分组名称
     * @return true-存在，false-不存在
     */
    boolean existsByGroupName(String groupName);
}
