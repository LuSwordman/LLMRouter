package com.example.llmrouter;

import dev.langchain4j.model.chat.ChatLanguageModel;

import java.time.Duration;
import java.util.List;

public class RouterDemo {
    public static void main(String[] args) {
        ChatLanguageModel modelA = OpenAiChatModel.builder()
                .apiKey("xxx")
                .modelName("qwen-turbo")
                .baseUrl("xxx")
                .timeout(Duration.ofSeconds(30))
                .build();

        ChatLanguageModel modelB = OpenAiChatModel.builder()
                .apiKey("xxx")
                .baseUrl("https://api.deepseek.com/v1")
                .modelName("deepseek-chat")
                .timeout(Duration.ofSeconds(30))
                .build();

        List<LLMRouter.ModelConfig> configs = List.of(
                new LLMRouter.ModelConfig(modelA, 3, "Qwen"),
                new LLMRouter.ModelConfig(modelB, 2, "DeepSeek")
        );

        LLMRouter router = new LLMRouter(configs, RoutingStrategy.ROUND_ROBIN);
        String result = router.invoke("Hello from router!");
        System.out.println(result);
        router.shutdown();
    }
}
