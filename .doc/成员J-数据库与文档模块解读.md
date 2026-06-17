# 成员 J — 数据库与项目文档 · 代码功能解读报告

> **面向读者**：刚接触本项目的开发者  
> **模块负责人**：成员 J（数据库与项目文档）  
> **主要路径**：`project/back/sql/` + `.doc/MODULE_FILES.md`  
> **文档版本**：2026-06-12

---

## 1. 这个模块是做什么的？

成员 J 负责**数据库脚本统筹**和**分工文档维护**——相当于银行的「档案室 + 数据字典」：

| 职责 | 说明 |
|------|------|
| 建表 | 全库表结构定义 |
| 测试数据 | 可重复执行的种子脚本 |
| 迁移补丁 | 小版本升级 SQL（OTP 列类型等） |
| 文档 | `MODULE_FILES.md` 十人分工清单 |

J **一般不写 Java 业务代码**，但所有模块都依赖 J 定义的表结构。

---

## 2. 新手必懂的基础概念

### 2.1 先 schema，再 seed

1. **`schema.sql`** — 创建空库结构（表、索引、初始 admin）  
2. **`seed-test-data.sql`** — 插入 zhangsan、lisi 等测试用户和账户  

顺序错了（没建表就 seed）会报错。

### 2.2 迁移脚本 vs 全量 schema

- 新环境：直接跑 `schema.sql`  
- 老环境升级：只跑 `fix-otp-type-column.sql` 等**增量**脚本  

### 2.3 BCrypt 密码在 SQL 里

测试用户密码不是明文写入，而是 `$2a$12$...` 哈希。  
成员 B 的 `SeedPasswordHashTest` 可生成新哈希。

---

## 3. 文件清单（6 个）

| 文件 | 说明 |
|------|------|
| **`schema.sql`** | 全库 DDL：users、accounts、transactions、system_config、operation_logs 等 |
| **`seed-test-data.sql`** | 6 个测试用户 + 5 个测试账户 |
| **`admin-extensions.sql`** | operation_logs 表 + transfer_fee_rate 配置项 |
| **`fix-otp-type-column.sql`** | otp_records.type 改为 VARCHAR，支持 TRANSFER_VERIFY |
| **`fix-admin-password.sql`** | 重置 admin 密码哈希 |
| **`.doc/MODULE_FILES.md`** | 十人分工与文件列表（无重复分配） |

---

## 4. 核心表结构说明

### 4.1 users（成员 B 使用）

登录账号：用户名、BCrypt 密码、邮箱、角色、状态、失败次数、锁定时间。

### 4.2 accounts（成员 C 使用）

账户号、user_id、余额、类型、状态、日限额。

### 4.3 transactions（成员 D 使用）

流水号、类型、from/to 账户 ID、金额、前后余额、操作人、IP。

### 4.4 otp_records / refresh_tokens（成员 B）

验证码与长期登录凭证。

### 4.5 system_config（成员 F）

键值配置，如 `transfer_fee_rate`。

### 4.6 operation_logs（成员 F）

管理员操作审计：谁、何时、对什么对象、做了什么。

---

## 5. 各 SQL 脚本详解

### 5.1 schema.sql

- `CREATE DATABASE banking_db`（可选）  
- 按依赖顺序建表  
- 插入默认管理员 `admin` / `Admin@123`（BCrypt）  
- 插入部分 `system_config` 默认值  

**全员协同**：B/C/D/F 各自业务会引用对应表，但**DDL 统一由 J 维护**，避免多人改表冲突。

### 5.2 seed-test-data.sql

**特点**：

- 可重复执行：先按用户名删除旧测试数据再插入  
- **不删除 admin**  
- 用户：zhangsan(Password1)、lisi(Password2)、wangwu(低余额)、zhaoliu(未验证)、sunqi(锁定)、disabled_user(禁用)  
- 账户：张三 2 个、李四 1 个等，账户号 `ACC20260612...`

**用法**：

```bash
mysql -uroot -p banking_db < project/back/sql/seed-test-data.sql
```

### 5.3 admin-extensions.sql

用于已存在库、补 `operation_logs` 表和 `transfer_fee_rate` 配置。  
与 `schema.sql` 部分内容可能重复，用 `CREATE TABLE IF NOT EXISTS` / `INSERT IGNORE` 保证幂等。

### 5.4 fix-otp-type-column.sql

**背景**：早期 `otp_records.type` 若是 MySQL ENUM，无法存 `TRANSFER_VERIFY`。  
**修复**：`ALTER TABLE ... MODIFY type VARCHAR(30)`。  
大额转账 OTP 上线后**必须执行**。

### 5.5 fix-admin-password.sql

admin 密码哈希与代码不一致时，用此脚本修复，避免无法登录管理后台。

---

## 6. MODULE_FILES.md

- 10 人分工总览  
- 每人：模块、功能、文件列表（**164 个文件无重复**）  
- 测试账号、模块协作关系  

J 负责随代码演进**更新此文档**。

---

## 7. 推荐初始化流程（新人必做）

```bash
# 1. 创建库并建表
mysql -uroot -p < project/back/sql/schema.sql

# 2. （可选）补扩展
mysql -uroot -p banking_db < project/back/sql/admin-extensions.sql

# 3. 测试数据
mysql -uroot -p banking_db < project/back/sql/seed-test-data.sql

# 4. 若大额 OTP 报错，执行
mysql -uroot -p banking_db < project/back/sql/fix-otp-type-column.sql
```

然后启动应用（成员 I），用 admin 或 zhangsan 登录。

---

## 8. 与其他模块协作

| 模块 | 依赖 J 的什么 |
|------|----------------|
| **B** | users、otp_records、refresh_tokens 表 |
| **C** | accounts |
| **D** | transactions |
| **E** | 读 transactions 做报表 |
| **F** | system_config、operation_logs |
| **全员** | MODULE_FILES.md 查谁负责哪文件 |

实体类（`@Entity`）字段应与 schema 一致；改表先改 J 的 SQL，再改对应成员实体。

---

## 9. 推荐阅读顺序

1. **`schema.sql`**（通读表结构）  
2. `seed-test-data.sql`（理解测试场景）  
3. `fix-otp-type-column.sql`（理解迁移原因）  
4. `MODULE_FILES.md`  

---

## 10. 小结

成员 J 是项目的**数据地基与文档中心**。新人应先跑通 SQL 初始化，再调试各模块；遇到 OTP 类型、admin 登录、测试账号问题，优先查 J 的脚本和种子数据说明。
