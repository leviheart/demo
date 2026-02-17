<!--
================================================================================
  MapView.vue - 企业级实时地图监控组件
================================================================================

  【第一章：Vue 3 Composition API 概述】
  ═══════════════════════════════════════════════════════════════════════════════
  
  1.1 什么是 Composition API？
  ─────────────────────────────────────────────────────────────────────────────
  Composition API 是 Vue 3 引入的新编程范式，相比 Options API：
  
  【Options API（Vue 2风格）】
  export default {
    data() {
      return { count: 0 }
    },
    methods: {
      increment() {
        this.count++
      }
    },
    mounted() {
      console.log('mounted')
    }
  }
  
  【Composition API（Vue 3风格）】
  import { ref, onMounted } from 'vue'
  
  const count = ref(0)
  const increment = () => count.value++
  onMounted(() => console.log('mounted'))
  
  【Composition API 的优势】
  1. 更好的逻辑复用：可以提取和组合多个功能
  2. 更好的类型推断：TypeScript支持更好
  3. 更灵活的代码组织：按功能而非选项分组
  4. 更小的打包体积：Tree-shaking更有效
  
  1.2 <script setup> 语法糖
  ─────────────────────────────────────────────────────────────────────────────
  <script setup> 是 Composition API 的编译时语法糖：
  
  【普通写法】
  <script>
  import { ref } from 'vue'
  export default {
    setup() {
      const count = ref(0)
      return { count }
    }
  }
  </script>
  
  【<script setup> 写法】
  <script setup>
  import { ref } from 'vue'
  const count = ref(0)  // 自动暴露给模板
  </script>
  
  优点：
  - 更简洁的代码
  - 自动暴露变量和函数
  - 更好的运行时性能
  - 更好的IDE支持
  
  1.3 响应式系统核心
  ─────────────────────────────────────────────────────────────────────────────
  
  【ref vs reactive】
  ┌─────────────────┬─────────────────────────────────────────────────────────┐
  │ 方式             │ 说明                                                    │
  ├─────────────────┼─────────────────────────────────────────────────────────┤
  │ ref             │ 用于基本类型，访问时需要.value                           │
  │ reactive        │ 用于对象类型，直接访问属性                               │
  │ computed        │ 计算属性，自动追踪依赖                                   │
  │ readonly        │ 只读响应式对象                                          │
  └─────────────────┴─────────────────────────────────────────────────────────┘
  
  【示例】
  const count = ref(0)           // 基本类型用ref
  const user = reactive({ name: '' })  // 对象用reactive
  const doubled = computed(() => count.value * 2)  // 计算属性

  【功能概述】
  提供车辆实时位置监控功能，基于Mapbox地图服务展示车辆位置和状态。
  支持车辆选择、位置追踪、轨迹显示、车辆沿预定义路线移动等功能。

  【设计标准】
  - 真实汽车图标：使用俯视图汽车图标，车头朝向行驶方向
  - 路线可视化：实线显示，已行驶深色高亮，待行驶浅色
  - 方向指示：车辆图标自动朝向行驶方向
================================================================================
-->

<template>
  <!--
  ═══════════════════════════════════════════════════════════════════════════════
  【第二章：模板语法详解】
  ═══════════════════════════════════════════════════════════════════════════════
  
  2.1 Vue 模板基础
  ─────────────────────────────────────────────────────────────────────────────
  Vue模板使用HTML语法，扩展了以下功能：
  
  【指令 Directives】
  ┌───────────────────┬─────────────────────────────────────────────────────┐
  │ 指令               │ 说明                                                │
  ├───────────────────┼─────────────────────────────────────────────────────┤
  │ v-if / v-else     │ 条件渲染                                            │
  │ v-show            │ 显示/隐藏（CSS display）                            │
  │ v-for             │ 列表渲染                                            │
  │ v-bind (:)        │ 属性绑定                                            │
  │ v-on (@)          │ 事件绑定                                            │
  │ v-model           │ 双向绑定                                            │
  │ v-slot (#)        │ 插槽                                                │
  └───────────────────┴─────────────────────────────────────────────────────┘
  
  【简写语法】
  v-bind:href="url"  →  :href="url"
  v-on:click="fn"    →  @click="fn"
  v-slot:header      →  #header
  -->
  
  <div class="map-view-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-title">
          <!-- Element Plus 图标组件 -->
          <el-icon class="title-icon"><MapLocation /></el-icon>
          <h1>实时地图监控</h1>
        </div>
        <div class="header-controls">
          <!-- 按钮组 -->
          <el-button-group>
            <!-- 
            2.2 动态属性绑定
            ───────────────────────────────────────────────────────────────────
            :type="isSimulating ? 'danger' : 'success'"
            
            【语法解析】
            :type - v-bind:type的简写
            isSimulating ? 'danger' : 'success' - JavaScript三元表达式
            
            【工作原理】
            当isSimulating为true时，按钮类型为'danger'（红色）
            当isSimulating为false时，按钮类型为'success'（绿色）
            -->
            <el-button 
              :type="isSimulating ? 'danger' : 'success'" 
              size="small"
              @click="toggleSimulation"
            >
              <!-- 
              2.3 动态组件
              ───────────────────────────────────────────────────────────────────
              <component :is="iconName" />
              
              这是Vue的动态组件语法，根据iconName的值渲染不同的组件。
              这里根据isSimulating的值切换播放/暂停图标。
              -->
              <el-icon><component :is="isSimulating ? 'VideoPause' : 'VideoPlay'"></component></el-icon>
              {{ isSimulating ? '停止模拟' : '开始模拟' }}
            </el-button>
            <el-button @click="centerOnVehicles" type="primary" size="small">
              <el-icon><Position /></el-icon>定位车辆
            </el-button>
            <el-button 
              :type="showTrail ? 'primary' : 'default'" 
              size="small"
              @click="toggleTrail"
            >
              <el-icon><Aim /></el-icon>
              {{ showTrail ? '隐藏路线' : '显示路线' }}
            </el-button>
          </el-button-group>
        </div>
      </div>
    </div>

    <div class="content-wrapper">
      <!-- 
      2.4 Element Plus 布局组件
      ───────────────────────────────────────────────────────────────────
      <el-row :gutter="20">
        <el-col :span="18">...</el-col>
        <el-col :span="6">...</el-col>
      </el-row>
      
      【栅格系统】
      - 总共24列
      - :span="18" 占18列（75%宽度）
      - :span="6" 占6列（25%宽度）
      - :gutter="20" 列间距20px
      -->
      <el-row :gutter="20">
        <el-col :span="18">
          <el-card class="map-card" shadow="never">
            <!-- 
            2.5 插槽 Slots
            ───────────────────────────────────────────────────────────────────
            <template #header>...</template>
            
            #header 是 v-slot:header 的简写
            用于向组件传递模板内容
            
            Element Plus Card组件有两个插槽：
            - #header: 卡片头部
            - default: 卡片主体
            -->
            <template #header>
              <div class="card-header">
                <span class="card-title">车辆实时位置</span>
                <div class="header-tags">
                  <!-- 
                  2.6 计算属性在模板中的使用
                  ───────────────────────────────────────────────────────────────────
                  :type="connectionStatusType"
                  
                  connectionStatusType 是一个计算属性
                  根据wsConnected的值返回'success'或'danger'
                  -->
                  <el-tag :type="connectionStatusType" effect="dark">
                    <el-icon><Connection /></el-icon>
                    {{ connectionStatus }}
                  </el-tag>
                  <el-tag :type="vehicles.length > 0 ? 'success' : 'warning'" effect="dark">
                    <el-icon><Van /></el-icon>
                    在线: {{ vehicles.length }} 辆
                  </el-tag>
                  <!-- 
                  2.7 条件渲染 v-if
                  ───────────────────────────────────────────────────────────────────
                  v-if="isSimulating"
                  
                  只有当isSimulating为true时才渲染这个元素
                  v-if 是真正的条件渲染，元素会被销毁和创建
                  -->
                  <el-tag v-if="isSimulating" type="danger" effect="dark">
                    <el-icon class="is-loading"><Loading /></el-icon>
                    运行中
                  </el-tag>
                </div>
              </div>
            </template>
            <!-- 
            2.8 ref 模板引用
            ───────────────────────────────────────────────────────────────────
            ref="mapContainer"
            
            用于获取DOM元素的引用
            在script中通过 mapContainer.value 访问这个DOM元素
            -->
            <div ref="mapContainer" class="map-container"></div>
            
            <div class="map-legend">
              <div class="legend-title">图例说明</div>
              <div class="legend-items">
                <div class="legend-item">
                  <div class="legend-line passed"></div>
                  <span>已行驶路段</span>
                </div>
                <div class="legend-item">
                  <div class="legend-line upcoming"></div>
                  <span>待行驶路段</span>
                </div>
                <div class="legend-item">
                  <div class="legend-car normal"></div>
                  <span>正常行驶 (&lt;60km/h)</span>
                </div>
                <div class="legend-item">
                  <div class="legend-car speeding"></div>
                  <span>超速行驶 (&gt;60km/h)</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card class="info-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">车辆状态</span>
                <el-button @click="refreshVehicles" size="small" circle>
                  <el-icon><Refresh /></el-icon>
                </el-button>
              </div>
            </template>
            
            <!-- 
            2.9 列表渲染 v-for
            ───────────────────────────────────────────────────────────────────
            v-for="vehicle in vehicles" :key="vehicle.id"
            
            【语法】
            v-for="(item, index) in items" :key="item.id"
            
            【key的重要性】
            - 帮助Vue识别节点，实现高效的DOM更新
            - 必须是唯一的
            - 不要使用index作为key（除非列表不会变化）
            
            【class绑定】
            :class="{ 'selected': selectedVehicle?.id === vehicle.id }"
            
            对象语法：当条件为true时添加对应的class
            selectedVehicle?.id 使用可选链操作符，防止selectedVehicle为null时报错
            -->
            <div class="vehicle-list" v-loading="loading">
              <div 
                v-for="vehicle in vehicles" 
                :key="vehicle.id"
                class="vehicle-item"
                :class="{ 'selected': selectedVehicle?.id === vehicle.id }"
                @click="selectVehicle(vehicle)"
              >
                <div class="vehicle-header">
                  <!-- 
                  2.10 样式绑定
                  ───────────────────────────────────────────────────────────────────
                  :style="{ borderColor: getSpeedColor(vehicle.speed) }"
                  
                  对象语法：直接绑定样式对象
                  也可以绑定数组：
                  :style="[baseStyles, customStyles]"
                  -->
                  <div class="vehicle-icon-wrapper" :style="{ borderColor: getSpeedColor(vehicle.speed) }">
                    <svg class="vehicle-svg" viewBox="0 0 24 24" :style="{ fill: getSpeedColor(vehicle.speed) }">
                      <path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z"/>
                    </svg>
                  </div>
                  <div class="vehicle-info">
                    <h4 class="vehicle-name">{{ vehicle.carName }}</h4>
                    <span class="vehicle-route">{{ getRouteName(vehicle) }}</span>
                  </div>
                  <div class="vehicle-speed" :class="getSpeedClass(vehicle.speed)">
                    <span class="speed-value">{{ Math.round(vehicle.speed) }}</span>
                    <span class="speed-unit">km/h</span>
                  </div>
                </div>
                
                <div class="vehicle-details">
                  <div class="detail-row">
                    <el-icon class="detail-icon"><Location /></el-icon>
                    <span class="detail-text">{{ vehicle.latitude.toFixed(5) }}, {{ vehicle.longitude.toFixed(5) }}</span>
                  </div>
                  <div class="detail-row">
                    <el-icon class="detail-icon"><Compass /></el-icon>
                    <span class="detail-text">航向 {{ Math.round(vehicle.direction) }}° · {{ getDirectionName(vehicle.direction) }}</span>
                  </div>
                  <div class="detail-row">
                    <el-icon class="detail-icon"><Odometer /></el-icon>
                    <!-- Element Plus 进度条组件 -->
                    <el-progress 
                      :percentage="getRouteProgress(vehicle)" 
                      :stroke-width="6"
                      :color="getProgressColor(vehicle)"
                      class="route-progress"
                    />
                  </div>
                </div>
              </div>
              
              <!-- v-if 与 v-else 配合使用 -->
              <div v-if="vehicles.length === 0" class="empty-state">
                <el-empty description="暂无车辆数据" />
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
/**
 * MapView.vue - 企业级实时地图监控组件
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第三章：Vue 3 响应式系统详解】
 * ═══════════════════════════════════════════════════════════════════════════════
 */

/**
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ import 语句详解                                                            │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * 3.1 从 Vue 导入的响应式API
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * 【ref】
 * 创建一个响应式引用，用于基本类型或需要替换整个值的场景。
 * 
 * const count = ref(0)
 * count.value = 1  // 修改值需要.value
 * 
 * 在模板中使用时自动解包，不需要.value：
 * <div>{{ count }}</div>
 * 
 * 【computed】
 * 创建计算属性，自动追踪依赖，依赖变化时重新计算。
 * 
 * const doubled = computed(() => count.value * 2)
 * 
 * 计算属性有缓存，只有依赖变化时才重新计算。
 * 
 * 【onMounted / onUnmounted】
 * 生命周期钩子函数。
 * 
 * onMounted(() => {
 *   // 组件挂载后执行
 *   // 类似于Vue 2的mounted钩子
 * })
 * 
 * onUnmounted(() => {
 *   // 组件卸载前执行
 *   // 类似于Vue 2的beforeUnmount + unmounted
 *   // 用于清理定时器、事件监听等
 * })
 */
import { ref, onMounted, onUnmounted, computed } from 'vue';

/**
 * 3.2 Element Plus 组件库
 * ─────────────────────────────────────────────────────────────────────────────
 * Element Plus 是 Vue 3 的企业级UI组件库。
 * 
 * 【ElMessage】
 * 消息提示组件，用于显示操作反馈。
 * 
 * ElMessage.success('操作成功')
 * ElMessage.error('操作失败')
 * ElMessage.warning('警告信息')
 * ElMessage.info('提示信息')
 */
import { ElMessage } from 'element-plus';

/**
 * 3.3 Element Plus 图标
 * ─────────────────────────────────────────────────────────────────────────────
 * Element Plus 使用 @element-plus/icons-vue 包提供图标组件。
 * 图标组件可以直接在模板中使用：
 * <el-icon><MapLocation /></el-icon>
 */
import { 
  MapLocation, Position, Refresh, Van, Location, Compass,
  VideoPause, VideoPlay, Loading, Odometer, Aim, Connection, Link
} from '@element-plus/icons-vue';

/**
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ Mapbox GL JS 详解                                                          │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * 3.4 什么是 Mapbox GL JS？
 * ─────────────────────────────────────────────────────────────────────────────
 * Mapbox GL JS 是一个用于在Web上渲染交互式地图的JavaScript库。
 * 
 * 【特点】
 * - 矢量地图：使用WebGL渲染，性能优秀
 * - 高度可定制：可以自定义地图样式
 * - 丰富的API：支持标记、弹窗、图层等
 * 
 * 【核心概念】
 * 1. Map: 地图实例
 * 2. Source: 数据源（GeoJSON、Vector Tile等）
 * 3. Layer: 图层（用于渲染数据源）
 * 4. Marker: 标记点
 * 5. Popup: 弹出框
 * 
 * 【使用步骤】
 * 1. 设置accessToken
 * 2. 创建Map实例
 * 3. 添加数据源和图层
 * 4. 添加交互（标记、弹窗等）
 */
import mapboxgl from 'mapbox-gl';
import 'mapbox-gl/dist/mapbox-gl.css';

/**
 * 3.5 导入自定义服务
 * ─────────────────────────────────────────────────────────────────────────────
 */
import WebSocketService, { ConnectionState } from '@/services/WebSocketService';
import NotificationService from '@/services/NotificationService';

/**
 * 3.6 环境变量
 * ─────────────────────────────────────────────────────────────────────────────
 * Vite 使用 import.meta.env 访问环境变量。
 * 
 * 在 .env 文件中定义：
 * VITE_MAPBOX_TOKEN=your_token_here
 * 
 * 使用：
 * import.meta.env.VITE_MAPBOX_TOKEN
 */
mapboxgl.accessToken = import.meta.env.VITE_MAPBOX_TOKEN;

// ==================== 响应式状态 ====================

/**
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ ref() 创建响应式变量                                                        │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * 3.7 ref 的使用
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * 【为什么用 ref 而不是普通变量？】
 * 普通变量不是响应式的，修改后视图不会更新：
 * 
 * let count = 0
 * count++  // 视图不会更新！
 * 
 * 使用 ref 创建响应式变量：
 * const count = ref(0)
 * count.value++  // 视图会自动更新
 * 
 * 【ref 的实现原理】
 * ref 实际上是创建了一个 RefImpl 对象：
 * {
 *   _value: 0,
 *   dep: Set,  // 依赖集合
 *   __v_isRef: true,
 *   get value() { track(this, 'value'); return this._value },
 *   set value(newVal) { this._value = newVal; trigger(this, 'value') }
 * }
 * 
 * 当读取.value时，触发track收集依赖
 * 当修改.value时，触发trigger通知更新
 */

/** 车辆列表数据 - 响应式数组 */
const vehicles = ref([]);

/** 数据加载状态 - 响应式布尔值 */
const loading = ref(false);

/** 当前选中的车辆 - 响应式对象 */
const selectedVehicle = ref(null);

/** 是否显示轨迹路线 */
const showTrail = ref(true);

/** 地图容器DOM引用 - 用于获取DOM元素 */
const mapContainer = ref(null);

/** 是否正在进行模拟行驶 */
const isSimulating = ref(false);

/** WebSocket连接状态 */
const wsConnected = ref(false);

/**
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ computed() 创建计算属性                                                    │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * 3.8 computed 的使用
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * 【computed vs 方法】
 * computed 有缓存，依赖不变就不会重新计算：
 * 
 * const fullName = computed(() => firstName.value + ' ' + lastName.value)
 * 
 * 方法每次调用都会执行：
 * function getFullName() {
 *   return firstName.value + ' ' + lastName.value
 * }
 * 
 * 【computed 的特点】
 * 1. 懒计算：只有被访问时才计算
 * 2. 缓存：依赖不变就返回缓存值
 * 3. 自动追踪依赖：自动检测使用了哪些响应式变量
 * 
 * 【可写计算属性】
 * const fullName = computed({
 *   get() { return firstName.value + ' ' + lastName.value },
 *   set(val) { [firstName.value, lastName.value] = val.split(' ') }
 * })
 */

/** 连接状态文本 - 计算属性 */
const connectionStatus = computed(() => {
  return wsConnected.value ? '已连接' : '未连接';
});

/** 连接状态标签类型 - 计算属性 */
const connectionStatusType = computed(() => {
  return wsConnected.value ? 'success' : 'danger';
});

// ==================== 非响应式变量 ====================

/**
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ 非响应式变量                                                                │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * 3.9 什么时候不需要响应式？
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * 以下情况不需要使用 ref/reactive：
 * 1. 不需要在模板中显示的变量
 * 2. 不需要触发视图更新的变量
 * 3. 第三方库的实例（如Mapbox Map）
 * 4. 常量配置
 * 
 * 使用普通变量可以提高性能，避免不必要的响应式开销。
 */

/** Mapbox地图实例 - 不需要响应式 */
let map = null;

/** 车辆标记对象字典 - 不需要响应式 */
let markers = {};

/** 车辆状态字典 - 不需要响应式 */
let vehicleStates = {};

/** 动画帧ID - 用于取消动画 */
let animationFrameId = null;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第四章：预定义路线数据】
 * ═══════════════════════════════════════════════════════════════════════════════
 */

/**
 * 4.1 路线数据结构
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * 【GeoJSON 格式】
 * 地理数据的标准格式：
 * {
 *   type: 'Feature',
 *   geometry: {
 *     type: 'LineString',
 *     coordinates: [[lng, lat], [lng, lat], ...]
 *   },
 *   properties: { ... }
 * }
 * 
 * 【坐标格式】
 * Mapbox 使用 [经度, 纬度] 格式（注意顺序）
 * - 经度范围：-180 到 180
 * - 纬度范围：-90 到 90
 */
const VEHICLE_ROUTES = {
  '京A10001': {
    name: '内环线A',
    color: '#1890ff',
    points: [
      [118.7500, 32.0650], [118.7550, 32.0680], [118.7620, 32.0700],
      [118.7700, 32.0690], [118.7780, 32.0660], [118.7830, 32.0600],
      [118.7820, 32.0540], [118.7760, 32.0500], [118.7680, 32.0480],
      [118.7600, 32.0500], [118.7540, 32.0550], [118.7500, 32.0600],
      [118.7500, 32.0650]
    ]
  },
  '京A10002': {
    name: '内环线B',
    color: '#52c41a',
    points: [
      [118.7800, 32.0550], [118.7760, 32.0500], [118.7680, 32.0480],
      [118.7600, 32.0500], [118.7540, 32.0550], [118.7500, 32.0600],
      [118.7500, 32.0650], [118.7550, 32.0680], [118.7620, 32.0700],
      [118.7700, 32.0690], [118.7780, 32.0660], [118.7830, 32.0600],
      [118.7800, 32.0550]
    ]
  },
  '京B20001': {
    name: '外环线A',
    color: '#fa8c16',
    points: [
      [118.7650, 32.0700], [118.7580, 32.0720], [118.7500, 32.0710],
      [118.7450, 32.0650], [118.7440, 32.0550], [118.7480, 32.0480],
      [118.7580, 32.0440], [118.7700, 32.0450], [118.7820, 32.0500],
      [118.7880, 32.0580], [118.7850, 32.0680], [118.7750, 32.0720],
      [118.7650, 32.0700]
    ]
  },
  '京B20002': {
    name: '外环线B',
    color: '#f5222d',
    points: [
      [118.7700, 32.0450], [118.7820, 32.0500], [118.7880, 32.0580],
      [118.7850, 32.0680], [118.7750, 32.0720], [118.7650, 32.0700],
      [118.7580, 32.0720], [118.7500, 32.0710], [118.7450, 32.0650],
      [118.7440, 32.0550], [118.7480, 32.0480], [118.7580, 32.0440],
      [118.7700, 32.0450]
    ]
  },
  '沪C30001': {
    name: '南北干线A',
    color: '#722ed1',
    points: [
      [118.7550, 32.0450], [118.7550, 32.0500], [118.7550, 32.0550],
      [118.7550, 32.0600], [118.7550, 32.0650], [118.7550, 32.0700],
      [118.7550, 32.0750], [118.7550, 32.0700], [118.7550, 32.0650],
      [118.7550, 32.0600], [118.7550, 32.0550], [118.7550, 32.0500],
      [118.7550, 32.0450]
    ]
  },
  '沪C30002': {
    name: '南北干线B',
    color: '#13c2c2',
    points: [
      [118.7750, 32.0750], [118.7750, 32.0700], [118.7750, 32.0650],
      [118.7750, 32.0600], [118.7750, 32.0550], [118.7750, 32.0500],
      [118.7750, 32.0450], [118.7750, 32.0500], [118.7750, 32.0550],
      [118.7750, 32.0600], [118.7750, 32.0650], [118.7750, 32.0700],
      [118.7750, 32.0750]
    ]
  },
  '沪D40001': {
    name: '东西干线A',
    color: '#eb2f96',
    points: [
      [118.7400, 32.0620], [118.7450, 32.0620], [118.7500, 32.0620],
      [118.7550, 32.0620], [118.7600, 32.0620], [118.7650, 32.0620],
      [118.7700, 32.0620], [118.7750, 32.0620], [118.7800, 32.0620],
      [118.7850, 32.0620], [118.7800, 32.0620], [118.7750, 32.0620],
      [118.7700, 32.0620], [118.7650, 32.0620], [118.7600, 32.0620],
      [118.7550, 32.0620], [118.7500, 32.0620], [118.7450, 32.0620],
      [118.7400, 32.0620]
    ]
  },
  '沪D40002': {
    name: '东西干线B',
    color: '#faad14',
    points: [
      [118.7850, 32.0580], [118.7800, 32.0580], [118.7750, 32.0580],
      [118.7700, 32.0580], [118.7650, 32.0580], [118.7600, 32.0580],
      [118.7550, 32.0580], [118.7500, 32.0580], [118.7450, 32.0580],
      [118.7500, 32.0580], [118.7550, 32.0580], [118.7600, 32.0580],
      [118.7650, 32.0580], [118.7700, 32.0580], [118.7750, 32.0580],
      [118.7800, 32.0580], [118.7850, 32.0580]
    ]
  },
  '粤E50001': {
    name: '配送区A',
    color: '#2f54eb',
    points: [
      [118.7580, 32.0720], [118.7620, 32.0720], [118.7660, 32.0720],
      [118.7660, 32.0680], [118.7660, 32.0640], [118.7620, 32.0640],
      [118.7580, 32.0640], [118.7580, 32.0680], [118.7580, 32.0720]
    ]
  },
  '粤E50002': {
    name: '配送区B',
    color: '#f5222d',
    points: [
      [118.7720, 32.0480], [118.7680, 32.0480], [118.7640, 32.0480],
      [118.7640, 32.0520], [118.7640, 32.0560], [118.7680, 32.0560],
      [118.7720, 32.0560], [118.7720, 32.0520], [118.7720, 32.0480]
    ]
  },
  '苏F60001': {
    name: '商业区巡游A',
    color: '#1890ff',
    points: [
      [118.7680, 32.0635], [118.7720, 32.0650], [118.7700, 32.0680],
      [118.7660, 32.0670], [118.7620, 32.0650], [118.7600, 32.0620],
      [118.7620, 32.0590], [118.7660, 32.0600], [118.7700, 32.0610],
      [118.7680, 32.0635]
    ]
  },
  '苏F60002': {
    name: '商业区巡游B',
    color: '#52c41a',
    points: [
      [118.7620, 32.0570], [118.7580, 32.0550], [118.7560, 32.0520],
      [118.7600, 32.0500], [118.7640, 32.0510], [118.7680, 32.0530],
      [118.7660, 32.0560], [118.7640, 32.0580], [118.7620, 32.0570]
    ]
  }
};

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第五章：Mapbox 地图初始化】
 * ═══════════════════════════════════════════════════════════════════════════════
 */

/**
 * 初始化地图
 * 
 * 5.1 Mapbox Map 配置选项
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * 【container】
 * 地图容器的DOM元素或元素ID
 * 
 * 【style】
 * 地图样式，可以是：
 * - Mapbox预设样式：mapbox://styles/mapbox/streets-v11
 * - 自定义样式URL
 * - 样式JSON对象
 * 
 * 【center】
 * 初始中心点 [经度, 纬度]
 * 
 * 【zoom】
 * 初始缩放级别，范围通常是0-20
 * 
 * 【pitch】
 * 地图倾斜角度，0-60度
 * 
 * 【bearing】
 * 地图旋转角度，-180到180度
 */
const initMap = () => {
  if (mapContainer.value) {
    /**
     * 创建地图实例
     */
    map = new mapboxgl.Map({
      container: mapContainer.value,
      style: 'mapbox://styles/mapbox/streets-v11',
      center: [118.7634, 32.0617],  // 南京市中心
      zoom: 13.5,
      pitch: 0
    });

    /**
     * 5.2 地图控件
     * ─────────────────────────────────────────────────────────────────────
     * 
     * 【NavigationControl】
     * 导航控件，包含缩放和指南针按钮
     * 
     * 【ScaleControl】
     * 比例尺控件
     * unit: 'metric' 使用公制单位（米/公里）
     * 
     * 【FullscreenControl】
     * 全屏控件
     */
    map.addControl(new mapboxgl.NavigationControl(), 'top-right');
    map.addControl(new mapboxgl.ScaleControl({ unit: 'metric' }));
    map.addControl(new mapboxgl.FullscreenControl());
    
    /**
     * 5.3 地图事件
     * ─────────────────────────────────────────────────────────────────────
     * 
     * map.on('load', callback)
     * 地图加载完成后触发
     * 在这里添加数据源和图层
     */
    map.on('load', () => {
      /**
       * 5.4 添加数据源
       * ─────────────────────────────────────────────────────────────────────
       * 
       * GeoJSON 数据源：
       * {
       *   type: 'geojson',
       *   data: {
       *     type: 'FeatureCollection',
       *     features: [...]
       *   }
       * }
       */
      map.addSource('passed-route-source', {
        type: 'geojson',
        data: { type: 'FeatureCollection', features: [] }
      });
      
      map.addSource('upcoming-route-source', {
        type: 'geojson',
        data: { type: 'FeatureCollection', features: [] }
      });
      
      /**
       * 5.5 添加图层
       * ─────────────────────────────────────────────────────────────────────
       * 
       * 图层定义如何渲染数据源：
       * - id: 图层唯一标识
       * - type: 图层类型（line, circle, fill, symbol等）
       * - source: 数据源ID
       * - paint: 绘制属性
       * - layout: 布局属性
       * 
       * 【表达式语法】
       * ['get', 'color'] - 从feature的properties中获取color属性
       */
      map.addLayer({
        id: 'upcoming-route-layer',
        type: 'line',
        source: 'upcoming-route-source',
        paint: {
          'line-color': ['get', 'color'],  // 使用表达式获取颜色
          'line-width': 5,
          'line-opacity': 0.35
        }
      });
      
      map.addLayer({
        id: 'passed-route-layer',
        type: 'line',
        source: 'passed-route-source',
        paint: {
          'line-color': ['get', 'color'],
          'line-width': 6,
          'line-opacity': 1.0
        }
      });
      
      renderAllRoutes();
    });
  }
};

/**
 * 渲染所有路线
 */
const renderAllRoutes = () => {
  const source = map.getSource('upcoming-route-source');
  if (!source) return;
  
  /**
   * 5.6 GeoJSON Feature 创建
   * ─────────────────────────────────────────────────────────────────────
   * 
   * Feature 是 GeoJSON 的基本单元：
   * {
   *   type: 'Feature',
   *   properties: { ... },  // 自定义属性
   *   geometry: {
   *     type: 'LineString',  // 几何类型
   *     coordinates: [...]   // 坐标数组
   *   }
   * }
   */
  const features = Object.entries(VEHICLE_ROUTES).map(([carName, route]) => ({
    type: 'Feature',
    properties: { color: route.color, carName },
    geometry: {
      type: 'LineString',
      coordinates: route.points
    }
  }));
  
  /**
   * 更新数据源
   * setData() 方法更新GeoJSON数据源
   */
  source.setData({ type: 'FeatureCollection', features });
};

/**
 * 更新路线分段（已行驶/待行驶）
 */
const updateRouteSegments = () => {
  const passedSource = map.getSource('passed-route-source');
  const upcomingSource = map.getSource('upcoming-route-source');
  if (!passedSource || !upcomingSource) return;
  
  const passedFeatures = [];
  const upcomingFeatures = [];
  
  vehicles.value.forEach(vehicle => {
    const state = vehicleStates[vehicle.id];
    if (!state || !state.route) return;
    
    const points = state.route.points;
    const totalPoints = points.length;
    const currentIdx = state.currentIndex;
    const progress = state.progress;
    
    // 构建已行驶路段
    if (currentIdx > 0 || progress > 0) {
      const passedPoints = [];
      for (let i = 0; i <= currentIdx; i++) {
        passedPoints.push(points[i]);
      }
      
      // 添加当前位置（插值点）
      if (progress > 0 && currentIdx < totalPoints - 1) {
        const currentPoint = points[currentIdx];
        const nextPoint = points[currentIdx + 1];
        const currentPos = [
          currentPoint[0] + (nextPoint[0] - currentPoint[0]) * progress,
          currentPoint[1] + (nextPoint[1] - currentPoint[1]) * progress
        ];
        passedPoints.push(currentPos);
      }
      
      if (passedPoints.length >= 2) {
        passedFeatures.push({
          type: 'Feature',
          properties: { color: state.route.color },
          geometry: {
            type: 'LineString',
            coordinates: passedPoints
          }
        });
      }
    }
    
    // 构建待行驶路段
    const upcomingPoints = [];
    if (currentIdx < totalPoints - 1) {
      const currentPoint = points[currentIdx];
      const nextPoint = points[currentIdx + 1];
      const currentPos = [
        currentPoint[0] + (nextPoint[0] - currentPoint[0]) * progress,
        currentPoint[1] + (nextPoint[1] - currentPoint[1]) * progress
      ];
      upcomingPoints.push(currentPos);
      
      for (let i = currentIdx + 1; i < totalPoints; i++) {
        upcomingPoints.push(points[i]);
      }
      
      if (upcomingPoints.length >= 2) {
        upcomingFeatures.push({
          type: 'Feature',
          properties: { color: state.route.color },
          geometry: {
            type: 'LineString',
            coordinates: upcomingPoints
          }
        });
      }
    } else {
      upcomingFeatures.push({
        type: 'Feature',
        properties: { color: state.route.color },
        geometry: {
          type: 'LineString',
          coordinates: [points[totalPoints - 1], points[0]]
        }
      });
    }
  });
  
  passedSource.setData({ type: 'FeatureCollection', features: passedFeatures });
  upcomingSource.setData({ type: 'FeatureCollection', features: upcomingFeatures });
};

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第六章：异步数据加载】
 * ═══════════════════════════════════════════════════════════════════════════════
 */

/**
 * 加载车辆数据
 * 
 * 6.1 async/await 异步编程
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * 【传统Promise写法】
 * fetch(url)
 *   .then(response => response.json())
 *   .then(data => console.log(data))
 *   .catch(error => console.error(error))
 * 
 * 【async/await写法】
 * try {
 *   const response = await fetch(url)
 *   const data = await response.json()
 *   console.log(data)
 * } catch (error) {
 *   console.error(error)
 * }
 * 
 * async/await 让异步代码看起来像同步代码，更易读。
 */
const loadVehicles = async () => {
  loading.value = true;
  try {
    /**
     * 6.2 Fetch API
     * ─────────────────────────────────────────────────────────────────────
     * 
     * fetch() 返回一个Promise，需要await等待响应
     * response.json() 也返回Promise，解析JSON响应体
     */
    const response = await fetch('http://localhost:8081/api/car-locations/latest');
    const data = await response.json();
    vehicles.value = Array.isArray(data) ? data : [];
    
    /**
     * 初始化车辆状态
     */
    vehicles.value.forEach(v => {
      const route = VEHICLE_ROUTES[v.carName];
      if (route && !vehicleStates[v.id]) {
        const points = route.points;
        const p0 = points[0];
        const p1 = points[1];
        const dx = p1[0] - p0[0];
        const dy = p1[1] - p0[1];
        let initialDirection = Math.atan2(dx, dy) * 180 / Math.PI;
        if (initialDirection < 0) initialDirection += 360;
        
        vehicleStates[v.id] = {
          route: route,
          currentIndex: 0,
          progress: 0,
          speed: v.speed || 30 + Math.random() * 30
        };
        v.longitude = p0[0];
        v.latitude = p0[1];
        v.direction = initialDirection;
      }
    });
    
    updateMapMarkers();
    renderAllRoutes();
  } catch (error) {
    ElMessage.error('加载车辆数据失败');
  } finally {
    /**
     * 6.3 finally 块
     * ─────────────────────────────────────────────────────────────────────
     * 无论成功还是失败都会执行
     * 常用于清理操作，如隐藏加载状态
     */
    loading.value = false;
  }
};

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第七章：地图标记创建】
 * ═══════════════════════════════════════════════════════════════════════════════
 */

/**
 * 创建车辆标记
 * 
 * 7.1 Mapbox Marker
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * Marker 是Mapbox提供的标记点组件：
 * - 可以使用自定义HTML元素
 * - 支持拖拽
 * - 可以绑定Popup
 * 
 * 【创建Marker】
 * const marker = new mapboxgl.Marker(options)
 *   .setLngLat([lng, lat])
 *   .addTo(map);
 */
const createVehicleMarker = (vehicle) => {
  const state = vehicleStates[vehicle.id];
  const color = state?.route?.color || '#1890ff';
  const speed = vehicle.speed || 30;
  const direction = vehicle.direction || 0;
  const isSpeeding = speed > 60;
  
  /**
   * 7.2 自定义Marker元素
   * ─────────────────────────────────────────────────────────────────────
   * 
   * 创建DOM元素作为Marker的内容
   * 可以使用HTML、SVG等
   */
  const el = document.createElement('div');
  el.className = 'vehicle-marker-container';
  
  const bodyColor = color;
  const windowColor = 'rgba(180, 210, 240, 0.9)';
  const wheelColor = '#2c2c2c';
  const lightColor = isSpeeding ? '#ff4d4f' : '#f5f5f5';
  
  /**
   * 7.3 SVG 图标
   * ─────────────────────────────────────────────────────────────────────
   * 
   * 使用SVG创建矢量图标：
   * - 可缩放不失真
   * - 可动态修改颜色
   * - 文件小
   * 
   * 【SVG渐变】
   * <linearGradient> 定义线性渐变
   * <filter> 定义滤镜效果（如阴影）
   */
  el.innerHTML = `
    <div class="vehicle-marker-wrapper" style="transform: rotate(${direction}deg);">
      <svg class="vehicle-icon" viewBox="0 0 16 28" width="16" height="28">
        <defs>
          <linearGradient id="car-body-${vehicle.id}" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" style="stop-color:${bodyColor};stop-opacity:1" />
            <stop offset="100%" style="stop-color:${bodyColor};stop-opacity:0.8" />
          </linearGradient>
          <filter id="car-shadow-${vehicle.id}">
            <feDropShadow dx="0" dy="1" stdDeviation="0.5" flood-color="rgba(0,0,0,0.3)"/>
          </filter>
        </defs>
        <g filter="url(#car-shadow-${vehicle.id})">
          <path d="M4 2 L6 0 L10 0 L12 2 L14 6 L14 22 L12 26 L10 28 L6 28 L4 26 L2 22 L2 6 Z" 
                fill="url(#car-body-${vehicle.id})" stroke="#fff" stroke-width="0.8"/>
          <rect x="4" y="5" width="8" height="6" rx="1" fill="${windowColor}"/>
          <rect x="4" y="14" width="8" height="8" rx="1" fill="${windowColor}"/>
          <circle cx="3" cy="4" r="1.5" fill="${wheelColor}"/>
          <circle cx="13" cy="4" r="1.5" fill="${wheelColor}"/>
          <circle cx="3" cy="24" r="1.5" fill="${wheelColor}"/>
          <circle cx="13" cy="24" r="1.5" fill="${wheelColor}"/>
          <rect x="6" y="0" width="2" height="1.5" rx="0.3" fill="${lightColor}"/>
          <rect x="9" y="0" width="2" height="1.5" rx="0.3" fill="${lightColor}"/>
          <rect x="6" y="26" width="2" height="1.5" rx="0.3" fill="#ff4444"/>
          <rect x="9" y="26" width="2" height="1.5" rx="0.3" fill="#ff4444"/>
        </g>
        ${isSpeeding ? `
          <circle cx="14" cy="2" r="3" fill="#ff4d4f"/>
          <text x="14" y="4" text-anchor="middle" fill="#fff" font-size="5" font-weight="bold">!</text>
        ` : ''}
      </svg>
    </div>
  `;
  
  /**
   * 创建Marker实例
   */
  const marker = new mapboxgl.Marker({
    element: el,
    anchor: 'center',
    offset: [0, 0]
  })
  .setLngLat([vehicle.longitude, vehicle.latitude])
  .addTo(map);
  
  /**
   * 7.4 Popup 弹出框
   * ─────────────────────────────────────────────────────────────────────
   * 
   * Popup 是信息弹出框：
   * - 可以设置HTML内容
   * - 支持多种触发方式
   * - 可自定义样式
   */
  const popup = new mapboxgl.Popup({
    offset: 25,
    closeButton: false,
    className: 'vehicle-popup'
  }).setHTML(`
    <div class="popup-content">
      <div class="popup-header" style="border-left: 4px solid ${color}">
        <span class="popup-name">${vehicle.carName}</span>
        <span class="popup-route">${state?.route?.name || '未知路线'}</span>
      </div>
      <div class="popup-body">
        <div class="popup-row">
          <span class="popup-label">速度</span>
          <span class="popup-value ${isSpeeding ? 'speeding' : ''}">${Math.round(speed)} km/h</span>
        </div>
        <div class="popup-row">
          <span class="popup-label">航向</span>
          <span class="popup-value">${Math.round(direction)}° ${getDirectionName(direction)}</span>
        </div>
        <div class="popup-row">
          <span class="popup-label">进度</span>
          <span class="popup-value">${getRouteProgress(vehicle)}%</span>
        </div>
      </div>
    </div>
  `);
  
  marker.setPopup(popup);
  
  /**
   * 7.5 事件监听
   * ─────────────────────────────────────────────────────────────────────
   * 
   * 使用addEventListener添加点击事件
   * e.stopPropagation() 阻止事件冒泡
   */
  el.addEventListener('click', (e) => {
    e.stopPropagation();
    selectVehicle(vehicle);
  });
  
  markers[vehicle.id] = marker;
};

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第八章：辅助函数】
 * ═══════════════════════════════════════════════════════════════════════════════
 */

const getSpeedColor = (speed) => {
  if (speed > 60) return '#ff4d4f';
  if (speed > 40) return '#faad14';
  return '#52c41a';
};

const getSpeedClass = (speed) => {
  if (speed > 60) return 'speeding';
  if (speed > 40) return 'warning';
  return 'normal';
};

const getDirectionName = (direction) => {
  const dirs = ['正北', '东北', '正东', '东南', '正南', '西南', '正西', '西北'];
  const index = Math.round(direction / 45) % 8;
  return dirs[index];
};

const getRouteName = (vehicle) => {
  const state = vehicleStates[vehicle.id];
  return state?.route?.name || '未分配路线';
};

const getRouteProgress = (vehicle) => {
  const state = vehicleStates[vehicle.id];
  if (!state) return 0;
  return Math.round((state.currentIndex + state.progress) / state.route.points.length * 100);
};

const getProgressColor = (vehicle) => {
  const state = vehicleStates[vehicle.id];
  return state?.route?.color || '#1890ff';
};

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第九章：交互功能】
 * ═══════════════════════════════════════════════════════════════════════════════
 */

/**
 * 选择车辆
 * 
 * 9.1 地图飞行动画
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * map.flyTo() 实现平滑的地图移动动画：
 * - center: 目标中心点
 * - zoom: 目标缩放级别
 * - duration: 动画时长（毫秒）
 * - essential: 是否为必要动画（不受prefers-reduced-motion影响）
 */
const selectVehicle = (vehicle) => {
  selectedVehicle.value = vehicle;
  
  if (map && vehicle.latitude && vehicle.longitude) {
    map.flyTo({
      center: [vehicle.longitude, vehicle.latitude],
      zoom: 15,
      essential: true
    });
    
    // 更新标记透明度，突出选中的车辆
    Object.values(markers).forEach(marker => {
      marker.getElement().style.opacity = '0.5';
    });
    if (markers[vehicle.id]) {
      markers[vehicle.id].getElement().style.opacity = '1';
    }
  }
};

/**
 * 居中显示所有车辆
 * 
 * 9.2 地图边界适配
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * map.fitBounds() 调整地图视野以包含指定范围：
 * - bounds: LngLatBounds对象
 * - padding: 边距（像素或对象{top, bottom, left, right}）
 * - animate: 是否使用动画
 */
const centerOnVehicles = () => {
  if (vehicles.value.length > 0 && map) {
    const coordinates = vehicles.value.map(v => [v.longitude, v.latitude]);
    const bounds = coordinates.reduce((bounds, coord) => {
      return bounds.extend(coord);
    }, new mapboxgl.LngLatBounds(coordinates[0], coordinates[0]));
    
    map.fitBounds(bounds, { padding: 80, animate: true });
    ElMessage.success('已居中显示所有车辆');
  }
};

const toggleTrail = () => {
  showTrail.value = !showTrail.value;
  
  const visibility = showTrail.value ? 'visible' : 'none';
  
  if (map && map.getLayer('passed-route-layer')) {
    map.setLayoutProperty('passed-route-layer', 'visibility', visibility);
  }
  if (map && map.getLayer('upcoming-route-layer')) {
    map.setLayoutProperty('upcoming-route-layer', 'visibility', visibility);
  }
  
  ElMessage.info(showTrail.value ? '已显示路线' : '已隐藏路线');
};

const refreshVehicles = () => {
  loadVehicles();
  ElMessage.success('数据已刷新');
};

const updateMapMarkers = () => {
  Object.values(markers).forEach(marker => marker.remove());
  markers = {};
  
  vehicles.value.forEach(vehicle => {
    if (vehicle.latitude && vehicle.longitude) {
      createVehicleMarker(vehicle);
    }
  });
};

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第十章：动画模拟】
 * ═══════════════════════════════════════════════════════════════════════════════
 */

/**
 * 切换模拟状态
 */
const toggleSimulation = () => {
  isSimulating.value = !isSimulating.value;
  
  if (isSimulating.value) {
    startSimulation();
    ElMessage.success('车辆模拟已启动');
  } else {
    stopSimulation();
    ElMessage.info('车辆模拟已停止');
  }
};

/**
 * 开始模拟
 * 
 * 10.1 requestAnimationFrame
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * requestAnimationFrame 是浏览器提供的动画API：
 * - 在下一次重绘之前调用回调
 * - 自动匹配显示器刷新率（通常是60fps）
 * - 页面不可见时自动暂停
 * 
 * 【与setInterval的区别】
 * setInterval: 固定间隔，可能丢帧或卡顿
 * requestAnimationFrame: 流畅动画，性能更好
 * 
 * 【使用模式】
 * function animate() {
 *   // 更新动画
 *   requestAnimationFrame(animate);  // 递归调用
 * }
 * requestAnimationFrame(animate);  // 启动
 * 
 * 【取消动画】
 * const id = requestAnimationFrame(animate);
 * cancelAnimationFrame(id);
 */
const startSimulation = () => {
  const simulate = () => {
    if (!isSimulating.value) return;
    
    vehicles.value.forEach(vehicle => {
      const state = vehicleStates[vehicle.id];
      if (!state || !state.route) return;
      
      const points = state.route.points;
      const totalPoints = points.length;
      const currentIdx = state.currentIndex;
      const nextIdx = (currentIdx + 1) % totalPoints;
      
      const currentPoint = points[currentIdx];
      const nextPoint = points[nextIdx];
      
      // 计算方向
      const dx = nextPoint[0] - currentPoint[0];
      const dy = nextPoint[1] - currentPoint[1];
      let direction = Math.atan2(dx, dy) * 180 / Math.PI;
      if (direction < 0) direction += 360;
      
      // 更新进度
      const moveSpeed = state.speed * 0.00015;
      state.progress += moveSpeed;
      
      // 检查是否到达下一个点
      if (state.progress >= 1) {
        state.progress = 0;
        state.currentIndex = nextIdx;
      }
      
      // 插值计算当前位置
      const lng = currentPoint[0] + (nextPoint[0] - currentPoint[0]) * state.progress;
      const lat = currentPoint[1] + (nextPoint[1] - currentPoint[1]) * state.progress;
      
      // 随机速度变化
      state.speed += (Math.random() - 0.5) * 2;
      state.speed = Math.max(25, Math.min(75, state.speed));
      
      // 更新车辆数据
      vehicle.longitude = lng;
      vehicle.latitude = lat;
      vehicle.speed = state.speed;
      vehicle.direction = direction;
      
      // 更新标记位置
      if (markers[vehicle.id]) {
        markers[vehicle.id].setLngLat([lng, lat]);
        
        const wrapper = markers[vehicle.id].getElement().querySelector('.vehicle-marker-wrapper');
        if (wrapper) {
          wrapper.style.transform = `rotate(${direction}deg)`;
        }
      }
    });
    
    updateRouteSegments();
    
    // 递归调用，形成动画循环
    animationFrameId = requestAnimationFrame(simulate);
  };
  
  animationFrameId = requestAnimationFrame(simulate);
};

const stopSimulation = () => {
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId);
    animationFrameId = null;
  }
};

// ==================== WebSocket相关方法 ====================

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第十一章：WebSocket 实时通信】
 * ═══════════════════════════════════════════════════════════════════════════════
 */

/**
 * 初始化WebSocket连接
 */
const initWebSocket = () => {
  console.log('[MapView] 正在初始化WebSocket连接...');
  
  WebSocketService.connect(
    // 连接成功回调
    () => {
      wsConnected.value = true;
      console.log('[MapView] WebSocket连接成功');
      
      // 订阅车辆位置更新
      WebSocketService.subscribe('/topic/vehicles', handleVehicleMessage);
      
      // 订阅告警消息
      WebSocketService.subscribe('/topic/alerts', handleAlertMessage);
      
      ElMessage.success('实时连接已建立');
    },
    
    // 断开连接回调
    () => {
      wsConnected.value = false;
      console.log('[MapView] WebSocket连接断开');
      ElMessage.warning('实时连接已断开，正在重连...');
    },
    
    // 错误回调
    (error) => {
      wsConnected.value = false;
      console.error('[MapView] WebSocket错误:', error);
    }
  );
};

/**
 * 处理车辆位置消息
 */
const handleVehicleMessage = (wsMessage) => {
  if (!wsMessage || !wsMessage.data) return;
  
  const vehicleDataList = wsMessage.data;
  
  if (Array.isArray(vehicleDataList)) {
    updateVehiclesFromWebSocket(vehicleDataList);
  }
};

/**
 * 处理告警消息
 */
const handleAlertMessage = (wsMessage) => {
  if (!wsMessage) return;
  
  NotificationService.handleAlertMessage(wsMessage);
  
  if (wsMessage.data && wsMessage.data.carName) {
    highlightVehicle(wsMessage.data.carName);
  }
};

/**
 * 从WebSocket数据更新车辆位置
 */
const updateVehiclesFromWebSocket = (vehicleDataList) => {
  vehicleDataList.forEach(vehicleData => {
    const existingVehicle = vehicles.value.find(v => v.id === vehicleData.id);
    
    if (existingVehicle) {
      // 更新现有车辆
      existingVehicle.latitude = vehicleData.latitude;
      existingVehicle.longitude = vehicleData.longitude;
      existingVehicle.speed = vehicleData.speed;
      existingVehicle.direction = vehicleData.direction;
      existingVehicle.status = vehicleData.status;
      if (vehicleData.routeName) existingVehicle.routeName = vehicleData.routeName;
      if (vehicleData.routeColor) existingVehicle.routeColor = vehicleData.routeColor;
      
      // 更新标记
      if (markers[vehicleData.id]) {
        markers[vehicleData.id].setLngLat([vehicleData.longitude, vehicleData.latitude]);
        
        const wrapper = markers[vehicleData.id].getElement().querySelector('.vehicle-marker-wrapper');
        if (wrapper && vehicleData.direction != null) {
          wrapper.style.transform = `rotate(${vehicleData.direction}deg)`;
        }
        
        const speed = vehicleData.speed || 30;
        const isSpeeding = speed > 60;
        const speedValueEl = markers[vehicleData.id].getElement().querySelector('.speed-value');
        if (speedValueEl) {
          speedValueEl.textContent = Math.round(speed);
        }
      }
    } else {
      // 添加新车辆
      vehicles.value.push(vehicleData);
      
      const route = VEHICLE_ROUTES[vehicleData.carName];
      if (route && !vehicleStates[vehicleData.id]) {
        vehicleStates[vehicleData.id] = {
          route: route,
          currentIndex: 0,
          progress: 0,
          speed: vehicleData.speed || 30
        };
      }
      
      if (map && vehicleData.latitude && vehicleData.longitude) {
        createVehicleMarker(vehicleData);
      }
    }
  });
  
  updateRouteSegments();
};

const highlightVehicle = (carName) => {
  const vehicle = vehicles.value.find(v => v.carName === carName);
  if (vehicle) {
    selectVehicle(vehicle);
  }
};

const reconnectWebSocket = () => {
  if (!wsConnected.value) {
    ElMessage.info('正在重新连接...');
    WebSocketService.reconnect();
  }
};

// ==================== 生命周期钩子 ====================

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第十二章：生命周期钩子】
 * ═══════════════════════════════════════════════════════════════════════════════
 */

/**
 * 组件挂载时初始化
 * 
 * 12.1 onMounted 钩子
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * 在组件挂载到DOM后调用：
 * - 此时可以访问DOM元素
 * - ref引用已经绑定
 * - 适合进行初始化操作
 * 
 * 【Vue 3 生命周期】
 * setup() → onBeforeMount → onMounted → onBeforeUpdate → onUpdated → onBeforeUnmount → onUnmounted
 */
onMounted(() => {
  // 初始化地图
  initMap();
  
  // 加载初始车辆数据
  loadVehicles();
  
  // 初始化WebSocket连接
  initWebSocket();
});

/**
 * 组件卸载时清理资源
 * 
 * 12.2 onUnmounted 钩子
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * 在组件卸载前调用：
 * - 清理定时器
 * - 取消事件监听
 * - 断开网络连接
 * - 释放资源
 * 
 * 【为什么需要清理？】
 * 1. 防止内存泄漏
 * 2. 避免无效操作
 * 3. 释放系统资源
 */
onUnmounted(() => {
  // 停止模拟
  stopSimulation();
  
  // 断开WebSocket连接
  WebSocketService.disconnect();
  
  // 移除地图
  if (map) {
    map.remove();
  }
});
</script>

<style scoped>
/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * 【第十三章：CSS 样式详解】
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * 13.1 scoped 样式
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * <style scoped> 使样式只作用于当前组件：
 * - Vue会为元素添加唯一属性 data-v-xxx
 * - CSS选择器会自动添加属性选择器
 * 
 * 编译前：
 * .title { color: red; }
 * 
 * 编译后：
 * .title[data-v-123] { color: red; }
 * 
 * 13.2 CSS 选择器
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * .class        类选择器
 * #id           ID选择器
 * element       元素选择器
 * [attr]        属性选择器
 * :hover        伪类
 * ::before      伪元素
 * parent child  后代选择器
 * parent > child 子选择器
 */

.map-view-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: 12px;
  padding: 16px 24px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 28px;
  color: #1890ff;
}

.header-title h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
}

.header-controls {
  display: flex;
  gap: 12px;
}

.header-tags {
  display: flex;
  gap: 8px;
}

.content-wrapper {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
}

.map-card, .info-card {
  border: none;
  border-radius: 12px;
  overflow: hidden;
  height: 100%;
}

/**
 * 13.3 :deep() 深度选择器
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * scoped 样式无法直接影响子组件的样式
 * 使用 :deep() 可以穿透 scoped 限制
 * 
 * :deep(.el-card__header) { ... }
 * 
 * 编译后：
 * [data-v-123] .el-card__header { ... }
 */
:deep(.el-card__header) {
  background: linear-gradient(90deg, #f8f9fa, #e9ecef);
  border-bottom: 1px solid #e8e8e8;
  padding: 16px 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.map-container {
  height: 580px;
  border-radius: 8px;
  overflow: hidden;
  background: #1a1a2e;
  position: relative;
}

.map-legend {
  position: absolute;
  bottom: 20px;
  left: 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  padding: 12px 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  z-index: 10;
}

.legend-title {
  font-size: 13px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e8e8e8;
}

.legend-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #595959;
}

.legend-line {
  width: 30px;
  height: 5px;
  border-radius: 2px;
}

.legend-line.passed {
  background: #1890ff;
}

.legend-line.upcoming {
  background: #1890ff;
  opacity: 0.35;
}

.legend-car {
  width: 16px;
  height: 10px;
  border-radius: 2px;
}

.legend-car.normal {
  background: #52c41a;
}

.legend-car.speeding {
  background: #ff4d4f;
}

.vehicle-list {
  max-height: 580px;
  overflow-y: auto;
}

.vehicle-item {
  padding: 14px;
  border-radius: 10px;
  margin-bottom: 10px;
  background: #f8f9fa;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
}

.vehicle-item:hover {
  transform: translateX(4px);
  border-color: #1890ff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
}

.vehicle-item.selected {
  background: linear-gradient(90deg, #e6f7ff, #f0f5ff);
  border-color: #1890ff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.2);
}

.vehicle-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.vehicle-icon-wrapper {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #1890ff;
  transition: border-color 0.3s ease;
}

.vehicle-svg {
  width: 26px;
  height: 26px;
}

.vehicle-info {
  flex: 1;
  min-width: 0;
}

.vehicle-name {
  margin: 0 0 2px 0;
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
}

.vehicle-route {
  font-size: 12px;
  color: #8c8c8c;
}

.vehicle-speed {
  text-align: center;
  padding: 4px 8px;
  border-radius: 6px;
  background: #f0f0f0;
}

.vehicle-speed.normal {
  background: #f6ffed;
}

.vehicle-speed.warning {
  background: #fffbe6;
}

.vehicle-speed.speeding {
  background: #fff2f0;
}

.speed-value {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1;
}

.vehicle-speed.speeding .speed-value {
  color: #ff4d4f;
}

.speed-unit {
  font-size: 10px;
  color: #8c8c8c;
}

.vehicle-details {
  padding-left: 54px;
}

.detail-row {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
  font-size: 12px;
  color: #595959;
}

.detail-icon {
  margin-right: 6px;
  color: #8c8c8c;
  font-size: 12px;
}

.detail-text {
  flex: 1;
}

.route-progress {
  flex: 1;
}

:deep(.el-progress__text) {
  font-size: 11px !important;
  margin-left: 8px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
}

.vehicle-marker-container {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.vehicle-marker-container:hover {
  transform: scale(1.2);
}

.vehicle-marker-wrapper {
  transition: transform 0.1s ease;
  transform-origin: center center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.vehicle-icon {
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.4));
}

:deep(.vehicle-popup) {
  max-width: 220px;
}

:deep(.mapboxgl-popup-content) {
  padding: 0;
  border-radius: 8px;
  overflow: hidden;
}

.popup-content {
  background: #fff;
}

.popup-header {
  padding: 10px 12px;
  background: #f8f9fa;
  border-bottom: 1px solid #e8e8e8;
}

.popup-name {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
}

.popup-route {
  font-size: 11px;
  color: #8c8c8c;
}

.popup-body {
  padding: 8px 12px;
}

.popup-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 12px;
}

.popup-label {
  color: #8c8c8c;
}

.popup-value {
  font-weight: 500;
  color: #1a1a2e;
}

.popup-value.speeding {
  color: #ff4d4f;
  font-weight: 600;
}

.is-loading {
  animation: rotating 1s linear infinite;
}

/**
 * 13.4 CSS 动画
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * @keyframes 定义关键帧动画
 * animation 属性应用动画
 * 
 * animation: name duration timing-function delay iteration-count direction;
 */
@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/**
 * 13.5 响应式设计
 * ─────────────────────────────────────────────────────────────────────────────
 * 
 * @media 查询根据屏幕尺寸应用不同样式
 */
@media (max-width: 1200px) {
  .content-wrapper {
    padding: 16px;
  }
  
  :deep(.el-col-18), :deep(.el-col-6) {
    width: 100%;
    margin-bottom: 20px;
  }
  
  .map-container {
    height: 450px;
  }
  
  .vehicle-list {
    max-height: 350px;
  }
  
  .map-legend {
    display: none;
  }
}
</style>
