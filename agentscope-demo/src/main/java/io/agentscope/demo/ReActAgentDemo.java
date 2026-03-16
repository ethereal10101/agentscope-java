package io.agentscope.demo;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.core.plan.PlanNotebook;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.mcp.McpClientBuilder;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ReActAgent 演示示例
 * <p>
 * 这个示例展示了如何使用 ReActAgent 进行推理和行动。
 * ReActAgent 是一个结合了推理（Reasoning）和行动（Acting）的智能代理。
 */
public class ReActAgentDemo {

    private static final Logger logger = LoggerFactory.getLogger(ReActAgentDemo.class);

    public static void main(String[] args) {
        logger.info("=== ReActAgent 演示开始 ===");

        // 1. 配置模型（需要根据实际情况配置）
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            logger.error("请设置环境变量 OPENAI_API_KEY");
            return;
        }

        // 获取基本URL
        String baseUrl = System.getenv("OPEN_BASE_URL");
        if (baseUrl == null || baseUrl.isEmpty()) {
            logger.error("请设置环境变量 OPEN_BASE_URL");
            return;
        }

        Model model = OpenAIChatModel.builder()
                .apiKey(apiKey)
                .modelName("qwen3-coder-plus")
                .baseUrl(baseUrl)
                .build();

        // 示例1: 基础使用
        //basicExample(model);

        // 示例2: 带工具的使用
        //toolExample(model);

        // 示例3: 带mcp工具的使用
        mcpToolExample(model);

        // 示例4: 计划
        //planExample(model);

        logger.info("=== ReActAgent 演示结束 ===");
    }

    private static void mcpToolExample(Model model) {
        // 示例3: mcp工具使用
        McpClientWrapper mcpClient = McpClientBuilder.create("filesystem-mcp")
                .stdioTransport("C:\\Users\\zzq\\Tools\\node\\node-v23.9.0-win-x64\\npx.cmd", "-y", "@modelcontextprotocol/server-filesystem", "D:\\ai\\test1")
                .buildAsync()
                .block();

        InMemoryMemory memory = new InMemoryMemory();

        // 注册 MCP 服务器的所有工具
        Toolkit toolkit = new Toolkit();
        toolkit.registerMcpClient(mcpClient).block();

        ReActAgent agent = ReActAgent.builder()
                .name("McpAgent")
                .model(model)
                .toolkit(toolkit)
                .memory(memory)
                .sysPrompt("You are a helpful assistant. You can use MCP tools to perform various tasks.")
                .build();
        Msg userMsg = Msg.builder()
                .name("用户")
                .role(MsgRole.USER)
                .content(TextBlock.builder()
                        .text("帮我读取文件 D:\\ai\\test1\\test.txt")
                        .build())
                .build();

        logger.info("用户: {}", userMsg.getContent());

        // 5. 调用 agent 进行回复（同步方式）
        Msg response = agent.call(userMsg).block();

        logger.info("助手: {}", response.getContent());

        // 6. 查看记忆中的消息
        List<Msg> messages = memory.getMessages();
        logger.info("记忆中共有 {} 条消息", messages.size());

    }


    /* 执行计划需要工具配合, 加个文件读取写入工具 */
    private static void planExample(Model model) {
        logger.info("\n--- 示例3: 计划 ---");

        // 2. 创建内存
        InMemoryMemory memory = new InMemoryMemory();

        PlanNotebook planNotebook = PlanNotebook.builder()
                .maxSubtasks(10)  // 限制子任务数量
                .build();


        // 2. 创建工具包并注册工具
        Toolkit toolkit = new Toolkit();
        // 注册包含 @Tool 注解的工具类
        toolkit.registerTool(List.of(new CalculatorTool(), new FileTool()));

        // 3. 创建 ReActAgent
        ReActAgent agent = ReActAgent.builder()
                .name("PlanAgent")
                .model(model)
                .memory(memory)
                .toolkit(toolkit)
                .sysPrompt("You are a systematic assistant. For multi-step tasks:\n"
                        + "1. Create a plan with create_plan tool\n"
                        + "2. Execute subtasks one by one\n"
                        + "3. Use finish_subtask after completing each\n"
                        + "4. Call finish_plan when all done")
                .planNotebook(planNotebook)
                .build();

        // 4. 创建用户消息
        Msg userMsg = Msg.builder()
                .name("用户")
                .role(MsgRole.USER)
                .content(TextBlock.builder()
                        .text("帮我找下2024年英雄联盟冠军的获得者的家乡都市是什么")
                        .build())
                .build();

        logger.info("用户: {}", userMsg.getContent());

        // 5. 调用 agent 进行回复（同步方式）
        Msg response = agent.call(userMsg).block();

        logger.info("助手: {}", response.getContent());

        // 6. 查看记忆中的消息
        List<Msg> messages = memory.getMessages();
        logger.info("记忆中共有 {} 条消息", messages.size());
    }

    /**
     * 基础示例：创建一个简单 ReActAgent 并进行对话
     */
    private static void basicExample(Model model) {

        // 2. 创建内存
        InMemoryMemory memory = new InMemoryMemory();

        // 3. 创建 ReActAgent
        ReActAgent agent = ReActAgent.builder()
                .name("助手")
                .model(model)
                .memory(memory)
                .sysPrompt("你是一个有帮助的AI助手。")
                .build();

        // 4. 创建用户消息
        Msg userMsg = Msg.builder()
                .name("用户")
                .role(MsgRole.USER)
                .content(TextBlock.builder()
                        .text("你好！请介绍一下你自己。")
                        .build())
                .build();

        logger.info("用户: {}", userMsg.getContent());

        // 5. 调用 agent 进行回复（同步方式）
        Msg response = agent.call(userMsg).block();

        logger.info("助手: {}", response.getContent());

        // 6. 查看记忆中的消息
        List<Msg> messages = memory.getMessages();
        logger.info("记忆中共有 {} 条消息", messages.size());
    }

    /**
     * 工具示例：创建带有自定义工具的 ReActAgent
     */
    private static void toolExample(Model model) {
        logger.info("\n--- 示例2: 带工具的使用 ---");

        // 2. 创建工具包并注册工具
        Toolkit toolkit = new Toolkit();
        // 注册包含 @Tool 注解的工具类
        toolkit.registerTool(new CalculatorTool());

        // 3. 创建带工具的 ReActAgent
        ReActAgent agent = ReActAgent.builder()
                .name("计算助手")
                .model(model)
                .toolkit(toolkit)
                .memory(new InMemoryMemory())
                .sysPrompt("你是一个数学助手，可以使用计算器工具进行计算。")
                .build();

        // 4. 提问
        Msg userMsg = Msg.builder()
                .name("用户")
                .role(MsgRole.USER)
                .content(TextBlock.builder()
                        .text("请帮我计算 123 + 456")
                        .build())
                .build();

        logger.info("用户: {}", userMsg.getContent());

        // 5. 获取回复
        Msg response = agent.call(userMsg).block();

        logger.info("助手: {}", response.getContent());
    }

    /**
     * 计算器工具类
     * 使用 @Tool 注解定义工具方法
     */
    public static class CalculatorTool {

        @Tool(
                name = "add",
                description = "计算两个数字的和"
        )
        public int add(
                @ToolParam(name = "a", description = "第一个数字") int a,
                @ToolParam(name = "b", description = "第二个数字") int b
        ) {
            int result = a + b;
            logger.info("计算: {} + {} = {}", a, b, result);
            return result;
        }

        @Tool(
                name = "get_current_time",
                description = "获取当前时间"
        )
        public String getCurrentTime() {
            LocalDateTime now = LocalDateTime.now();
            String time = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            logger.info("当前时间: {}", time);
            return time;
        }
    }
    private static final Map<String, String> fileStorage = new HashMap<>();
    public static class FileTool {
        @Tool(name = "write_file", description = "Write content to a file")
        public Mono<String> writeFile(
                @ToolParam(name = "filename", description = "File name") String filename,
                @ToolParam(name = "content", description = "Content") String content) {
            System.out.println("\n📝 [write_file] " + filename + " (" + content.length() + " chars)");
            fileStorage.put(filename, content);
            return Mono.just("File saved: " + filename);
        }

        @Tool(name = "read_file", description = "Read content from a file")
        public Mono<String> readFile(
                @ToolParam(name = "filename", description = "File name") String filename) {
            System.out.println("\n📖 [read_file] " + filename);
            if (!fileStorage.containsKey(filename)) {
                return Mono.just("Error: File not found");
            }
            return Mono.just(fileStorage.get(filename));
        }
    }
}
