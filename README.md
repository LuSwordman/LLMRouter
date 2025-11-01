
# 🧠 LLMRouter — 高并发多模型智能路由系统

一个基于 **LangChain4j + OpenAI 接口兼容模型** 的 **高并发多模型调用与智能调度工具**。  
支持 **轮询、权重、故障转移** 三种路由策略，通过线程池并发架构实现 **无 IP 锁定、高吞吐量** 的大模型调用能力。

---

## 🚀 特性 Highlights

- ⚙️ **多策略调度**
  - `ROUND_ROBIN` —— 平滑轮询，模型均衡调用  
  - `WEIGHTED` —— 按权重动态分配请求比例  
  - `FAILOVER` —— 故障自动切换，保证业务连续性  

- 🤖 **多模型接入**
  - 支持 **OpenAI / DeepSeek / 智谱 GLM / Moonshot / Qwen** 等  
  - 任何遵循 OpenAI ChatCompletion 接口规范的模型均可直接接入  

- ⚡ **高并发执行**
  - 基于 **线程池 + Future 异步任务**  
  - 支持同时分发上百请求，无阻塞、无锁 IP  
  - 典型应用：多路模型投票、批量语义任务分发、聚合推理  

- 💡 **容错机制**
  - 单模型异常自动重试下一个  
  - 支持自定义最大重试次数与超时  

- 🔄 **轻量可扩展**
  - 独立工具类封装，无框架依赖  
  - 可直接嵌入任意 Spring Boot / Java 服务  

---

## 🧱 系统架构

```mermaid
graph LR
A[Client] --> B[LLMRouter]
B -->|Round-Robin| C1[OpenAI]
B -->|Weighted| C2[DeepSeek]
B -->|Failover| C3[GLM]
B -->|Async ThreadPool| D[并发调度层]
B --> E[统一响应聚合]
````

---

## 🧰 快速上手

```java
List<ModelConfig> configs = List.of(
    new ModelConfig(qwenModel, 3, "Qwen"),
    new ModelConfig(deepseekModel, 2, "DeepSeek")
);

LLMRouter router = new LLMRouter(configs, RoutingStrategy.ROUND_ROBIN);
String result = router.invoke("Explain Java concurrency model.");
System.out.println(result);
```

---

## 🧵 并发演示

```java
ExecutorService pool = Executors.newFixedThreadPool(10);
List<Future<String>> futures = IntStream.range(0, 50)
    .mapToObj(i -> pool.submit(() -> router.invoke("Task #" + i)))
    .toList();

for (Future<String> f : futures) {
    System.out.println(f.get());
}
```

---

## 📈 设计亮点

* 🚫 **不锁 IP**：路由层自动分流请求，避免单节点速率限制
* 🧠 **可插拔模型**：通过 `ModelConfig` 自定义接入源
* 🔁 **线程安全**：内部使用 `AtomicInteger` 实现安全轮询
* 🧩 **可观测性**：控制台打印执行模型、耗时与失败切换信息

---

## 🧩 应用场景

* 多大模型 **AB Test / 负载均衡**
* 高并发任务分发（内容生成、摘要、问答聚合）
* LLM 服务聚合层（Router-as-a-Service）
* 自动容灾与任务调度系统

---

## 📜 License

MIT © 2025 [LuSwordman](https://github.com/LuSwordman)

```

---

💡 **使用方式：**
1. 在项目根目录下创建或替换文件：  
```

E:\Java_learning\LLMRouter\README.md

````
2. 粘贴以上内容保存；
3. 运行：
```bash
git add README.md
git commit -m "更新 README：并发多模型路由说明"
git push origin main
````

