package tw_six.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw_six.demo.entity.VehicleTrack;
import tw_six.demo.repository.VehicleTrackRepository;
import tw_six.demo.dto.TrackPlaybackDTO;
import tw_six.demo.dto.TrackPlaybackResponse;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
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
 * │ getPlaybackData      │ 获取轨迹回放数据               │ TrackPlaybackResponse│
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
    
    /** 地球半径 - 用于距离计算（单位：公里） */
    private static final double EARTH_RADIUS = 6371.0;
    
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
    
    /**
     * 获取轨迹回放数据
     * 
     * 功能说明:
     * - 获取指定车辆在时间范围内的轨迹回放数据
     * - 计算每个轨迹点的序号、距离、进度等信息
     * - 返回包含统计信息的完整回放数据
     * 
     * @param carName 车辆名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 轨迹回放响应数据
     */
    public TrackPlaybackResponse getPlaybackData(String carName, LocalDateTime startTime, LocalDateTime endTime) {
        // 查询轨迹数据
        List<VehicleTrack> tracks = getTracksByTimeRange(carName, startTime, endTime);
        
        // 转换为回放DTO
        List<TrackPlaybackDTO> playbackDTOs = convertToPlaybackDTO(tracks);
        
        // 构建响应
        return TrackPlaybackResponse.of(carName, playbackDTOs);
    }
    
    /**
     * 获取车辆最近一次行程的回放数据
     * 
     * @param carName 车辆名称
     * @param hours 查询最近N小时的数据
     * @return 轨迹回放响应数据
     */
    public TrackPlaybackResponse getRecentPlaybackData(String carName, int hours) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(hours);
        return getPlaybackData(carName, startTime, endTime);
    }
    
    /**
     * 将轨迹实体列表转换为回放DTO列表
     * 
     * 计算每个轨迹点的：
     * - 序号（从1开始）
     * - 距起点累计距离
     * - 回放进度百分比
     * - 与上一个点的时间间隔
     * 
     * @param tracks 轨迹实体列表
     * @return 回放DTO列表
     */
    private List<TrackPlaybackDTO> convertToPlaybackDTO(List<VehicleTrack> tracks) {
        List<TrackPlaybackDTO> dtos = new ArrayList<>();
        
        if (tracks.isEmpty()) {
            return dtos;
        }
        
        double totalDistance = 0;
        LocalDateTime prevTime = null;
        double[] prevCoord = null;
        
        for (int i = 0; i < tracks.size(); i++) {
            VehicleTrack track = tracks.get(i);
            TrackPlaybackDTO dto = new TrackPlaybackDTO(
                track.getId(),
                track.getCarName(),
                track.getLatitude(),
                track.getLongitude(),
                track.getSpeed(),
                track.getDirection(),
                track.getRecordTime(),
                track.getStatus()
            );
            
            // 设置序号
            dto.setSequence(i + 1);
            
            // 计算累计距离
            if (prevCoord != null) {
                double segmentDist = calculateDistance(
                    prevCoord[0], prevCoord[1],
                    track.getLatitude(), track.getLongitude()
                );
                totalDistance += segmentDist;
            }
            dto.setDistance(totalDistance);
            
            // 计算时间间隔
            if (prevTime != null) {
                long seconds = Duration.between(prevTime, track.getRecordTime()).getSeconds();
                dto.setTimeInterval(seconds);
            }
            
            prevCoord = new double[]{track.getLatitude(), track.getLongitude()};
            prevTime = track.getRecordTime();
            
            dtos.add(dto);
        }
        
        // 设置总距离和进度
        double finalTotalDistance = totalDistance;
        dtos.forEach(dto -> {
            dto.setTotalDistance(finalTotalDistance);
            if (finalTotalDistance > 0) {
                dto.setProgress((dto.getDistance() / finalTotalDistance) * 100);
            } else {
                dto.setProgress(0.0);
            }
        });
        
        return dtos;
    }
    
    /**
     * 计算两点之间的距离（Haversine公式）
     * 
     * 使用球面距离公式计算两个GPS坐标点之间的距离。
     * 
     * @param lat1 点1纬度
     * @param lon1 点1经度
     * @param lat2 点2纬度
     * @param lon2 点2经度
     * @return 距离（单位：米）
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 将角度转换为弧度
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);
        
        // Haversine公式
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        // 返回距离（米）
        return EARTH_RADIUS * c * 1000;
    }
}
