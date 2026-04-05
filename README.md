# <span style="color:#2E86C1">clj-ai-agent</span> 🤖✨

<span style="color:#27AE60">一个基于 Spring Boot 3.5（Java 21）的 AI Agent 后端项目</span>，内置 MCP 客户端能力，并包含一个独立的图片搜索 MCP Server。

---

## <span style="color:#8E44AD">🌟 项目功能概览</span>
- <span style="color:#16A085">AI Agent 对话与工具调用</span>（包含多种 Agent 实现与 Advisor）
- <span style="color:#16A085">RAG 能力</span>（文档加载、向量检索、查询改写）
- <span style="color:#16A085">MCP 客户端支持</span>（stdio + sse）
- <span style="color:#16A085">图片搜索 MCP Server</span>（可独立部署）
- <span style="color:#16A085">Swagger/Knife4j 接口文档</span>

---

## <span style="color:#8E44AD">🧩 模块说明</span>
### <span style="color:#2E86C1">clj-ai-agent（主服务）</span>
负责 Agent 能力、对话逻辑、工具调用、RAG 相关功能与 API 暴露。  
默认端口：`8092`，上下文路径：`/api`

### <span style="color:#2E86C1">cli-image-search-mcp-server（图片搜索 MCP Server）</span>
独立的 MCP Server，可用 `stdio` 或 `sse` 方式接入主服务。  
默认端口：`8866`

---

## <span style="color:#8E44AD">🧱 主要目录结构（功能分区）</span>
- `src/main/java/xyz/bx25/cljaiagent/agent`：<span style="color:#16A085">Agent 实现</span>
- `src/main/java/xyz/bx25/cljaiagent/advisor`：<span style="color:#16A085">对话 Advisor</span>
- `src/main/java/xyz/bx25/cljaiagent/rag`：<span style="color:#16A085">RAG 相关组件</span>
- `src/main/java/xyz/bx25/cljaiagent/tools`：<span style="color:#16A085">工具能力</span>
- `src/main/java/xyz/bx25/cljaiagent/controller`：<span style="color:#16A085">对外 API</span>
- `src/main/resources`：<span style="color:#16A085">配置文件 / 文档 / 资源</span>
- `cli-image-search-mcp-server/`：<span style="color:#16A085">独立图片搜索 MCP Server</span>

---

## <span style="color:#8E44AD">🧠 Advisor 说明</span>
- <span style="color:#16A085">MyLoggerAdvisor</span>：记录模型输出，便于调试和排查对话效果。
- <span style="color:#16A085">ReReadingAdvisor</span>：对用户问题做“再阅读”式改写，提高模型理解一致性。
- <span style="color:#16A085">RAG 相关 Advisor</span>：在 `rag` 包内提供检索增强配置与工厂。

---

## <span style="color:#8E44AD">🧭 Agent 设计说明</span>
- <span style="color:#16A085">EternityManus</span>：参考大名鼎鼎的 Manus 设计方法，强调可解释、可编排的 Agent 流程。
- <span style="color:#16A085">ReActAgent</span>：ReAct 思路的工具调用与推理流程。
- <span style="color:#16A085">ToolCallAgent</span>：面向工具调用的对话编排与执行。

---

## <span style="color:#8E44AD">📚 RAG 能力说明</span>
- <span style="color:#16A085">LoveAppDocumentLoader</span>：加载 `resources/document/*.md` 文档作为知识源。
- <span style="color:#16A085">LoveAppVectorStoreConfig</span>：向量库构建（SimpleVectorStore），含关键词补全。
- <span style="color:#16A085">LoveAppRagCustomAdvisorFactory</span>：构建检索增强 Advisor（带过滤与阈值）。
- <span style="color:#16A085">LoveAppContextualQueryAugmenterFactory</span>：空上下文时返回友好提示。
- <span style="color:#16A085">QueryRewriter</span>：查询改写（RewriteQueryTransformer）。
- <span style="color:#16A085">LoveAppCloudAdvisorConfig</span>：基于 DashScope 知识库的检索增强配置。

---

## <span style="color:#8E44AD">🔑 需要你填写的密钥</span>
> <span style="color:#E74C3C">请务必在运行前设置这些环境变量，否则功能不可用。</span>

**主服务（clj-ai-agent）需要：**
- <span style="color:#E67E22">DASHSCOPE_API_KEY</span> = **你的 DashScope Key**
- <span style="color:#E67E22">SEARCHAPI_API_KEY</span> = **你的 SearchAPI Key**
- <span style="color:#E67E22">AMAP_MAPS_API_KEY</span> = **你的高德地图 Key**（MCP stdio 用）

**图片搜索 MCP Server 需要：**
- <span style="color:#E67E22">PEXELS_API_KEY</span> = **你的 Pexels Key**

---

## <span style="color:#8E44AD">🚀 本地运行（主服务）</span>
```bash
./mvnw -DskipTests package
export SPRING_PROFILES_ACTIVE=local
export DASHSCOPE_API_KEY=你的key
export SEARCHAPI_API_KEY=你的key
export AMAP_MAPS_API_KEY=你的key
java -jar target/clj-ai-agent-0.0.1-SNAPSHOT.jar
```

Swagger UI：`http://localhost:8092/api/swagger-ui.html`

---

## <span style="color:#8E44AD">🖼️ 本地运行（图片搜索 MCP Server）</span>
```bash
cd cli-image-search-mcp-server
./mvnw -DskipTests package
export SPRING_PROFILES_ACTIVE=sse
export PEXELS_API_KEY=你的key
java -jar target/cli-image-search-mcp-server-0.0.1-SNAPSHOT.jar
```

---

## <span style="color:#8E44AD">🔌 MCP 接入方式</span>
主服务配置在 `src/main/resources/application.yml` 中：
- `stdio` MCP：使用 `mcp-servers.json`
- `sse` MCP：默认连接 `http://localhost:8866`

如果你把 `cli-image-search-mcp-server` 改成 SSE，只要保证服务在 `8866` 启动，主服务会通过 SSE 连接它。

---

> <span style="color:#E74C3C">注意：</span>如果生产要用图片搜索 MCP Server，请单独部署它，并把主服务的 `sse` 指向对应地址。
