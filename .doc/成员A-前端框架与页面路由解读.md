# 成员 A — 前端框架与页面路由 · 代码功能解读报告

> **面向读者**：刚接触本项目的开发者  
> **模块负责人**：成员 A（前端负责人）  
> **主要路径**：`project/front/` + `PageController` + `WebMvcConfig` + `OpenApiConfig`  
> **文档版本**：2026-06-12

---

## 1. 这个模块是做什么的？

成员 A 负责**用户能看到的网页壳子**和**页面怎么打开**，相当于银行的「装修 + 导览牌」：

| 职责 | 说明 |
|------|------|
| 页面 HTML | 登录、注册、邮箱验证、首页等 Thymeleaf 模板 |
| 全局样式 | `common.css` 统一颜色、按钮、表单 |
| 全局 JS | `api.js` 调后端、`app.js` 公共工具 |
| 路由 | `PageController` 把 URL 映射到哪个 HTML |
| 登录分流 | 管理员登录后进 `/admin/dashboard`，普通用户进 `/dashboard` |

**成员 A 不负责**：账户余额计算、转账扣款、管理后台业务逻辑（在 C/D/F/G 等模块）。

---

## 2. 新手必懂的基础概念

### 2.1 前后端分离 vs 本项目的混合模式

- **页面**：Spring Boot 用 **Thymeleaf** 在服务端渲染 HTML（浏览器收到完整网页）
- **数据**：页面里的 JS 用 `fetch` 调 **REST API**（如 `/api/auth/login`）拿 JSON

所以 A 的工作 = **HTML 骨架** + **JS 调 API 填数据**。

### 2.2 Thymeleaf 是什么？

模板引擎。HTML 里可写 `th:href="@{/static/css/common.css}"`，服务端替换成真实路径。  
`PageController` 返回字符串 `"login"` → Spring 去找 `templates/login.html`。

### 2.3 localStorage 存登录信息

登录成功后，前端通常保存：

- `accessToken` — 调 API 时放在 Header
- `refreshToken` — 续期用
- `userRole` — 判断跳转管理端还是用户端

`api.js` 自动从 `localStorage` 读取 Token。

### 2.4 页面路由 vs API 路由

| 类型 | 示例 | 谁负责 |
|------|------|--------|
| 页面路由 | `GET /login` → 显示登录页 | **成员 A** `PageController` |
| API 路由 | `POST /api/auth/login` → 返回 JSON | **成员 G** `WebAuthController` |

---

## 3. 模块架构

```mermaid
flowchart LR
    Browser[浏览器]
    PC[PageController]
    HTML[templates/*.html]
    JS[api.js / app.js]
    API[/api/** Web层]

    Browser -->|访问 /login| PC
    PC --> HTML
    HTML --> JS
    JS -->|fetch + JWT| API
```

---

## 4. 文件清单（13 个）

### 4.1 前端页面

| 文件 | 作用 |
|------|------|
| `login.html` | 登录；成功后按 `role` 跳转 admin 或 dashboard |
| `register.html` | 注册表单，提交后引导邮箱验证 |
| `verify-email.html` | 输入 6 位邮箱验证码 |
| `dashboard.html` | 用户首页，展示账户概览 |
| `fragments/nav.html` | 顶部导航（账户、转账、交易记录等链接） |

### 4.2 静态资源

| 文件 | 作用 |
|------|------|
| `common.css` | 全局设计系统：颜色变量、按钮、卡片、表格、admin 样式类 |
| **`api.js`** | **核心**：`apiGet/apiPost/apiPut/apiDelete`，自动带 JWT，401 清缓存并跳登录 |
| **`app.js`** | 金额格式化、日期、Toast、`initNavbar()`；**admin 页跳过 navbar 初始化**防死循环 |
| `auth-verify.js` | 邮箱验证页专用 API 调用 |

### 4.3 后端配置（A 维护部分）

| 文件 | 作用 |
|------|------|
| **`PageController.java`** | 14 条 `@GetMapping`：6 个用户页 + 7 个 admin 页 + 验证页 |
| `WebMvcConfig.java` | 静态资源映射、CORS、根路径重定向 |
| `OpenApiConfig.java` | Swagger 文档标题、分组 |

### 4.4 文档

| 文件 | 作用 |
|------|------|
| `project/front/README.md` | 前端目录结构说明 |

---

## 5. 核心功能流程

### 5.1 用户打开登录页

1. 浏览器访问 `http://localhost:8080/login`
2. `PageController.loginPage()` 返回视图名 `login`
3. Spring 渲染 `templates/login.html`
4. 页面加载 `common.css`、`api.js`

### 5.2 登录与角色分流

`login.html` 内 JS 调用：

```javascript
const data = await apiPost('/api/auth/login', { username, password });
localStorage.setItem('accessToken', data.accessToken);
// 根据 data.userInfo.role 或类似字段
if (role === 'ROLE_ADMIN') {
    location.href = '/admin/dashboard';
} else {
    location.href = '/dashboard';
}
```

这样**同一个登录页**服务普通用户和管理员。

### 5.3 注册 → 邮箱验证

1. `register.html` → `POST /api/auth/register`
2. 跳转 `verify-email.html?username=xxx`
3. `auth-verify.js` 调 `POST /api/auth/verify-email`

### 5.4 导航栏与权限

`nav.html` 被各用户页引入。`app.js` 的 `initNavbar()`：

- 读 Token，无 Token 跳转登录
- 若是管理员访问用户页，可重定向到 admin（与 F 协作）
- **若页面已有 `.admin-navbar`（管理端），则跳过 `initNavbar`**，避免 admin 页无限刷新

### 5.5 管理后台页面入口

A 只负责**注册路由**，页面内容由 F 编写：

| URL | 模板 |
|-----|------|
| `/admin/dashboard` | `admin/dashboard.html` |
| `/admin/users` | `admin/users.html` |
| … | 共 7 个 |

---

## 6. api.js 工作机制（重点）

```text
apiFetch(url, options)
  ├─ 从 localStorage 取 accessToken
  ├─ Header: Authorization: Bearer <token>
  ├─ fetch(url)
  ├─ status === 401 → 清空 localStorage → 跳转 /login
  └─ 解析 JSON，code !== 200 时 throw Error(message)
```

业务页（H 负责）统一调用 `apiGet('/api/accounts/my')` 等，**不必每个页面重复写 Token 逻辑**。

---

## 7. 与其他模块协作

| 模块 | 协作 |
|------|------|
| **B** | 登录 API 返回 Token；Security 白名单放行 `/login` 等页面 |
| **G** | 所有 `api.js` 请求的目标 API |
| **F** | admin 页面 HTML/JS 由 F 写，路由由 A 注册 |
| **H** | 业务页 `accounts.html` 等使用 A 的 `api.js` + `nav.html` |
| **I** | Thymeleaf 路径在 `application.yml`（I 维护） |

---

## 8. 本地调试

1. 启动后端（见成员 I 文档）
2. 访问 `http://localhost:8080/login`
3. F12 → Network：看 API 是否带 `Authorization`
4. 401 时检查 Token 是否过期

---

## 9. 推荐阅读顺序

1. `PageController.java` — 有哪些 URL  
2. `api.js` — 前端如何调后端  
3. `login.html` — 登录 + 分流逻辑  
4. `app.js` — 公共工具与 navbar  
5. `common.css` — 样式变量  

---

## 10. 小结

成员 A 搭建了整个系统的**用户界面基础设施**：页面能打开、样式统一、API 调用有统一封装、登录后能去对的地方。读懂 `PageController` + `api.js` + `login.html` 三条线，就掌握了本模块的核心。
