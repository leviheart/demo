package tw_six.demo.entity; // 定义实体类所在的包，属于数据模型层

import jakarta.persistence.Entity; // JPA注解，标记此类为数据库实体，对应数据表
import jakarta.persistence.GeneratedValue; // JPA注解，指定主键生成策略
import jakarta.persistence.GenerationType; // JPA枚举，定义主键生成方式
import jakarta.persistence.Id; // JPA注解，标记字段为主键
import jakarta.validation.constraints.NotBlank; // Bean Validation注解，验证字段非空
import jakarta.validation.constraints.Email; // Bean Validation注解，验证邮箱格式
import lombok.Data; // Lombok注解，自动生成getter/setter/toString等方法

/**
 * 用户实体类 - 数据模型层组件
 * 
 * 文件关联说明：
 * 1. 与UserRepository关联：通过JPA自动实现CRUD操作
 * 2. 与UserService关联：作为业务逻辑层处理的数据对象
 * 3. 与UserController关联：作为REST接口的数据传输对象
 * 4. 与数据库表关联：通过@Entity注解映射到users表
 * 
 * 作用说明：
 * - 定义用户数据结构和验证规则
 * - 通过JPA实现对象关系映射
 * - 为上层提供标准化的数据模型
 */
@Entity // 标记此类为JPA实体，Hibernate会自动创建对应的数据库表
@Data // Lombok注解，自动生成getter、setter、toString、equals、hashCode等方法
public class User { // 用户实体类定义
    @Id // 标记此字段为主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 设置主键自增策略，数据库自动分配ID
    private Long id; // 用户唯一标识符，Long类型支持大数值
    
    @NotBlank(message = "姓名不能为空") // 验证姓名不为空，如果为空则显示指定消息
    private String name; // 用户姓名字段
    
    @Email(message = "邮箱格式不正确") // 验证邮箱格式，如果不正确则显示指定消息
    private String email; // 用户邮箱字段，必须符合邮箱格式规范
}