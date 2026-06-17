# 成员 I — 基础设施与工程化 · 代码功能解读报告

> **面向读者**：刚接触本项目的开发者  
> **模块负责人**：成员 I（基础设施工程师）  
> **文档版本**：2026-06-12

---

## 1. 这个模块是做什么的？

成员 I 负责**让整个项目能跑起来、能编译、能配置**——像大楼的「水电、物业、消防规范」：

| 职责 | 文件 |
|------|------|
| 程序入口 | `OnlineBankApplication.java` |
| 依赖管理 | `pom.xml`（根 + back） |
| 全局配置 | **`application.yml`**（唯一维护者） |
| 本地数据库 | `docker-compose.yml` |
| 启动脚本 | `start.bat`、`build-all.bat` |
| 项目说明 | `README.md`、`.gitignore` |

**I 不写业务逻辑**，但所有模块的端口、JWT 过期时间、转账限额都写在 I 维护的配置里。

---

## 2. 新手必懂的基础概念

### 2.1 Spring Boot 启动

`OnlineBankApplication` 上有 `@SpringBootApplication`，`main` 方法一行 `SpringApplication.run` 就会：

- 启动内嵌 Tomcat（默认 8080）  
- 扫描 `com.bank`、`com.banking` 包下所有 `@Service`、`@Controller`  
- 读取 `application.yml`

### 2.2 Maven 多模块

```
OnlineBankSystem/pom.xml          ← 父工程（聚合）
project/back/pom.xml              ← 后端模块，打包时把 front 资源打进 jar
```

`back/pom.xml` 会把 `project/front` 的 templates、static 复制到 `classpath`，所以**一个 jar 同时提供 API 和网页**。

### 2.3 application.yml 分段

各成员「协同」的配置，**文件所有权归 I**：

| 配置节 | 服务对象 |
|--------|----------|
| `spring.datasource` | 全员数据库 |
| `jwt.*` | 成员 B |
| `otp.*` | 成员 B |
| `transaction.*` | 成员 D、G |
| `export.*` | 成员 E |
| `springdoc.*` | 成员 A Swagger |

改配置后需**重启**应用生效。

---

## 3. 文件清单（9 个）

| 文件 | 说明 |
|------|------|
| `OnlineBankApplication.java` | 启动类；可能有 `@EnableScheduling` 支持 OTP/Token 定时清理 |
| `project/back/pom.xml` | Spring Boot、JPA、Security、POI、JWT 等依赖；front 资源打包 |
| `pom.xml` | 根聚合，模块列表 |
| **`application.yml`** | 见下文详解 |
| `docker-compose.yml` | 本地 MySQL 8 容器 |
| `scripts/start.bat` | Windows 一键启动 |
| `scripts/build-all.bat` | 编译 |
| `README.md` | 克隆、构建、运行说明 |
| `.gitignore` | 忽略 target、.idea 等 |

---

## 4. application.yml 关键项解读

```yaml
server:
  port: 8080                    # 访问 http://localhost:8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/banking_db?...
    username: root
    password: ...               # 本地 MySQL 密码，部署时需修改
  jpa:
    hibernate:
      ddl-auto: update          # 启动时按实体自动更新表结构（开发方便）
  thymeleaf:
    cache: false                # 开发时改 HTML 立即生效

jwt:
  access-token-expiration: 3600000    # 1 小时
  refresh-token-expiration: 604800000 # 7 天

otp:
  expiration: 300000            # 5 分钟

transaction:
  transfer:
    otp-threshold: 5000.00      # 超过此金额要邮箱 OTP
    single-limit: 100000.00
    daily-limit: 500000.00

export:
  max-rows: 50000

springdoc:
  swagger-ui:
    path: /swagger-ui.html
```

---

## 5. 启动流程（新手第一次跑项目）

### 方式一：IDEA

1. 用 IDEA 打开仓库，Maven 导入 `project/back`  
2. Working directory 设为 `project/back`  
3. 运行 `OnlineBankApplication`  
4. 确保 MySQL 已建库 `banking_db` 并执行 J 的 SQL（见成员 J 文档）

### 方式二：脚本

```bat
cd project\back\scripts
build-all.bat
start.bat
```

### 方式三：Docker MySQL

```bash
cd project/back
docker-compose up -d
```

再用客户端执行 `schema.sql`、`seed-test-data.sql`。

---

## 6. 与其他模块协作

| 模块 | I 提供什么 |
|------|------------|
| **全员** | 数据库连接、JPA、日志级别 |
| **B** | jwt、otp 配置 |
| **D/G** | transaction 限额、otp-threshold |
| **E** | export 配置 |
| **A** | thymeleaf、springdoc |

业务代码**不要硬编码**限额/过期时间，应读配置类绑定 `application.yml`。

---

## 7. 常见问题

| 问题 | 排查 |
|------|------|
| 8080 端口占用 | 改 `server.port` 或关占用进程 |
| 连不上 MySQL | 检查 url、密码、库是否存在 |
| 页面 404 | 确认 front 资源是否打进 jar（Maven package） |
| IDEA 找不到主类 | 模块选 `back`，working dir 为 `project/back` |
| 表不存在 | 执行 J 的 `schema.sql` 或依赖 ddl-auto=update |

---

## 8. 推荐阅读顺序

1. `README.md`  
2. **`application.yml`**  
3. `project/back/pom.xml`（看依赖）  
4. `OnlineBankApplication.java`  
5. `docker-compose.yml`  

---

## 9. 小结

成员 I 是项目的**运行与配置中枢**。新人应先能按 I 的文档把服务跑起来，再去看 B/C/D 的业务代码。`application.yml` 是调试各类行为（OTP 时间、转账阈值）的第一入口。
