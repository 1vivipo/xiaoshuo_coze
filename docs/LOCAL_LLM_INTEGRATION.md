# 本地大模型集成方案

## 概述

笔核计划支持本地大模型，实现离线AI写作功能。

## 推荐模型列表

### 1. 轻量级中文模型

| 模型名称 | 大小 | 特点 | 下载链接 |
|---------|------|------|---------|
| Qwen2.5-1.5B-Instruct-Q4_K_M.gguf | ~1GB | 中文友好，轻量 | https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF |
| Qwen2.5-3B-Instruct-Q4_K_M.gguf | ~2GB | 中文友好，平衡 | https://huggingface.co/Qwen/Qwen2.5-3B-Instruct-GGUF |
| Phi-3.5-mini-instruct-Q4_K_M.gguf | ~2GB | 多语言，快速 | https://huggingface.co/microsoft/Phi-3.5-mini-instruct-gguf |

### 2. 写作专用模型

| 模型名称 | 大小 | 特点 | 下载链接 |
|---------|------|------|---------|
| Mistral-7B-Instruct-v0.3-Q4_K_M.gguf | ~4GB | 通用写作 | https://huggingface.co/maziyarpanahi/Mistral-7B-Instruct-v0.3-GGUF |
| Llama-3.2-3B-Instruct-Q4_K_M.gguf | ~2GB | 创意写作 | https://huggingface.co/bartowski/Llama-3.2-3B-Instruct-GGUF |

### 3. NSFW支持模型

| 模型名称 | 大小 | 特点 | 下载链接 |
|---------|------|------|---------|
| Dirty-Muse-Writer-v01-Q2_K.gguf | ~1.5GB | 成人向写作 | https://huggingface.co/TheDrummer/Dark-Muse-Writer-v01-GGUF |
| Midnight-Miqu-70B-v1.5-Q2_K.gguf | ~25GB | 高质量成人向 | https://huggingface.co/sophosympatheia/Midnight-Miqu-70B-v1.5-GGUF |

## 技术实现方案

### 方案一：使用llama.cpp Android绑定

```kotlin
// 添加依赖
implementation("com.squareup.okio:okio:3.6.0")

// 加载模型
val llama = LlamaAndroid()
llama.loadModel(modelPath, nCtx = 2048, nThreads = 4)

// 生成文本
val result = llama.generate(prompt, maxTokens = 500)
```

### 方案二：使用MLC LLM

```kotlin
// 添加依赖
implementation("ai.mlc.mlcnn:mlcnn:0.1.0")

// 加载模型
val engine = MLCEngine(modelPath)

// 生成文本
val result = engine.generate(prompt, maxTokens = 500)
```

### 方案三：使用Gecko

```kotlin
// 使用Gecko库
val gecko = Gecko(modelPath)
val result = gecko.generate(prompt)
```

## 推荐方案

**推荐使用llama.cpp Android绑定**，原因：
1. 开源免费
2. 支持GGUF格式
3. 性能优秀
4. 社区活跃

## 实现步骤

### 1. 添加Native库

```gradle
// build.gradle
android {
    ndk {
        abiFilters 'armeabi-v7a', 'arm64-v8a'
    }
}

dependencies {
    implementation("com.squareup.okio:okio:3.6.0")
}
```

### 2. 创建LLM服务

```kotlin
class LocalLLMService(private val modelPath: String) {
    private var llama: LlamaAndroid? = null
    
    fun loadModel() {
        llama = LlamaAndroid()
        llama?.loadModel(modelPath, nCtx = 2048, nThreads = 4)
    }
    
    fun generate(prompt: String, maxTokens: Int = 500): String {
        return llama?.generate(prompt, maxTokens) ?: ""
    }
    
    fun close() {
        llama?.close()
    }
}
```

### 3. 下载管理

使用前台服务确保下载不被中断：

```kotlin
class ModelDownloadService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 创建通知
        val notification = createNotification()
        startForeground(1, notification)
        
        // 开始下载
        downloadModel()
        
        return START_STICKY
    }
}
```

## 性能要求

| 模型大小 | 最低内存 | 推荐内存 | CPU要求 |
|---------|---------|---------|--------|
| 1-2GB | 4GB | 6GB+ | 中端 |
| 3-4GB | 6GB | 8GB+ | 中高端 |
| 7GB+ | 8GB | 12GB+ | 高端 |

## 注意事项

1. **内存管理**：大模型需要大量内存，注意及时释放
2. **存储空间**：确保有足够空间存储模型
3. **电量消耗**：本地推理耗电较大
4. **首次加载**：模型加载需要时间，显示进度

## 下一步计划

1. 集成llama.cpp Android绑定
2. 实现模型下载前台服务
3. 添加模型管理界面
4. 优化推理性能
