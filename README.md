# LLMRouter
LLMRouter是一个面向高并发场景的多模型智能调度系统，基于 LangChain4j构建，支持 OpenAI、DeepSeek、智谱 GLM等多源大语言模型接入。系统内置 轮询、权重、故障转移 三种动态路由策略，采用线程池与异步 Future 并发架构，在高并发下依然保持稳定吞吐与智能容错能力。 
