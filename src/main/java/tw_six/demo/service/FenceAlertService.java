package tw_six.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tw_six.demo.entity.FenceAlert;
import tw_six.demo.repository.FenceAlertRepository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 围栏告警服务类 - 业务逻辑层
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 提供围栏告警记录的管理服务，包括告警查询、处理、统计等功能。
 * 是告警管理模块的核心服务，支持运营人员对告警进行跟踪和处理。
 * 
 * 【服务方法列表】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 方法名              │ 功能描述                       │ 返回值           │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ getAllAlerts        │ 获取所有告警记录               │ List<FenceAlert> │
 * │ getUnhandledAlerts  │ 获取未处理告警                 │ List<FenceAlert> │
 * │ getAlertsByCar      │ 按车辆查询告警                 │ List<FenceAlert> │
 * │ getAlertsByTimeRange│ 按时间范围查询告警             │ List<FenceAlert> │
 * │ createAlert         │ 创建告警记录                   │ FenceAlert       │
 * │ handleAlert         │ 处理告警                       │ FenceAlert       │
 * │ deleteAlert         │ 删除告警                       │ void             │
 * │ getUnhandledAlertCount│ 获取未处理告警数量           │ Long             │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【告警处理流程】
 * 1. 系统自动检测车辆越界并创建告警
 * 2. 运营人员在告警列表查看未处理告警
 * 3. 确认告警后点击"处理"按钮
 * 4. 系统记录处理人和处理时间
 * 5. 告警状态变更为"已处理"
 * 
 * 【关联文件】
 * - 控制器: tw_six.demo.controller.FenceAlertController
 * - 实体类: tw_six.demo.entity.FenceAlert
 * - 仓库层: tw_six.demo.repository.FenceAlertRepository
 * - 调用方: tw_six.demo.service.GeoFenceService
 * 
 * 【状态说明】
 * - isHandled=false: 未处理，需要运营人员关注
 * - isHandled=true: 已处理，记录了处理人和处理时间
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Service
public class FenceAlertService {
    
    @Autowired
    private FenceAlertRepository fenceAlertRepository;
    
    /**
     * 获取所有告警记录
     * 
     * 功能说明:
     * - 查询系统中所有围栏告警记录
     * - 包含已处理和未处理的告警
     * 
     * @return 所有告警记录列表
     */
    public List<FenceAlert> getAllAlerts() {
        return fenceAlertRepository.findAll();
    }
    
    /**
     * 获取未处理告警列表
     * 
     * 功能说明:
     * - 查询所有状态为"未处理"的告警
     * - 用于告警提醒和待办事项展示
     * - 通常在首页或通知栏显示
     * 
     * @return 未处理告警列表
     */
    public List<FenceAlert> getUnhandledAlerts() {
        return fenceAlertRepository.findByIsHandled(false);
    }
    
    /**
     * 按车辆查询告警记录
     * 
     * 功能说明:
     * - 查询指定车辆的所有围栏告警历史
     * - 按创建时间倒序排列
     * - 用于车辆详情页面的告警记录展示
     * 
     * @param carName 车辆名称
     * @return 该车辆的告警记录列表
     */
    public List<FenceAlert> getAlertsByCar(String carName) {
        return fenceAlertRepository.findByCarNameOrderByCreatedTimeDesc(carName);
    }
    
    /**
     * 按时间范围查询告警记录
     * 
     * 功能说明:
     * - 查询指定时间范围内的告警记录
     * - 用于报表统计和历史查询功能
     * 
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 时间范围内的告警记录列表
     */
    public List<FenceAlert> getAlertsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return fenceAlertRepository.findByTimeRange(startTime, endTime);
    }
    
    /**
     * 创建告警记录
     * 
     * 功能说明:
     * - 创建新的告警记录
     * - 自动设置创建时间为当前时间
     * - 通常由GeoFenceService检测到越界时调用
     * 
     * @param alert 告警对象
     * @return 保存后的告警对象
     */
    public FenceAlert createAlert(FenceAlert alert) {
        alert.setCreatedTime(LocalDateTime.now());
        return fenceAlertRepository.save(alert);
    }
    
    /**
     * 处理告警
     * 
     * 功能说明:
     * - 将告警状态更新为"已处理"
     * - 记录处理人和处理时间
     * - 用于告警确认和处理流程
     * 
     * @param id      告警ID
     * @param handler 处理人姓名/账号
     * @return 更新后的告警对象，如果告警不存在返回null
     */
    public FenceAlert handleAlert(Long id, String handler) {
        FenceAlert alert = fenceAlertRepository.findById(id).orElse(null);
        if (alert != null) {
            alert.setIsHandled(true);
            alert.setHandler(handler);
            alert.setHandleTime(LocalDateTime.now());
            return fenceAlertRepository.save(alert);
        }
        return null;
    }
    
    /**
     * 删除告警记录
     * 
     * 功能说明:
     * - 物理删除指定的告警记录
     * - 通常用于清理测试数据或误报记录
     * 
     * @param id 告警ID
     */
    public void deleteAlert(Long id) {
        fenceAlertRepository.deleteById(id);
    }
    
    /**
     * 获取未处理告警数量
     * 
     * 功能说明:
     * - 统计当前未处理的告警总数
     * - 用于仪表盘数字展示和红点提醒
     * 
     * @return 未处理告警数量
     */
    public Long getUnhandledAlertCount() {
        return fenceAlertRepository.countUnhandledAlerts();
    }
}
