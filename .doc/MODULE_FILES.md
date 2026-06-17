# 在线银行系统 — 十人代码分工与文件清单

> **文档版本**：2026-06-12  
> **说明**：每个文件仅分配给一名成员，无重复条目。

---

## 一、成员总览

| 成员 | 类型 | 角色 | 负责模块 | 文件数 | 占比 |
|------|------|------|----------|--------|------|
| **A** 成员A | 原核心成员 | 前端负责人 | 前端框架与页面路由 | 13 | 7.9% |
| **B** 成员B | 原核心成员 | 认证安全工程师 | 认证与安全 | 21 | 12.8% |
| **C** 成员C | 原核心成员 | 账户模块开发 | 账户管理 | 22 | 13.4% |
| **D** 成员D | 原核心成员 | 交易模块开发 | 交易处理 | 19 | 11.6% |
| **E** 成员E | 原核心成员 | 报表模块开发 | 账单与报表 | 9 | 5.5% |
| **F** 成员F | 原核心成员 | 管理员后台开发 | 管理后台（API + UI） | 46 | 28.0% |
| **G** 成员G | 新增成员 | API 集成工程师 | Web API 集成层 | 15 | 9.1% |
| **H** 成员H | 新增成员 | 业务前端开发 | 用户业务前端 | 4 | 2.4% |
| **I** 成员I | 新增成员 | 基础设施工程师 | 基础设施与工程化 | 9 | 5.5% |
| **J** 成员J | 新增成员 | 数据库与项目文档 | 数据库脚本与本分工文档 | 6 | 3.7% |
| **合计** | | | | **164** | **100%** |

- 原核心成员（A–F）：130 个文件（79.3%）
- 新增成员（G–J）：34 个文件（20.7%）

**去重规则**：`application.yml` 归 **I**；全部 SQL 脚本归 **J**；`BankUserDetails.java` 归 **C**。

---

## 二、各成员详细分工

## 成员 A — 前端负责人（原核心成员）

**负责模块**：前端框架与页面路由  
**职责概述**：认证相关页面、全局样式与 JS、页面路由（含 /admin/*）、登录角色分流、Swagger 配置

### 实现功能

- 用户登录/注册/邮箱验证页面渲染
- 登录成功后按角色分流：管理员 → `/admin/dashboard`，普通用户 → `/dashboard`
- 全局 CSS 设计系统、Toast/金额/日期等公共 JS 工具
- HTTP 请求封装（JWT 注入、401 跳转登录、统一错误提示）
- Thymeleaf 页面路由：用户前台 6 页 + 管理后台 7 页
- 静态资源与 CORS、首页重定向配置
- Swagger / OpenAPI 文档入口配置

### 代码文件列表

| 分类 | 文件路径 | 说明 |
|------|----------|------|
| 前端页面 | `project/front/templates/login.html` | 登录页（管理员→/admin/dashboard，用户→/dashboard） |
| 前端页面 | `project/front/templates/register.html` | 注册页（含邮箱验证步骤） |
| 前端页面 | `project/front/templates/verify-email.html` | 邮箱验证页 |
| 前端页面 | `project/front/templates/dashboard.html` | 首页 / 账户概览 |
| 前端页面 | `project/front/templates/fragments/nav.html` | 公共导航栏片段 |
| 静态资源 | `project/front/static/css/common.css` | 全局样式（设计系统） |
| 静态资源 | `project/front/static/js/api.js` | HTTP 请求封装（JWT、错误处理） |
| 静态资源 | `project/front/static/js/app.js` | 公共工具；admin 页跳过 initNavbar 防重定向循环 |
| 静态资源 | `project/front/static/js/auth-verify.js` | 邮箱验证相关 API |
| 文档 | `project/front/README.md` | 前端目录说明 |
| 后端-路由 | `project/back/src/main/java/com/bank/controller/PageController.java` | 用户页 + 7 条 /admin/* 管理页路由 |
| 后端-配置 | `project/back/src/main/java/com/bank/config/WebMvcConfig.java` | 静态资源、CORS、首页重定向 |
| 后端-配置 | `project/back/src/main/java/com/banking/config/OpenApiConfig.java` | Swagger / OpenAPI 配置 |

---

## 成员 B — 认证安全工程师（原核心成员）

**负责模块**：认证与安全  
**职责概述**：注册/登录、JWT、Spring Security、OTP（含 TRANSFER_VERIFY 大额转账验证码）、用户表

### 实现功能

- 用户注册、邮箱 OTP 验证、登录、登出、Refresh Token 刷新
- JWT 签发/解析、Spring Security 过滤器链与白名单（含 `/admin/**` 页面）
- 忘记密码 / 重置密码、修改密码
- OTP 生成、校验、过期处理（注册验证、找回密码、大额转账 TRANSFER_VERIFY）
- `sendTransferOtp` / `verifyTransferOtp` 支持转账前邮箱验证码
- 用户状态校验（ACTIVE / LOCKED / DISABLED / PENDING_VERIFY）

### 代码文件列表

| 分类 | 文件路径 | 说明 |
|------|----------|------|
| DTO | `project/back/src/main/java/com/banking/auth/dto/AuthRequest.java` | 登录、注册、OTP、改密请求 |
| DTO | `project/back/src/main/java/com/banking/auth/dto/AuthResponse.java` | 登录结果、Token 响应 |
| 实体 | `project/back/src/main/java/com/banking/auth/entity/User.java` | 用户实体 |
| 实体 | `project/back/src/main/java/com/banking/auth/entity/RefreshToken.java` | 刷新 Token 实体 |
| 实体 | `project/back/src/main/java/com/banking/auth/entity/OtpRecord.java` | OTP 记录（含 TRANSFER_VERIFY 类型） |
| 异常 | `project/back/src/main/java/com/banking/auth/exception/AuthException.java` | 认证异常 |
| 过滤器 | `project/back/src/main/java/com/banking/auth/filter/JwtAuthenticationFilter.java` | JWT 过滤器 |
| Repository | `project/back/src/main/java/com/banking/auth/repository/UserRepository.java` | 用户仓储 |
| Repository | `project/back/src/main/java/com/banking/auth/repository/RefreshTokenRepository.java` | Token 仓储 |
| Repository | `project/back/src/main/java/com/banking/auth/repository/OtpRecordRepository.java` | OTP 仓储 |
| Service | `project/back/src/main/java/com/banking/auth/service/AuthService.java` | 认证逻辑；sendTransferOtp() 大额转账验证码 |
| Service | `project/back/src/main/java/com/banking/auth/service/OtpService.java` | OTP 生成与校验 |
| Service | `project/back/src/main/java/com/banking/auth/service/RefreshTokenService.java` | Refresh Token 管理 |
| Service | `project/back/src/main/java/com/banking/auth/service/CustomUserDetailsService.java` | 用户加载 |
| 工具 | `project/back/src/main/java/com/banking/auth/util/JwtUtil.java` | JWT 签发与解析 |
| 工具 | `project/back/src/main/java/com/banking/auth/util/OtpUtil.java` | OTP 工具 |
| 配置 | `project/back/src/main/java/com/banking/config/SecurityConfig.java` | Security 白名单（含 /admin/** 页面）、过滤器链 |
| 测试 | `project/back/src/test/java/com/banking/auth/AuthServiceTest.java` | 认证服务测试 |
| 测试 | `project/back/src/test/java/com/banking/auth/JwtUtilTest.java` | JWT 工具测试 |
| 测试 | `project/back/src/test/java/com/banking/auth/AdminPasswordHashTest.java` | 管理员密码哈希测试 |
| 测试 | `project/back/src/test/java/com/banking/auth/SeedPasswordHashTest.java` | 种子数据密码 BCrypt 哈希生成/校验 |

---

## 成员 C — 账户模块开发（原核心成员）

**负责模块**：账户管理  
**职责概述**：账户 CRUD、余额查询、账户状态、内部服务接口

### 实现功能

- 用户开户、查询账户列表/详情/余额
- 账户信息更新、账户状态变更（正常/冻结/关闭）
- 账户号自动生成（ACC 前缀规则）
- 内部余额更新接口（供交易模块调用）
- 交易前账户有效性校验（状态、余额）
- 管理员账户查询、状态变更（AdminAccountController）
- Spring Security UserDetails 扩展（BankUserDetails）

### 代码文件列表

| 分类 | 文件路径 | 说明 |
|------|----------|------|
| 配置 | `project/back/src/main/java/com/bank/account/config/AccountNumberGenerator.java` | 账户号生成器 |
| 配置 | `project/back/src/main/java/com/bank/account/config/BankUserDetails.java` | UserDetails 扩展（供 JWT 认证使用） |
| Controller | `project/back/src/main/java/com/bank/account/controller/AdminAccountController.java` | 管理员账户接口 |
| Controller | `project/back/src/main/java/com/bank/account/controller/InternalAccountController.java` | 内部服务接口 |
| DTO | `project/back/src/main/java/com/bank/account/dto/ApiResponse.java` | 模块响应包装 |
| DTO | `project/back/src/main/java/com/bank/account/dto/AccountResponse.java` | 账户响应 |
| DTO | `project/back/src/main/java/com/bank/account/dto/BalanceResponse.java` | 余额响应 |
| DTO | `project/back/src/main/java/com/bank/account/dto/CreateAccountRequest.java` | 开户请求 |
| DTO | `project/back/src/main/java/com/bank/account/dto/UpdateAccountRequest.java` | 更新账户请求 |
| DTO | `project/back/src/main/java/com/bank/account/dto/AccountStatusRequest.java` | 状态变更请求 |
| DTO | `project/back/src/main/java/com/bank/account/dto/InternalBalanceUpdateRequest.java` | 内部余额更新 |
| 实体 | `project/back/src/main/java/com/bank/account/entity/Account.java` | 账户实体 |
| 枚举 | `project/back/src/main/java/com/bank/account/enums/AccountType.java` | 账户类型 |
| 枚举 | `project/back/src/main/java/com/bank/account/enums/AccountStatus.java` | 账户状态 |
| 异常 | `project/back/src/main/java/com/bank/account/exception/AccountNotFoundException.java` | 账户不存在 |
| 异常 | `project/back/src/main/java/com/bank/account/exception/AccountStatusException.java` | 账户状态异常 |
| 异常 | `project/back/src/main/java/com/bank/account/exception/AccountAccessDeniedException.java` | 访问拒绝 |
| 异常 | `project/back/src/main/java/com/bank/account/exception/InsufficientBalanceException.java` | 余额不足 |
| Repository | `project/back/src/main/java/com/bank/account/repository/AccountRepository.java` | 账户仓储 |
| Service | `project/back/src/main/java/com/bank/account/service/AccountService.java` | 账户服务接口 |
| Service | `project/back/src/main/java/com/bank/account/service/AccountServiceImpl.java` | 账户服务实现 |
| 测试 | `project/back/src/test/java/com/bank/account/AccountServiceTest.java` | 账户服务测试 |

---

## 成员 D — 交易模块开发（原核心成员）

**负责模块**：交易处理  
**职责概述**：转账、存款、取款、事务控制、流水号生成

### 实现功能

- 同行转账：扣款、入账、流水记录、事务一致性
- 存款、取款
- 交易流水号生成
- 单笔/日累计转账限额校验（TransactionProperties）
- 余额不足、账户不存在、账户状态异常、超限等业务异常

### 代码文件列表

| 分类 | 文件路径 | 说明 |
|------|----------|------|
| 配置 | `project/back/src/main/java/com/bank/transaction/config/TransactionNoGenerator.java` | 流水号生成 |
| 配置 | `project/back/src/main/java/com/bank/transaction/config/TransactionProperties.java` | 限额配置 |
| DTO | `project/back/src/main/java/com/bank/transaction/dto/ApiResponse.java` | 模块响应包装 |
| DTO | `project/back/src/main/java/com/bank/transaction/dto/DepositRequest.java` | 存款请求 |
| DTO | `project/back/src/main/java/com/bank/transaction/dto/WithdrawRequest.java` | 取款请求 |
| DTO | `project/back/src/main/java/com/bank/transaction/dto/TransferRequest.java` | 转账请求 |
| DTO | `project/back/src/main/java/com/bank/transaction/dto/TransactionResponse.java` | 交易响应 |
| 实体 | `project/back/src/main/java/com/bank/transaction/entity/Transaction.java` | 交易实体 |
| 枚举 | `project/back/src/main/java/com/bank/transaction/enums/TransactionType.java` | 交易类型 |
| 枚举 | `project/back/src/main/java/com/bank/transaction/enums/TransactionStatus.java` | 交易状态 |
| 异常 | `project/back/src/main/java/com/bank/transaction/exception/BusinessException.java` | 通用业务异常 |
| 异常 | `project/back/src/main/java/com/bank/transaction/exception/InsufficientBalanceException.java` | 余额不足 |
| 异常 | `project/back/src/main/java/com/bank/transaction/exception/AccountNotFoundException.java` | 账户不存在 |
| 异常 | `project/back/src/main/java/com/bank/transaction/exception/AccountStatusException.java` | 账户状态异常 |
| 异常 | `project/back/src/main/java/com/bank/transaction/exception/TransactionLimitException.java` | 交易限额 |
| Repository | `project/back/src/main/java/com/bank/transaction/repository/TransactionRepository.java` | 交易仓储 |
| Service | `project/back/src/main/java/com/bank/transaction/service/TransactionService.java` | 交易服务接口 |
| Service | `project/back/src/main/java/com/bank/transaction/service/TransactionServiceImpl.java` | 交易服务实现 |
| 测试 | `project/back/src/test/java/com/bank/transaction/TransactionServiceTest.java` | 交易服务测试 |

---

## 成员 E — 报表模块开发（原核心成员）

**负责模块**：账单与报表  
**职责概述**：交易历史查询、分页、统计、CSV/Excel/PDF 导出

### 实现功能

- 按账户、时间、类型等条件分页查询交易历史
- 交易详情查询、账户统计汇总
- 动态条件查询（TransactionSpecification）
- 导出 CSV / Excel / PDF（ExportUtil）
- 分页参数与排序字段安全校验（PageUtil）

### 代码文件列表

| 分类 | 文件路径 | 说明 |
|------|----------|------|
| DTO | `project/back/src/main/java/com/banking/report/dto/ReportResponse.java` | 报表响应 DTO 集合 |
| DTO | `project/back/src/main/java/com/banking/report/dto/TransactionQueryRequest.java` | 查询条件 |
| 异常 | `project/back/src/main/java/com/banking/report/exception/ReportException.java` | 报表异常 |
| Repository | `project/back/src/main/java/com/banking/report/repository/TransactionSpecification.java` | 动态查询 |
| Service | `project/back/src/main/java/com/banking/report/service/ReportService.java` | 报表核心服务 |
| Service | `project/back/src/main/java/com/banking/report/service/UserContext.java` | 用户上下文 |
| 工具 | `project/back/src/main/java/com/banking/report/util/ExportUtil.java` | CSV/Excel/PDF 导出 |
| 工具 | `project/back/src/main/java/com/banking/report/util/PageUtil.java` | 分页与排序安全 |
| 测试 | `project/back/src/test/java/com/banking/report/ExportUtilTest.java` | 导出工具测试 |

---

## 成员 F — 管理员后台开发（原核心成员）

**负责模块**：管理后台（API + UI）  
**职责概述**：管理后台 API + 管理端 UI：用户/账户/交易/配置、操作日志、创建管理员

### 实现功能

- 管理后台概览：用户数、账户数、交易量等统计
- 用户管理：分页查询、详情、启用/冻结/禁用、重置/设置密码
- 管理员管理：创建管理员、管理员列表、设置管理员密码
- 账户管理：全平台账户查询、冻结/解冻、限额查看
- 交易监控：全平台交易流水查询
- 系统配置：转账限额、手续费率等 key-value 配置
- 操作日志：管理员操作审计查询（OperationLog）
- 管理端 7 个 HTML 页面 + admin-nav + admin-api.js
- AdminAuditHelper 自动记录关键管理操作

### 代码文件列表

| 分类 | 文件路径 | 说明 |
|------|----------|------|
| Controller | `project/back/src/main/java/com/bank/admin/controller/AdminUserController.java` | 用户管理、创建管理员、重置密码 API |
| Controller | `project/back/src/main/java/com/bank/admin/controller/TransactionMonitorController.java` | 交易监控 API |
| Controller | `project/back/src/main/java/com/bank/admin/controller/SystemConfigController.java` | 系统配置 API |
| Controller | `project/back/src/main/java/com/bank/admin/controller/OperationLogController.java` | 操作日志查询 API |
| DTO | `project/back/src/main/java/com/bank/admin/dto/request/PageRequest.java` | 分页请求 |
| DTO | `project/back/src/main/java/com/bank/admin/dto/request/UserQueryRequest.java` | 用户查询 |
| DTO | `project/back/src/main/java/com/bank/admin/dto/request/UpdateUserStatusRequest.java` | 状态更新 |
| DTO | `project/back/src/main/java/com/bank/admin/dto/request/TransactionQueryRequest.java` | 交易查询 |
| DTO | `project/back/src/main/java/com/bank/admin/dto/request/SystemConfigRequest.java` | 配置请求 |
| DTO | `project/back/src/main/java/com/bank/admin/dto/request/CreateAdminRequest.java` | 创建管理员请求 |
| DTO | `project/back/src/main/java/com/bank/admin/dto/request/AdminSetPasswordRequest.java` | 管理员重置密码 |
| DTO | `project/back/src/main/java/com/bank/admin/dto/request/OperationLogQueryRequest.java` | 操作日志查询 |
| DTO | `project/back/src/main/java/com/bank/admin/dto/response/Result.java` | 统一响应 |
| DTO | `project/back/src/main/java/com/bank/admin/dto/response/PageResult.java` | 分页响应 |
| DTO | `project/back/src/main/java/com/bank/admin/dto/response/UserVO.java` | 用户 VO |
| DTO | `project/back/src/main/java/com/bank/admin/dto/response/TransactionVO.java` | 交易 VO |
| DTO | `project/back/src/main/java/com/bank/admin/dto/response/DashboardStatsVO.java` | 仪表盘统计 |
| DTO | `project/back/src/main/java/com/bank/admin/dto/response/OperationLogVO.java` | 操作日志 VO |
| 实体 | `project/back/src/main/java/com/bank/admin/entity/SystemConfig.java` | 系统配置实体 |
| 实体 | `project/back/src/main/java/com/bank/admin/entity/OperationLog.java` | 操作日志实体 |
| 枚举 | `project/back/src/main/java/com/bank/admin/enums/AdminTransactionType.java` | 管理端交易类型 |
| Repository | `project/back/src/main/java/com/bank/admin/repository/AdminUserRepository.java` | 用户仓储 |
| Repository | `project/back/src/main/java/com/bank/admin/repository/AdminTransactionRepository.java` | 交易仓储 |
| Repository | `project/back/src/main/java/com/bank/admin/repository/SystemConfigRepository.java` | 配置仓储 |
| Repository | `project/back/src/main/java/com/bank/admin/repository/OperationLogRepository.java` | 操作日志仓储 |
| Service | `project/back/src/main/java/com/bank/admin/service/AdminUserService.java` | 用户管理服务 |
| Service | `project/back/src/main/java/com/bank/admin/service/TransactionMonitorService.java` | 交易监控服务 |
| Service | `project/back/src/main/java/com/bank/admin/service/SystemConfigService.java` | 配置服务 |
| Service | `project/back/src/main/java/com/bank/admin/service/OperationLogService.java` | 操作日志服务 |
| Service | `project/back/src/main/java/com/bank/admin/service/AdminRuntimeConfigService.java` | 运行时配置（限额/费率） |
| Service | `project/back/src/main/java/com/bank/admin/service/impl/AdminUserServiceImpl.java` | 用户管理实现 |
| Service | `project/back/src/main/java/com/bank/admin/service/impl/TransactionMonitorServiceImpl.java` | 交易监控实现 |
| Service | `project/back/src/main/java/com/bank/admin/service/impl/SystemConfigServiceImpl.java` | 配置服务实现 |
| Service | `project/back/src/main/java/com/bank/admin/service/impl/OperationLogServiceImpl.java` | 操作日志实现 |
| 支持 | `project/back/src/main/java/com/bank/admin/support/AdminAuditHelper.java` | 管理操作审计写日志 |
| 前端页面 | `project/front/templates/admin/dashboard.html` | 管理后台概览 |
| 前端页面 | `project/front/templates/admin/users.html` | 用户管理页 |
| 前端页面 | `project/front/templates/admin/accounts.html` | 账户管理页 |
| 前端页面 | `project/front/templates/admin/config.html` | 系统配置页 |
| 前端页面 | `project/front/templates/admin/transactions.html` | 交易日志页 |
| 前端页面 | `project/front/templates/admin/operation-logs.html` | 操作日志页 |
| 前端页面 | `project/front/templates/admin/admins.html` | 管理员账号页 |
| 前端片段 | `project/front/templates/fragments/admin-nav.html` | 管理顶栏 + admin-api.js |
| 静态资源 | `project/front/static/js/admin-api.js` | 管理端 API、requireAdmin() |
| 测试 | `project/back/src/test/java/com/bank/admin/service/AdminUserServiceTest.java` | 用户管理测试 |
| 测试 | `project/back/src/test/java/com/bank/admin/service/SystemConfigServiceTest.java` | 配置服务测试 |

---

## 成员 G — API 集成工程师（新增成员）

**负责模块**：Web API 集成层  
**职责概述**：Web API 控制器、前端 DTO 适配、全局异常处理

### 实现功能

- `/api/auth/**`：登录、注册、邮箱验证、OTP、刷新 Token、登出
- `/api/accounts/**`：我的账户、余额、开户
- `/api/transactions/**`：转账、存款、取款；`POST /transfer/send-otp` 发送转账验证码
- 转账金额超过阈值时校验 OTP
- `/api/bills/**`：账单历史查询、多格式导出
- 前端专用 DTO 与领域层适配
- GlobalExceptionHandler：认证/OTP/403/业务异常统一 JSON 响应

### 代码文件列表

| 分类 | 文件路径 | 说明 |
|------|----------|------|
| Controller | `project/back/src/main/java/com/bank/web/WebAuthController.java` | /api/auth/** 认证接口 |
| Controller | `project/back/src/main/java/com/bank/web/WebAccountController.java` | /api/accounts/** 账户接口 |
| Controller | `project/back/src/main/java/com/bank/web/WebTransactionController.java` | 交易接口；POST /transfer/send-otp；转账 OTP 校验 |
| Controller | `project/back/src/main/java/com/bank/web/WebBillController.java` | /api/bills/** 账单接口 |
| DTO | `project/back/src/main/java/com/bank/dto/request/LoginRequest.java` | 登录请求 |
| DTO | `project/back/src/main/java/com/bank/dto/request/RegisterRequest.java` | 注册请求 |
| DTO | `project/back/src/main/java/com/bank/dto/request/TransferRequest.java` | 转账请求 |
| DTO | `project/back/src/main/java/com/bank/dto/request/DepositWithdrawRequest.java` | 存取款请求 |
| DTO | `project/back/src/main/java/com/bank/dto/response/ApiResponse.java` | 统一 API 响应 |
| DTO | `project/back/src/main/java/com/bank/dto/response/LoginResponse.java` | 登录响应 |
| DTO | `project/back/src/main/java/com/bank/dto/response/RegisterResponse.java` | 注册响应 |
| DTO | `project/back/src/main/java/com/bank/dto/response/AccountResponse.java` | 账户响应 |
| DTO | `project/back/src/main/java/com/bank/dto/response/TransactionResponse.java` | 交易响应 |
| DTO | `project/back/src/main/java/com/bank/dto/response/PageResponse.java` | 分页响应 |
| 配置 | `project/back/src/main/java/com/banking/config/GlobalExceptionHandler.java` | 全局异常（OTP 过期/错误、403 等） |

---

## 成员 H — 业务前端开发（新增成员）

**负责模块**：用户业务前端  
**职责概述**：账户、转账、交易记录、个人中心等业务页面

### 实现功能

- 账户管理页：展示账户列表、余额、开户
- 转账页：选择付款/收款账户、输入金额；大额转账发送邮箱 OTP 后再提交
- 交易记录页：历史流水展示、导出
- 个人中心：资料查看与修改

### 代码文件列表

| 分类 | 文件路径 | 说明 |
|------|----------|------|
| 前端页面 | `project/front/templates/accounts.html` | 账户管理页 |
| 前端页面 | `project/front/templates/transfer.html` | 转账页；大额时调用 /transfer/send-otp 邮箱验证码 |
| 前端页面 | `project/front/templates/transactions.html` | 交易记录页（含导出） |
| 前端页面 | `project/front/templates/profile.html` | 个人中心页 |

---

## 成员 I — 基础设施工程师（新增成员）

**负责模块**：基础设施与工程化  
**职责概述**：工程构建、启动类、运行环境、全局配置统筹

### 实现功能

- Spring Boot 启动类与组件扫描
- Maven 聚合工程、前后端资源打包
- application.yml 全局配置：数据库、端口、JWT、OTP、交易限额、导出、Swagger 等
- Docker Compose 本地 MySQL
- 一键编译/启动脚本
- 项目 README 与 Git 忽略规则

### 代码文件列表

| 分类 | 文件路径 | 说明 |
|------|----------|------|
| 启动 | `project/back/src/main/java/com/banking/OnlineBankApplication.java` | Spring Boot 启动类 |
| 构建 | `project/back/pom.xml` | 后端 Maven 配置（含 front 资源打包） |
| 构建 | `pom.xml` | 根目录 Maven 聚合工程 |
| 配置 | `project/back/src/main/resources/application.yml` | 全局配置（数据库、JWT、OTP、transaction、export、springdoc 等） |
| 运维 | `project/back/docker-compose.yml` | MySQL 本地环境 |
| 脚本 | `project/back/scripts/start.bat` | 一键启动 |
| 脚本 | `project/back/scripts/build-all.bat` | 编译脚本 |
| 文档 | `README.md` | 项目说明 |
| 其他 | `.gitignore` | Git 忽略规则 |

---

## 成员 J — 数据库与项目文档（新增成员）

**负责模块**：数据库脚本与本分工文档  
**职责概述**：数据库脚本统筹、测试数据、迁移脚本、分工文档维护

### 实现功能

- 统一 schema.sql：全库表结构（users、accounts、transactions、system_config、operation_logs 等）
- seed-test-data.sql：测试用户与账户种子数据
- admin-extensions.sql：操作日志表、转账费率配置项
- fix-otp-type-column.sql：OTP 类型列迁移（支持 TRANSFER_VERIFY）
- fix-admin-password.sql：管理员密码修复
- 维护本文件 MODULE_FILES.md

### 代码文件列表

| 分类 | 文件路径 | 说明 |
|------|----------|------|
| 数据库 | `project/back/sql/schema.sql` | 统一数据库初始化 |
| 数据库 | `project/back/sql/seed-test-data.sql` | 测试用户与账户种子数据 |
| 数据库 | `project/back/sql/admin-extensions.sql` | 操作日志表、transfer_fee_rate |
| 数据库 | `project/back/sql/fix-otp-type-column.sql` | OTP 类型列迁移 |
| 数据库 | `project/back/sql/fix-admin-password.sql` | 修复管理员密码 |
| 文档 | `.doc/MODULE_FILES.md` | 十人代码分工与文件清单（本文档） |

---

## 三、各成员代码解读文档（新手向）

| 成员 | 文档 |
|------|------|
| A | [成员A-前端框架与页面路由解读.md](./成员A-前端框架与页面路由解读.md) |
| B | [成员B-认证安全模块解读.md](./成员B-认证安全模块解读.md) |
| C | [成员C-账户管理模块解读.md](./成员C-账户管理模块解读.md) |
| D | [成员D-交易模块解读.md](./成员D-交易模块解读.md) |
| E | [成员E-账单报表模块解读.md](./成员E-账单报表模块解读.md) |
| F | [成员F-管理后台模块解读.md](./成员F-管理后台模块解读.md) |
| G | [成员G-API集成模块解读.md](./成员G-API集成模块解读.md) |
| H | [成员H-用户业务前端解读.md](./成员H-用户业务前端解读.md) |
| I | [成员I-基础设施模块解读.md](./成员I-基础设施模块解读.md) |
| J | [成员J-数据库与文档模块解读.md](./成员J-数据库与文档模块解读.md) |

---

## 四、测试入口

| 角色 | 用户名 | 密码 | 入口 |
|------|--------|------|------|
| 管理员 | admin | Admin@123 | `/admin/dashboard` |
| 普通用户 | zhangsan | Password1 | `/dashboard` |

种子数据见 `project/back/sql/seed-test-data.sql`。

---

## 五、模块协作关系

| 调用关系 | 说明 |
|----------|------|
| A → F | A 注册 `/admin/*` 路由；F 提供 admin 页面与 admin-api.js |
| B → G | B 提供 AuthService/OTP；G 在 Web 层暴露 REST 并校验 |
| C → D | C 提供账户与余额；D 调用内部接口完成转账 |
| D → G | D 实现交易核心；G 封装 REST 与 DTO |
| E → G | E 实现报表；G 通过 WebBillController 对外提供 API |
| F ↔ C | F 管理 UI 调用 AdminAccountController（C 维护） |
| I → 全员 | I 维护 application.yml 中各模块配置节 |
| J → 全员 | J 维护 schema 与各迁移/种子脚本 |
