/**
 * main.js - Vue应用入口文件
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 这是Vue 3应用的入口文件，负责创建和挂载Vue应用实例。
 * 配置全局插件、注册全局组件、设置应用级别的配置。
 * 
 * 【Vue 3应用初始化流程】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                      Vue 3 应用初始化流程                                │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │                                                                         │
 * │  1. 导入依赖                                                            │
 * │     ├── createApp: Vue 3应用创建函数                                    │
 * │     ├── ElementPlus: UI组件库                                           │
 * │     └── App.vue: 根组件                                                 │
 * │                                                                         │
 * │  2. 创建应用实例                                                        │
 * │     └── const app = createApp(App)                                      │
 * │                                                                         │
 * │  3. 注册全局组件                                                        │
 * │     └── 注册所有Element Plus图标组件                                    │
 * │                                                                         │
 * │  4. 安装插件                                                            │
 * │     └── app.use(ElementPlus)                                            │
 * │                                                                         │
 * │  5. 挂载应用                                                            │
 * │     └── app.mount('#app')                                               │
 * │                                                                         │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【createApp vs new Vue】
 * Vue 3使用createApp函数替代Vue 2的new Vue构造函数：
 * - createApp返回一个独立的应用实例
 * - 多个应用实例互不干扰，可以独立配置
 * - 全局API改为实例方法，避免全局污染
 * 
 * 【Element Plus】
 * Element Plus是Element UI的Vue 3版本：
 * - 提供丰富的UI组件（表格、表单、弹窗等）
 * - 支持按需引入，减少打包体积
 * - 完整支持TypeScript
 * 
 * 【全局图标注册】
 * 通过遍历注册所有图标组件，使其可以在任何组件中直接使用：
 * - 无需在每个组件中单独导入图标
 * - 使用方式：<el-icon><Edit /></el-icon>
 * 
 * 【关联文件】
 * - App.vue: 根组件
 * - index.html: HTML入口（包含#app挂载点）
 * ═══════════════════════════════════════════════════════════════════════════
 */

// ==================== 导入依赖 ====================

/**
 * createApp - Vue 3应用创建函数
 * 
 * 这是Vue 3的核心API，用于创建一个新的Vue应用实例。
 * 
 * 与Vue 2的区别：
 * - Vue 2: new Vue({ ...options })
 * - Vue 3: createApp({ ...options })
 * 
 * createApp的优势：
 * 1. 每个应用实例独立，全局配置互不影响
 * 2. 更好的TypeScript支持
 * 3. Tree-shaking友好，减少打包体积
 */
import { createApp } from 'vue';

/**
 * ElementPlus - Element Plus UI组件库
 * 
 * Element Plus是基于Vue 3的企业级UI组件库，提供：
 * - 基础组件：Button, Input, Select等
 * - 表单组件：Form, DatePicker, Upload等
 * - 数据展示：Table, Tree, Pagination等
 * - 通知组件：Message, Notification, Dialog等
 * 
 * 完整引入会打包所有组件，生产环境建议按需引入。
 */
import ElementPlus from 'element-plus';

/**
 * Element Plus样式文件
 * 包含所有组件的CSS样式
 */
import 'element-plus/dist/index.css';

/**
 * Element Plus图标库
 * 包含所有图标组件，如Edit, Delete, Search等
 */
import * as ElementPlusIconsVue from '@element-plus/icons-vue';

/**
 * App根组件
 * Vue应用的根组件，定义应用的整体结构
 */
import App from './App.vue';

// ==================== 创建应用实例 ====================

/**
 * 创建Vue应用实例
 * 
 * createApp函数接收根组件作为参数，返回一个应用实例。
 * 应用实例提供了一系列方法用于配置和扩展应用：
 * - app.use(): 安装插件
 * - app.component(): 注册全局组件
 * - app.directive(): 注册全局指令
 * - app.provide(): 提供全局依赖
 * - app.mount(): 挂载应用到DOM
 */
const app = createApp(App);

// ==================== 注册全局图标组件 ====================

/**
 * 遍历注册所有Element Plus图标组件
 * 
 * Object.entries()将对象转换为[key, value]数组
 * 例如：{ Edit: Component, Delete: Component } => [['Edit', Component], ['Delete', Component]]
 * 
 * 注册后可以在任何组件中直接使用图标：
 * <el-icon><Edit /></el-icon>
 * 
 * 这样做的好处：
 * 1. 无需在每个组件中单独导入图标
 * 2. 统一管理所有图标
 * 3. 减少重复代码
 */
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  /**
   * app.component(name, component)
   * 
   * 注册全局组件
   * - name: 组件名称，用于在模板中使用
   * - component: 组件定义
   * 
   * 注册后的组件可以在任何模板中直接使用，无需导入
   */
  app.component(key, component);
}

// ==================== 安装插件 ====================

/**
 * app.use(plugin)
 * 
 * 安装Vue插件
 * 
 * Element Plus插件会：
 * 1. 注册所有Element Plus组件
 * 2. 注册所有Element Plus指令
 * 3. 添加全局配置
 * 
 * 等价于：
 * app.use(ElementPlus, { size: 'default', zIndex: 2000 })
 */
app.use(ElementPlus);

// ==================== 挂载应用 ====================

/**
 * app.mount(selector)
 * 
 * 将应用挂载到DOM元素上
 * 
 * - selector: CSS选择器，指定挂载点
 * - '#app'对应index.html中的<div id="app"></div>
 * 
 * 挂载过程：
 * 1. 查找DOM元素
 * 2. 创建应用根实例
 * 3. 渲染根组件
 * 4. 替换挂载点内容
 * 
 * 注意：mount()方法必须在最后调用，之后不能再链式调用其他方法
 */
app.mount('#app');
