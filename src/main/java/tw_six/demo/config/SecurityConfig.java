package tw_six.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import tw_six.demo.security.JwtAuthenticationFilter;
import tw_six.demo.security.UserDetailsServiceImpl;

/**
 * Spring Security安全配置类 - 认证授权核心配置
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第一章：Spring Security 概述】
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * 1.1 什么是 Spring Security？
 * ─────────────────────────────────────────────────────────────────────────────
 * Spring Security 是一个强大且高度可定制的认证和访问控制框架。
 * 它是保护基于Spring应用程序的事实标准。
 * 
 * 【核心功能】
 * 1. 认证（Authentication）：验证"你是谁"
 *    - 用户名密码认证
 *    - JWT令牌认证
 *    - OAuth2认证
 *    - LDAP认证
 * 
 * 2. 授权（Authorization）：验证"你能做什么"
 *    - URL级别权限控制
 *    - 方法级别权限控制
 *    - 基于角色的访问控制（RBAC）
 * 
 * 3. 防护（Protection）：防止常见攻击
 *    - CSRF（跨站请求伪造）防护
 *    - XSS（跨站脚本攻击）防护
 *    - Session固定攻击防护
 *    - 点击劫持防护
 * 
 * 1.2 认证 vs 授权
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * 【认证 Authentication】
 * 问题：你是谁？
 * 方式：用户名密码、令牌、证书等
 * 结果：确认用户身份
 * 
 * 例如：
 * - 用户登录时输入用户名密码
 * - 系统验证密码是否正确
 * - 正确则认证成功，建立用户身份
 * 
 * 【授权 Authorization】
 * 问题：你能做什么？
 * 方式：角色、权限、规则等
 * 结果：决定用户能否访问某资源
 * 
 * 例如：
 * - 用户请求 /admin 页面
 * - 系统检查用户是否有 ADMIN 角色
 * - 有则允许访问，无则拒绝
 * 
 * 1.3 Spring Security 架构
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 *   ┌─────────────────────────────────────────────────────────────────────┐
 *   │                         HTTP请求                                     │
 *   └─────────────────────────────────────────────────────────────────────┘
 *                                    │
 *                                    ▼
 *   ┌─────────────────────────────────────────────────────────────────────┐
 *   │                    SecurityFilterChain                               │
 *   │  ┌─────────────────────────────────────────────────────────────┐   │
 *   │  │ Filter 1: SecurityContextFilter (安全上下文)                   │   │
 *   │  ├─────────────────────────────────────────────────────────────┤   │
 *   │  │ Filter 2: JwtAuthenticationFilter (JWT认证)                   │   │
 *   │  ├─────────────────────────────────────────────────────────────┤   │
 *   │  │ Filter 3: AuthorizationFilter (授权检查)                       │   │
 *   │  ├─────────────────────────────────────────────────────────────┤   │
 *   │  │ Filter N: ...                                                 │   │
 *   │  └─────────────────────────────────────────────────────────────┘   │
 *   └─────────────────────────────────────────────────────────────────────┘
 *                                    │
 *                                    ▼
 *   ┌─────────────────────────────────────────────────────────────────────┐
 *   │                         Controller                                   │
 *   └─────────────────────────────────────────────────────────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第二章：核心注解详解】
 * ═══════════════════════════════════════════════════════════════════════════════
 */

/**
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ @Configuration 注解详解                                                    │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * 2.1 @Configuration 是什么？
 * ─────────────────────────────────────────────────────────────────────────────
 * @Configuration 用于标记一个类为配置类，该类可以包含一个或多个@Bean方法。
 * 
 * 【源码定义】
 * @Target(ElementType.TYPE)
 * @Retention(RetentionPolicy.RUNTIME)
 * @Documented
 * @Component  // @Configuration 本质上也是一个@Component
 * public @interface Configuration { ... }
 * 
 * 【作用】
 * 1. 标记为配置类：告诉Spring这是一个配置类
 * 2. 代理模式：Spring会对@Configuration类进行CGLIB代理
 * 3. Bean单例保证：确保@Bean方法返回的单例Bean是同一个实例
 * 
 * 【为什么需要代理？】
 * 
 * // 没有代理时的问题：
 * @Configuration
 * public class Config {
 *     @Bean
 *     public ServiceA serviceA() {
 *         return new ServiceA(repository());  // 调用repository()
 *     }
 *     
 *     @Bean
 *     public ServiceB serviceB() {
 *         return new ServiceB(repository());  // 再次调用repository()
 *     }
 *     
 *     @Bean
 *     public Repository repository() {
 *         return new Repository();  // 会创建两次！
 *     }
 * }
 * 
 * // 有代理后：
 * Spring会拦截repository()调用，确保只创建一个实例
 */
@Configuration

/**
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ @EnableWebSecurity 注解详解                                               │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * 2.2 @EnableWebSecurity 是什么？
 * ─────────────────────────────────────────────────────────────────────────────
 * @EnableWebSecurity 用于启用Spring Security的Web安全功能。
 * 
 * 【源码定义】
 * @Retention(RetentionPolicy.RUNTIME)
 * @Target(ElementType.TYPE)
 * @Documented
 * @Import({WebSecurityConfiguration.class, SpringWebMvcImportSelector.class})
 * @EnableGlobalAuthentication  // 启用全局认证配置
 * public @interface EnableWebSecurity { ... }
 * 
 * 【作用】
 * 1. 导入WebSecurityConfiguration配置
 * 2. 注册DelegatingFilterProxy过滤器
 * 3. 启用Spring Security的默认配置
 * 
 * 【Spring Security 6.x 的变化】
 * 在Spring Security 6.x中，@EnableWebSecurity不再是必需的，
 * 只要类上有@Configuration注解，Spring Boot会自动配置Security。
 * 但显式添加可以更清晰地表达意图。
 */
@EnableWebSecurity

/**
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ @EnableMethodSecurity 注解详解                                            │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * 2.3 @EnableMethodSecurity 是什么？
 * ─────────────────────────────────────────────────────────────────────────────
 * @EnableMethodSecurity 用于启用方法级别的安全注解。
 * 
 * 【源码定义】
 * @Retention(RetentionPolicy.RUNTIME)
 * @Target(ElementType.TYPE)
 * @EnableGlobalMethodSecurity(prePostEnabled = true)  // 启用@PreAuthorize等注解
 * public @interface EnableMethodSecurity { ... }
 * 
 * 【启用后可用的注解】
 * 
 * 1. @PreAuthorize：方法执行前检查权限
 *    @PreAuthorize("hasRole('ADMIN')")
 *    public void deleteUser(Long id) { ... }
 * 
 * 2. @PostAuthorize：方法执行后检查权限
 *    @PostAuthorize("returnObject.owner == authentication.name")
 *    public Document getDocument(Long id) { ... }
 * 
 * 3. @PreFilter：过滤方法参数
 *    @PreFilter("filterObject.owner == authentication.name")
 *    public void saveDocuments(List<Document> documents) { ... }
 * 
 * 4. @PostFilter：过滤返回值
 *    @PostFilter("filterObject.owner == authentication.name")
 *    public List<Document> getDocuments() { ... }
 * 
 * 5. @Secured：基于角色的简单检查
 *    @Secured("ROLE_ADMIN")
 *    public void adminOnly() { ... }
 * 
 * 【Spring Security 6.x 的变化】
 * 旧版本使用 @EnableGlobalMethodSecurity，已废弃。
 * 新版本使用 @EnableMethodSecurity，默认启用prePostEnabled。
 */
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 依赖注入：安全配置需要的组件                                              │
     * └────────────────────────────────────────────────────────────────────────┘
     * 
     * 2.4 UserDetailsServiceImpl 的作用
     * ─────────────────────────────────────────────────────────────────────────
     * 
     * UserDetailsService 是Spring Security的核心接口，用于加载用户信息。
     * 
     * 【接口定义】
     * public interface UserDetailsService {
     *     UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
     * }
     * 
     * 【工作流程】
     * 1. 用户提交用户名密码
     * 2. Spring Security调用loadUserByUsername(username)
     * 3. 从数据库查询用户信息
     * 4. 返回UserDetails对象（包含用户名、密码、权限）
     * 5. Spring Security比对密码
     * 
     * 2.5 JwtAuthenticationFilter 的作用
     * ─────────────────────────────────────────────────────────────────────────
     * 
     * 自定义过滤器，用于：
     * 1. 从请求头提取JWT令牌
     * 2. 验证令牌有效性
     * 3. 解析用户信息
     * 4. 设置Spring Security上下文
     */
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 构造函数 - 依赖注入
     * 
     * 【为什么使用构造函数注入？】
     * - 保证依赖不可变（final字段）
     * - 明确表达类的依赖关系
     * - 便于单元测试
     * 
     * @param userDetailsService 用户详情服务
     * @param jwtAuthenticationFilter JWT认证过滤器
     */
    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * ══════════════════════════════════════════════════════════════════════════
     * 【第三章：安全过滤器链配置】
     * ══════════════════════════════════════════════════════════════════════════
     */
    
    /**
     * 配置安全过滤器链
     * 
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ @Bean 注解详解                                                          │
     * └────────────────────────────────────────────────────────────────────────┘
     * 
     * 3.1 @Bean 是什么？
     * ─────────────────────────────────────────────────────────────────────────
     * @Bean 用于声明一个方法返回的对象应该由Spring容器管理。
     * 
     * 【源码定义】
     * @Target(ElementType.METHOD)
     * @Retention(RetentionPolicy.RUNTIME)
     * @Documented
     * public @interface Bean {
     *     String[] name() default {};      // Bean名称
     *     String[] initMethod() default {}; // 初始化方法
     *     String[] destroyMethod() default {}; // 销毁方法
     * }
     * 
     * 【@Bean vs @Component】
     * ┌─────────────────┬─────────────────────────────────────────────────────┐
     * │ 方式             │ 说明                                                │
     * ├─────────────────┼─────────────────────────────────────────────────────┤
     * │ @Component      │ 用于类级别，Spring自动扫描注册                         │
     * │ @Bean           │ 用于方法级别，手动控制Bean创建过程                     │
     * └─────────────────┴─────────────────────────────────────────────────────┘
     * 
     * 【@Bean的使用场景】
     * 1. 第三方库的类：无法添加@Component注解
     * 2. 需要复杂初始化逻辑
     * 3. 条件性创建Bean
     * 4. 配置类中定义Bean
     * 
     * 3.2 SecurityFilterChain 是什么？
     * ─────────────────────────────────────────────────────────────────────────
     * SecurityFilterChain 定义了HTTP请求的安全过滤器链。
     * 
     * 【工作原理】
     * 1. 每个HTTP请求都会经过这个过滤器链
     * 2. 过滤器链按顺序执行各个过滤器
     * 3. 每个过滤器负责特定的安全功能
     * 
     * 【常用过滤器】
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │ 过滤器名称                    │ 功能                                  │
     * ├─────────────────────────────────────────────────────────────────────┤
     * │ SecurityContextFilter        │ 管理安全上下文                        │
     * │ HeaderWriterFilter           │ 写入安全相关的HTTP头                  │
     * │ CsrfFilter                   │ CSRF防护                             │
     * │ LogoutFilter                 │ 处理注销请求                          │
     * │ UsernamePasswordAuthenticationFilter │ 处理表单登录                   │
     * │ AuthorizationFilter          │ 授权检查                              │
     * └─────────────────────────────────────────────────────────────────────┘
     * 
     * @param http HttpSecurity配置对象
     * @return 配置好的安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        /**
         * ┌────────────────────────────────────────────────────────────────────┐
         * │ HttpSecurity 配置详解                                               │
         * └────────────────────────────────────────────────────────────────────┘
         * 
         * HttpSecurity 是Spring Security的核心配置类，用于配置HTTP安全。
         * 
         * 【配置方式】
         * Spring Security 6.x 使用Lambda DSL配置风格：
         * 
         * // 旧版本（已废弃）
         * http.csrf().disable();
         * 
         * // 新版本（推荐）
         * http.csrf(csrf -> csrf.disable());
         * 
         * 【Lambda DSL的优点】
         * 1. 类型安全
         * 2. 更好的IDE支持
         * 3. 配置更清晰
         */
        http
            /**
             * ┌────────────────────────────────────────────────────────────────┐
             * │ CSRF防护配置                                                    │
             * └────────────────────────────────────────────────────────────────┘
             * 
             * 3.3 什么是CSRF？
             * ─────────────────────────────────────────────────────────────────
             * CSRF = Cross-Site Request Forgery（跨站请求伪造）
             * 
             * 【攻击原理】
             * 1. 用户登录了银行网站A
             * 2. 用户访问了恶意网站B
             * 3. 网站B发送请求到银行A（携带用户的Cookie）
             * 4. 银行A认为这是用户的合法请求
             * 
             * 【防护方式】
             * 1. CSRF Token：每个请求携带随机Token
             * 2. SameSite Cookie：限制Cookie跨站发送
             * 3. 验证Referer头
             * 
             * 【为什么JWT不需要CSRF防护？】
             * - JWT存储在localStorage/sessionStorage，不是Cookie
             * - 攻击者无法获取JWT令牌
             * - 每次请求需要手动添加Authorization头
             * 
             * 【禁用CSRF的场景】
             * - 纯API服务（无浏览器客户端）
             * - 使用JWT认证
             * - 无状态服务
             */
            .csrf(csrf -> csrf.disable())
            
            /**
             * ┌────────────────────────────────────────────────────────────────┐
             * │ 会话管理配置                                                    │
             * └────────────────────────────────────────────────────────────────┘
             * 
             * 3.4 SessionCreationPolicy 策略选项
             * ─────────────────────────────────────────────────────────────────
             * 
             * ┌──────────────────┬─────────────────────────────────────────────┐
             * │ 策略              │ 说明                                        │
             * ├──────────────────┼─────────────────────────────────────────────┤
             * │ IF_REQUIRED      │ 需要时创建Session（默认）                     │
             * │ ALWAYS           │ 总是创建Session                              │
             * │ NEVER            │ 从不创建Session，但使用已有的                │
             * │ STATELESS        │ 无状态，不创建也不使用Session                │
             * └──────────────────┴─────────────────────────────────────────────┘
             * 
             * 【为什么JWT要用STATELESS？】
             * - JWT本身包含用户信息，不需要服务端存储
             * - 无状态便于水平扩展
             * - 减少服务器内存占用
             * 
             * 【有状态 vs 无状态】
             * 有状态（Session）：
             * 服务端存储用户信息，Session ID存储在Cookie
             * 
             * 无状态（JWT）：
             * 用户信息存储在Token中，Token存储在客户端
             * 服务端不存储任何会话信息
             */
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            /**
             * ┌────────────────────────────────────────────────────────────────┐
             * │ 请求授权配置                                                    │
             * └────────────────────────────────────────────────────────────────┘
             * 
             * 3.5 authorizeHttpRequests 配置详解
             * ─────────────────────────────────────────────────────────────────
             * 
             * authorizeHttpRequests 用于配置URL级别的访问控制。
             * 
             * 【配置规则】
             * 1. 按配置顺序匹配，先匹配的生效
             * 2. 更具体的规则应该放在前面
             * 3. anyRequest()应该放在最后
             * 
             * 【常用方法】
             * ┌─────────────────────────────────────────────────────────────────┐
             * │ 方法                      │ 说明                                  │
             * ├─────────────────────────────────────────────────────────────────┤
             * │ permitAll()              │ 允许所有人访问（包括匿名）              │
             * │ denyAll()                │ 拒绝所有人访问                        │
             * │ authenticated()          │ 需要认证                              │
             * │ hasRole("ADMIN")         │ 需要ADMIN角色                         │
             * │ hasAnyRole("A","B")      │ 需要A或B角色                          │
             * │ hasAuthority("PERM")     │ 需要PERM权限                          │
             * │ access(spel)             │ 使用SpEL表达式                        │
             * └─────────────────────────────────────────────────────────────────┘
             */
            .authorizeHttpRequests(auth -> auth
                /**
                 * 3.6 requestMatchers 路径匹配
                 * ─────────────────────────────────────────────────────────────
                 * 
                 * requestMatchers 用于匹配请求路径。
                 * 
                 * 【匹配方式】
                 * ┌─────────────────────────────────────────────────────────────┐
                 * │ 方式                    │ 说明                                │
                 * ├─────────────────────────────────────────────────────────────┤
                 * │ requestMatchers("/api") │ 精确匹配                            │
                 * │ requestMatchers("/api/**") │ 通配符匹配（多级目录）           │
                 * │ requestMatchers("/api/*")  │ 通配符匹配（单级目录）           │
                 * │ requestMatchers(HttpMethod.GET, "/api") │ 指定HTTP方法    │
                 * │ requestMatchers(regex)  │ 正则表达式匹配                      │
                 * │ requestMatchers(mvcMatcher) │ MVC匹配器                      │
                 * └─────────────────────────────────────────────────────────────┘
                 * 
                 * 【通配符说明】
                 * * : 匹配0个或多个字符（不含路径分隔符）
                 * **: 匹配0个或多个目录
                 * 
                 * 例如：
                 * /api/*   匹配 /api/users，不匹配 /api/users/1
                 * /api/**  匹配 /api/users 和 /api/users/1
                 */
                
                // 公开访问路径 - 认证相关
                // 登录、注册、刷新令牌等接口不需要认证
                .requestMatchers("/api/auth/**").permitAll()
                
                // WebSocket连接
                // WebSocket握手需要允许匿名访问，认证在握手后进行
                .requestMatchers("/ws/**").permitAll()
                
                // 健康检查和错误页面
                // 监控系统需要能够访问健康检查接口
                .requestMatchers("/api/health/**").permitAll()
                .requestMatchers("/error").permitAll()
                
                // 业务API - 开发模式下允许公开访问
                // ⚠️ 生产环境应移除以下配置，要求认证
                // 这些路径在开发阶段允许公开访问，方便前端调试
                .requestMatchers("/api/map/**").permitAll()
                .requestMatchers("/api/tracks/**").permitAll()
                .requestMatchers("/api/behaviors/**").permitAll()
                .requestMatchers("/api/vehicle-groups/**").permitAll()
                .requestMatchers("/api/fence-alerts/**").permitAll()
                .requestMatchers("/api/geo-fences/**").permitAll()
                .requestMatchers("/api/car-locations/**").permitAll()
                .requestMatchers("/api/driving-statistics/**").permitAll()
                .requestMatchers("/api/vehicles/**").permitAll()
                .requestMatchers("/api/users/**").permitAll()
                .requestMatchers("/api/reports/**").permitAll()
                .requestMatchers("/api/statistics/**").permitAll()
                
                // 管理员路径 - 需要ADMIN角色
                // hasRole会自动添加"ROLE_"前缀
                // 实际检查的是 "ROLE_ADMIN"
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // 其他所有请求需要认证
                // anyRequest()必须放在最后，作为默认规则
                .anyRequest().authenticated()
            )
            
            /**
             * ┌────────────────────────────────────────────────────────────────┐
             * │ 过滤器配置                                                      │
             * └────────────────────────────────────────────────────────────────┘
             * 
             * 3.7 addFilterBefore 过滤器顺序
             * ─────────────────────────────────────────────────────────────────
             * 
             * addFilterBefore 在指定过滤器之前添加自定义过滤器。
             * 
             * 【为什么要在UsernamePasswordAuthenticationFilter之前？】
             * - UsernamePasswordAuthenticationFilter处理表单登录
             * - 我们使用JWT，不需要表单登录
             * - JWT验证应该在用户名密码验证之前
             * - 如果JWT有效，直接设置认证信息，跳过后续认证
             * 
             * 【过滤器顺序】
             * 1. JwtAuthenticationFilter (自定义)
             * 2. UsernamePasswordAuthenticationFilter
             * 3. AuthorizationFilter
             * ...
             * 
             * 【其他添加方式】
             * addFilterAfter(filter, afterFilter)  // 在指定过滤器之后
             * addFilterAt(filter, atFilter)        // 替换指定过滤器
             * addFilter(filter)                    // 添加到过滤器链
             */
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            /**
             * ┌────────────────────────────────────────────────────────────────┐
             * │ 认证提供者配置                                                  │
             * └────────────────────────────────────────────────────────────────┘
             * 
             * 3.8 authenticationProvider 认证提供者
             * ─────────────────────────────────────────────────────────────────
             * 
             * 认证提供者负责实际的认证逻辑。
             * 
             * 【认证流程】
             * 1. AuthenticationManager接收认证请求
             * 2. 委托给AuthenticationProvider
             * 3. DaoAuthenticationProvider调用UserDetailsService
             * 4. UserDetailsService从数据库加载用户
             * 5. PasswordEncoder验证密码
             * 6. 返回认证结果
             */
            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    /**
     * ══════════════════════════════════════════════════════════════════════════
     * 【第四章：密码编码器】
     * ══════════════════════════════════════════════════════════════════════════
     */
    
    /**
     * 创建密码编码器
     * 
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ BCryptPasswordEncoder 详解                                              │
     * └────────────────────────────────────────────────────────────────────────┘
     * 
     * 4.1 什么是BCrypt？
     * ─────────────────────────────────────────────────────────────────────────
     * BCrypt是一种密码哈希算法，专门设计用于密码存储。
     * 
     * 【特点】
     * 1. 自带盐值：每次加密自动生成随机盐
     * 2. 计算慢：防止暴力破解
     * 3. 可配置强度：通过cost参数控制计算复杂度
     * 
     * 【BCrypt加密过程】
     * 
     * 原始密码: "password123"
     * 
     * BCrypt加密后:
     * $2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq7Xf8uLk4VX5Yj5j5j5j5j5j5j5j5j
     * 
     * 格式解析:
     * $2a$     - BCrypt算法版本
     * $10$     - cost参数（计算强度，默认10）
     * $N9qo... - 22字符的盐值
     * 剩余部分 - 31字符的哈希值
     * 
     * 4.2 为什么BCrypt安全？
     * ─────────────────────────────────────────────────────────────────────────
     * 
     * 【对比其他哈希算法】
     * ┌─────────────────┬─────────────────────────────────────────────────────┐
     * │ 算法             │ 安全性                                              │
     * ├─────────────────┼─────────────────────────────────────────────────────┤
     * │ MD5             │ ❌ 不安全，可被彩虹表破解                            │
     * │ SHA-256         │ ⚠️ 不推荐，计算太快，易被暴力破解                    │
     * │ PBKDF2          │ ✅ 安全，但需要正确配置                              │
     * │ BCrypt          │ ✅ 推荐，自动加盐，计算慢                            │
     * │ Argon2          │ ✅ 最安全，但配置复杂                                │
     * └─────────────────┴─────────────────────────────────────────────────────┘
     * 
     * 【BCrypt的优势】
     * 1. 自动加盐：每次加密结果不同
     *    同一个密码加密两次：
     *    $2a$10$abc...xyz...
     *    $2a$10$def...uvw...  // 完全不同
     * 
     * 2. 计算慢：cost=10时，每次加密约100ms
     *    攻击者每秒只能尝试约10次
     * 
     * 3. 验证方式：
     *    BCrypt.matches(rawPassword, encodedPassword)
     *    从encodedPassword中提取盐值，用同样的盐加密rawPassword，比较结果
     * 
     * 4.3 其他PasswordEncoder选项
     * ─────────────────────────────────────────────────────────────────────────
     * 
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │ 编码器                    │ 说明                                      │
     * ├─────────────────────────────────────────────────────────────────────┤
     * │ BCryptPasswordEncoder     │ 推荐，自动加盐                            │
     * │ Pbkdf2PasswordEncoder     │ 安全，可配置迭代次数                      │
     * │ SCryptPasswordEncoder     │ 安全，内存密集型                          │
     * │ Argon2PasswordEncoder     │ 最安全，需要额外依赖                      │
     * │ NoOpPasswordEncoder       │ 明文存储，仅用于测试！                    │
     * │ DelegatingPasswordEncoder │ 支持多种编码器，便于迁移                  │
     * └─────────────────────────────────────────────────────────────────────┘
     * 
     * @return BCrypt密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        /**
         * BCryptPasswordEncoder构造参数：
         * - strength: cost参数，范围4-31，默认10
         *   值越大，计算越慢，越安全
         *   10: 约100ms
         *   12: 约400ms
         *   14: 约1.5s
         * 
         * 生产环境建议使用10-12
         */
        return new BCryptPasswordEncoder();
    }

    /**
     * ══════════════════════════════════════════════════════════════════════════
     * 【第五章：认证提供者】
     * ══════════════════════════════════════════════════════════════════════════
     */
    
    /**
     * 创建认证提供者
     * 
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ DaoAuthenticationProvider 详解                                          │
     * └────────────────────────────────────────────────────────────────────────┘
     * 
     * 5.1 什么是DaoAuthenticationProvider？
     * ─────────────────────────────────────────────────────────────────────────
     * DaoAuthenticationProvider是一个AuthenticationProvider实现，
     * 使用UserDetailsService和PasswordEncoder进行认证。
     * 
     * 【认证流程】
     * 
     *   用户提交认证请求
     *         │
     *         ▼
     *   AuthenticationManager.authenticate()
     *         │
     *         ▼
     *   DaoAuthenticationProvider.authenticate()
     *         │
     *         ├── userDetailsService.loadUserByUsername()
     *         │       │
     *         │       └── 从数据库查询用户信息
     *         │
     *         ├── passwordEncoder.matches()
     *         │       │
     *         │       └── 验证密码是否正确
     *         │
     *         └── 返回Authentication对象
     * 
     * 5.2 为什么需要自定义AuthenticationProvider？
     * ─────────────────────────────────────────────────────────────────────────
     * 
     * 【自定义的好处】
     * 1. 指定UserDetailsService：从数据库加载用户
     * 2. 指定PasswordEncoder：使用自定义的加密方式
     * 3. 添加额外的认证逻辑：如验证码、IP限制等
     * 
     * @return 认证提供者
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        
        /**
         * setUserDetailsService: 设置用户详情服务
         * 用于从数据库或其他数据源加载用户信息
         */
        provider.setUserDetailsService(userDetailsService);
        
        /**
         * setPasswordEncoder: 设置密码编码器
         * 用于验证用户输入的密码
         */
        provider.setPasswordEncoder(passwordEncoder());
        
        /**
         * 其他可选配置：
         * 
         * // 隐藏用户不存在异常
         * provider.setHideUserNotFoundExceptions(false);
         * 
         * // 设置用户缓存
         * provider.setUserCache(new SpringCacheBasedUserCache(cache));
         * 
         * // 设置权限前缀
         * provider.setRolePrefix("ROLE_");
         */
        
        return provider;
    }

    /**
     * ══════════════════════════════════════════════════════════════════════════
     * 【第六章：认证管理器】
     * ══════════════════════════════════════════════════════════════════════════
     */
    
    /**
     * 创建认证管理器
     * 
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ AuthenticationManager 详解                                              │
     * └────────────────────────────────────────────────────────────────────────┘
     * 
     * 6.1 什么是AuthenticationManager？
     * ─────────────────────────────────────────────────────────────────────────
     * AuthenticationManager是认证的核心接口，负责处理认证请求。
     * 
     * 【接口定义】
     * public interface AuthenticationManager {
     *     Authentication authenticate(Authentication authentication) 
     *         throws AuthenticationException;
     * }
     * 
     * 【工作原理】
     * 1. 接收Authentication对象（包含用户名密码）
     * 2. 委托给配置的AuthenticationProvider
     * 3. 返回已认证的Authentication对象（包含权限信息）
     * 
     * 【使用场景】
     * 在登录Controller中手动调用认证：
     * 
     * @PostMapping("/login")
     * public ApiResponse<?> login(@RequestBody LoginRequest request) {
     *     // 创建认证令牌
     *     Authentication authToken = new UsernamePasswordAuthenticationToken(
     *         request.getUsername(), 
     *         request.getPassword()
     *     );
     *     
     *     // 执行认证
     *     Authentication authentication = authenticationManager.authenticate(authToken);
     *     
     *     // 生成JWT
     *     String jwt = jwtUtils.generateToken(authentication);
     *     
     *     return ApiResponse.success(jwt);
     * }
     * 
     * 6.2 AuthenticationConfiguration 的作用
     * ─────────────────────────────────────────────────────────────────────────
     * 
     * AuthenticationConfiguration 是Spring Security提供的配置类，
     * 包含全局的AuthenticationManager配置。
     * 
     * 【为什么通过它获取AuthenticationManager？】
     * - AuthenticationManager是全局共享的
     * - 需要使用Spring配置的AuthenticationProvider
     * - 避免手动创建AuthenticationManager
     * 
     * @param authConfig 认证配置
     * @return 认证管理器
     * @throws Exception 配置异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) 
            throws Exception {
        return authConfig.getAuthenticationManager();
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第七章：JWT认证流程总结】
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * 7.1 完整的JWT认证流程
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * 【登录流程】
 * 1. 用户提交用户名密码 → POST /api/auth/login
 * 2. Controller调用AuthenticationManager.authenticate()
 * 3. DaoAuthenticationProvider验证用户
 * 4. 验证成功，生成JWT令牌
 * 5. 返回JWT给客户端
 * 
 * 【请求认证流程】
 * 1. 客户端请求携带JWT → Authorization: Bearer <token>
 * 2. JwtAuthenticationFilter拦截请求
 * 3. 解析JWT，验证有效性
 * 4. 从JWT提取用户信息
 * 5. 设置SecurityContext
 * 6. 请求继续到Controller
 * 
 * 7.2 SecurityContext 的作用
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * SecurityContext存储当前认证用户的信息。
 * 
 * 【获取当前用户】
 * // 方式1：通过SecurityContextHolder
 * Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 * String username = auth.getName();
 * 
 * // 方式2：通过@AuthenticationPrincipal
 * @GetMapping("/me")
 * public User getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
 *     return userService.findByUsername(userDetails.getUsername());
 * }
 * 
 * // 方式3：通过Principal
 * @GetMapping("/me")
 * public User getCurrentUser(Principal principal) {
 *     return userService.findByUsername(principal.getName());
 * }
 * 
 * 7.3 安全最佳实践
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * 【生产环境建议】
 * 1. 移除permitAll的业务API配置，要求认证
 * 2. 使用HTTPS传输
 * 3. JWT设置合理的过期时间
 * 4. 实现刷新令牌机制
 * 5. 敏感操作要求二次验证
 * 6. 记录安全审计日志
 * 
 * 【JWT安全建议】
 * 1. 使用强密钥（至少256位）
 * 2. 设置合理的过期时间（如2小时）
 * 3. 实现令牌刷新机制
 * 4. 考虑令牌撤销机制（黑名单）
 * 5. 不在JWT中存储敏感信息
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */
