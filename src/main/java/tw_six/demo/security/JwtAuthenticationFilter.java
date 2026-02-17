package tw_six.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器 - 请求拦截认证
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 拦截所有HTTP请求，从请求头中提取JWT令牌并验证。
 * 验证通过后设置认证信息到Security上下文。
 * 
 * 【工作流程】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                      JWT认证过滤器工作流程                               │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │                                                                         │
 * │  HTTP请求                                                               │
 * │      │                                                                  │
 * │      ▼                                                                  │
 * │  doFilterInternal()                                                     │
 * │      │                                                                  │
 * │      ├── 1. 从请求头获取JWT令牌                                         │
 * │      │       Authorization: Bearer <token>                              │
 * │      │                                                                  │
 * │      ├── 2. 验证令牌有效性                                              │
 * │      │       ├── 有效 → 继续                                           │
 * │      │       └── 无效 → 跳过认证                                        │
 * │      │                                                                  │
 * │      ├── 3. 从令牌获取用户名                                            │
 * │      │                                                                  │
 * │      ├── 4. 加载用户详情                                                │
 * │      │                                                                  │
 * │      ├── 5. 创建认证对象                                                │
 * │      │       UsernamePasswordAuthenticationToken                        │
 * │      │                                                                  │
 * │      ├── 6. 设置认证到Security上下文                                    │
 * │      │       SecurityContextHolder.getContext().setAuthentication()     │
 * │      │                                                                  │
 * │      ▼                                                                  │
 * │  继续过滤器链 → Controller                                              │
 * │                                                                         │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【请求头格式】
 * ```
 * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
 * ```
 * 
 * 【关联文件】
 * - SecurityConfig.java: 注册此过滤器
 * - JwtUtils.java: JWT验证工具
 * - UserDetailsServiceImpl.java: 用户详情服务
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** JWT工具类 */
    @Autowired
    private JwtUtils jwtUtils;

    /** 用户详情服务 */
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * 过滤器核心方法
     * 
     * 每个请求都会经过此方法，进行JWT认证。
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // 1. 从请求头获取JWT令牌
            String jwt = parseJwt(request);
            
            // 2. 验证令牌并设置认证
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                // 从令牌获取用户名
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                
                // 加载用户详情
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // 创建认证对象
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                
                // 设置认证详情
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                // 设置认证到Security上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("无法设置用户认证: " + e.getMessage());
        }

        // 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头解析JWT令牌
     * 
     * 令牌格式: Authorization: Bearer <token>
     * 
     * @param request HTTP请求
     * @return JWT令牌字符串，如果不存在则返回null
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
