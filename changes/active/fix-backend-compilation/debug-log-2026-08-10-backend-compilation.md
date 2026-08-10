> 来源: debugging skill
> 生成时间: 2026-08-10
> 阶段: debugging

# 排查记录：后端评估模块编译失败

## 问题

Maven 编译报告 `EvaluationPersistenceService` 和 `ChatEvaluationEntity` 无法解析 -> 评估持久化子模块文件缺失，但聊天提交和自动评估调用仍保留（逻辑/代码完整性问题）。

## 排查过程

- [x] 识别 `backend/pom.xml`，使用项目 IDEA 配置中的 Maven 3.9.9 执行编译 -> 复现 12 个编译错误。
- [x] 汇总错误符号并全仓搜索 -> 12 个错误均由两个缺失类型级联产生，不是依赖下载或包名拼写问题。
- [x] 阅读 `GroundedTurnModule`、`AutoEvaluationService`、`ChatEvaluationAutoMapper` 和测试 -> 确认需要恢复 `chat_evaluation` 实体、Mapper 与持久化服务闭环。
- [x] 重新执行主代码与测试代码编译 -> 均成功。

## 假设与验证

| 假设 | 置信度 | 验证 | 结果 |
|------|--------|------|------|
| Maven/JDK 环境导致源码无法编译 | 中 | Java 17 正常，使用 IDEA 配置 Maven 后可进入 javac | 排除 |
| 引用包名已改动，类型存在于其他目录 | 中 | 全仓搜索类型名和相近文件名 | 排除 |
| 评估持久化文件缺失 | 高 | 调用、测试、SQL 表名契约均存在，但定义文件不存在 | 确认 |

## 根因链

```
症状：后端出现 12 个 cannot find symbol/package does not exist 错误
  ↓
直接原因：聊天与自动评估代码引用两个不存在的类型
  ↓
根本原因：chat_evaluation 持久化实体、Mapper、Service 未包含在当前源码中
  ↓
问题层级：逻辑/代码完整性
```

## 修复

- **改了**：新增 `ChatEvaluationEntity`、`ChatEvaluationMapper`、`EvaluationPersistenceService`，恢复现有调用链所需的最小持久化闭环。
- **理由**：保留当前聊天提交和 RAGAS 自动评估设计，不通过删除调用或伪造接口绕过编译错误。
- **验证**：`mvn -DskipTests compile` 和 `mvn -DskipTests test-compile` 均为 `BUILD SUCCESS`。
- **审查**：六维度自检通过；修改仅覆盖缺失模块，JDBC 调用放在 `boundedElastic`，JSON 序列化失败会明确报错。
