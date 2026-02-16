package tw_six.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw_six.demo.entity.DrivingStatistics;
import tw_six.demo.repository.DrivingStatisticsRepository;
import java.time.LocalDate;
import java.util.List;

/**
 * 驾驶统计服务类 - 业务逻辑层
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供车辆驾驶统计数据的管理服务，支持里程统计、排名查询、汇总分析。
 * 用于生成运营报表和驾驶行为分析，是数据分析模块的核心服务。
 * 
 * 【服务方法列表】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 方法名                │ 功能描述                     │ 返回值           │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ saveStatistics        │ 保存统计数据                 │ DrivingStatistics│
 * │ getStatisticsByCar    │ 按车辆查询统计               │ List<DrivingStatistics>│
 * │ getStatisticsByDateRange│ 按日期范围查询统计         │ List<DrivingStatistics>│
 * │ getDailyRanking       │ 获取日里程排名               │ List<DrivingStatistics>│
 * │ getAverageMileage     │ 获取平均里程                 │ Double           │
 * │ getTotalMileage       │ 获取总里程                   │ Double           │
 * │ getLatestStatistics   │ 获取最新统计                 │ DrivingStatistics│
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【统计指标说明】
 * - totalMileage: 总行驶里程（公里）
 * - totalDuration: 总行驶时长（分钟）
 * - averageSpeed: 平均速度（公里/小时）
 * - maxSpeed: 最高速度（公里/小时）
 * 
 * 【关联文件】
 * - 控制器: tw_six.demo.controller.DrivingStatisticsController
 * - 实体类: tw_six.demo.entity.DrivingStatistics
 * - 仓库层: tw_six.demo.repository.DrivingStatisticsRepository
 * 
 * 【使用场景】
 * 1. 日报生成：每日自动汇总车辆行驶里程
 * 2. 绩效考核：统计驾驶员工作量和效率
 * 3. 运营分析：分析车队整体运营情况
 * 4. 成本核算：根据里程计算燃油和维护成本
 * 
 * 【统计周期】
 * - 按日统计：每日凌晨自动汇总前一天数据
 * - 按周/月统计：基于日统计数据聚合
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Service
@Transactional
public class DrivingStatisticsService {
    
    private final DrivingStatisticsRepository drivingStatisticsRepository;
    
    @Autowired
    public DrivingStatisticsService(DrivingStatisticsRepository drivingStatisticsRepository) {
        this.drivingStatisticsRepository = drivingStatisticsRepository;
    }
    
    /**
     * 保存驾驶统计数据
     * 
     * 功能说明:
     * - 保存一条驾驶统计记录
     * - 通常由定时任务自动调用
     * - 也可手动录入补充数据
     * 
     * @param statistics 统计数据对象
     * @return 保存后的统计对象
     */
    public DrivingStatistics saveStatistics(DrivingStatistics statistics) {
        return drivingStatisticsRepository.save(statistics);
    }
    
    /**
     * 按车辆查询统计数据
     * 
     * 功能说明:
     * - 查询指定车辆的所有历史统计记录
     * - 按记录日期倒序排列
     * 
     * @param carName 车辆名称
     * @return 该车辆的统计记录列表
     */
    public List<DrivingStatistics> getStatisticsByCar(String carName) {
        return drivingStatisticsRepository.findByCarNameOrderByRecordDateDesc(carName);
    }
    
    /**
     * 按日期范围查询统计数据
     * 
     * 功能说明:
     * - 查询指定车辆在特定日期范围内的统计
     * - 用于生成周报、月报等周期性报表
     * 
     * @param carName   车辆名称
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 日期范围内的统计记录列表
     */
    public List<DrivingStatistics> getStatisticsByDateRange(String carName, LocalDate startDate, LocalDate endDate) {
        return drivingStatisticsRepository.findByCarNameAndRecordDateBetweenOrderByRecordDateDesc(carName, startDate, endDate);
    }
    
    /**
     * 获取日里程排名
     * 
     * 功能说明:
     * - 查询指定日期所有车辆的里程排名
     * - 按里程从高到低排序
     * - 用于绩效展示和竞赛排名
     * 
     * @param date 查询日期
     * @return 当日里程排名列表
     */
    public List<DrivingStatistics> getDailyRanking(LocalDate date) {
        return drivingStatisticsRepository.findByRecordDateOrderByMileageDesc(date);
    }
    
    /**
     * 获取车辆平均里程
     * 
     * 功能说明:
     * - 计算指定车辆的平均日里程
     * - 用于评估车辆使用频率
     * 
     * @param carName 车辆名称
     * @return 平均里程（公里），无数据时返回null
     */
    public Double getAverageMileage(String carName) {
        return drivingStatisticsRepository.findAverageMileageByCarName(carName);
    }
    
    /**
     * 获取车辆总里程
     * 
     * 功能说明:
     * - 计算指定车辆的累计总里程
     * - 用于车辆维护周期计算
     * 
     * @param carName 车辆名称
     * @return 总里程（公里），无数据时返回null
     */
    public Double getTotalMileage(String carName) {
        return drivingStatisticsRepository.findTotalMileageByCarName(carName);
    }
    
    /**
     * 获取最新统计数据
     * 
     * 功能说明:
     * - 获取指定车辆最近一条统计记录
     * - 用于车辆详情页的概览展示
     * 
     * @param carName 车辆名称
     * @return 最新统计对象，无数据时返回null
     */
    public DrivingStatistics getLatestStatistics(String carName) {
        List<DrivingStatistics> stats = getStatisticsByCar(carName);
        return stats.isEmpty() ? null : stats.get(0);
    }
}
