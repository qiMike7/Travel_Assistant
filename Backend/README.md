# 智趣AI旅行助手（Meta-Tourism Travel Assistant）

本项目由 **前端 uni-app（`Frontend/`）** 与 **后端 Spring Boot（`Backend/`）** 两部分组成，
数据库使用 **MySQL**。前端各页面已从原来的本地 mock / 直连大模型改为统一调用后端接口，
大模型 API Key 全部收敛到服务端，前端不再暴露密钥。

```
Travel_Assistant/
├── Frontend/     uni-app（Vue3）移动端工程
└── Backend/      Spring Boot 3 + JPA + MySQL 后端服务（本 README 所在目录）
```

---

## 一、技术栈

### 后端
- Java 17（可用 JDK 17 / 21 编译运行）
- Spring Boot 3.3.5
- Spring Data JPA（Hibernate，`ddl-auto=update` 自动建表）
- MySQL 8.x（Connector/J）
- JWT 鉴权（jjwt 0.12.6）
- Lombok、Spring Validation
- RestClient 代理调用大模型（默认 **DeepSeek**，OpenAI 兼容 `/chat/completions`）

### 前端
- uni-app + Vue 3（HBuilderX 或 CLI 均可）
- 统一请求封装：`Frontend/common/utils/request.js`
- 登录态工具：`Frontend/common/utils/auth.js`
- 接口集中定义：`Frontend/common/api/index.js`

---

## 二、环境要求

| 依赖 | 版本建议 | 说明 |
|------|----------|------|
| JDK | 17 及以上 | 后端编译运行 |
| Maven | 3.8+ | 后端构建（IDEA 自带即可） |
| MySQL | 8.x | 数据库，需先启动并可连接 |
| HBuilderX | 最新版 | 运行前端（推荐），或使用 uni-app CLI |
| Node.js | 18+ | 仅 CLI 方式需要 |

---

## 三、后端运行步骤

### 1. 准备数据库
确保本地 MySQL 已启动。默认连接信息（见 `src/main/resources/application.yml`）：

- 地址：`jdbc:mysql://localhost:3306/travel_assistant`
- 用户名：`root`
- 密码：**无内置默认值**，需自行提供（见下方两种方式）

> 数据库 `travel_assistant` **无需手动创建**，连接串带有 `createDatabaseIfNotExist=true`，
> 首次启动会自动建库；表结构由 JPA 自动创建；商品与地图路线等种子数据由
> `src/main/resources/db/data.sql` 自动初始化（幂等，可重复执行）。

如果你的 MySQL 账号/密码/端口与上面不同，有两种覆盖方式：

**方式一（本机推荐）：不改代码、不设环境变量**
在 `Backend/config/application.properties` 写覆盖值即可。Spring Boot 会自动加载 `./config/` 下的配置，
且**优先级高于** jar 内的 `application.yml`；该文件已在 `Backend/.gitignore` 中被 `config/` 规则忽略，不会提交：

```properties
spring.datasource.username=root
spring.datasource.password=你的密码
travel.llm.api-key=sk-你的DeepSeekKey
```

> 本机已按此方式配好，所以在 `Backend/` 下直接 `mvn spring-boot:run`（或 IDEA 里点 Run）就能启动，无需任何环境变量。
> 换电脑/新克隆时该文件不存在，需自行创建，或改用方式二。

**方式二：环境变量**（适合临时切换或 CI）

| 环境变量 | 默认值 | 含义 |
|----------|--------|------|
| `MYSQL_HOST` | `localhost` | 数据库主机 |
| `MYSQL_PORT` | `3306` | 端口 |
| `MYSQL_DB` | `travel_assistant` | 库名 |
| `MYSQL_USER` | `root` | 用户名 |
| `MYSQL_PASSWORD` | 无（需自行提供） | 密码，出于安全不再内置默认值 |

> 密码不再内置默认值，必须显式提供；PowerShell 里不要用 `$env:X=''` 传值——
> 空值的环境变量会被直接丢弃、等同于未设置；确需空密码（数据库账号本就无密码）时，
> 用 `$env:SPRING_APPLICATION_JSON='{"spring":{"datasource":{"password":""}}}'` 覆盖。

### 2. 配置大模型密钥（重要，已改为 **DeepSeek**）
后端代理两类对话（见 `application.yml` 的 `travel.llm`），默认均调 **DeepSeek**（OpenAI 兼容接口）：

| 用途 | 前端页面 | 接口 | 默认模型 | 需要的环境变量 |
|------|----------|------|----------|----------------|
| 智慧问答 | `chat.vue` | `POST /api/chat` | `deepseek-chat` | **必填** `LLM_API_KEY` |
| 会员客服 | `huiyuankefu.vue` | `POST /api/kefu/chat` | `deepseek-chat` | 可不填：未设 `KEFU_API_KEY` 时**自动复用** `LLM_API_KEY` |

密钥在 <https://platform.deepseek.com> → API Keys 创建（`sk-` 开头），只放服务端，不要写进前端。
未配置 Key 时，对话接口会直接返回：「未配置大模型 API Key，请设置环境变量 LLM_API_KEY」。

> 模型名可用 `GET https://api.deepseek.com/v1/models`（带 Bearer Key）查询自己账号实际开通的模型。
> 实测该接口返回的是 `deepseek-flash` / `deepseek-v4-pro`；请求 `deepseek-chat` 仍可用，
> 但响应体的 `model` 会被服务端回写为实际处理的模型名。如需固定模型，改 `LLM_MODEL` 即可。

可用环境变量（括号内为默认值）：

```
LLM_BASE_URL      DeepSeek 接口地址（默认 https://api.deepseek.com/v1）
LLM_API_KEY       DeepSeek API Key（必填，无默认值）
LLM_MODEL         模型名（默认 deepseek-chat；推理型可用 deepseek-reasoner）
LLM_TIMEOUT       单请求超时秒数（默认 60）
LLM_HISTORY_LIMIT 下发给模型的历史消息条数上限（默认 20，防止上下文无限增长）
KEFU_BASE_URL     客服接口地址（默认同 DeepSeek，可指向豆包等其它厂商）
KEFU_API_KEY      客服自有 Key（不设则复用 LLM_API_KEY）
KEFU_MODEL        客服模型名（默认 deepseek-chat）
```

> 想换回豆包/ModelScope？DeepSeek 用的是 OpenAI 兼容协议，只需把 `LLM_BASE_URL` / `LLM_MODEL` 改成对应厂商的值并换 Key，代码无需修改。

### 3. 其他可配置项

| 环境变量 | 默认值 | 含义 |
|----------|--------|------|
| `JWT_SECRET` | 内置开发密钥 | JWT 签名密钥，**生产必须覆盖** |
| `UPLOAD_DIR` | `./uploads` | 图片（头像/相册）本地存储目录 |

上传的图片存到 `travel.upload.dir`，通过 `travel.upload.url-prefix`（默认 `/uploads`）对外访问。
接口返回的是**相对路径**（如 `/uploads/2026-09-22/xxx.jpg`），
前端 `request.js` 的 `toAbsoluteUrl()` 会拼上 `BASE_URL` 后交给 `<image>` 使用
（小程序不支持相对路径资源，这一步是必需的）。

### 4. 启动后端

在 **`Backend/`** 目录下执行：

```bash
# 方式一：Maven 命令行
mvn spring-boot:run

# 方式二：先打包再运行
mvn clean package -DskipTests
java -jar target/travel-assistant-backend-1.0.0.jar
```

或用 **IntelliJ IDEA**：打开 `Backend` 目录 → 等待 Maven 导入完成 →
运行主类 `TravelAssistantApplication`。

启动成功后，服务监听 `http://localhost:8080`。控制台无报错、
且访问商品接口能看到数据即代表后端 OK：

```bash
curl http://localhost:8080/api/products
```

> 命令行设置环境变量（PowerShell 示例）：
> ```powershell
> $env:MYSQL_PASSWORD="你的密码"
> $env:LLM_API_KEY="sk-你的DeepSeekKey"
> mvn spring-boot:run
> ```
>
> IDEA 中运行：Run/Debug Configurations → Environment variables 填同一串即可。

### 5. 启动自检（已实测通过的接口）
后端启动后，可用下面命令逐个自检（PowerShell）：

```powershell
curl.exe http://localhost:8080/api/products
curl.exe -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{\"username\":\"demo\",\"password\":\"test123456\"}'
curl.exe -X POST http://localhost:8080/api/chat   -H "Content-Type: application/json" -H "Authorization: Bearer 上一步返回的token" -d '{\"content\":\"推荐一个周末去哪玩\"}'
curl.exe -X POST http://localhost:8080/api/files/upload -H "Authorization: Bearer 上一步返回的token" -F "file=@D:/某张图.jpg"
```

当前已端到端实测通过（含真实 DeepSeek 对话）：

| 能力 | 接口 | 实测结果 |
|------|------|----------|
| 免登录商品列表 | `GET /api/products` | 200，返回 6 条种子商品 |
| 注册/登录 + JWT | `POST /api/auth/register`、`/login` | 200，返回 token 与 `user.id` |
| 鉴权拦截 | 无 token / 非法 token 访问 `GET /api/users/me` | body 内 `code=401`（HTTP 仍为 200，统一响应体设计） |
| AI 问答 | `POST /api/chat` | 200，拿到模型真实回复，会话与两条消息落库 |
| 客服问答 | `POST /api/kefu/chat` | 200，未配 `KEFU_API_KEY` 时自动复用主 Key |
| 历史回显 | `GET /api/chat/sessions`、`/sessions/{id}/messages` | 200，`roles=user,assistant` |
| 下单扣库存 | `POST /api/orders` | 200，库存 -2、销量 +2 |
| 取消回退 | `DELETE /api/orders/{id}` | 库存复原；重复取消被拒绝 |
| 超卖拦截 | `quantity` 大于库存 | 被拒绝 |
| 资料/偏好 | `PUT /api/users/me`、`PUT /api/preferences` | 200，再 GET 可验证已落库 |
| 上传+静态回源 | `POST /api/files/upload` → `GET /uploads/...` | 200，`image/png` 可正常读回 |
| 相册增删改查 | `POST/GET/PUT/DELETE /api/albums` | 200；空照片返回 400；他人相册访问/删除返回 404 |
| 地图路线 | `GET /api/routes` | 200，免登录可取 |
| 开通 VIP | `POST /api/users/me/vip` | 200，默认 12 小时（`hours` 可传参），`vip` 推导为 true |

> 表中 `code` 指响应体里的业务码；本项目统一返回 HTTP 200，前端 `request.js` 根据 `code` 判断成功与否。

---

### 6. 已内置的性能与安全处理

| 位置 | 处理 | 目的 |
|------|------|------|
| `LlmClient` | 连接超时 10s、读超时 `LLM_TIMEOUT`（默认 60s） | 防止上游模型卡死时一直占着 Tomcat 线程 |
| `ChatService` | 只下发最近 `LLM_HISTORY_LIMIT` 条历史；对话方法不在事务内调用 HTTP | 控制 token 成本；避免大模型耗时期间长期占用数据库连接 |
| `OrderService` | 下单/取消时对商品行加 `PESSIMISTIC_WRITE` 锁；取消时回退库存与销量，并阻止重复取消 | 防止并发超卖；修复取消后库存不回滚 |
| 实体索引 | `t_order(user_id,create_time)`、`t_chat_session`、`t_chat_message`、`t_album`、`t_album_photo` 补索引 | 列表接口按 user_id / session_id 过滤+排序，无索引会全表扫 |
| `JwtUtil` | 签名密钥构建一次后缓存 | 每请求解析 token 时不再重复派生密钥 |
| `UserService` | `vip` 按 `vipExpireTime` 推导（`isVipActive`） | 之前一旦开通就永久为 true |
| 上传 | 扩展名白名单 + UUID 重命名 + 日期分目录 | 防目录穿越 / 任意文件写入 |
| 前端 `request.js` | `buildUrl()` / `toAbsoluteUrl()`；普通接口 30s、AI 接口 120s 超时 | 小程序 `<image>` 不支持相对路径；AI 回复耗时长不能被默认超时掉 |

---

## 四、前端运行步骤（目标平台：微信小程序）

工程 `Frontend/manifest.json` 已配置好微信小程序：

```json
"mp-weixin": {
  "appid": "wxdbbabadc246625a4",
  "setting": { "urlCheck": false }, 
  "usingComponents": true
}
```

> 如果要换成自己的小程序，把 `appid` 改为你的小程序 AppID（没有的话可在微信公众平台申请，或使用"测试号"）。

### 1. 确认后端地址
所有请求地址集中在 `Frontend/common/utils/request.js`：

```js
export const BASE_URL = 'http://localhost:8080'
```

| 运行方式 | BASE_URL 应设为 |
|----------|-----------------|
| 微信开发者工具**模拟器** | `http://localhost:8080`（保持默认即可） |
| 开发者工具**真机预览**（手机微信扫码） | `http://<电脑局域网IP>:8080`，如 `http://192.168.1.10:8080` |
| 正式发布 | 必须 **HTTPS** 域名，并在小程序后台配置 request / uploadFile / downloadFile 合法域名 |

**模拟器访问 localhost 后端的前提**：开发者工具需关闭域名校验（两处任选其一即可，建议都确认）：
1. 开发者工具右上角「详情 → 本地设置」→ 勾选「不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书」；
2. `manifest.json` 中 `mp-weixin.setting.urlCheck` 为 `false`（本工程已配置，重新编译后会自动同步该项）。

> 真机预览时手机与电脑需在**同一局域网**，且电脑防火墙需放行 8080 端口；此时开发版小程序同样保持勾选第 1 项即可用 HTTP 调试。正式发版前必须换成备案过的 HTTPS 域名并加入白名单，否则线上请求会被微信拦截。

### 2. 用 HBuilderX 运行到微信开发者工具（推荐）
1. 安装并登录**微信开发者工具**，在其「设置 → 安全设置」中打开**服务端口**（供 HBuilderX 调用）；
2. HBuilderX → 文件 → 打开目录 → 选择 **`Frontend/`**；
3. 菜单：运行 → 运行到小程序模拟器 → 微信小程序；
4. 首次会自动安装依赖并编译，完成后微信开发者工具会自动打开小程序预览。

### 3. 用 CLI 运行（可选）
在 **`Frontend/`** 目录下：

```bash
npm install
npm run dev:mp-weixin   # 监听编译，产物在 Frontend/dist/dev/mp-weixin
```

然后打开**微信开发者工具** → 导入项目 → 目录选择 `Frontend/dist/dev/mp-weixin`，
AppID 填 `manifest.json` 中同一个即可。改代码后 uni-app 会自动重新编译，开发者工具热刷新。

### 4. 页面能力在小程序下的说明
- **地图（map-test / shopping）**：小程序端 `<map>` 为原生组件，无需申请 key；轨迹数据来自后端 `GET /api/routes`。
- **图片上传（头像 / 旅行相册）**：使用 `uni.uploadFile` 上传至 `POST /api/files/upload`，小程序端可直接选系统相册/拍照；
  后端返回的 `/uploads/...` 相对路径由 `common/utils/request.js` 的 `toAbsoluteUrl()` 统一拼成绝对地址（小程序 `<image>` 不支持相对路径）。
- **登录态（JWT）**：token 存在 `uni.setStorageSync`，小程序存储中，重新编译后仍保留；「我的」页点击头像区即可进入登录/注册。
- **语音播报（会员客服）**：小程序端不支持 Web Speech API，会提示"当前环境不支持"，属预期行为。

### 5. 登录 / 注册
- 工程已内置登录注册页：`Frontend/pages/login/login.vue`，并在 `pages.json` 注册路由。
- 进入「我的」页 → 点击顶部头像区即可进入登录页；也可从任意需登录功能被自动引导。
- 注册后 token 保存在本地（`travel_token`），后续请求自动携带 `Authorization: Bearer <token>`。

---

## 五、前端页面与接口对应关系

| 页面 | 功能 | 主要调用的后端接口 | 是否需要登录 |
|------|------|--------------------|--------------|
| `login.vue` | 登录 / 注册 | `POST /api/auth/login`、`POST /api/auth/register` | 否 |
| `chat.vue` | 智慧问答（AI） | `POST /api/chat` | 是 |
| `huiyuankefu.vue` | 会员客服（AI） | `POST /api/kefu/chat` | 是 |
| `guide.vue` | 精品商城、下单 | `GET /api/products`、`POST /api/orders` | 下单需登录 |
| `order.vue` | 我的订单 | `GET /api/orders` | 是 |
| `user.vue` | 我的（资料/VIP） | `GET /api/users/me`、`POST /api/users/me/vip` | 是（未登录引导） |
| `profile.vue` | 个人资料编辑/头像 | `GET/PUT /api/users/me`、`POST /api/files/upload` | 是 |
| `preference.vue` | 旅行偏好 | `GET/PUT /api/preferences` | 是（未登录退回本地缓存） |
| `settings.vue` | 通知/深色模式/退出 | `GET/PUT /api/preferences` | 是 |
| `lvxingxiangce.vue` | 旅行相册 | `POST /api/files/upload`、`POST /api/albums` | 是 |
| `map-test.vue` / `shopping.vue` | 地图路线 | `GET /api/routes` | 否 |

### 统一返回格式
后端所有接口返回：

```json
{ "code": 200, "message": "ok", "data": { } }
```

- `code=200` 成功，`request.js` 会把 `data` 直接返回给业务代码；
- `code=401` 未登录/登录过期，自动清除本地 token 并跳转登录页；
- 其他 code 弹出 `message` 提示。

---

## 六、接口一览（后端）

| 分类 | 方法 | 路径 | 鉴权 |
|------|------|------|------|
| 认证 | POST | `/api/auth/register` | 公开 |
| 认证 | POST | `/api/auth/login` | 公开 |
| 用户 | GET | `/api/users/me` | 登录 |
| 用户 | PUT | `/api/users/me` | 登录 |
| 用户 | PUT | `/api/users/me/password` | 登录 |
| 用户 | POST | `/api/users/me/vip?hours=12` | 登录 |
| 偏好 | GET | `/api/preferences` | 登录 |
| 偏好 | PUT | `/api/preferences` | 登录 |
| 商品 | GET | `/api/products`、`/api/products/{id}` | 公开 |
| 订单 | GET/POST | `/api/orders` | 登录 |
| 订单 | GET/DELETE | `/api/orders/{id}` | 登录 |
| 相册 | GET/POST | `/api/albums` | 登录 |
| 相册 | GET/PUT/DELETE | `/api/albums/{id}` | 登录 |
| 路线 | GET | `/api/routes`、`/api/routes/{id}` | 公开 |
| 对话 | POST | `/api/chat` | 登录 |
| 对话 | POST | `/api/kefu/chat` | 登录 |
| 对话 | GET/DELETE | `/api/chat/sessions`、`/api/chat/sessions/{id}` | 登录 |
| 上传 | POST | `/api/files/upload`（表单字段 `file`） | 登录 |

---

## 七、常见问题（FAQ）

**Q1：前端提示「网络连接失败，请检查服务是否启动」**
后端未启动或 `BASE_URL` 不对。先确认 `curl http://localhost:8080/api/products` 有返回，
再按运行环境改 `request.js` 的 `BASE_URL`。

**Q2：会员客服提示「未配置大模型 API Key」**
`LLM_API_KEY` 与 `KEFU_API_KEY` 都为空。设一个 `LLM_API_KEY`（DeepSeek Key）即可，
客服会自动复用；要给客服单独用别的厂商时再设 `KEFU_API_KEY` + `KEFU_BASE_URL` + `KEFU_MODEL`。

**Q3：智慧问答回复报错「大模型服务暂时不可用」**
`LLM_API_KEY` 失效/余额不足，或本机访问 `api.deepseek.com` 网络不通。看后端日志里的具体异常（401/402/超时），
确认后覆盖 `LLM_API_KEY` 重启；慢回答可调大 `LLM_TIMEOUT`。

**Q4：登录/下单/相册等功能跳转到登录页**
这些接口需要登录。先在「我的」页完成注册/登录。

**Q5：数据库连不上（Access denied / Unknown database）**
检查 MySQL 服务是否启动、用户名密码是否与本机一致。推荐在已被 git 忽略的
`Backend/config/application.properties` 里覆盖（见「三、后端运行步骤」1）；库会自动创建，无需手动建。
注意 `Access denied ... (using password: NO)` 表示服务端收到了空密码——该账号其实设了密码。

**Q6：真机打不开接口**
真机无法访问 `localhost`，需把 `BASE_URL` 改成电脑局域网 IP，且手机与电脑同网段。

**Q7：微信开发者工具报「url not in domain list / 合法域名校验出错」**
未关闭域名校验。在开发者工具「详情 → 本地设置」勾选「不校验合法域名…」后重新编译；
确认 `manifest.json` 的 `mp-weixin.setting.urlCheck` 为 `false`。若仍报错，检查是否改过 `BASE_URL` 后未重新编译（小程序只在启动时读取域名校验设置，改动后建议重新编译+清缓存）。

---

## 八、目录结构速览（后端）

```
Backend/src/main/java/com/meta/travel/
├── common/      统一返回 Result、异常、全局异常处理
├── config/      JWT / 大模型 / 上传 / WebMvc(CORS+拦截器+静态资源) 配置
├── controller/  REST 接口层
├── dto/         请求/响应对象（request / response）
├── entity/      JPA 实体
├── repository/  数据访问层
├── security/    JWT 工具、登录上下文、鉴权拦截器
├── service/     业务逻辑（含 LlmClient 大模型代理）
└── util/        密码、日期工具
Backend/src/main/resources/
├── application.yml   配置（端口、数据源、travel.*）
└── db/data.sql       种子数据（商品、地图路线）
```

祝运行顺利！如有问题优先查看「常见问题」。
