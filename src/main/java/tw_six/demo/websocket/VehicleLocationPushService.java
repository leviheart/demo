package tw_six.demo.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import tw_six.demo.entity.CarLocation;
import tw_six.demo.service.CarLocationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 车辆位置推送服务 - WebSocket实时推送核心服务
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第一章：WebSocket 技术概述】
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * 1.1 什么是 WebSocket？
 * ─────────────────────────────────────────────────────────────────────────────
 * WebSocket 是一种在单个TCP连接上进行全双工通信的协议。
 * 
 * 【与传统HTTP的区别】
 * 
 * HTTP（请求-响应模式）：
 * ┌─────────┐                    ┌─────────┐
 * │  客户端   │ ──── 请求 ────────▶ │  服务器   │
 * │         │ ◀─── 响应 ───────── │         │
 * └─────────┘                    └─────────┘
 * 
 * 特点：
 * - 单向：客户端发起，服务器响应
 * - 短连接：请求完成后连接关闭
 * - 无法主动推送：服务器不能主动发消息给客户端
 * 
 * WebSocket（全双工模式）：
 * ┌─────────┐  ════════════════  ┌─────────┐
 * │  客户端   │  双向实时通信通道   │  服务器   │
 * │         │  ════════════════  │         │
 * └─────────┘                    └─────────┘
 * 
 * 特点：
 * - 双向：客户端和服务器都可以主动发消息
 * - 长连接：连接保持打开
 * - 实时推送：服务器可以主动推送消息
 * 
 * 1.2 WebSocket 的应用场景
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * 【适合场景】
 * ✅ 实时聊天：即时消息传递
 * ✅ 实时监控：车辆位置、设备状态
 * ✅ 协作编辑：多人在线文档
 * ✅ 在线游戏：实时对战
 * ✅ 股票行情：实时价格推送
 * ✅ 体育直播：实时比分更新
 * 
 * 【不适合场景】
 * ❌ 简单的数据查询：HTTP更简单
 * ❌ 文件上传下载：HTTP更适合
 * ❌ 偶尔的通信：保持连接浪费资源
 * 
 * 1.3 STOMP 协议
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * STOMP = Simple Text Oriented Messaging Protocol（简单文本消息协议）
 * 
 * 【为什么使用 STOMP？】
 * WebSocket 只是传输通道，不定义消息格式。
 * STOMP 在 WebSocket 之上提供了消息协议：
 * 
 * 1. 订阅/发布模式：
 *    - 客户端订阅某个"主题"（Topic）
 *    - 服务器向该主题发布消息
 *    - 所有订阅者都会收到消息
 * 
 * 2. 消息格式：
 *    COMMAND
 *    header1:value1
 *    header2:value2
 *    
 *    Body^@
 * 
 * 【STOMP 常用命令】
 * ┌──────────────┬─────────────────────────────────────────────────────────┐
 * │ 命令          │ 说明                                                    │
 * ├──────────────┼─────────────────────────────────────────────────────────┤
 * │ CONNECT      │ 建立连接                                                │
 * │ SUBSCRIBE    │ 订阅主题                                                │
 * │ UNSUBSCRIBE  │ 取消订阅                                                │
 * │ SEND         │ 发送消息                                                │
 * │ MESSAGE      │ 接收消息（服务器推送）                                    │
 * │ DISCONNECT   │ 断开连接                                                │
 * └──────────────┴─────────────────────────────────────────────────────────┘
 * 
 * 1.4 Spring WebSocket 架构
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 *   ┌─────────────────────────────────────────────────────────────────────┐
 *   │                          前端                                        │
 *   │  ┌─────────────────────────────────────────────────────────────┐   │
 *   │  │ SockJS (WebSocket降级方案)                                    │   │
 *   │  │ STOMP.js (STOMP客户端)                                        │   │
 *   │  └─────────────────────────────────────────────────────────────┘   │
 *   └─────────────────────────────────────────────────────────────────────┘
 *                                    │
 *                                    │ WebSocket连接
 *                                    ▼
 *   ┌─────────────────────────────────────────────────────────────────────┐
 *   │                          后端                                        │
 *   │  ┌─────────────────────────────────────────────────────────────┐   │
 *   │  │ WebSocketConfig (@EnableWebSocketMessageBroker)              │   │
 *   │  │   - 配置消息代理                                              │   │
 *   │  │   - 配置STOMP端点                                             │   │
 *   │  └─────────────────────────────────────────────────────────────┘   │
 *   │                                │                                    │
 *   │                                ▼                                    │
 *   │  ┌─────────────────────────────────────────────────────────────┐   │
 *   │  │ SimpMessagingTemplate (消息发送模板)                          │   │
 *   │  │   - convertAndSend() 发送消息                                 │   │
 *   │  └─────────────────────────────────────────────────────────────┘   │
 *   └─────────────────────────────────────────────────────────────────────┘
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第二章：核心注解详解】
 * ═══════════════════════════════════════════════════════════════════════════════
 */

/**
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ @Service 注解详解                                                          │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * 2.1 @Service 是什么？
 * ─────────────────────────────────────────────────────────────────────────────
 * @Service 是 @Component 的特化版本，用于标记服务层组件。
 * 
 * 【源码定义】
 * @Target(ElementType.TYPE)
 * @Retention(RetentionPolicy.RUNTIME)
 * @Documented
 * @Component  // 本质上是一个@Component
 * public @interface Service { ... }
 * 
 * 【为什么要用 @Service 而不是 @Component？】
 * 1. 语义清晰：一眼就知道这是服务层
 * 2. 分层架构：配合@Controller、@Repository使用
 * 3. AOP切面：可以针对Service层做特殊处理
 * 4. 异常处理：Spring对@Service有特殊的异常转换
 */
@Service

/**
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ @EnableScheduling 注解详解                                                 │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * 2.2 @EnableScheduling 是什么？
 * ─────────────────────────────────────────────────────────────────────────────
 * @EnableScheduling 用于启用Spring的定时任务功能。
 * 
 * 【源码定义】
 * @Target(ElementType.TYPE)
 * @Retention(RetentionPolicy.RUNTIME)
 * @Import(SchedulingConfiguration.class)
 * @Documented
 * public @interface EnableScheduling { ... }
 * 
 * 【作用】
 * 1. 导入SchedulingConfiguration配置类
 * 2. 注册ScheduledAnnotationBeanPostProcessor
 * 3. 扫描所有@Scheduled注解的方法
 * 4. 创建定时任务执行器
 * 
 * 【工作原理】
 * Spring容器启动时：
 * 1. 扫描所有带@Scheduled注解的方法
 * 2. 为每个方法创建一个定时任务
 * 3. 提交到TaskScheduler执行
 */
@EnableScheduling
public class VehicleLocationPushService {

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ SimpMessagingTemplate 详解                                              │
     * └────────────────────────────────────────────────────────────────────────┘
     * 
     * 2.3 SimpMessagingTemplate 是什么？
     * ─────────────────────────────────────────────────────────────────────────
     * SimpMessagingTemplate 是Spring提供的消息发送模板，
     * 用于向WebSocket客户端发送消息。
     * 
     * 【继承关系】
     * SimpMessagingTemplate extends SimpMessageSendingOperations
     * 
     * 【核心方法】
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │ 方法                          │ 说明                                  │
     * ├─────────────────────────────────────────────────────────────────────┤
     * │ convertAndSend(dest, msg)    │ 发送消息到指定目的地                   │
     * │ convertAndSendToUser(user, dest, msg) │ 发送消息给特定用户           │
     * │ convertAndSend(dest, msg, headers) │ 带消息头发送                    │
     * └─────────────────────────────────────────────────────────────────────┘
     * 
     * 【目的地格式】
     * /topic/xxx    - 广播消息，所有订阅者都能收到
     * /queue/xxx    - 点对点消息，只有特定用户收到
     * /user/queue/xxx - 用户专属消息
     * 
     * 【使用示例】
     * // 广播消息
     * messagingTemplate.convertAndSend("/topic/vehicles", message);
     * 
     * // 发送给特定用户
     * messagingTemplate.convertAndSendToUser("userId", "/queue/notifications", message);
     */
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * 车辆位置服务 - 用于获取车辆数据
     */
    private final CarLocationService carLocationService;
    
    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ DateTimeFormatter 详解                                                  │
     * └────────────────────────────────────────────────────────────────────────┘
     * 
     * 2.4 DateTimeFormatter 是什么？
     * ─────────────────────────────────────────────────────────────────────────
     * DateTimeFormatter 是Java 8引入的日期时间格式化器。
     * 
     * 【为什么用 DateTimeFormatter 而不是 SimpleDateFormat？】
     * 1. 线程安全：DateTimeFormatter是不可变的，线程安全
     * 2. SimpleDateFormat是线程不安全的，多线程下会出错
     * 3. 更好的API设计
     * 
     * 【格式化符号】
     * ┌─────────┬─────────────────────────────────────────────────────────────┐
     * │ 符号      │ 含义                                                        │
     * ├─────────┼─────────────────────────────────────────────────────────────┤
     * │ yyyy    │ 四位年份                                                    │
     * │ MM      │ 两位月份                                                    │
     * │ dd      │ 两位日期                                                    │
     * │ HH      │ 24小时制小时                                                │
     * │ mm      │ 分钟                                                        │
     * │ ss      │ 秒                                                          │
     * └─────────┴─────────────────────────────────────────────────────────────┘
     * 
     * 【使用示例】
     * DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
     * String formatted = LocalDateTime.now().format(formatter);
     * // 结果: "2024-01-15 10:30:00"
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ AtomicInteger 详解                                                      │
     * └────────────────────────────────────────────────────────────────────────┘
     * 
     * 2.5 AtomicInteger 是什么？
     * ─────────────────────────────────────────────────────────────────────────
     * AtomicInteger 是一个提供原子操作的整数类。
     * 
     * 【为什么不用 int？】
     * 普通int在多线程环境下不安全：
     * 
     * // 线程不安全的操作
     * int count = 0;
     * count++;  // 不是原子操作！
     * 
     * count++ 实际上是三步操作：
     * 1. 读取count的值
     * 2. 加1
     * 3. 写回count
     * 
     * 多线程下可能发生：
     * 线程A: 读取count=0
     * 线程B: 读取count=0  (在A写回之前)
     * 线程A: 写回count=1
     * 线程B: 写回count=1  (应该是2！)
     * 
     * 【AtomicInteger 的原子操作】
     * AtomicInteger count = new AtomicInteger(0);
     * count.incrementAndGet();  // 原子操作，线程安全
     * 
     * 【常用方法】
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │ 方法                    │ 说明                                      │
     * ├─────────────────────────────────────────────────────────────────────┤
     * │ get()                  │ 获取当前值                                │
     * │ set(int)               │ 设置值                                    │
     * │ incrementAndGet()      │ 原子自增并返回新值                         │
     * │ getAndIncrement()      │ 原子自增并返回旧值                         │
     * │ decrementAndGet()      │ 原子自减并返回新值                         │
     * │ compareAndSet(exp, upd)│ CAS操作，如果当前值等于exp则更新为upd       │
     * └─────────────────────────────────────────────────────────────────────┘
     * 
     * 【实现原理】
     * AtomicInteger 使用 CAS（Compare And Swap）实现原子操作：
     * 1. 读取当前值
     * 2. 计算新值
     * 3. 如果当前值还是原来的值，则更新
     * 4. 否则重试
     */
    private final AtomicInteger pushCount = new AtomicInteger(0);
    
    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ volatile 关键字详解                                                     │
     * └────────────────────────────────────────────────────────────────────────┘
     * 
     * 2.6 volatile 是什么？
     * ─────────────────────────────────────────────────────────────────────────
     * volatile 是Java的关键字，用于保证变量的可见性。
     * 
     * 【Java内存模型问题】
     * 在多线程环境下，每个线程有自己的工作内存（缓存）：
     * 
     *   线程A工作内存        主内存         线程B工作内存
     *   ┌─────────┐      ┌─────────┐      ┌─────────┐
     *   │ x = 0   │ ◀─── │ x = 0   │ ───▶ │ x = 0   │
     *   └─────────┘      └─────────┘      └─────────┘
     * 
     * 线程A修改x=1，可能只更新了自己的工作内存，
     * 线程B看不到这个变化，仍然读取x=0。
     * 
     * 【volatile 的作用】
     * 1. 可见性：一个线程修改后，其他线程立即可见
     * 2. 禁止指令重排序：防止编译器优化导致的顺序问题
     * 
     * 【volatile vs synchronized】
     * ┌───────────────┬─────────────────────────────────────────────────────┐
     * │ 方面           │ volatile                    │ synchronized         │
     * ├───────────────┼─────────────────────────────────────────────────────┤
     * │ 可见性         │ ✅ 保证                      │ ✅ 保证               │
     * │ 原子性         │ ❌ 不保证                    │ ✅ 保证               │
     * │ 性能           │ 高                          │ 较低                  │
     * │ 适用场景       │ 简单状态标志                 │ 复杂操作              │
     * └───────────────┴─────────────────────────────────────────────────────┘
     * 
     * 【使用场景】
     * - 状态标志：如 running = false
     * - 双重检查锁定：单例模式
     * - 只读变量的缓存
     */
    private volatile LocalDateTime lastPushTime;
    
    /**
     * 是否启用模拟数据 - 当数据库无数据时使用
     */
    private boolean useSimulation = true;
    
    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ CopyOnWriteArrayList 详解                                               │
     * └────────────────────────────────────────────────────────────────────────┘
     * 
     * 2.7 CopyOnWriteArrayList 是什么？
     * ─────────────────────────────────────────────────────────────────────────
     * CopyOnWriteArrayList 是线程安全的List实现。
     * 
     * 【为什么不用 ArrayList？】
     * ArrayList 不是线程安全的：
     * - 多线程同时添加元素可能导致数据丢失
     * - 多线程同时遍历可能抛出ConcurrentModificationException
     * 
     * 【CopyOnWriteArrayList 工作原理】
     * "写时复制"：
     * 1. 读操作：直接读取，不需要加锁
     * 2. 写操作：复制整个数组，修改后替换引用
     * 
     * 【适用场景】
     * - 读多写少
     * - 遍历操作远多于修改操作
     * 
     * 【优缺点】
     * ✅ 读操作高性能，不需要加锁
     * ✅ 遍历时不会抛出ConcurrentModificationException
     * ❌ 写操作需要复制整个数组，性能较低
     * ❌ 内存占用较高
     */
    private final List<VehicleLocationData> simulatedVehicles = new java.util.concurrent.CopyOnWriteArrayList<>();
    
    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ ConcurrentHashMap 详解                                                  │
     * └────────────────────────────────────────────────────────────────────────┘
     * 
     * 2.8 ConcurrentHashMap 是什么？
     * ─────────────────────────────────────────────────────────────────────────
     * ConcurrentHashMap 是线程安全的HashMap实现。
     * 
     * 【为什么不用 HashMap？】
     * HashMap 不是线程安全的：
     * - 多线程put可能导致数据丢失
     * - 扩容时可能导致死循环（JDK7）
     * 
     * 【为什么不用 Hashtable？】
     * Hashtable 是线程安全的，但性能差：
     * - 所有方法都加synchronized
     * - 同一时间只有一个线程能操作
     * 
     * 【ConcurrentHashMap 工作原理】
     * JDK 8+ 使用 CAS + synchronized：
     * 
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │ 桶0    │ 桶1    │ 桶2    │ 桶3    │ ... │ 桶15    │               │
     * └─────────────────────────────────────────────────────────────────────┘
     *     │        │        │        │              │
     *     ▼        ▼        ▼        ▼              ▼
     *   链表     链表     红黑树    链表           链表
     * 
     * - 每个桶独立加锁，不同桶可以并发操作
     * - 使用CAS进行无锁插入
     * - 只有哈希冲突时才加synchronized
     * 
     * 【常用方法】
     * map.put(key, value);           // 添加元素
     * map.get(key);                  // 获取元素
     * map.putIfAbsent(key, value);   // 如果不存在则添加
     * map.computeIfAbsent(key, k -> new Value());  // 如果不存在则计算并添加
     */
    private final Map<String, VehicleRouteState> vehicleRouteStates = new ConcurrentHashMap<>();
    
    /**
     * 预定义路线 - 与前端MapView.vue保持一致
     */
    private final Map<String, double[][]> vehicleRoutes = new HashMap<>();

    /**
     * 构造函数 - 依赖注入
     * 
     * @param messagingTemplate WebSocket消息发送模板
     * @param carLocationService 车辆位置服务
     */
    @Autowired
    public VehicleLocationPushService(SimpMessagingTemplate messagingTemplate,
            CarLocationService carLocationService) {
        this.messagingTemplate = messagingTemplate;
        this.carLocationService = carLocationService;
        
        initRoutes();
        initSimulatedVehicles();
    }

    /**
     * ══════════════════════════════════════════════════════════════════════════
     * 【第三章：定时任务详解】
     * ══════════════════════════════════════════════════════════════════════════
     */
    
    /**
     * 初始化预定义路线
     * 路线坐标格式: [经度, 纬度]
     * 
     * 3.1 地理坐标系统
     * ─────────────────────────────────────────────────────────────────────────
     * 
     * 【经度 Longitude】
     * - 范围：-180 到 180
     * - 东经为正，西经为负
     * - 中国大约在 73°E 到 135°E
     * 
     * 【纬度 Latitude】
     * - 范围：-90 到 90
     * - 北纬为正，南纬为负
     * - 中国大约在 4°N 到 53°N
     * 
     * 【坐标格式】
     * double[] point = {118.7500, 32.0650};  // {经度, 纬度}
     * 
     * 【南京坐标范围】
     * 经度：118.3 ~ 119.2
     * 纬度：31.1 ~ 32.6
     */
    private void initRoutes() {
        /**
         * 内环线A - 京A10001
         * 环绕市中心的环线，顺时针方向
         */
        vehicleRoutes.put("京A10001", new double[][] {
            {118.7500, 32.0650}, {118.7550, 32.0680}, {118.7620, 32.0700},
            {118.7700, 32.0690}, {118.7780, 32.0660}, {118.7830, 32.0600},
            {118.7820, 32.0540}, {118.7760, 32.0500}, {118.7680, 32.0480},
            {118.7600, 32.0500}, {118.7540, 32.0550}, {118.7500, 32.0600},
            {118.7500, 32.0650}
        });
        
        /**
         * 内环线B - 京A10002
         * 与内环线A相同路线，逆时针方向
         */
        vehicleRoutes.put("京A10002", new double[][] {
            {118.7800, 32.0550}, {118.7760, 32.0500}, {118.7680, 32.0480},
            {118.7600, 32.0500}, {118.7540, 32.0550}, {118.7500, 32.0600},
            {118.7500, 32.0650}, {118.7550, 32.0680}, {118.7620, 32.0700},
            {118.7700, 32.0690}, {118.7780, 32.0660}, {118.7830, 32.0600},
            {118.7800, 32.0550}
        });
        
        /**
         * 外环线A - 京B20001
         * 外围环线，范围更大
         */
        vehicleRoutes.put("京B20001", new double[][] {
            {118.7650, 32.0700}, {118.7580, 32.0720}, {118.7500, 32.0710},
            {118.7450, 32.0650}, {118.7440, 32.0550}, {118.7480, 32.0480},
            {118.7580, 32.0440}, {118.7700, 32.0450}, {118.7820, 32.0500},
            {118.7880, 32.0580}, {118.7850, 32.0680}, {118.7750, 32.0720},
            {118.7650, 32.0700}
        });
        
        /**
         * 外环线B - 京B20002
         * 外围环线，逆时针方向
         */
        vehicleRoutes.put("京B20002", new double[][] {
            {118.7700, 32.0450}, {118.7820, 32.0500}, {118.7880, 32.0580},
            {118.7850, 32.0680}, {118.7750, 32.0720}, {118.7650, 32.0700},
            {118.7580, 32.0720}, {118.7500, 32.0710}, {118.7450, 32.0650},
            {118.7440, 32.0550}, {118.7480, 32.0480}, {118.7580, 32.0440},
            {118.7700, 32.0450}
        });
        
        /**
         * 南北干线A - 沪C30001
         * 南北方向主干道，往返行驶
         */
        vehicleRoutes.put("沪C30001", new double[][] {
            {118.7550, 32.0450}, {118.7550, 32.0500}, {118.7550, 32.0550},
            {118.7550, 32.0600}, {118.7550, 32.0650}, {118.7550, 32.0700},
            {118.7550, 32.0750}, {118.7550, 32.0700}, {118.7550, 32.0650},
            {118.7550, 32.0600}, {118.7550, 32.0550}, {118.7550, 32.0500},
            {118.7550, 32.0450}
        });
        
        /**
         * 南北干线B - 沪C30002
         * 南北方向主干道，另一条线路
         */
        vehicleRoutes.put("沪C30002", new double[][] {
            {118.7750, 32.0750}, {118.7750, 32.0700}, {118.7750, 32.0650},
            {118.7750, 32.0600}, {118.7750, 32.0550}, {118.7750, 32.0500},
            {118.7750, 32.0450}, {118.7750, 32.0500}, {118.7750, 32.0550},
            {118.7750, 32.0600}, {118.7750, 32.0650}, {118.7750, 32.0700},
            {118.7750, 32.0750}
        });
        
        /**
         * 东西干线A - 沪D40001
         * 东西方向主干道，往返行驶
         */
        vehicleRoutes.put("沪D40001", new double[][] {
            {118.7400, 32.0620}, {118.7450, 32.0620}, {118.7500, 32.0620},
            {118.7550, 32.0620}, {118.7600, 32.0620}, {118.7650, 32.0620},
            {118.7700, 32.0620}, {118.7750, 32.0620}, {118.7800, 32.0620},
            {118.7850, 32.0620}, {118.7800, 32.0620}, {118.7750, 32.0620},
            {118.7700, 32.0620}, {118.7650, 32.0620}, {118.7600, 32.0620},
            {118.7550, 32.0620}, {118.7500, 32.0620}, {118.7450, 32.0620},
            {118.7400, 32.0620}
        });
        
        /**
         * 东西干线B - 沪D40002
         * 东西方向主干道，另一条线路
         */
        vehicleRoutes.put("沪D40002", new double[][] {
            {118.7850, 32.0580}, {118.7800, 32.0580}, {118.7750, 32.0580},
            {118.7700, 32.0580}, {118.7650, 32.0580}, {118.7600, 32.0580},
            {118.7550, 32.0580}, {118.7500, 32.0580}, {118.7450, 32.0580},
            {118.7500, 32.0580}, {118.7550, 32.0580}, {118.7600, 32.0580},
            {118.7650, 32.0580}, {118.7700, 32.0580}, {118.7750, 32.0580},
            {118.7800, 32.0580}, {118.7850, 32.0580}
        });
        
        /**
         * 配送区A - 粤E50001
         * 配送区域内的巡游路线
         */
        vehicleRoutes.put("粤E50001", new double[][] {
            {118.7580, 32.0720}, {118.7620, 32.0720}, {118.7660, 32.0720},
            {118.7660, 32.0680}, {118.7660, 32.0640}, {118.7620, 32.0640},
            {118.7580, 32.0640}, {118.7580, 32.0680}, {118.7580, 32.0720}
        });
        
        /**
         * 配送区B - 粤E50002
         * 配送区域内的另一条巡游路线
         */
        vehicleRoutes.put("粤E50002", new double[][] {
            {118.7720, 32.0480}, {118.7680, 32.0480}, {118.7640, 32.0480},
            {118.7640, 32.0520}, {118.7640, 32.0560}, {118.7680, 32.0560},
            {118.7720, 32.0560}, {118.7720, 32.0520}, {118.7720, 32.0480}
        });
        
        /**
         * 商业区巡游A - 苏F60001
         * 商业区内的巡游路线
         */
        vehicleRoutes.put("苏F60001", new double[][] {
            {118.7680, 32.0635}, {118.7720, 32.0650}, {118.7700, 32.0680},
            {118.7660, 32.0670}, {118.7620, 32.0650}, {118.7600, 32.0620},
            {118.7620, 32.0590}, {118.7660, 32.0600}, {118.7700, 32.0610},
            {118.7680, 32.0635}
        });
        
        /**
         * 商业区巡游B - 苏F60002
         * 商业区内的另一条巡游路线
         */
        vehicleRoutes.put("苏F60002", new double[][] {
            {118.7620, 32.0570}, {118.7580, 32.0550}, {118.7560, 32.0520},
            {118.7600, 32.0500}, {118.7640, 32.0510}, {118.7680, 32.0530},
            {118.7660, 32.0560}, {118.7640, 32.0580}, {118.7620, 32.0570}
        });
    }

    /**
     * 初始化模拟车辆数据
     * 当数据库中没有真实数据时，使用模拟数据进行演示
     */
    private void initSimulatedVehicles() {
        String[] carNames = {
            "京A10001", "京A10002", "京B20001", "京B20002",
            "沪C30001", "沪C30002", "沪D40001", "沪D40002",
            "粤E50001", "粤E50002", "苏F60001", "苏F60002"
        };
        
        String[] groupNames = {
            "华北区车队", "华北区车队", "华北区车队", "华北区车队",
            "华东区车队", "华东区车队", "华东区车队", "华东区车队",
            "华南区车队", "华南区车队", "华南区车队", "华南区车队"
        };
        
        String[] routeNames = {
            "内环线A", "内环线B", "外环线A", "外环线B",
            "南北干线A", "南北干线B", "东西干线A", "东西干线B",
            "配送区A", "配送区B", "商业区巡游A", "商业区巡游B"
        };
        
        String[] routeColors = {
            "#1890ff", "#52c41a", "#fa8c16", "#f5222d",
            "#722ed1", "#13c2c2", "#eb2f96", "#faad14",
            "#2f54eb", "#f5222d", "#1890ff", "#52c41a"
        };
        
        for (int i = 0; i < carNames.length; i++) {
            String carName = carNames[i];
            double[][] route = vehicleRoutes.get(carName);
            
            if (route != null && route.length > 0) {
                VehicleLocationData vehicle = new VehicleLocationData();
                vehicle.setId((long) (i + 1));
                vehicle.setCarName(carName);
                vehicle.setLatitude(route[0][1]);
                vehicle.setLongitude(route[0][0]);
                vehicle.setSpeed(35 + Math.random() * 25);
                vehicle.setDirection(0.0);
                vehicle.setStatus("行驶中");
                vehicle.setGroupName(groupNames[i]);
                vehicle.setRouteName(routeNames[i]);
                vehicle.setRouteColor(routeColors[i]);
                vehicle.setLastUpdate(LocalDateTime.now().format(TIME_FORMATTER));
                
                simulatedVehicles.add(vehicle);
                
                VehicleRouteState state = new VehicleRouteState();
                state.route = route;
                state.currentIndex = 0;
                state.progress = 0.0;
                state.speed = 35 + Math.random() * 25;
                vehicleRouteStates.put(carName, state);
            }
        }
    }

    /**
     * ══════════════════════════════════════════════════════════════════════════
     * 【第四章：@Scheduled 定时任务详解】
     * ══════════════════════════════════════════════════════════════════════════
     */
    
    /**
     * 定时推送车辆位置 - 核心推送方法
     * 
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ @Scheduled 注解详解                                                     │
     * └────────────────────────────────────────────────────────────────────────┘
     * 
     * 4.1 @Scheduled 是什么？
     * ─────────────────────────────────────────────────────────────────────────
     * @Scheduled 用于声明一个方法是定时任务。
     * 
     * 【源码定义】
     * @Target(ElementType.METHOD)
     * @Retention(RetentionPolicy.RUNTIME)
     * @Documented
     * public @interface Scheduled {
     *     String cron() default "";           // Cron表达式
     *     String zone() default "";           // 时区
     *     long fixedDelay() default -1;       // 固定延迟
     *     long fixedRate() default -1;        // 固定频率
     *     long initialDelay() default -1;     // 初始延迟
     * }
     * 
     * 4.2 定时任务配置方式
     * ─────────────────────────────────────────────────────────────────────────
     * 
     * 【方式一：fixedRate - 固定频率】
     * @Scheduled(fixedRate = 5000)  // 每5秒执行一次
     * 
     * 执行时间线：
     * |----任务执行----|----任务执行----|----任务执行----|
     * 0s              5s              10s             15s
     * 
     * 特点：不管任务执行多久，固定每5秒开始一次新执行
     * 
     * 【方式二：fixedDelay - 固定延迟】
     * @Scheduled(fixedDelay = 5000)  // 任务完成后等待5秒再执行
     * 
     * 执行时间线：
     * |--任务执行--|等待5s|--任务执行--|等待5s|--任务执行--|
     * 0s          3s     8s          11s    16s
     * 
     * 特点：任务完成后等待指定时间再开始下一次
     * 
     * 【方式三：initialDelay + fixedRate - 初始延迟】
     * @Scheduled(initialDelay = 5000, fixedRate = 1000)
     * 
     * 执行时间线：
     * |等待5s|--任务--|--任务--|--任务--|--任务--|
     * 0s     5s      6s      7s      8s      9s
     * 
     * 特点：首次执行前等待指定时间
     * 
     * 【方式四：Cron表达式 - 灵活调度】
     * @Scheduled(cron = "0 0 12 * * ?")  // 每天中午12点执行
     * 
     * Cron格式：
     * ┌───────────── 秒 (0-59)
     * │ ┌───────────── 分钟 (0-59)
     * │ │ ┌───────────── 小时 (0-23)
     * │ │ │ ┌───────────── 日 (1-31)
     * │ │ │ │ ┌───────────── 月 (1-12)
     * │ │ │ │ │ ┌───────────── 星期 (0-7, 0和7都是周日)
     * │ │ │ │ │ │
     * * * * * * *
     * 
     * 常用表达式：
     * "0 0 12 * * ?"      每天中午12点
     * "0 15 10 * * ?"     每天上午10:15
     * "0 0/5 * * * ?"     每5分钟
     * "0 0 0 1 * ?"       每月1号零点
     * 
     * 4.3 fixedRate vs fixedDelay 的选择
     * ─────────────────────────────────────────────────────────────────────────
     * 
     * 【使用 fixedRate 的场景】
     * - 需要精确的时间间隔
     * - 任务执行时间短且稳定
     * - 例如：定时推送数据、心跳检测
     * 
     * 【使用 fixedDelay 的场景】
     * - 任务执行时间不确定
     * - 需要确保两次执行之间有间隔
     * - 例如：数据同步、文件处理
     * 
     * 【本项目的选择】
     * 使用 fixedRate = 1000（每秒执行一次）
     * 原因：
     * 1. 车辆位置推送需要固定频率
     * 2. 任务执行时间很短（毫秒级）
     * 3. 保证流畅的移动效果
     */
    @Scheduled(initialDelay = 5000, fixedRate = 1000)
    public void pushVehicleLocations() {
        try {
            List<VehicleLocationData> vehicleDataList;
            
            if (useSimulation) {
                updateSimulatedPositions();
                vehicleDataList = simulatedVehicles;
            } else {
                vehicleDataList = getRealVehicleData();
            }
            
            if (vehicleDataList.isEmpty()) {
                return;
            }
            
            /**
             * ┌────────────────────────────────────────────────────────────────┐
             * │ WebSocket消息发送                                               │
             * └────────────────────────────────────────────────────────────────┘
             * 
             * 4.4 convertAndSend 方法详解
             * ─────────────────────────────────────────────────────────────────
             * 
             * messagingTemplate.convertAndSend(destination, message)
             * 
             * 【参数说明】
             * - destination: 目的地地址，如 "/topic/vehicles"
             * - message: 要发送的消息对象
             * 
             * 【工作流程】
             * 1. 将message对象序列化为JSON
             * 2. 封装为STOMP MESSAGE帧
             * 3. 发送到消息代理
             * 4. 消息代理分发给所有订阅者
             * 
             * 【前端订阅方式】
             * stompClient.subscribe('/topic/vehicles', function(message) {
             *     const data = JSON.parse(message.body);
             *     // 处理接收到的数据
             * });
             */
            WebSocketMessage message = WebSocketMessage.vehicleList(vehicleDataList);
            messagingTemplate.convertAndSend("/topic/vehicles", message);
            
            /**
             * 原子自增操作
             * incrementAndGet(): 原子自增并返回新值
             */
            pushCount.incrementAndGet();
            lastPushTime = LocalDateTime.now();
            
        } catch (Exception e) {
            System.err.println("推送车辆位置失败: " + e.getMessage());
        }
    }

    /**
     * ══════════════════════════════════════════════════════════════════════════
     * 【第五章：车辆位置插值算法】
     * ══════════════════════════════════════════════════════════════════════════
     */
    
    /**
     * 更新模拟车辆位置
     * 车辆沿预定义路线流畅移动，模拟真实行驶效果
     * 
     * 5.1 线性插值算法
     * ─────────────────────────────────────────────────────────────────────────
     * 
     * 【什么是插值？】
     * 插值是在已知数据点之间估算中间值的方法。
     * 
     * 【线性插值公式】
     * 已知两点 A(x1, y1) 和 B(x2, y2)
     * 在A和B之间，进度为 t (0 <= t <= 1)
     * 
     * 插值点坐标：
     * x = x1 + (x2 - x1) * t
     * y = y1 + (y2 - y1) * t
     * 
     * 【示例】
     * A点: (118.7500, 32.0650)
     * B点: (118.7550, 32.0680)
     * t = 0.5 (中点)
     * 
     * 插值结果：
     * lng = 118.7500 + (118.7550 - 118.7500) * 0.5 = 118.7525
     * lat = 32.0650 + (32.0680 - 32.0650) * 0.5 = 32.0665
     * 
     * 5.2 方向角计算
     * ─────────────────────────────────────────────────────────────────────────
     * 
     * 【atan2 函数】
     * Math.atan2(dy, dx) 返回从原点到点(dx, dy)的角度（弧度）
     * 
     * 【方向角计算】
     * dx = nextLng - currentLng
     * dy = nextLat - currentLat
     * direction = Math.toDegrees(Math.atan2(dy, dx))
     * 
     * 【角度范围】
     * atan2 返回 -π 到 π
     * 转换为度数后是 -180° 到 180°
     * 如果为负数，加360转为 0° 到 360°
     */
    private void updateSimulatedPositions() {
        for (VehicleLocationData vehicle : simulatedVehicles) {
            VehicleRouteState state = vehicleRouteStates.get(vehicle.getCarName());
            if (state == null || state.route == null) continue;
            
            double[][] points = state.route;
            int totalPoints = points.length;
            int currentIdx = state.currentIndex;
            int nextIdx = (currentIdx + 1) % totalPoints;
            
            double[] currentPoint = points[currentIdx];
            double[] nextPoint = points[nextIdx];
            
            /**
             * 计算方向角
             * atan2(dy, dx) 计算从当前点到下一点的角度
             */
            double dx = nextPoint[0] - currentPoint[0];
            double dy = nextPoint[1] - currentPoint[1];
            double direction = Math.toDegrees(Math.atan2(dy, dx));
            if (direction < 0) direction += 360;
            
            /**
             * 计算移动进度
             * moveSpeed: 根据车辆速度计算每帧移动的进度
             * 0.00012 是一个经验值，用于将km/h转换为合适的进度增量
             */
            double moveSpeed = state.speed * 0.00012;
            state.progress += moveSpeed;
            
            /**
             * 检查是否到达下一个路点
             * 如果进度 >= 1.0，说明已经到达下一个点
             */
            if (state.progress >= 1.0) {
                state.progress = 0.0;
                state.currentIndex = nextIdx;
                currentIdx = nextIdx;
                nextIdx = (currentIdx + 1) % totalPoints;
                currentPoint = points[currentIdx];
                nextPoint = points[nextIdx];
            }
            
            /**
             * 线性插值计算当前位置
             * lng = currentLng + (nextLng - currentLng) * progress
             * lat = currentLat + (nextLat - currentLat) * progress
             */
            double lng = currentPoint[0] + (nextPoint[0] - currentPoint[0]) * state.progress;
            double lat = currentPoint[1] + (nextPoint[1] - currentPoint[1]) * state.progress;
            
            /**
             * 模拟速度变化
             * 添加随机波动，使速度更真实
             * 限制在 25-70 km/h 范围内
             */
            state.speed += (Math.random() - 0.5) * 3;
            state.speed = Math.max(25, Math.min(70, state.speed));
            
            /**
             * 更新车辆数据
             */
            vehicle.setLatitude(lat);
            vehicle.setLongitude(lng);
            vehicle.setSpeed(state.speed);
            vehicle.setDirection(direction);
            
            /**
             * 根据速度更新状态
             */
            if (state.speed > 60) {
                vehicle.setStatus("超速");
            } else if (state.speed < 5) {
                vehicle.setStatus("停车中");
            } else {
                vehicle.setStatus("行驶中");
            }
            
            vehicle.setLastUpdate(LocalDateTime.now().format(TIME_FORMATTER));
        }
    }

    /**
     * 从数据库获取真实车辆数据
     * 
     * 5.3 Stream API 详解
     * ─────────────────────────────────────────────────────────────────────────
     * 
     * 【什么是Stream？】
     * Stream是Java 8引入的函数式编程API，用于处理集合数据。
     * 
     * 【Stream vs 传统循环】
     * // 传统方式
     * List<VehicleLocationData> result = new ArrayList<>();
     * for (CarLocation loc : carLocations) {
     *     result.add(convertToVehicleData(loc));
     * }
     * 
     * // Stream方式
     * List<VehicleLocationData> result = carLocations.stream()
     *     .map(this::convertToVehicleData)
     *     .toList();
     * 
     * 【常用Stream操作】
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │ 操作          │ 说明                                                │
     * ├─────────────────────────────────────────────────────────────────────┤
     * │ filter()     │ 过滤元素                                            │
     * │ map()        │ 转换元素                                            │
     * │ sorted()     │ 排序                                                │
     * │ distinct()   │ 去重                                                │
     * │ limit(n)     │ 取前n个                                             │
     * │ forEach()    │ 遍历                                                │
     * │ collect()    │ 收集结果                                            │
     * │ toList()     │ 转为List (Java 16+)                                 │
     * └─────────────────────────────────────────────────────────────────────┘
     * 
     * @return 车辆位置数据列表
     */
    private List<VehicleLocationData> getRealVehicleData() {
        List<CarLocation> carLocations = carLocationService.getAllLocations();
        return carLocations.stream()
            .map(this::convertToVehicleData)
            .toList();
    }

    /**
     * 将数据库实体转换为传输对象
     * 
     * @param carLocation 数据库实体
     * @return 传输对象
     */
    private VehicleLocationData convertToVehicleData(CarLocation carLocation) {
        VehicleLocationData data = new VehicleLocationData();
        data.setId(carLocation.getId());
        data.setCarName(carLocation.getCarName());
        data.setLatitude(carLocation.getLatitude());
        data.setLongitude(carLocation.getLongitude());
        data.setSpeed(carLocation.getSpeed());
        data.setDirection(carLocation.getDirection());
        data.setStatus(carLocation.getSpeed() != null && carLocation.getSpeed() > 60 ? "超速" : "正常");
        data.setLastUpdate(LocalDateTime.now().format(TIME_FORMATTER));
        return data;
    }

    /**
     * 推送单个车辆位置更新
     * 用于特定车辆位置变化时立即通知客户端
     * 
     * @param vehicleData 车辆位置数据
     */
    public void pushSingleVehicle(VehicleLocationData vehicleData) {
        WebSocketMessage message = WebSocketMessage.vehicleUpdate(vehicleData);
        messagingTemplate.convertAndSend("/topic/vehicles", message);
    }

    /**
     * 手动触发推送
     * 可通过API调用立即推送一次数据
     */
    public void triggerPush() {
        pushVehicleLocations();
    }

    /**
     * 获取推送统计信息
     * 
     * @return 统计信息字符串
     */
    public String getPushStatistics() {
        return String.format("推送次数: %d, 最后推送时间: %s", 
            pushCount.get(), 
            lastPushTime != null ? lastPushTime.format(TIME_FORMATTER) : "未推送");
    }

    /**
     * 切换模拟模式
     * 
     * @param useSimulation true使用模拟数据，false使用真实数据
     */
    public void setUseSimulation(boolean useSimulation) {
        this.useSimulation = useSimulation;
    }
    
    /**
     * ══════════════════════════════════════════════════════════════════════════
     * 【第六章：内部类设计】
     * ══════════════════════════════════════════════════════════════════════════
     */
    
    /**
     * 车辆路线状态内部类
     * 用于跟踪每辆车在路线上的位置
     * 
     * 6.1 为什么使用内部类？
     * ─────────────────────────────────────────────────────────────────────────
     * 
     * 【内部类的优点】
     * 1. 封装性：只在当前类中使用，不需要暴露给外部
     * 2. 逻辑分组：相关的类放在一起
     * 3. 访问外部类成员：可以访问外部类的私有成员
     * 
     * 【内部类类型】
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │ 类型              │ 说明                                          │
     * ├─────────────────────────────────────────────────────────────────────┤
     * │ 成员内部类        │ 定义在类内部，可以访问外部类成员                  │
     * │ 静态内部类        │ 使用static修饰，不能访问外部类非静态成员          │
     * │ 局部内部类        │ 定义在方法内部，只在该方法内有效                  │
     * │ 匿名内部类        │ 没有名字的内部类，用于一次性实现                 │
     * └─────────────────────────────────────────────────────────────────────┘
     * 
     * 【本类使用静态内部类的原因】
     * 1. 不需要访问外部类的实例成员
     * 2. 独立的数据结构
     * 3. 可以在外部类外部创建实例（如果需要）
     * 
     * 6.2 字段说明
     * ─────────────────────────────────────────────────────────────────────────
     * 
     * route: 路线坐标数组，每个元素是 [经度, 纬度]
     * currentIndex: 当前所在的路点索引
     * progress: 当前路点到下一个路点的进度 (0.0 ~ 1.0)
     * speed: 当前速度 (km/h)
     */
    private static class VehicleRouteState {
        double[][] route;
        int currentIndex;
        double progress;
        double speed;
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第七章：WebSocket通信完整流程】
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * 7.1 前端订阅流程
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * // 1. 建立WebSocket连接
 * const socket = new SockJS('/ws');
 * const stompClient = Stomp.over(socket);
 * 
 * // 2. 连接成功后订阅主题
 * stompClient.connect({}, function(frame) {
 *     stompClient.subscribe('/topic/vehicles', function(message) {
 *         // 3. 接收到消息后处理
 *         const data = JSON.parse(message.body);
 *         updateMapMarkers(data.data);
 *     });
 * });
 * 
 * 7.2 后端推送流程
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * // 1. 定时任务触发
 * @Scheduled(fixedRate = 1000)
 * public void pushVehicleLocations() {
 *     // 2. 获取车辆数据
 *     List<VehicleLocationData> vehicles = getVehicleData();
 *     
 *     // 3. 封装消息
 *     WebSocketMessage message = WebSocketMessage.vehicleList(vehicles);
 *     
 *     // 4. 推送到消息代理
 *     messagingTemplate.convertAndSend("/topic/vehicles", message);
 * }
 * 
 * 7.3 消息流转图
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 *   后端定时任务
 *       │
 *       ▼
 *   VehicleLocationPushService
 *       │
 *       │ convertAndSend("/topic/vehicles", message)
 *       ▼
 *   SimpleBrokerMessageHandler (消息代理)
 *       │
 *       │ 分发给所有订阅者
 *       ▼
 *   WebSocket连接
 *       │
 *       │ STOMP MESSAGE帧
 *       ▼
 *   前端 stompClient
 *       │
 *       │ 触发回调函数
 *       ▼
 *   更新地图显示
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */
