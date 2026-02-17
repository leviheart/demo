package tw_six.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tw_six.demo.entity.DrivingBehavior;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 驾驶行为数据仓库 - 数据访问层
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供驾驶行为数据的数据库访问接口，继承Spring Data JPA的基础CRUD功能。
 * 定义自定义查询方法，支持按车辆、时间范围、行为类型等条件查询。
 * 
 * 【查询方法命名规则】
 * Spring Data JPA会根据方法名自动生成查询语句：
 * - findBy: 查询
 * - By: 条件
 * - And/Or: 条件连接
 * - OrderBy: 排序
 * - Asc/Desc: 升序/降序
 * 
 * 【关联文件】
 * - DrivingBehavior.java: 实体类
 * - DrivingBehaviorService.java: 服务层
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Repository
public interface DrivingBehaviorRepository extends JpaRepository<DrivingBehavior, Long> {

    /**
     * 按车辆查询所有行为记录
     * 
     * @param carName 车辆名称
     * @return 该车辆的行为记录列表
     */
    List<DrivingBehavior> findByCarNameOrderByEventTimeDesc(String carName);

    /**
     * 按时间范围查询行为记录
     * 
     * @param carName 车辆名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 时间范围内的行为记录列表
     */
    List<DrivingBehavior> findByCarNameAndEventTimeBetweenOrderByEventTimeDesc(
        String carName, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 按行为类型查询
     * 
     * @param carName 车辆名称
     * @param behaviorType 行为类型
     * @return 指定类型的行为记录列表
     */
    List<DrivingBehavior> findByCarNameAndBehaviorTypeOrderByEventTimeDesc(
        String carName, String behaviorType);

    /**
     * 按风险等级查询
     * 
     * @param riskLevel 风险等级
     * @return 指定风险等级的行为记录列表
     */
    List<DrivingBehavior> findByRiskLevelOrderByEventTimeDesc(String riskLevel);

    /**
     * 查询未处理的行为记录
     * 
     * @param carName 车辆名称
     * @return 未处理的行为记录列表
     */
    List<DrivingBehavior> findByCarNameAndIsHandledFalseOrderByEventTimeDesc(String carName);

    /**
     * 统计指定时间范围内的行为数量
     * 
     * @param carName 车辆名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 行为记录数量
     */
    @Query("SELECT COUNT(db) FROM DrivingBehavior db WHERE db.carName = :carName " +
           "AND db.eventTime BETWEEN :startTime AND :endTime")
    Long countByCarNameAndTimeRange(@Param("carName") String carName,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 按行为类型统计数量
     * 
     * @param carName 车辆名称
     * @param behaviorType 行为类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 行为记录数量
     */
    @Query("SELECT COUNT(db) FROM DrivingBehavior db WHERE db.carName = :carName " +
           "AND db.behaviorType = :behaviorType AND db.eventTime BETWEEN :startTime AND :endTime")
    Long countByCarNameAndTypeAndTimeRange(@Param("carName") String carName,
                                           @Param("behaviorType") String behaviorType,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 获取所有有行为记录的车辆名称
     * 
     * @return 车辆名称列表
     */
    @Query("SELECT DISTINCT db.carName FROM DrivingBehavior db")
    List<String> findDistinctCarNames();

    /**
     * 查询最近的行为记录
     * 
     * @param limit 限制数量
     * @return 最近的行为记录列表
     */
    @Query(value = "SELECT * FROM driving_behaviors ORDER BY event_time DESC LIMIT :limit", 
           nativeQuery = true)
    List<DrivingBehavior> findRecentBehaviors(@Param("limit") int limit);
}
