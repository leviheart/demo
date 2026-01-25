package tw_six.demo.repository; // 定义数据访问层所在的包

import org.springframework.data.jpa.repository.JpaRepository; // 导入JPA基础数据访问接口
import org.springframework.stereotype.Repository; // 导入Repository注解
import tw_six.demo.entity.User; // 导入User实体类

@Repository // 标记此类为数据访问层组件
public interface UserRepository extends JpaRepository<User, Long> { // 继承JpaRepository，获得基本的CRUD操作
    // 此接口继承了JpaRepository的所有方法，如findAll(), findById(), save(), deleteById()等
    // 无需编写具体实现，Spring Data JPA会自动生成实现
}