# 前端说明 · 智趣 AI 旅行助手（uni-app / 微信小程序）

本目录是基于 **uni-app + Vue 3** 的移动端工程，运行目标为**微信小程序**。
所有数据来自后端 Spring Boot 服务（默认 `http://localhost:8080`），前端不再直连大模型、不持有任何 API Key。

> 项目总览与端到端快速启动见仓库根目录 [`../README.md`](../README.md)；后端接口详见 [`../Backend/README.md`](../Backend/README.md)。

---

## 一、工程结构

```
Frontend/
├── App.vue                     应用生命周期 / 全局样式入口
├── main.js                     应用入口（createSSRApp）
├── index.html                  H5 入口
├── pages.json                  路由 / 页面样式 / tabBar 配置
├── manifest.json               各端配置（微信小程序 appid、权限、urlCheck 等）
├── uni.scss                    全局样式变量
├── uni.promisify.adaptor.js    uni API Promise 化适配
├── common/
│   ├── api/index.js            后端接口集中定义（按模块导出 xxxApi）
│   ├── utils/request.js        统一请求封装（BASE_URL / JWT / 返回体解析 / 上传）
│   ├── utils/auth.js           登录态存取（token、用户信息）
│   ├── utils/pay.js            微信支付调起封装
│   └── style/base.scss         基础样式
├── components/
│   └── CustomNavBar.vue        自定义导航栏
├── pages/                      各业务页面（每页一个目录）
└── static/                     图片、图标等静态资源
```

## 二、页面清单与导航

底部 **tabBar** 四个主页面（见 `pages.json`）：

| Tab | 页面 | 说明 |
|-----|------|------|
| 首页 | `pages/index/index` | 入口与功能引导 |
| 相册 | `pages/album/album` | 旅行相册总览 |
| 精品购物 | `pages/guide/guide` | 商城 / 下单 |
| 我的 | `pages/user/user` | 资料 / VIP / 各功能入口 |

其余子页面：`chat`（AI 问答）、`huiyuankefu`（会员客服）、`shopping`（攻略规划 + 地图）、
`plan-history`（攻略历史）、`order` / `order-confirm`（订单 / 支付确认）、
`profile`（资料编辑/头像）、`preference`（旅行偏好）、`settings`（设置）、
`lvxingxiangce`（相册详情）、`login`（登录/注册）、`aboutus` / `help` / `privacy` / `share`。

## 三、网络请求封装（`common/utils/request.js`）

- **`BASE_URL`**：后端地址，默认 `http://localhost:8080`。
- **自动带 JWT**：从本地存储读取 token，注入 `Authorization: Bearer <token>`。
- **统一返回体**：后端恒为 HTTP 200 + `{ code, message, data }`；`code=200` 时 `request()` 直接把 `data` 交给业务代码。
- **401 自动跳转登录**：清除本地登录态并跳到 `login` 页。
- **额度类业务码静默处理**：`4291 / 4292 / 4293`（提问额度 / 会员专属 / 攻略保存上限）不弹通用 toast，交由对应页面弹窗引导开通会员。
- **超时**：普通接口 30s，AI 对话/攻略生成接口 120s（`LLM_TIMEOUT`）。
- **`toAbsoluteUrl()`**：把后端返回的相对资源路径（如 `/uploads/xxx.png`）拼成绝对地址——微信小程序 `<image>` 不支持相对路径，这一步是必需的。
- **`uploadFile()`**：封装 `uni.uploadFile` 上传到 `POST /api/files/upload`。

接口按模块集中在 `common/api/index.js`：`authApi`、`userApi`、`preferenceApi`、`productApi`、
`orderApi`、`payApi`、`albumApi`、`chatApi`、`planApi`、`fileApi`。

## 四、页面 ↔ 后端接口对应关系

| 页面 | 功能 | 主要接口 | 需登录 |
|------|------|----------|--------|
| `login` | 登录 / 注册 / 用户名查重 | `POST /api/auth/login`、`/register`、`GET /api/auth/check-username` | 否 |
| `chat` | AI 智慧问答（非会员每日 5 次） | `POST /api/chat`、`GET /api/chat/quota`、`/chat/sessions` | 是 |
| `huiyuankefu` | 会员客服（VIP 专属） | `POST /api/kefu/chat` | 是 |
| `shopping` | 快速攻略规划 + 地图路线 | `POST /api/itinerary`(生成)、`/api/itinerary/save`(保存)、`GET /api/routes` | 是 |
| `plan-history` | 攻略历史 列表/详情/删除 | `GET /api/itinerary`、`/api/itinerary/{id}`、`DELETE /api/itinerary/{id}` | 是 |
| `guide` | 精品商城、下单 | `GET /api/products`、`POST /api/orders` | 下单需登录 |
| `order` / `order-confirm` | 我的订单 / 支付 | `GET /api/orders`、`POST /api/payments/wxpay`、`/api/payments/confirm` | 是 |
| `user` | 我的（资料/VIP 入口） | `GET /api/users/me`、`POST /api/users/me/vip` | 是（未登录引导） |
| `profile` | 资料编辑 / 头像上传 | `GET/PUT /api/users/me`、`POST /api/files/upload` | 是 |
| `preference` / `settings` | 旅行偏好 | `GET/PUT /api/preferences` | 是 |
| `album` / `lvxingxiangce` | 旅行相册增删改查 | `GET/POST/PUT/DELETE /api/albums`、`POST /api/files/upload` | 是 |
| `share` | 分享领会员时长（12h） | `POST /api/users/me/vip?hours=12` | 是 |

## 五、运行到微信开发者工具

### 方式 A：HBuilderX（推荐）
1. 微信开发者工具 →「设置 → 安全设置」→ 打开**服务端口**（供 HBuilderX 调用）；
2. HBuilderX → 文件 → 打开目录 → 选择 **`Frontend/`**；
3. 运行 → 运行到小程序模拟器 → 微信小程序；
4. 首次自动编译，完成后开发者工具自动打开预览。

### 方式 B：CLI
在 `Frontend/` 下：

```bash
npm install
npm run dev:mp-weixin     # 监听编译，产物在 Frontend/dist/dev/mp-weixin
```

再用微信开发者工具导入 `Frontend/dist/dev/mp-weixin`，AppID 填 `manifest.json` 里同一个。

### `BASE_URL` 随运行环境切换

| 运行方式 | BASE_URL 设为 |
|----------|----------------|
| 开发者工具**模拟器** | `http://localhost:8080`（默认） |
| **真机预览** | `http://<电脑局域网IP>:8080`，手机与电脑同网段、防火墙放行 8080 |
| **正式发布** | 必须 HTTPS 备案域名，并在小程序后台配置 request/uploadFile/downloadFile 合法域名 |

## 六、常见问题（前端）

- **提示「网络连接失败，请检查服务是否启动」**：后端未启动或 `BASE_URL` 不对，先确认 `curl http://localhost:8080/api/products` 有返回。
- **报「url not in domain list / 合法域名校验出错」**：开发者工具「详情 → 本地设置」勾选「不校验合法域名…」，确认 `manifest.json` 的 `mp-weixin.setting.urlCheck` 为 `false`；改完 `BASE_URL` 需**重新编译 + 清缓存**。
- **图片不显示**：后端返回的是相对路径，务必经 `toAbsoluteUrl()` 拼接后再交给 `<image>`。
- **会员客服提示「未配置大模型 API Key」**：这是后端配置问题，去 `Backend/config/application.properties` 或环境变量配 `LLM_API_KEY`。
- **语音播报无反应**：微信小程序不支持 Web Speech API，会提示"当前环境不支持"，属预期行为。
- **`uni.showModal` 弹窗不出现**：按钮文案超过 4 个汉字会导致小程序静默失败，需缩短按钮文本。

## 七、微信小程序相关配置（`manifest.json`）

```json
"mp-weixin": {
  "appid": "wxdbbabadc246625a4",
  "setting": { "urlCheck": false },
  "usingComponents": true,
  "permission": { "scope.userLocation": { "desc": "用于在地图上展示您的当前位置与周边旅行路线" } },
  "requiredPrivateInfos": [ "getLocation" ]
}
```

> 换成自己的小程序时，把 `appid` 改为你的 AppID；地图 `<map>` 为原生组件无需申请 key，
> 但定位需在 `permission` 声明 `scope.userLocation`（本工程已配置）。
