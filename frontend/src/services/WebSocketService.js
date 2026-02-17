/**
 * WebSocket服务 - 实时通信核心服务
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 封装WebSocket连接和消息处理逻辑，提供统一的实时通信接口。
 * 使用STOMP协议进行消息传输，支持SockJS降级。
 * 
 * 【技术架构】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                      WebSocket通信架构                                  │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │                                                                         │
 * │  Vue组件                    WebSocketService                           │
 * │    │                              │                                     │
 * │    │ ── connect() ───────────────▶│ 建立连接                            │
 * │    │                              │                                     │
 * │    │ ── subscribe() ─────────────▶│ 订阅主题                            │
 * │    │                              │                                     │
 * │    │◀── onMessage() ─────────────│ 接收消息回调                        │
 * │    │                              │                                     │
 * │    │ ── send() ─────────────────▶│ 发送消息                            │
 * │    │                              │                                     │
 * │    │ ── disconnect() ───────────▶│ 断开连接                            │
 * │    │                              │                                     │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【使用示例】
 * ```javascript
 * import WebSocketService from '@/services/WebSocketService';
 * 
 * // 连接WebSocket
 * WebSocketService.connect();
 * 
 * // 订阅车辆位置更新
 * WebSocketService.subscribe('/topic/vehicles', (message) => {
 *   console.log('收到车辆更新:', message);
 *   // 更新地图上的车辆位置
 *   updateVehicleMarkers(message.data);
 * });
 * 
 * // 订阅告警消息
 * WebSocketService.subscribe('/topic/alerts', (message) => {
 *   // 显示告警通知
 *   showNotification(message);
 * });
 * 
 * // 断开连接
 * WebSocketService.disconnect();
 * ```
 * 
 * 【消息格式】
 * 所有消息使用统一的WebSocketMessage格式：
 * ```javascript
 * {
 *   type: 'VEHICLE_UPDATE' | 'VEHICLE_LIST' | 'ALERT' | 'SYSTEM' | 'HEARTBEAT',
 *   timestamp: '2024-01-15T10:30:00',
 *   data: { /* 消息数据 *\/ },
 *   message: '消息描述'
 * }
 * ```
 * 
 * 【连接状态】
 * - DISCONNECTED: 未连接
 * - CONNECTING: 连接中
 * - CONNECTED: 已连接
 * - RECONNECTING: 重连中
 * 
 * 【自动重连】
 * 当连接断开时，会自动尝试重连：
 * - 重连间隔: 5秒
 * - 最大重连次数: 10次
 * - 重连成功后自动恢复订阅
 * 
 * 【关联文件】
 * - WebSocketConfig.java: 后端WebSocket配置
 * - MapView.vue: 使用此服务接收车辆位置更新
 * ═══════════════════════════════════════════════════════════════════════════
 */

import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

/**
 * WebSocket连接状态枚举
 */
const ConnectionState = {
  DISCONNECTED: 'DISCONNECTED',
  CONNECTING: 'CONNECTING',
  CONNECTED: 'CONNECTED',
  RECONNECTING: 'RECONNECTING'
};

/**
 * WebSocket服务类
 * 提供连接管理、消息订阅、消息发送等功能
 */
class WebSocketService {
  /**
   * 构造函数
   * 初始化服务配置和状态
   */
  constructor() {
    /** WebSocket服务器地址 */
    this.serverUrl = 'http://localhost:8081/ws';
    
    /** STOMP客户端实例 */
    this.client = null;
    
    /** 当前连接状态 */
    this.connectionState = ConnectionState.DISCONNECTED;
    
    /** 订阅列表 - 存储所有活跃的订阅 */
    this.subscriptions = new Map();
    
    /** 重连计数器 */
    this.reconnectAttempts = 0;
    
    /** 最大重连次数 */
    this.maxReconnectAttempts = 10;
    
    /** 重连间隔（毫秒） */
    this.reconnectInterval = 5000;
    
    /** 心跳间隔（毫秒） */
    this.heartbeatInterval = 30000;
    
    /** 心跳定时器 */
    this.heartbeatTimer = null;
    
    /** 状态变更回调函数列表 */
    this.stateCallbacks = [];
    
    /** 连接成功回调 */
    this.onConnectCallback = null;
    
    /** 断开连接回调 */
    this.onDisconnectCallback = null;
    
    /** 错误回调 */
    this.onErrorCallback = null;
  }

  /**
   * 建立WebSocket连接
   * 
   * 创建STOMP客户端并连接到服务器。
   * 连接成功后会自动发送订阅请求。
   * 
   * @param {Function} onConnect - 连接成功回调
   * @param {Function} onDisconnect - 断开连接回调
   * @param {Function} onError - 错误回调
   */
  connect(onConnect = null, onDisconnect = null, onError = null) {
    // 保存回调函数
    this.onConnectCallback = onConnect;
    this.onDisconnectCallback = onDisconnect;
    this.onErrorCallback = onError;
    
    // 如果已经连接，直接返回
    if (this.connectionState === ConnectionState.CONNECTED) {
      console.log('[WebSocket] 已经连接，无需重复连接');
      return;
    }
    
    // 更新连接状态
    this.updateConnectionState(ConnectionState.CONNECTING);
    
    console.log('[WebSocket] 正在连接...', this.serverUrl);
    
    // 创建STOMP客户端
    this.client = new Client({
      // 使用SockJS作为WebSocket传输层
      webSocketFactory: () => new SockJS(this.serverUrl),
      
      // 连接超时时间
      connectionTimeout: 10000,
      
      // 心跳配置
      heartbeatIncoming: 20000,
      heartbeatOutgoing: 20000,
      
      // 重连延迟
      reconnectDelay: 0, // 禁用自动重连，使用自定义重连逻辑
      
      // 连接成功回调
      onConnect: () => {
        this.onConnect();
      },
      
      // 连接错误回调
      onStompError: (frame) => {
        this.onError(frame);
      },
      
      // WebSocket错误回调
      onWebSocketError: (event) => {
        console.error('[WebSocket] WebSocket错误:', event);
        if (this.onErrorCallback) {
          this.onErrorCallback(event);
        }
      },
      
      // WebSocket关闭回调
      onWebSocketClose: (event) => {
        this.onDisconnect(event);
      }
    });
    
    // 激活连接
    this.client.activate();
  }

  /**
   * 连接成功处理
   */
  onConnect() {
    console.log('[WebSocket] 连接成功');
    
    // 更新连接状态
    this.updateConnectionState(ConnectionState.CONNECTED);
    
    // 重置重连计数
    this.reconnectAttempts = 0;
    
    // 恢复之前的订阅
    this.restoreSubscriptions();
    
    // 发送订阅请求
    this.sendSubscriptionRequest();
    
    // 启动心跳
    this.startHeartbeat();
    
    // 调用连接成功回调
    if (this.onConnectCallback) {
      this.onConnectCallback();
    }
  }

  /**
   * 断开连接处理
   * @param {Object} event - 关闭事件
   */
  onDisconnect(event) {
    console.log('[WebSocket] 连接断开:', event);
    
    // 更新连接状态
    this.updateConnectionState(ConnectionState.DISCONNECTED);
    
    // 停止心跳
    this.stopHeartbeat();
    
    // 尝试重连
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnect();
    }
    
    // 调用断开连接回调
    if (this.onDisconnectCallback) {
      this.onDisconnectCallback(event);
    }
  }

  /**
   * 错误处理
   * @param {Object} frame - STOMP错误帧
   */
  onError(frame) {
    console.error('[WebSocket] STOMP错误:', frame);
    
    // 调用错误回调
    if (this.onErrorCallback) {
      this.onErrorCallback(frame);
    }
  }

  /**
   * 重新连接
   */
  reconnect() {
    this.reconnectAttempts++;
    this.updateConnectionState(ConnectionState.RECONNECTING);
    
    console.log(`[WebSocket] 尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`);
    
    setTimeout(() => {
      if (this.connectionState !== ConnectionState.CONNECTED) {
        this.connect(this.onConnectCallback, this.onDisconnectCallback, this.onErrorCallback);
      }
    }, this.reconnectInterval);
  }

  /**
   * 订阅主题
   * 
   * @param {string} destination - 订阅目标地址（如 '/topic/vehicles'）
   * @param {Function} callback - 消息回调函数
   * @returns {Object} 订阅对象，可用于取消订阅
   */
  subscribe(destination, callback) {
    // 如果未连接，先存储订阅信息
    if (this.connectionState !== ConnectionState.CONNECTED) {
      console.log('[WebSocket] 未连接，订阅信息已缓存');
      this.subscriptions.set(destination, { callback, subscription: null });
      return null;
    }
    
    // 执行订阅
    const subscription = this.client.subscribe(destination, (message) => {
      try {
        // 解析消息体
        const body = JSON.parse(message.body);
        // 调用回调函数
        callback(body);
      } catch (error) {
        console.error('[WebSocket] 消息解析错误:', error);
      }
    });
    
    // 存储订阅信息
    this.subscriptions.set(destination, { callback, subscription });
    
    console.log(`[WebSocket] 已订阅: ${destination}`);
    
    return subscription;
  }

  /**
   * 取消订阅
   * 
   * @param {string} destination - 订阅目标地址
   */
  unsubscribe(destination) {
    const subInfo = this.subscriptions.get(destination);
    if (subInfo && subInfo.subscription) {
      subInfo.subscription.unsubscribe();
      this.subscriptions.delete(destination);
      console.log(`[WebSocket] 已取消订阅: ${destination}`);
    }
  }

  /**
   * 恢复之前的订阅
   * 在重连成功后调用
   */
  restoreSubscriptions() {
    if (this.subscriptions.size === 0) return;
    
    console.log('[WebSocket] 恢复订阅...');
    
    this.subscriptions.forEach((subInfo, destination) => {
      if (!subInfo.subscription) {
        this.subscribe(destination, subInfo.callback);
      }
    });
  }

  /**
   * 发送消息
   * 
   * @param {string} destination - 目标地址
   * @param {Object} body - 消息体
   */
  send(destination, body = {}) {
    if (this.connectionState !== ConnectionState.CONNECTED) {
      console.warn('[WebSocket] 未连接，无法发送消息');
      return;
    }
    
    this.client.publish({
      destination: destination,
      body: JSON.stringify(body)
    });
    
    console.log(`[WebSocket] 消息已发送: ${destination}`, body);
  }

  /**
   * 发送订阅请求
   * 通知服务端客户端已准备好接收消息
   */
  sendSubscriptionRequest() {
    this.send('/app/subscribe', {
      clientId: 'web-client-' + Date.now(),
      topics: ['vehicles', 'alerts']
    });
  }

  /**
   * 启动心跳检测
   */
  startHeartbeat() {
    this.stopHeartbeat();
    
    this.heartbeatTimer = setInterval(() => {
      if (this.connectionState === ConnectionState.CONNECTED) {
        this.send('/app/heartbeat', { timestamp: Date.now() });
      }
    }, this.heartbeatInterval);
  }

  /**
   * 停止心跳检测
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  /**
   * 断开WebSocket连接
   */
  disconnect() {
    if (this.client) {
      // 停止心跳
      this.stopHeartbeat();
      
      // 取消所有订阅
      this.subscriptions.forEach((subInfo, destination) => {
        this.unsubscribe(destination);
      });
      
      // 断开连接
      this.client.deactivate();
      this.client = null;
      
      // 更新状态
      this.updateConnectionState(ConnectionState.DISCONNECTED);
      
      console.log('[WebSocket] 已断开连接');
    }
  }

  /**
   * 更新连接状态
   * 并通知所有状态监听器
   * 
   * @param {string} state - 新的连接状态
   */
  updateConnectionState(state) {
    this.connectionState = state;
    
    // 通知状态监听器
    this.stateCallbacks.forEach(callback => callback(state));
  }

  /**
   * 添加状态变更监听器
   * 
   * @param {Function} callback - 状态变更回调
   */
  onStateChange(callback) {
    this.stateCallbacks.push(callback);
  }

  /**
   * 移除状态变更监听器
   * 
   * @param {Function} callback - 要移除的回调
   */
  offStateChange(callback) {
    const index = this.stateCallbacks.indexOf(callback);
    if (index > -1) {
      this.stateCallbacks.splice(index, 1);
    }
  }

  /**
   * 获取当前连接状态
   * 
   * @returns {string} 连接状态
   */
  getState() {
    return this.connectionState;
  }

  /**
   * 检查是否已连接
   * 
   * @returns {boolean} 是否已连接
   */
  isConnected() {
    return this.connectionState === ConnectionState.CONNECTED;
  }
}

// 导出单例实例
export default new WebSocketService();

// 导出连接状态枚举
export { ConnectionState };
