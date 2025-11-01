package com.example.llmrouter;

import dev.langchain4j.model.chat.ChatLanguageModel;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 通用 LLM 路由工具类：
 * - 支持轮询 / 权重 / 故障转移策略
 * - 支持同步与异步调用
 * - 具备线程池并发调度能力
 */
public class LLMRouter {

    // 模型配置类：封装模型、权重、名称
    public static class ModelConfig {
        public final ChatLanguageModel model;
        public final int weight;
        public final String name;

        public ModelConfig(ChatLanguageModel model, int weight, String name) {
            this.model = model;
            this.weight = weight;
            this.name = name;
        }
    }

    // 路由策略枚举
    public enum RoutingStrategy {
        ROUND_ROBIN, // 轮询
        WEIGHTED,    // 权重
        FAILOVER     // 故障转移
    }

    private final List<ModelConfig> models;
    private final RoutingStrategy strategy;
    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);
    private final ExecutorService executorService;

    public LLMRouter(List<ModelConfig> models, RoutingStrategy strategy) {
        this.models = models;
        this.strategy = strategy;
        int poolSize = Math.max(1, models.size() * 5);
        this.executorService = Executors.newFixedThreadPool(poolSize);
    }

    /**
     * 同步调用（阻塞）
     */
    public String invoke(String prompt) {
        ChatLanguageModel model = selectModel();
        try {
            return executorService.submit(() -> model.generate(prompt)).get();
        } catch (Exception e) {
            System.err.println("模型 " + getModelName(model) + " 调用失败，执行故障转移...");
            return failoverInvoke(prompt, model);
        }
    }

    /**
     * 异步调用（返回 Future）
     */
    public Future<String> invokeAsync(String prompt) {
        ChatLanguageModel model = selectModel();
        return executorService.submit(() -> {
            try {
                return model.generate(prompt);
            } catch (Exception e) {
                System.err.println("模型 " + getModelName(model) + " 调用失败，执行故障转移...");
                return failoverInvoke(prompt, model);
            }
        });
    }

    // ================= 路由策略 =================

    private ChatLanguageModel selectModel() {
        return switch (strategy) {
            case ROUND_ROBIN -> selectRoundRobin();
            case WEIGHTED -> selectWeighted();
            case FAILOVER -> selectFailover();
        };
    }

    private ChatLanguageModel selectRoundRobin() {
        int index = roundRobinIndex.getAndIncrement() % models.size();
        return models.get(index).model;
    }

    private ChatLanguageModel selectWeighted() {
        int totalWeight = models.stream().mapToInt(m -> m.weight).sum();
        int random = (int) (Math.random() * totalWeight);
        int cumulative = 0;
        for (ModelConfig m : models) {
            cumulative += m.weight;
            if (random < cumulative) {
                return m.model;
            }
        }
        return models.get(0).model;
    }

    private ChatLanguageModel selectFailover() {
        return models.get(0).model;
    }

    private String failoverInvoke(String prompt, ChatLanguageModel failedModel) {
        for (ModelConfig m : models) {
            if (m.model != failedModel) {
                try {
                    return m.model.generate(prompt);
                } catch (Exception ignored) {}
            }
        }
        throw new RuntimeException("所有模型均调用失败");
    }

    private String getModelName(ChatLanguageModel model) {
        return models.stream()
                .filter(m -> m.model == model)
                .map(m -> m.name)
                .findFirst()
                .orElse("未知模型");
    }

    /**
     * 优雅关闭线程池
     */
    public void shutdown() {
        executorService.shutdown();
    }
}
