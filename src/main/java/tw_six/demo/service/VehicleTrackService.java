package tw_six.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw_six.demo.entity.VehicleTrack;
import tw_six.demo.repository.VehicleTrackRepository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 车辆轨迹服务类 - 业务逻辑层
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供车辆轨迹数据的管理服务，支持轨迹记录的保存、查询、时间范围检索。
 * 轨迹数据用于轨迹回放、历史路线分析、里程计算等场景。
 * 
 * 【服务方法列表】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 方法名               │ 功能描述                       │ 返回值          │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ saveTrack            │ 保存轨迹记录                   │ VehicleTrack    │
 * │ getTracksByCarName   │ 按车辆查询轨迹                 │ List<VehicleTrack>│
 * │ getTracksByTimeRange │ 按时间范围查询轨迹             │ List<VehicleTrack>│
 * │ getAllCarNames       │ 获取所有车辆名称               │ List<String>    │
 * │ getRecentTracks      │ 获取最近轨迹                   │ List<VehicleTrack>│
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【数据特点】
 * - 轨迹数据量大，需要定期归档
 * - 建议按时间分区存储提高查询效率
 * - 查询时注意分页和索引优化
 * 
 * 【关联文件】
 * - 控制器: tw_six.demo.controller.VehicleTrackController
 * - 实体类: tw_six.demo.entity.VehicleTrack
 * - 仓库层: tw_six.demo.repository.VehicleTrackRepository
 * 
 * 【使用场景】
 * 1. 轨迹回放：在地图上回放车辆历史行驶路线
 * 2. 路线分析：分析车辆常走路线和行驶习惯
 * 3. 里程计算：根据轨迹点计算实际行驶里程
 * 4. 位置追踪：实时追踪车辆当前位置和历史轨迹
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Service
@Transactional
public class VehicleTrackService {
    
    private final VehicleTrackRepository vehicleTrackRepository;
    
    @Autowired
    public VehicleTrackService(VehicleTrackRepository vehicleTrackRepository) {
        this.vehicleTrackRepository = vehicleTrackRepository;
    }
    
    /**
     * 保存轨迹记录
     * 
     * 功能说明:
     * - 保存一条新的轨迹点记录
     * - 通常由GPS设备定时上报调用
     * - 轨迹点按时间顺序存储
     * 
     * @param track 轨迹点对象
     * @return 保存后的轨迹对象
     */
    public VehicleTrack saveTrack(VehicleTrack track) {
        return vehicleTrackRepository.save(track);
    }
    
    /**
     * 按车辆查询所有轨迹
     * 
     * 功能说明:
     * - 查询指定车辆的所有历史轨迹点
     * - 按记录时间升序排列（便于轨迹回放）
     * - 数据量可能很大，建议配合时间范围使用
     * 
     * @param carName 车辆名称
     * @return 该车辆的轨迹点列表
     */
    public List<VehicleTrack> getTracksByCarName(String carName) {
        return vehicleTrackRepository.findByCarNameOrderByRecordTimeAsc(carName);
    }
    
    /**
     * 按时间范围查询轨迹
     * 
     * 功能说明:
     * - 查询指定车辆在特定时间范围内的轨迹
     * - 用于轨迹回放和历史查询
     * - 按记录时间升序排列
     * 
     * @param carName   车辆名称
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 时间范围内的轨迹点列表
     */
    public List<VehicleTrack> getTracksByTimeRange(String carName, LocalDateTime startTime, LocalDateTime endTime) {
        return vehicleTrackRepository.findByCarNameAndRecordTimeBetweenOrderByRecordTimeAsc(carName, startTime, endTime);
    }
    
    /**
     * 获取所有车辆名称列表
     * 
     * 功能说明:
     * - 查询系统中有轨迹记录的所有车辆
     * - 用于车辆选择下拉框
     * - 返回去重后的车辆名称列表
     * 
     * @return 车辆名称列表
     */
    public List<String> getAllCarNames() {
        return vehicleTrackRepository.findDistinctCarNames();
    }
    
    /**
     * 获取最近轨迹
     * 
     * 功能说明:
     * - 查询指定车辆最近N分钟内的轨迹
     * - 用于实时追踪和轨迹回放
     * - 按记录时间升序排列
     * 
     * @param carName 车辆名称
     * @param minutes 时间范围（分钟）
     * @return 最近时间段内的轨迹点列表
     */
    public List<VehicleTrack> getRecentTracks(String carName, int minutes) {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(minutes);
        return vehicleTrackRepository.findRecentTracksByCarName(carName, startTime);
    }
}
