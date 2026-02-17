package tw_six.demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT工具类 - JSON Web Token生成与验证
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供JWT令牌的生成、解析、验证功能。
 * JWT用于无状态认证，服务端不需要存储会话信息。
 * 
 * 【JWT结构】
 * JWT由三部分组成，用点号分隔：
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ Header.Payload.Signature                                                 │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ Header: 令牌类型和签名算法                                               │
 * │ Payload: 用户信息和声明                                                  │
 * │ Signature: 签名，防止篡改                                                │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【Payload示例】
 * ```json
 * {
 *   "sub": "admin",           // 主题（用户名）
 *   "iat": 1705312800,        // 签发时间
 *   "exp": 1705399200,        // 过期时间
 *   "roles": ["ADMIN"]        // 自定义声明（角色）
 * }
 * ```
 * 
 * 【安全考虑】
 * 1. 使用强密钥（至少256位）
 * 2. 设置合理的过期时间
 * 3. 使用HTTPS传输
 * 4. 不在JWT中存储敏感信息
 * 
 * 【关联文件】
 * - SecurityConfig.java: 安全配置
 * - JwtAuthenticationFilter.java: JWT认证过滤器
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Component
public class JwtUtils {

    /** JWT签名密钥 - 使用HMAC-SHA256算法 */
    private final Key jwtSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    /** JWT过期时间（毫秒）- 默认24小时 */
    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    /**
     * 生成JWT令牌
     * 
     * 根据认证信息生成JWT令牌，包含用户名和角色信息。
     * 
     * @param authentication 认证对象
     * @return JWT令牌字符串
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateTokenFromUsername(userDetails.getUsername());
    }

    /**
     * 根据用户名生成JWT令牌
     * 
     * @param username 用户名
     * @return JWT令牌字符串
     */
    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(jwtSecretKey, SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * 从JWT令牌中获取用户名
     * 
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(jwtSecretKey)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    /**
     * 验证JWT令牌
     * 
     * 检查令牌是否有效：
     * 1. 签名是否正确
     * 2. 是否已过期
     * 3. 格式是否正确
     * 
     * @param authToken JWT令牌
     * @return true表示有效，false表示无效
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build()
                .parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            System.err.println("无效的JWT令牌: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.err.println("JWT令牌已过期: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("不支持的JWT令牌: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("JWT声明为空: " + e.getMessage());
        }
        return false;
    }

    /**
     * 获取JWT过期时间（毫秒）
     * 
     * @return 过期时间
     */
    public long getJwtExpiration() {
        return jwtExpiration;
    }
}
