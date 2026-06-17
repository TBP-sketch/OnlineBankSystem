# 成员 H — 用户业务前端 · 代码功能解读报告

> **面向读者**：刚接触本项目的开发者  
> **模块负责人**：成员 H（业务前端开发）  
> **主要路径**：`project/front/templates/` 下 4 个业务页  
> **文档版本**：2026-06-12

---

## 1. 这个模块是做什么的？

成员 H 负责**登录后的银行日常操作页面**：

| 页面 | 用户做什么 |
|------|------------|
| `accounts.html` | 看账户、开户 |
| `transfer.html` | 转账 |
| `transactions.html` | 查流水、导出 |
| `profile.html` | 个人资料 |

这些页面**依赖成员 A** 提供的 `api.js`、`common.css`、`nav.html`，**依赖成员 G** 提供的 `/api/**` 接口。

---

## 2. 新手必懂的基础概念

### 2.1 单页如何工作

典型结构：

```html
<head>
  <link th:href="@{/static/css/common.css}">
</head>
<body>
  <div th:replace="~{fragments/nav :: nav}"></div>
  <main>... 表单和表格 ...</main>
  <script th:src="@{/static/js/api.js}"></script>
  <script th:src="@{/static/js/app.js}"></script>
  <script>
    document.addEventListener('DOMContentLoaded', loadData);
    async function loadData() {
      const list = await apiGet('/api/accounts/my');
      // 渲染到表格
    }
  </script>
</body>
```

页面打开时 HTML 几乎是空的壳，**JS 请求 API 后动态填内容**。

### 2.2 错误提示

调用 `apiGet/apiPost` 失败时用 `app.js` 的 `showToast(message, 'error')` 提示用户。

---

## 3. 四个页面功能详解

### 3.1 accounts.html — 账户管理

**功能**：

- 页面加载：`GET /api/accounts/my` 列表展示账户号、类型、余额、状态  
- 开户：弹窗填类型、别名、初始存款 → `POST /api/accounts`  
- 展示日转账限额等字段  

**新手关注点**：账户号复制、余额 `formatMoney()` 格式化显示。

### 3.2 transfer.html — 转账（重点）

**功能**：

1. 下拉选择**付款账户**（自己的 ACTIVE 账户）  
2. 输入**收款账户号**（对方 `ACC...`）  
3. 输入金额、备注  
4. **大额 OTP 区域**（金额 > 5000 时显示）：
   - 点「发送验证码」→ `POST /api/transactions/transfer/send-otp`  
   - 到后端日志找 6 位码，填入 `otpCode`  
5. 提交 → `POST /api/transactions/transfer` body 含 `fromAccountNo`、`toAccountNo`、`amount`、`otpCode`（大额时）

**常见错误**：

- 收款号填成 `TXN` 流水号 → G 返回格式错误提示  
- 大额未填 OTP → 「请先获取并填写邮箱验证码」

### 3.3 transactions.html — 交易记录

**功能**：

- 选择账户、时间范围、类型筛选  
- `GET /api/bills/history?...` 分页表格  
- 导出按钮：`window.open` 或 `location` 到 `/api/bills/export?format=csv&...`（需带 Token 时可能用 fetch + blob 下载）

展示字段：流水号、时间、类型、金额、对方账户、状态。

### 3.4 profile.html — 个人中心

**功能**：

- 展示当前用户邮箱、姓名等（从登录时缓存的 userInfo 或调 API）  
- 修改密码（若实现则调 B/G 的改密 API）  
- 退出：清 `localStorage`，跳 `/login`

---

## 4. 页面与 API 对照表

| 页面 | 主要 API |
|------|----------|
| accounts.html | `GET/POST /api/accounts/**` |
| transfer.html | `GET /api/accounts/my`、`POST /api/transactions/transfer/send-otp`、`POST /api/transactions/transfer` |
| transactions.html | `GET /api/bills/history`、`GET /api/bills/export` |
| profile.html | 用户信息、改密相关 `/api/auth/**` |

---

## 5. 与其他模块协作

| 模块 | 关系 |
|------|------|
| **A** | 路由 `PageController` 注册 `/accounts` 等；共用 `api.js` |
| **G** | 所有数据来自 G 的 REST API |
| **B** | 大额转账 OTP 由 B 发码，H 的页面触发 |
| **C/D/E** | 业务数据最终来自这些后端模块 |

---

## 6. 本地调试步骤

1. `zhangsan` / `Password1` 登录  
2. 打开 `/accounts` 应看到至少 2 个账户  
3. `/transfer` 向 `ACC202606120000003` 转 100 元  
4. 转 6000 元测试 OTP 流程  
5. `/transactions` 应能看到刚产生的流水  

---

## 7. 推荐阅读顺序

1. `accounts.html` — 最简单的列表+表单  
2. **`transfer.html`** — 含 OTP 交互，最复杂  
3. `transactions.html`  
4. `profile.html`  

配合阅读：成员 A 的 `api.js`、成员 G 的 `WebTransactionController`。

---

## 8. 小结

成员 H 的 4 个页面是**普通用户日常使用银行功能的前端界面**，本身几乎没有后端代码，核心是 **HTML 结构 + 调用 api.js + 处理表单与 OTP 交互**。把 `transfer.html` 读懂，就掌握了本模块最难的部分。
