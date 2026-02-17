package tw_six.demo.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import tw_six.demo.common.ApiResponse;
import tw_six.demo.entity.UserEntity;
import tw_six.demo.repository.UserRepository;
import tw_six.demo.security.JwtUtils;

/**
 * 认证控制器 - 用户登录注册API
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供用户认证相关的API接口，包括登录、注册、获取用户信息等。
 * 使用JWT令牌实现无状态认证。
 * 
 * 【API端点列表】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 方法 │ 路径                          │ 功能描述                       │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ POST │ /api/auth/login               │ 用户登录                       │
 * │ POST │ /api/auth/register            │ 用户注册                       │
 * │ GET  │ /api/auth/me                  │ 获取当前用户信息               │
 * │ POST │ /api/auth/logout              │ 用户登出                       │
 * │ GET  │ /api/auth/users               │ 获取用户列表（管理员）         │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【登录流程】
 * 1. 客户端发送用户名和密码
 * 2. 服务端验证凭据
 * 3. 验证成功生成JWT令牌
 * 4. 返回令牌给客户端
 * 5. 客户端后续请求携带令牌
 * 
 * 【关联文件】
 * - SecurityConfig.java: 安全配置
 * - JwtUtils.java: JWT工具类
 * - UserRepository.java: 用户数据仓库
 * ═══════════════════════════════════════════════════════════════════════════
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /** 认证管理器 */
    @Autowired
    private AuthenticationManager authenticationManager;

    /** 用户数据仓库 */
    @Autowired
    private UserRepository userRepository;

    /** 密码编码器 */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /** JWT工具类 */
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 用户登录
     * 
     * 验证用户凭据，成功后返回JWT令牌。
     * 
     * @param loginRequest 登录请求
     * @return JWT令牌和用户信息
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // 执行认证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // 设置认证到上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 生成JWT令牌
            String jwt = jwtUtils.generateJwtToken(authentication);

            // 更新最后登录时间
            UserEntity user = userRepository.findByUsername(loginRequest.getUsername()).orElse(null);
            if (user != null) {
                user.setLastLoginTime(LocalDateTime.now());
                userRepository.save(user);
            }

            // 返回响应
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("type", "Bearer");
            response.put("expiresIn", jwtUtils.getJwtExpiration() / 1000);
            response.put("username", loginRequest.getUsername());
            response.put("role", user != null ? user.getRole() : "VIEWER");

            return ResponseEntity.ok(ApiResponse.success("登录成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("登录失败: 用户名或密码错误"));
        }
    }

    /**
     * 用户注册
     * 
     * 创建新用户账号。
     * 
     * @param registerRequest 注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("用户名已被使用"));
        }

        // 检查邮箱是否已存在
        if (registerRequest.getEmail() != null && 
            userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("邮箱已被注册"));
        }

        // 创建用户
        UserEntity user = new UserEntity();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setRealName(registerRequest.getRealName());
        user.setPhone(registerRequest.getPhone());
        user.setRole("VIEWER"); // 默认角色
        user.setActive(true);

        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("注册成功"));
    }

    /**
     * 获取当前用户信息
     * 
     * @return 当前登录用户的信息
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("未登录"));
        }

        String username = authentication.getName();
        UserEntity user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("用户不存在"));
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("realName", user.getRealName());
        userInfo.put("phone", user.getPhone());
        userInfo.put("role", user.getRole());
        userInfo.put("active", user.getActive());
        userInfo.put("lastLoginTime", user.getLastLoginTime());

        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }

    /**
     * 用户登出
     * 
     * JWT是无状态的，登出只需客户端删除令牌。
     * 此接口主要用于记录登出日志。
     * 
     * @return 登出结果
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponse.success("登出成功"));
    }

    /**
     * 获取用户列表（管理员）
     * 
     * @return 用户列表
     */
    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        List<UserEntity> users = userRepository.findAll();
        
        List<Map<String, Object>> userList = users.stream().map(user -> {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("realName", user.getRealName());
            userInfo.put("role", user.getRole());
            userInfo.put("active", user.getActive());
            userInfo.put("createdTime", user.getCreatedTime());
            return userInfo;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(userList));
    }

    // ==================== 请求对象定义 ====================

    /**
     * 登录请求对象
     */
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /**
     * 注册请求对象
     */
    public static class RegisterRequest {
        private String username;
        private String password;
        private String email;
        private String realName;
        private String phone;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
}
