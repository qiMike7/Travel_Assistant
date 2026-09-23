# 后端说明 · 智趣 AI 旅行助手（Spring Boot）

Spring Boot 3 + JPA + MySQL 的后端服务，为 uni-app 微信小程序提供 REST 接口，
并作为**大模型代理**（默认 DeepSeek，OpenAI 兼容）——API Key 全部保存在服务端，前端不接触密钥。

> 项目总览与端到端启动：见仓库根目录 [`../README.md`](../README.md)
> 前端（工程结构 / 页面 / 运行到微信小程序 / 页面↔接口映射）：见 [`../Frontend/README.md`](../Frontend/README.md)

---

## 一、技术栈

- Java 17（可用 JDK 17 / 21 编译运行；⚠️ **不要用 JDK 25**，与当前 Lombok 不兼容会抛 `TypeTag UNKNOWN`）
- Spring Boot 3.3.5、Spring Data JPA（Hibernate，`ddl-auto=update` 自动建表）
- MySQL 8.x（Connector/J）
- JWT 鉴权（jjwt 0.12.6）+ Spring Validation + Lombok
- `RestClient` 代理调用大模型

## 二、架构与分层

```
src/main/java/com/meta/travel/
├── common/      统一返回 Result、ResultCode、BusinessException、全局异常处理
├── config/      JWT / 大模型 / 支付 / 上传 / WebMvc(CORS+拦截器+静态资源) 配置
├── controller/  REST 接口层
├── dto/         请求 / 响应对象（request / response）
├── entity/      JPA 实体
├── repository/  数据访问层
├── security/    JwtUtil、UserContext、AuthInterceptor、@PublicApi
├── service/     业务逻辑（含 LlmClient 大模型代理、ItineraryService 攻略规划）
└── util/        密码、日期工具
src/main/resources/
├── application.yml   配置（端口、数据源、travel.*）
└── db/data.sql       种子数据（商品、地图路线；幂等）
```

**鉴权模型**：`AuthInterceptor` 默认拦截 `/api/**`，校验 `Authorization: Bearer <token>` 并写入 `UserContext`；
标了 `@PublicApi`（方法或类级）的接口免登录（如商品、路线、注册登录）。
所有接口统一返回 `Result`：`{ code, message, data }`，**HTTP 恒为 200**，业务成败由 `code` 表示。

---

## 三、配置说明

配置集中在 `application.yml` 的 `travel.*` 与数据源。敏感值支持环境变量覆盖，且**数据库密码 / 大模型 Key 无内置默认值**。

### 1. 数据源（`spring.datasource`）
默认 `jdbc:mysql://localhost:3306/travel_assistant`、用户 `root`。
连接串带 `createDatabaseIfNotExist=true`，**库自动创建**；表由 JPA 建；种子数据由 `data.sql` 幂等初始化。

| 环境变量 | 默认值 | 含义 |
|----------|--------|------|
| `MYSQL_HOST` | `localhost` | 数据库主机 |
| `MYSQL_PORT` | `3306` | 端口 |
| `MYSQL_DB` | `travel_assistant` | 库名 |
| `MYSQL_USER` | `root` | 用户名 |
| `MYSQL_PASSWORD` | 无（必填） | 密码 |

### 2. 大模型（`travel.llm`，默认 DeepSeek）
后端代理两类对话，均为 OpenAI 兼容协议：

| 用途 | 前端页面 | 接口 | 默认模型 | 需要的 Key |
|------|----------|------|----------|------------|
| 智慧问答 | `chat.vue` | `POST /api/chat` | `deepseek-chat` | 必填 `LLM_API_KEY` |
| 会员客服 | `huiyuankefu.vue` | `POST /api/kefu/chat` | `deepseek-chat` | 不设 `KEFU_API_KEY` 时自动复用 `LLM_API_KEY` |

Key 在 <https://platform.deepseek.com> 创建（`sk-` 开头）。未配置时对话接口返回「未配置大模型 API Key」。

| 环境变量 | 默认值 | 含义 |
|----------|--------|------|
| `LLM_BASE_URL` | `https://api.deepseek.com/v1` | 主模型接口地址 |
| `LLM_API_KEY` | 无（必填） | 主模型 Key |
| `LLM_MODEL` | `deepseek-chat` | 模型名 |
| `LLM_TIMEOUT` | `60` | 单请求超时秒数 |
| `LLM_HISTORY_LIMIT` | `20` | 下发给模型的历史消息条数上限 |
| `KEFU_BASE_URL` | 同 DeepSeek | 客服接口地址（可指向别的厂商） |
| `KEFU_API_KEY` | 不设则复用主 Key | 客服自有 Key |
| `KEFU_MODEL` | `deepseek-chat` | 客服模型名 |

> 换厂商（豆包/ModelScope 等）只改 `LLM_BASE_URL` / `LLM_MODEL` 并换 Key，代码无需改动。

### 3. 其他

| 环境变量 | 默认值 | 含义 |
|----------|--------|------|
| `JWT_SECRET` | 无（必填） | JWT 签名密钥，**至少 32 字符**；不再内置默认值，缺失/过短则应用启动即失败 |
| `UPLOAD_DIR` | `./uploads` | 头像/相册图片本地存储目录 |

上传接口返回**相对路径**（如 `/uploads/2026-09-23/xxx.png`），静态资源经 `travel.upload.url-prefix`（默认 `/uploads`）对外访问，由前端拼成绝对 URL。

### 4. 本机覆盖配置的推荐做法
不改代码、不设环境变量：在 **`Backend/config/application.properties`** 写覆盖值即可（Spring Boot 自动加载 `./config/`，优先级高于 jar 内 `application.yml`；该文件已被 `.gitignore` 的 `/config/` 规则忽略，不会提交，也不影响源码里的 `config` 包）：

```properties
spring.datasource.username=root
spring.datasource.password=你的密码
travel.llm.api-key=sk-你的DeepSeekKey
# JWT 签名密钥：无内置默认值，缺失则启动失败；随机串至少 32 字符，生产务必换独立强随机值
travel.jwt.secret=请改成至少32字符的随机密钥
```

> 密钥可用 PowerShell 生成：`-join ((48..57)+(97..122)+(65..90) | Get-Random -Count 64 | % {[char]$_})`

> ⚠️ PowerShell 里空字符串环境变量会被丢弃、等同未设置。确需空密码时用
> `$env:SPRING_APPLICATION_JSON='{"spring":{"datasource":{"password":""}}}'` 覆盖，而不是 `$env:MYSQL_PASSWORD=''`。

---

## 四、启动

在 **`Backend/`** 目录下：

```powershell
# 用仓库自带的 Maven Wrapper，无需预装 Maven
.\mvnw.cmd spring-boot:run

# 或先打包再运行
.\mvnw.cmd clean package -DskipTests
java -jar target/travel-assistant-backend-1.0.0.jar
```

或 IntelliJ IDEA 打开 `Backend` → 运行主类 `TravelAssistantApplication`。
启动成功后监听 `http://localhost:8080`。

**自检（PowerShell）**：

```powershell
curl.exe http://localhost:8080/api/products
curl.exe -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{\"username\":\"demo\",\"password\":\"test123456\"}'
curl.exe -X POST http://localhost:8080/api/chat -H "Content-Type: application/json" -H "Authorization: Bearer 上一步的token" -d '{\"content\":\"推荐一个周末去哪玩\"}'
```

---

## 五、核心业务规则（会员 / 配额）

| 规则 | 说明 | 触发码 |
|------|------|--------|
| 智慧问答额度 | 非会员每日免费 **5 次**（`ChatService.FREE_DAILY_LIMIT`），会员不限 | `4291` |
| 会员客服 | **VIP 专属**，非会员访问被拒 | `4292` |
| 攻略保存名额 | 生成不限；**保存**时校验：非会员 **1 份** / 会员 **5 份**，删除历史可释放 | `4293` |
| VIP 判定 | 按 `vipExpireTime` 推导（`UserService.isVipActive`），过期即失效 | — |
| 开通 VIP | `POST /api/users/me/vip?hours=12`，默认 **12 小时**；分享领时长同为 12h | — |
| 订单状态机 | 下单为「待付款」→ 支付确认推进「已付款」（幂等）；取消回退库存与销量，禁止重复取消 | — |

**攻略规划两步走**（`ItineraryService`）：
1. `POST /api/itinerary` 让大模型产出结构化 JSON（分日行程 + 坐标），**仅生成不入库**，名额已满也能预览；
2. `POST /api/itinerary/save` 才写入 `t_travel_plan` 并校验保存名额。
解析时对模型返回的 ```json 围栏 / 多余文字做容错，坐标为 0 视为无效置空。

---

## 六、接口一览

标注「公开」的免登录（`@PublicApi`）；其余需 `Authorization: Bearer <token>`。

| 分类 | 方法 | 路径 | 鉴权 |
|------|------|------|------|
| 认证 | POST | `/api/auth/register` | 公开 |
| 认证 | POST | `/api/auth/login` | 公开 |
| 认证 | GET | `/api/auth/check-username?username=` | 公开 |
| 用户 | GET / PUT | `/api/users/me` | 登录 |
| 用户 | PUT | `/api/users/me/password` | 登录 |
| 用户 | POST | `/api/users/me/vip?hours=12` | 登录 |
| 偏好 | GET / PUT | `/api/preferences` | 登录 |
| 商品 | GET | `/api/products`、`/api/products/{id}` | 公开 |
| 订单 | GET / POST | `/api/orders` | 登录 |
| 订单 | GET / DELETE | `/api/orders/{id}` | 登录 |
| 支付 | POST | `/api/payments/wxpay`（下单并返回调起参数） | 登录 |
| 支付 | POST | `/api/payments/confirm`（推进为已付款） | 登录 |
| 相册 | GET / POST | `/api/albums` | 登录 |
| 相册 | GET / PUT / DELETE | `/api/albums/{id}` | 登录 |
| 路线 | GET | `/api/routes`、`/api/routes/{id}` | 公开 |
| 对话 | POST | `/api/chat` | 登录 |
| 对话 | POST | `/api/kefu/chat` | 登录（VIP） |
| 对话 | GET | `/api/chat/quota`（额度） | 登录 |
| 对话 | GET / DELETE | `/api/chat/sessions`、`/api/chat/sessions/{id}` | 登录 |
| 对话 | GET | `/api/chat/sessions/{id}/messages` | 登录 |
| 攻略 | POST | `/api/itinerary`（生成，不保存） | 登录 |
| 攻略 | POST | `/api/itinerary/save`（保存到历史） | 登录 |
| 攻略 | GET | `/api/itinerary`、`/api/itinerary/{id}` | 登录 |
| 攻略 | DELETE | `/api/itinerary/{id}`（删除，释放名额） | 登录 |
| 上传 | POST | `/api/files/upload`（表单字段 `file`） | 登录 |

**统一返回格式**：

```json
{ "code": 200, "message": "ok", "data": { } }
```

`code` 见 `ResultCode`：`200` 成功；`400` 参数错误；`401` 未登录/过期；`403` 越权；`404` 不存在；
`409` 冲突；`4291/4292/4293` 会员/额度类（见第五节）；`500` 业务/服务器错误。

---

## 七、已内置的性能与安全处理

| 位置 | 处理 | 目的 |
|------|------|------|
| `LlmClient` | 连接超时 10s、读超时 `LLM_TIMEOUT`（默认 60s） | 上游模型卡死时不长期占用 Tomcat 线程 |
| `ChatService` / `ItineraryService` | 只下发最近 `LLM_HISTORY_LIMIT` 条历史；生成方法**不加事务** | 控制 token 成本；避免大模型耗时期间长期占用数据库连接 |
| `OrderService` | 下单/取消对商品行加 `PESSIMISTIC_WRITE` 锁；取消回退库存销量并阻止重复取消 | 防并发超卖；修复取消不回滚 |
| 实体索引 | `t_order(user_id,create_time)`、`t_chat_session`、`t_chat_message`、`t_album`、`t_album_photo`、`t_travel_plan` 补索引 | 列表按 user_id / session_id 过滤+排序，无索引会全表扫 |
| `JwtUtil` | 签名密钥构建一次后缓存 | 每请求解析 token 不再重复派生密钥 |
| `UserService` | `vip` 按 `vipExpireTime` 推导 | 避免一旦开通即永久有效 |
| 上传 | 扩展名白名单 + UUID 重命名 + 日期分目录 | 防目录穿越 / 任意文件写入 |
| 权限 | 会话/订单/相册/攻略访问均校验归属（`requireOwned` / `userId` 过滤），越权返回 403/404 | 服务端强制校验，不信任前端 |

---

## 八、常见问题（FAQ）

**Q：启动报 `Access denied` / `Unknown database`？**
MySQL 未启动或账号密码不符。推荐在 git 忽略的 `config/application.properties` 覆盖（见三、4）；库会自动创建，无需手动建。
`Access denied ... (using password: NO)` 表示读到了空密码——该账号其实设了密码，注意 PowerShell 空变量坑。

**Q：对话/攻略报「未配置大模型 API Key」？**
`LLM_API_KEY` 与 `KEFU_API_KEY` 都为空。配一个 `LLM_API_KEY`（DeepSeek Key）即可，客服会自动复用。

**Q：报「大模型服务暂时不可用」？**
Key 失效/余额不足，或本机访问 `api.deepseek.com` 网络不通。看后端日志具体异常（401/402/超时），慢回答可调大 `LLM_TIMEOUT`。

**Q：编译报 `TypeTag UNKNOWN`（Lombok）？**
用了不兼容的 JDK（如 JDK 25）。切到 JDK 17 或 21 重新编译。

**Q：生产部署需注意什么？**
务必覆盖 `JWT_SECRET`、`MYSQL_PASSWORD`、`LLM_API_KEY`；`ddl-auto` 建议改为 `none` 并用 schema 管理；
上传目录与数据库需持久化；微信支付以异步通知（验签后）为准，`/api/payments/confirm` 仅演示用途。
