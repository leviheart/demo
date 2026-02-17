package tw_six.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tw_six.demo.entity.UserEntity;

import java.util.Optional;

/**
 * 用户认证数据仓库 - 数据访问层
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供用户认证数据的数据库访问接口。
 * 支持按用户名、邮箱查询用户。
 * 
 * 【关联文件】
 * - UserEntity.java: 用户实体类
 * - UserDetailsServiceImpl.java: 用户详情服务
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * 按用户名查询用户
     * 
     * @param username 用户名
     * @return 用户实体（Optional包装）
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * 按邮箱查询用户
     * 
     * @param email 邮箱
     * @return 用户实体（Optional包装）
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);
}
