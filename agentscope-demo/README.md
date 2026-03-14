# AgentScope Demo

这是 AgentScope Java 的演示项目，包含了各种使用示例，帮助你快速上手和调试学习。

## 项目结构

```
agentscope-demo/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── io/agentscope/demo/
│       │       └── ReActAgentDemo.java    # ReActAgent 演示
│       └── resources/
│           └── logback.xml                # 日志配置
└── README.md
```

## 快速开始

### 1. 配置环境变量

在运行示例之前，需要配置 API Key：

```bash
# Windows
set OPENAI_API_KEY=your_api_key_here

# Linux/Mac
export OPENAI_API_KEY=your_api_key_here
```

### 2. 编译项目

```bash
cd agentscope-demo
mvn clean compile
```

### 3. 运行示例

```bash
# 运行 ReActAgent 演示
mvn exec:java -Dexec.mainClass="io.agentscope.demo.ReActAgentDemo"
```

## 示例说明

### ReActAgentDemo

展示了 ReActAgent 的基本用法：

1. **基础示例**：创建一个简单的对话 Agent
   - 配置 OpenAI 模型
   - 创建内存
   - 发送消息并获取回复

2. **工具示例**（已注释）：展示如何为 Agent 添加自定义工具
   - 使用 MethodTool 定义工具
   - 注册工具到 Toolkit
   - Agent 调用工具

## 调试技巧

### 1. 查看日志

项目已配置 logback，可以在 `src/main/resources/logback.xml` 中调整日志级别：

```xml
<!-- 调整为 DEBUG 查看详细日志 -->
<logger name="io.agentscope" level="DEBUG" />
```

### 2. 断点调试

在 IDE 中设置断点的关键位置：

- `ReActAgent.call()` - Agent 调用入口
- `ReActAgent` 内部的推理和行动方法
- `Memory.addMessage()` - 消息存储
- 工具执行方法 - 查看工具调用过程

### 3. 查看内存状态

```java
// 在代码中添加
List<Msg> messages = memory.getMessages();
messages.forEach(msg -> logger.info("消息: {}", msg));
```

## 常见问题

### Q: 如何切换不同的模型？

修改模型配置：

```java
// 使用 OpenAI
Model model = OpenAIChatModel.builder()
    .apiKey(apiKey)
    .modelName("gpt-4")  // 修改模型名称
    .build();
```

### Q: 如何添加自定义工具？

参考 `toolExample()` 方法中的示例，使用 `@Tool` 注解创建工具类。

### Q: 如何查看 Agent 的推理过程？

可以添加 Hook 来监听 Agent 的各个阶段：

```java
agent.addHook(new Hook() {
    @Override
    public void onEvent(HookEvent event) {
        logger.info("事件: {}", event);
    }
});
```

## 下一步

- 查看 `agentscope-core` 模块的测试代码了解更多用法
- 阅读 ReActAgent 源码理解实现原理
- 尝试创建自己的 Agent 和技能

## 相关资源

- [AgentScope 文档](https://github.com/modelscope/agentscope)
- [API 文档](https://agentscope.io/docs)
