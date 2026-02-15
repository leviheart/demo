package tw_six.demo.repository; // 定义数据访问层接口所在的包

import org.springframework.data.jpa.repository.JpaRepository; // Spring Data JPA核心接口
import org.springframework.stereotype.Repository; // Spring注解，标记为数据访问组件
import tw_six.demo.entity.User; // 引入用户实体类

/**
 * 用户数据访问接口 - 数据访问层组件
 * 
 * 文件关联说明：
 * 1. 与User实体类关联：泛型参数指定操作User实体
 * 2. 与UserService关联：通过依赖注入为业务层提供数据访问能力
 * 3. 与数据库表关联：继承JpaRepository自动映射到users表
 * 4. 与Spring容器关联：通过@Repository注解注册为Spring Bean
 * 
 * 作用说明：
 * - 继承JpaRepository获得标准CRUD操作（save, findById, findAll, delete等）
 * - 作为数据访问层的核心接口，屏蔽底层数据库操作细节
 * - 通过Spring Data JPA自动生成实现类，无需手动编写SQL
 * - 为上层业务逻辑提供统一的数据访问入口
 */
@Repository // 标记为Spring数据访问组件，纳入Spring容器管理
public interface UserRepository extends JpaRepository<User, Long> { // 继承JPA标准接口
    // JpaRepository<User, Long> 泛型说明：
    // User：操作的实体类类型
    // Long：实体主键的数据类型
    // 自动获得以下方法：
    // - save(User entity)：保存或更新用户
    // - findById(Long id)：根据ID查找用户
    // - findAll()：获取所有用户
    // - deleteById(Long id)：根据ID删除用户
    // - count()：统计用户总数
}