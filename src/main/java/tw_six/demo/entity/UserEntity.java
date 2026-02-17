package tw_six.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户认证实体类 - 存储用户认证信息
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 存储用户的认证信息，包括用户名、密码、角色等。
 * 用于Spring Security认证和授权。
 * 
 * 【角色说明】
 * ┌──────────────────┬───────────────────────────────────────────────────────┐
 * │ 角色              │ 权限说明                                              │
 * ├──────────────────┼───────────────────────────────────────────────────────┤
 * │ ADMIN            │ 管理员，拥有所有权限                                  │
 * │ OPERATOR         │ 操作员，可管理车辆和围栏                              │
 * │ VIEWER           │ 观察者，只能查看数据                                  │
 * └──────────────────┴───────────────────────────────────────────────────────┘
 * 
 * 【关联文件】
 * - UserDetailsServiceImpl.java: 加载用户信息
 * - AuthController.java: 用户认证API
 * - UserRepository.java: 数据访问
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Entity
@Table(name = "sys_users")
public class UserEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名 - 登录账号
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 密码 - BCrypt加密后的密码
     */
    @Column(nullable = false, length = 100)
    private String password;

    /**
     * 邮箱
     */
    @Column(unique = true, length = 100)
    private String email;

    /**
     * 手机号
     */
    @Column(length = 20)
    private String phone;

    /**
     * 真实姓名
     */
    @Column(length = 50)
    private String realName;

    /**
     * 角色 - ADMIN, OPERATOR, VIEWER
     */
    @Column(nullable = false, length = 20)
    private String role = "VIEWER";

    /**
     * 是否激活
     */
    @Column(nullable = false)
    private Boolean active = true;

    /**
     * 创建时间
     */
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 最后登录时间
     */
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 默认构造函数
     */
    public UserEntity() {
    }

    /**
     * 实体持久化前的回调
     */
    @PrePersist
    protected void onCreate() {
        if (createdTime == null) {
            createdTime = LocalDateTime.now();
        }
        if (updatedTime == null) {
            updatedTime = LocalDateTime.now();
        }
    }

    /**
     * 实体更新前的回调
     */
    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }

    // ==================== Getter和Setter方法 ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }

    public LocalDateTime getLastLoginTime() { return lastLoginTime; }
    public void setLastLoginTime(LocalDateTime lastLoginTime) { this.lastLoginTime = lastLoginTime; }
}
