package tw_six.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tw_six.demo.entity.UserEntity;
import tw_six.demo.repository.UserRepository;

import java.util.Collections;

/**
 * 用户详情服务实现 - Spring Security认证支持
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 实现Spring Security的UserDetailsService接口，提供用户认证信息。
 * 在用户登录时被调用，加载用户的权限信息。
 * 
 * 【认证流程】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                      用户认证流程                                        │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │                                                                         │
 * │  用户登录请求                                                            │
 * │      │                                                                  │
 * │      ▼                                                                  │
 * │  AuthController.login()                                                 │
 * │      │                                                                  │
 * │      ▼                                                                  │
 * │  AuthenticationManager.authenticate()                                   │
 * │      │                                                                  │
 * │      ▼                                                                  │
 * │  DaoAuthenticationProvider                                              │
 * │      │                                                                  │
 * │      ▼                                                                  │
 * │  UserDetailsService.loadUserByUsername()  ← 本类实现                    │
 * │      │                                                                  │
 * │      ├── 从数据库查询用户                                               │
 * │      ├── 转换为UserDetails对象                                          │
 * │      └── 返回用户信息和权限                                             │
 * │      │                                                                  │
 * │      ▼                                                                  │
 * │  验证密码                                                                │
 * │      │                                                                  │
 * │      ├── 成功 → 生成JWT令牌                                             │
 * │      └── 失败 → 抛出异常                                                │
 * │                                                                         │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【UserDetails结构】
 * UserDetails包含以下信息：
 * - username: 用户名
 * - password: 密码（加密后）
 * - authorities: 权限列表
 * - accountNonExpired: 账户未过期
 * - accountNonLocked: 账户未锁定
 * - credentialsNonExpired: 凭证未过期
 * - enabled: 账户启用
 * 
 * 【关联文件】
 * - SecurityConfig.java: 注册此服务
 * - UserEntity.java: 用户实体类
 * - UserRepository.java: 用户数据仓库
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    /** 用户数据仓库 */
    @Autowired
    private UserRepository userRepository;

    /**
     * 根据用户名加载用户详情
     * 
     * Spring Security在认证时会调用此方法获取用户信息。
     * 返回的UserDetails对象包含用户名、密码和权限列表。
     * 
     * @param username 用户名
     * @return UserDetails对象
     * @throws UsernameNotFoundException 用户不存在时抛出
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库查询用户
        UserEntity user = userRepository.findByUsername(username)
            .orElseThrow(() -> 
                new UsernameNotFoundException("用户不存在: " + username));

        // 构建权限列表
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
            "ROLE_" + user.getRole()
        );

        // 返回Spring Security的User对象
        return User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .authorities(Collections.singletonList(authority))
            .accountExpired(false)
            .accountLocked(!user.getActive())
            .credentialsExpired(false)
            .disabled(!user.getActive())
            .build();
    }
}
