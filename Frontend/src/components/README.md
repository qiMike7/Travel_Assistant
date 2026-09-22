# 自定义导航栏组件使用说明

## 组件位置
`/components/CustomNavBar.vue`

## 功能特性

✅ **透明背景** - 支持完全透明的导航栏背景  
✅ **响应式设计** - 适配不同屏幕尺寸  
✅ **智能适配** - 自动获取系统信息，适配微信小程序胶囊按钮 <mcreference link="https://juejin.cn/post/7457758555173896227" index="0">0</mcreference>  
✅ **相对定位** - 使用相对定位，避免内容与状态栏冲突  
✅ **三种布局模式**：
1. 左侧返回按钮 + 居中标题
2. 仅居中标题
3. 左侧自定义文本 + 居中标题

✅ **自动状态栏适配** - 自动获取设备状态栏高度  
✅ **胶囊按钮适配** - 微信小程序自动适配胶囊按钮尺寸  
✅ **毛玻璃效果** - 返回按钮带有毛玻璃背景效果  

## 核心改进

### 1. 系统信息获取 <mcreference link="https://juejin.cn/post/7457758555173896227" index="0">0</mcreference>
- **状态栏高度**：通过 `uni.getSystemInfoSync()` 获取
- **胶囊信息**：微信小程序通过 `uni.getMenuButtonBoundingClientRect()` 获取
- **导航栏高度**：根据胶囊位置自动计算：`(胶囊上边距 - 状态栏高度) * 2 + 胶囊高度`

### 2. 相对定位设计
- 使用 `position: relative` 替代 `position: fixed`
- 自动填充状态栏高度，确保内容不与状态栏冲突
- 页面内容无需额外的顶部间距调整

### 3. 多平台适配
- **微信小程序**：自动适配胶囊按钮宽度和位置
- **H5/App**：使用默认尺寸，保持一致的视觉效果
- **支付宝小程序**：组件自动隐藏（通过条件编译）

## 使用方法

### 1. 导入组件
```vue
<script>
import CustomNavBar from '@/components/CustomNavBar.vue'

export default {
  components: {
    CustomNavBar
  }
}
</script>
```

### 2. 在模板中使用

#### 模式一：仅显示居中标题
```vue
<CustomNavBar 
  :title="'页面标题'"
  :showBack="false"
/>
```

#### 模式二：显示返回按钮 + 居中标题
```vue
<CustomNavBar 
  :title="'页面标题'"
  :showBack="true"
  @back="handleBack"
  @home="handleHome"
/>
```

#### 模式三：自定义左侧文本 + 居中标题
```vue
<CustomNavBar 
  :title="'页面标题'"
  :leftText="'取消'"
  :showBack="false"
  @leftClick="handleLeftClick"
/>
```

#### 模式四：自定义右侧内容
```vue
<CustomNavBar :title="'页面标题'">
  <template #right>
    <view class="custom-right">
      <text>分享</text>
    </view>
  </template>
</CustomNavBar>
```

## Props 参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| title | String | '' | 导航栏标题文本 |
| leftText | String | '' | 左侧自定义文本 |
| showBack | Boolean | true | 是否显示返回按钮 |
| backgroundColor | String | 'transparent' | 背景颜色 |
| textColor | String | '#000000' | 文字颜色 |

## Events 事件

| 事件名 | 说明 | 参数 |
|--------|------|------|
| back | 返回按钮点击事件（非首页时） | - |
| home | 首页返回按钮点击事件 | - |
| leftClick | 左侧区域点击事件（当showBack为false时） | - |

## Slots 插槽

| 插槽名 | 说明 |
|--------|------|
| right | 右侧自定义内容区域 |

## 样式说明

- 导航栏使用相对定位，自动占据页面顶部空间
- 返回按钮带有半透明白色背景和毛玻璃效果
- 标题文本自动居中，支持文本溢出省略
- 支持响应式设计，在不同屏幕尺寸下自动调整
- 左右区域宽度根据平台自动调整（微信小程序使用胶囊宽度）

## 注意事项

1. **页面配置**：确保在`pages.json`中设置`"navigationStyle": "custom"`
2. **内容布局**：由于使用相对定位，页面内容会自动跟在导航栏下方，无需手动调整间距
3. **状态栏适配**：组件会自动处理状态栏高度和胶囊按钮适配
4. **首页判断**：组件会自动判断是否为首页，首页点击返回按钮会触发`home`事件而非`back`事件

## 示例页面

- 首页：`/pages/index/index.vue` - 仅显示标题，无返回按钮
- 用户页：`/pages/user/user.vue` - 显示返回按钮 + 标题

## 技术实现

### 核心算法 <mcreference link="https://juejin.cn/post/7457758555173896227" index="0">0</mcreference>
```javascript
// 获取系统信息
const systemInfo = uni.getSystemInfoSync();
this.statusBarHeight = systemInfo.statusBarHeight || 0;

// 微信小程序胶囊适配
if (uni.canIUse('getMenuButtonBoundingClientRect')) {
  this.menuButtonRect = uni.getMenuButtonBoundingClientRect();
  // 计算导航栏高度
  this.navBarHeight = (this.menuButtonRect.top - this.statusBarHeight) * 2 + this.menuButtonRect.height;
}

// 计算总高度
this.totalHeight = this.statusBarHeight + this.navBarHeight;
```

### 布局结构
```
┌─────────────────────────┐
│      状态栏占位区域        │ ← statusBarHeight
├─────────────────────────┤
│ 左侧 │    标题居中    │右侧 │ ← navBarHeight  
└─────────────────────────┘
│      页面内容区域        │
```

## 响应式适配

组件已内置响应式设计：
- 小屏幕（≤375px）：缩小字体和按钮尺寸
- 大屏幕（≥768px）：增大字体和按钮尺寸，提升视觉效果
- 微信小程序：自动适配胶囊按钮尺寸