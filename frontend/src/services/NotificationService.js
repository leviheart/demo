/**
 * 通知服务 - 告警和消息通知管理
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 统一管理应用内的所有通知消息，包括告警通知、系统消息、操作提示等。
 * 支持多种通知类型和显示样式。
 * 
 * 【通知类型】
 * ┌──────────────────┬───────────────────────────────────────────────────────┐
 * │ 类型              │ 说明                                                  │
 * ├──────────────────┼───────────────────────────────────────────────────────┤
 * │ success          │ 成功操作提示                                          │
 * │ warning          │ 警告信息                                              │
 * │ info             │ 一般信息提示                                          │
 * │ error            │ 错误信息                                              │
 * │ alert            │ 告警通知（特殊样式）                                  │
 * └──────────────────┴───────────────────────────────────────────────────────┘
 * 
 * 【使用示例】
 * ```javascript
 * import NotificationService from '@/services/NotificationService';
 * 
 * // 显示成功消息
 * NotificationService.success('操作成功');
 * 
 * // 显示告警通知
 * NotificationService.alert({
 *   type: 'GEOFENCE_EXIT',
 *   level: 'WARNING',
 *   carName: '京A10001',
 *   message: '车辆已离开围栏'
 * });
 * 
 * // 显示持久通知（需手动关闭）
 * NotificationService.persistent('重要消息', 'error');
 * ```
 * 
 * 【关联文件】
 * - WebSocketService.js: 接收告警消息
 * - MapView.vue: 显示车辆相关通知
 * ═══════════════════════════════════════════════════════════════════════════
 */

import { ElMessage, ElNotification } from 'element-plus';

/**
 * 通知服务类
 */
class NotificationService {
  /**
   * 构造函数
   */
  constructor() {
    /** 通知历史记录 */
    this.notificationHistory = [];
    
    /** 最大历史记录数 */
    this.maxHistorySize = 100;
    
    /** 告警声音是否启用 */
    this.soundEnabled = true;
    
    /** 告警声音URL */
    this.alertSoundUrl = null;
  }

  /**
   * 显示成功消息
   * 
   * @param {string} message - 消息内容
   * @param {number} duration - 显示时长（毫秒），默认3000
   */
  success(message, duration = 3000) {
    ElMessage({
      type: 'success',
      message: message,
      duration: duration,
      showClose: true
    });
    this.addToHistory('success', message);
  }

  /**
   * 显示警告消息
   * 
   * @param {string} message - 消息内容
   * @param {number} duration - 显示时长（毫秒），默认5000
   */
  warning(message, duration = 5000) {
    ElMessage({
      type: 'warning',
      message: message,
      duration: duration,
      showClose: true
    });
    this.addToHistory('warning', message);
  }

  /**
   * 显示信息消息
   * 
   * @param {string} message - 消息内容
   * @param {number} duration - 显示时长（毫秒），默认3000
   */
  info(message, duration = 3000) {
    ElMessage({
      type: 'info',
      message: message,
      duration: duration,
      showClose: true
    });
    this.addToHistory('info', message);
  }

  /**
   * 显示错误消息
   * 
   * @param {string} message - 消息内容
   * @param {number} duration - 显示时长（毫秒），默认5000
   */
  error(message, duration = 5000) {
    ElMessage({
      type: 'error',
      message: message,
      duration: duration,
      showClose: true
    });
    this.addToHistory('error', message);
  }

  /**
   * 显示持久消息（需手动关闭）
   * 
   * @param {string} message - 消息内容
   * @param {string} type - 消息类型
   */
  persistent(message, type = 'info') {
    ElMessage({
      type: type,
      message: message,
      duration: 0, // 不自动关闭
      showClose: true
    });
    this.addToHistory(type, message);
  }

  /**
   * 显示告警通知
   * 
   * 根据告警级别显示不同样式的通知。
   * 严重告警会播放声音提示。
   * 
   * @param {Object} alertData - 告警数据对象
   * @param {string} alertData.alertType - 告警类型
   * @param {string} alertData.alertLevel - 告警级别
   * @param {string} alertData.carName - 车辆名称
   * @param {string} alertData.message - 告警消息
   */
  alert(alertData) {
    const level = alertData.alertLevel || 'WARNING';
    const message = alertData.message || '收到告警通知';
    
    // 根据告警级别确定显示样式
    const type = this.getAlertType(level);
    const duration = this.getAlertDuration(level);
    
    // 严重告警使用通知框
    if (level === 'CRITICAL' || level === 'ERROR') {
      ElNotification({
        title: this.getAlertTitle(alertData.alertType),
        message: message,
        type: type,
        duration: 0, // 需手动关闭
        position: 'top-right',
        dangerouslyUseHTMLString: true
      });
      
      // 播放告警声音
      if (this.soundEnabled && level === 'CRITICAL') {
        this.playAlertSound();
      }
    } else {
      // 普通告警使用消息提示
      ElMessage({
        type: type,
        message: message,
        duration: duration,
        showClose: true
      });
    }
    
    // 添加到历史记录
    this.addToHistory('alert', message, alertData);
  }

  /**
   * 根据告警级别获取消息类型
   * 
   * @param {string} level - 告警级别
   * @returns {string} Element Plus消息类型
   */
  getAlertType(level) {
    const typeMap = {
      'INFO': 'info',
      'WARNING': 'warning',
      'ERROR': 'error',
      'CRITICAL': 'error'
    };
    return typeMap[level] || 'warning';
  }

  /**
   * 根据告警级别获取显示时长
   * 
   * @param {string} level - 告警级别
   * @returns {number} 显示时长（毫秒）
   */
  getAlertDuration(level) {
    const durationMap = {
      'INFO': 3000,
      'WARNING': 5000,
      'ERROR': 8000,
      'CRITICAL': 0 // 需手动关闭
    };
    return durationMap[level] || 5000;
  }

  /**
   * 获取告警标题
   * 
   * @param {string} alertType - 告警类型
   * @returns {string} 告警标题
   */
  getAlertTitle(alertType) {
    const titleMap = {
      'GEOFENCE_ENTER': '🚗 围栏进入告警',
      'GEOFENCE_EXIT': '🚨 围栏离开告警',
      'OVERSPEED': '⚡ 超速告警',
      'DEVICE_OFFLINE': '📱 设备离线告警',
      'LOW_BATTERY': '🔋 低电量告警',
      'EMERGENCY': '🆘 紧急告警'
    };
    return titleMap[alertType] || '⚠️ 系统告警';
  }

  /**
   * 播放告警声音
   */
  playAlertSound() {
    try {
      // 使用浏览器内置的提示音
      const audio = new Audio('data:audio/wav;base64,UklGRnoGAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQoGAACBhYqFbF1fdJivrJBhNjVgodDbq2EcBj+a2teleQsFYLTo8J1lGgU3pOf0uGQVDUGo7/SoWxILQK3x9qVYEgtArfH2pVgSC0Ct8falWBILQK3x9qVYEgtArfH2pVgSC0Ct8falWBILQK3x9qVYEgtArfH2pVgSC0Ct8falWBILQK3x9qVYEgtArfH2pVgSC0Ct8falWBILQK3x9qVYEgtArfH2pVgSC0Ct8falWBILQK3x9qVYEgtArfH2pVgSC0Ct8falWBILQK3x9qVg=');
      audio.volume = 0.5;
      audio.play().catch(e => console.log('播放声音失败:', e));
    } catch (error) {
      console.log('播放告警声音失败:', error);
    }
  }

  /**
   * 设置声音开关
   * 
   * @param {boolean} enabled - 是否启用声音
   */
  setSoundEnabled(enabled) {
    this.soundEnabled = enabled;
  }

  /**
   * 添加到历史记录
   * 
   * @param {string} type - 通知类型
   * @param {string} message - 消息内容
   * @param {Object} data - 附加数据
   */
  addToHistory(type, message, data = null) {
    const record = {
      type: type,
      message: message,
      data: data,
      timestamp: new Date().toISOString()
    };
    
    this.notificationHistory.unshift(record);
    
    // 限制历史记录数量
    if (this.notificationHistory.length > this.maxHistorySize) {
      this.notificationHistory = this.notificationHistory.slice(0, this.maxHistorySize);
    }
  }

  /**
   * 获取通知历史
   * 
   * @param {number} limit - 返回数量限制
   * @returns {Array} 通知历史记录
   */
  getHistory(limit = 20) {
    return this.notificationHistory.slice(0, limit);
  }

  /**
   * 清空历史记录
   */
  clearHistory() {
    this.notificationHistory = [];
  }

  /**
   * 处理WebSocket告警消息
   * 
   * @param {Object} wsMessage - WebSocket消息对象
   */
  handleAlertMessage(wsMessage) {
    if (wsMessage.type === 'ALERT' && wsMessage.data) {
      this.alert(wsMessage.data);
    } else if (wsMessage.type === 'SYSTEM') {
      this.info(wsMessage.message);
    }
  }
}

// 导出单例实例
export default new NotificationService();
