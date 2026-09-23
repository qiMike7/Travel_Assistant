# 智趣 AI 旅行助手（Meta-Tourism Travel Assistant）

一个「AI 驱动的一站式旅行助手」全栈项目：**微信小程序前端** + **Spring Boot 后端** + **MySQL** + **大模型（DeepSeek）**。

前端页面统一调用后端接口，大模型 API Key 全部收敛在服务端，前端不再暴露密钥。
核心能力：AI 智慧问答、快速智慧旅行攻略规划、旅行相册、精品商城下单与微信支付、地图路线、会员(VIP)体系与配额控制。

> 本文是**总览 + 端到端快速启动教程**：从零克隆后，跟着本文即可把前后端跑起来。
> 更深入的内容分别见：
> - 后端（架构 / 配置 / 全部接口 / 自检）：[`Backend/README.md`](Backend/README.md)
> - 前端（工程结构 / 页面 / 运行到微信小程序 / 页面↔接口映射）：[`Frontend/README.md`](Frontend/README.md)

---

## 一、仓库结构

```
Travel_Assistant/
├── Frontend/     uni-app（Vue3）工程，运行目标：微信小程序
├── Backend/      Spring Boot 3 + JPA + MySQL 后端服务
└── README.md     本文件（总览 + 快速启动）
```

## 二、技术栈

| 层 | 技术 |
|----|------|
| 前端 | uni-app + Vue 3、HBuilderX 或 CLI，目标平台微信小程序 |
| 后端 | Java 17、Spring Boot 3.3.5、Spring Data JPA、JWT(jjwt 0.12.6)、Lombok、Validation |
| 数据库 | MySQL 8.x（自动建库建表，`data.sql` 幂等种子数据） |
| 大模型 | DeepSeek（OpenAI 兼容 `/chat/completions`），由后端 `LlmClient` 代理 |

## 三、前置依赖

| 依赖 | 版本建议 | 用途 |
|------|----------|------|
| JDK | **17 或 21**（⚠️ 不要用 25） | 后端编译运行 |
| Maven | 3.8+ | 后端构建（仓库已带 Maven Wrapper `mvnw`，无需预装也可用） |
| MySQL | 8.x | 数据库，需先启动可连接 |
| HBuilderX | 最新版 | 运行前端（推荐） |
| 微信开发者工具 | 最新版 | 小程序预览/调试 |

> ⚠️ **JDK 版本坑**：JDK 25 与当前 Lombok 版本不兼容，编译会抛 `TypeTag UNKNOWN`。请用 JDK 17 或 21。

---

## 四、快速启动（照做即可跑起来）

### 步骤 1 · 准备数据库
启动本地 MySQL 即可，**无需手动建库建表**：
- 连接串带 `createDatabaseIfNotExist=true`，首次启动自动创建库 `travel_assistant`；
- 表结构由 JPA（`ddl-auto=update`）自动创建；
- 商品、地图路线等种子数据由 `Backend/src/main/resources/db/data.sql` 自动初始化（幂等，可重复执行）。

### 步骤 2 · 配置后端密钥（两种方式任选其一）

后端默认连接 `localhost:3306`、用户 `root`。数据库密码、大模型 Key、JWT 签名密钥 **没有内置默认值**，需自行提供。

**方式 A（本机推荐，不改代码）：覆盖配置文件**
新建 `Backend/config/application.properties`（该文件已被 `.gitignore` 忽略，不会提交，也不影响源码里的 `config` 包）：

```properties
spring.datasource.username=root
spring.datasource.password=你的MySQL密码
travel.llm.api-key=sk-你的DeepSeekKey
# JWT 签名密钥：出于安全无内置默认值，缺失则后端启动即失败（随机串，至少 32 字符）
travel.jwt.secret=请改成至少32字符的随机密钥
```

> 新克隆时该文件不存在，需自己创建。Spring Boot 会自动加载 `./config/` 下的配置，优先级高于 jar 内的 `application.yml`。
> 随机密钥可用 PowerShell 生成：`-join ((48..57)+(97..122)+(65..90) | Get-Random -Count 64 | % {[char]$_})`

**方式 B：环境变量**（适合临时切换 / CI）
关键变量：`MYSQL_PASSWORD`、`LLM_API_KEY`、`JWT_SECRET`（均必填）；其余可选覆盖见 [`Backend/README.md`](Backend/README.md)。

> ⚠️ **PowerShell 坑**：空字符串环境变量会被丢弃、等同未设置。确需空密码时用
> `$env:SPRING_APPLICATION_JSON='{"spring":{"datasource":{"password":""}}}'` 覆盖，而不是 `$env:MYSQL_PASSWORD=''`。

DeepSeek Key 在 <https://platform.deepseek.com> → API Keys 创建（`sk-` 开头）。

### 步骤 3 · 启动后端

在 **`Backend/`** 目录下执行（PowerShell）：

```powershell
# 用仓库自带的 Maven Wrapper，无需预装 Maven
.\mvnw.cmd spring-boot:run
```

或用 IntelliJ IDEA：打开 `Backend` → 等 Maven 导入完成 → 运行主类 `TravelAssistantApplication`。

启动成功后服务监听 `http://localhost:8080`。**验证后端是否 OK**：

```powershell
curl.exe http://localhost:8080/api/products
```

返回带 6 条种子商品的 JSON（`{"code":200,...}`）即代表后端正常。

### 步骤 4 · 启动前端（微信小程序）

1. 打开**微信开发者工具** → 设置 → 安全设置 → 打开**服务端口**（供 HBuilderX 调用）；
2. 打开 **HBuilderX** → 文件 → 打开目录 → 选择 **`Frontend/`**；
3. 菜单：运行 → 运行到小程序模拟器 → 微信小程序；
4. 首次会自动编译，完成后微信开发者工具自动打开预览。

前端默认请求 `http://localhost:8080`（见 `Frontend/common/utils/request.js` 的 `BASE_URL`），模拟器下保持默认即可。

> ⚠️ **小程序域名校验**：模拟器访问 localhost 需在开发者工具「详情 → 本地设置」勾选
> 「不校验合法域名…」；本工程 `manifest.json` 的 `mp-weixin.setting.urlCheck` 已设为 `false`。
> 真机预览需把 `BASE_URL` 改成电脑局域网 IP（如 `http://192.168.1.10:8080`），手机与电脑同网段、防火墙放行 8080。

### 步骤 5 · 在小程序里注册 / 登录
进入「我的」页 → 点击顶部头像区 → 注册或登录。token 存在本地（`travel_token`），后续请求自动携带 `Authorization: Bearer <token>`。

至此即可正常使用各功能。详细运行/排错见 [`Frontend/README.md`](Frontend/README.md)。

---

## 五、核心业务规则（会员 / 配额）

后端统一返回 `{ code, message, data }` 且 HTTP 恒为 200，前端按 `code` 判断结果。与会员相关的业务码：

| 场景 | 规则 | 业务码 |
|------|------|--------|
| AI 智慧问答 | 非会员每日免费 **5 次**，会员不限 | `4291` 超出后前端引导开通 |
| 会员客服 | **VIP 专属**，非会员不可用 | `4292` 前端引导开通并退出 |
| 旅行攻略规划 | 生成不限；**保存**名额：非会员 **1 份** / 会员 **5 份**，删除可释放 | `4293` 前端引导删除或开通 |
| 开通 VIP | `POST /api/users/me/vip?hours=12`，默认 **12 小时**；分享领时长同为 12h | — |
| 下单 / 支付 | 下单为「待付款」→ 支付确认推进「已付款」（幂等）；取消回退库存与销量，禁止重复取消 | — |

## 六、功能模块一览

| 模块 | 后端接口前缀 | 主要前端页面 |
|------|--------------|--------------|
| 认证 / 用户 / VIP | `/api/auth`、`/api/users/me` | `login`、`user`、`profile`、`settings`、`share` |
| 旅行偏好 | `/api/preferences` | `preference` |
| 商城 / 订单 / 支付 | `/api/products`、`/api/orders`、`/api/payments` | `guide`、`order`、`order-confirm` |
| AI 问答 / 客服 | `/api/chat`、`/api/kefu` | `chat`、`huiyuankefu` |
| 攻略规划 | `/api/itinerary` | `shopping`、`plan-history` |
| 旅行相册 / 上传 | `/api/albums`、`/api/files` | `album`、`lvxingxiangce` |
| 地图路线 | `/api/routes` | `shopping` |

## 七、常见问题速查

- **后端起不来 / `Access denied (using password: NO)`**：密码没读到。用第 2 步方式 A 写 `config/application.properties`，注意 PowerShell 空变量坑。
- **后端启动即报 `travel.jwt.secret 未配置`**：JWT 签名密钥不再有内置默认值，按第 2 步在 `config/application.properties` 加一行 `travel.jwt.secret=...`（至少 32 字符）或设 `JWT_SECRET` 环境变量。
- **对话报「未配置大模型 API Key」**：`LLM_API_KEY` 为空，配一个 DeepSeek Key 重启即可。
- **前端「网络连接失败，请检查服务是否启动」**：后端未启动或 `BASE_URL` 不对，先 `curl http://localhost:8080/api/products` 确认。
- **小程序报「url not in domain list」**：没关闭域名校验，见步骤 4 提示，改完需重新编译。
- **更多排错**：见 [`Backend/README.md`](Backend/README.md) 与 [`Frontend/README.md`](Frontend/README.md)。

祝运行顺利 🎉
