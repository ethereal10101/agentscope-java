package io.agentscope.demo;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ReActAgent 演示示例
 *
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

        Model model = OpenAIChatModel.builder()
                .apiKey(apiKey)
                .modelName("qwen3-coder-plus")
                .baseUrl("http://192.168.3.192:10010")
                .build();

        // 示例1: 基础使用
        //basicExample(model);

        // 示例2: 带工具的使用
         toolExample(model);

        logger.info("=== ReActAgent 演示结束 ===");
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
}
