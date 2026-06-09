# Spring AI 2.x 14 天系列 · 配套代码

公众号「**技术洋**」**Spring AI 2.x 14 天系列**的配套可运行项目。

基于 **Spring AI 2.0-M8** + Spring Boot 3.4 + DeepSeek，从第一个对话一路搭到带记忆、RAG、工具调用的完整 AI 应用。

> ✅ 已在 JDK 21 + Maven 3.9.9 下 `mvn compile` 验证编译通过（Spring AI 2.0.0-M8）。

---

## 技术栈

- JDK 17+
- Spring Boot 3.4.1
- Spring AI 2.0.0-M8（里程碑预览版）
- DeepSeek（对话）+ 本地 Transformers（嵌入，离线零成本）

---

## 快速开始

```bash
# 1. 准备 DeepSeek API Key：https://platform.deepseek.com/
# 2. 设置环境变量
export DEEPSEEK_API_KEY=sk-你的key

# 3. 启动
mvn spring-boot:run
```

> ⚠️ 首次启动会自动下载本地嵌入模型（ONNX，约 100MB+），需联网一次，请耐心等待。

---

## 14 天索引

| Day | 主题 | 包 | 试一下 |
|-----|------|-----|--------|
| 1 | 第一个对话 | `day01` | `GET /day1/chat?message=你好` |
| 2 | ChatClient / 流式 | `day02` | `GET /day2/stream?message=写首诗` |
| 3 | Prompt 模板 / 角色 | `day03` | `GET /day3/code?language=Java&algorithm=快排` |
| 4 | 结构化输出 | `day04` | `GET /day4/films?actor=汤姆·汉克斯` |
| 5 | 多模态 | `day05` | `GET /day5/image`（需视觉模型 + 图片） |
| 6 | Chat Memory | `day06` | `GET /day6/chat?message=我叫张三&conversationId=u1` |
| 7 | Advisors | `day07` | `GET /day7/chat?message=你好`（看控制台日志） |
| 8 | Embedding / 向量库 | `day08` | `POST /day8/load` → `GET /day8/search?query=Spring AI` |
| 9 | 文档 ETL | `day09` | `POST /day9/ingest` |
| 10 | RAG | `day10` | 先 `POST /day9/ingest` → `GET /day10/ask?question=退货政策几天` |
| 11 | 模块化 RAG | `day11` | 先 `POST /day9/ingest` → `GET /day11/ask?question=2.0有什么变化` |
| 12 | Tool Calling | `day12` | `GET /day12/ask?message=现在几点？北京天气如何` |
| 13 | MCP | `day13` | 需配 MCP server，见下方 |
| 14 | 全景图谱 + 面试 | — | 见公众号文章 |

---

## 番外篇（连载之外的加餐）

### 综合实战 · 生产加固版客服（`customerservice` 包，HTTP `/cs`）

| 接口 | 作用 | 试一下 |
|------|------|--------|
| `GET /cs/chat` | 纯文字客服：DeepSeek + 记忆 / RAG / Tool / 容错 | 先 `POST /day9/ingest` → `GET /cs/chat?message=你们退货政策几天？&userId=u1` |
| `GET /cs/chat-image` | 识图演示：MiniMax-M3 读固定 `sample.png` | 放 `src/main/resources/images/sample.png` + `export MINIMAX_API_KEY=xxx` → `GET /cs/chat-image?message=这张图里的商品有什么问题？` |
| `POST /cs/ask` | 统一入口：有图→MiniMax-M3，无图→DeepSeek，跨模型共享上下文 | `POST /cs/ask` form-data：`message` + 可选 `images`（支持多图） |

> 容错、token 记录、敏感词、会话隔离是真实可跑代码；更重的特性（记忆落库 JDBC/Redis、Moderation 内容审核、Micrometer 指标）需引入额外依赖，文中以配置示例 + 注释呈现，详见公众号文章。识图的 MiniMax-M3 走 OpenAI 兼容通道、懒加载，未配 `MINIMAX_API_KEY` 不影响主线 DeepSeek 的自动装配。

### Agent 编排 · 5 种 workflow 模式（`agentic` 包，HTTP `/agentic`）

| 模式 | 接口 | 试一下 |
|------|------|--------|
| ① Chain 链式 | `GET /agentic/chain` | `GET /agentic/chain?message=我买的卷发棒坏了，很生气` |
| ② Routing 路由 | `GET /agentic/routing` | `GET /agentic/routing?message=我上周被重复扣款了` |
| ③ Parallelization 并行 | `POST /agentic/parallel` | body 传 JSON 数组：`["卷发棒坏了要退","按钮按不动","怎么开发票"]` |
| ④ Orchestrator-Workers | `GET /agentic/orchestrate` | `GET /agentic/orchestrate?task=帮我规划一次三天两夜的杭州周末游` |
| ⑤ Evaluator-Optimizer | `GET /agentic/evaluate` | `GET /agentic/evaluate?task=起草一条退货拒绝话术` |

> 这 5 个 workflow 类都不是框架内置 API，全是 `ChatClient` + Java 控制流（`for` / `if` / 并行流）拼出来的——详见番外篇《Agent 编排：5 种模式》。

---

## 重要提示（DeepSeek 能力边界）

- **Day5 多模态**：DeepSeek 不支持图像输入。需换视觉模型（gpt-4o / qwen-vl / GLM-4V / Ollama llava）——换对应 starter + 配 key，Media 代码不变；并在 `src/main/resources/images/` 放一张 `sample.png`。
- **Day8–11 嵌入**：DeepSeek 无嵌入模型，本项目用本地 Transformers（离线零成本）。
- **Day10 / 11 RAG**：提问前先 `POST /day9/ingest` 把示例知识库灌入向量库。
- **Day13 MCP**：默认不连接任何 server。在 `application.yml` 取消 MCP 配置注释、本机装好 `npx` 后启用。

---

## 关于版本

Spring AI 2.0 仍是**里程碑预览版**，少数 API / 包路径可能随版本微调。若 IDE 出现 import 红线，用「自动导入」修正即可（类名通常不变，仅包路径可能调整）。要上生产可改用 1.1.x GA 稳定版。

---

配套文章请见公众号「**技术洋**」Spring AI 2.x 14 天系列。
