# 问题排查与解决方案

## 当前问题列表

### 问题1：测试连接无反应

**症状**：点击"测试连接"按钮后，没有任何反应，不显示成功也不显示失败。

**排查步骤**：
1. 检查网络权限 - ✅ 已添加
2. 检查网络请求是否执行 - 需要验证
3. 检查UI更新逻辑 - 需要验证

**可能原因**：
1. 协程没有正确启动
2. 网络请求被阻塞
3. 错误被静默捕获
4. StateFlow没有正确收集

**解决方案**：
```kotlin
// 确保在正确的Scope启动
viewModelScope.launch(Dispatchers.IO) {
    try {
        val result = service.chat(...)
        withContext(Dispatchers.Main) {
            // 更新UI
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            _error.value = e.message
        }
    }
}
```

### 问题2：AI续写不工作

**症状**：点击"AI续写"按钮后，进度条不显示，没有内容生成。

**排查步骤**：
1. 检查DeepSeek服务是否初始化
2. 检查API Key是否正确
3. 检查网络请求是否执行

**解决方案**：
1. 添加详细日志
2. 显示加载状态
3. 捕获并显示所有错误

### 问题3：本地模型下载卡在0%

**症状**：下载进度一直是0%，退出页面后下载停止。

**原因**：
1. 下载链接可能无效
2. 没有使用前台服务
3. 下载管理器配置问题

**解决方案**：
1. 使用前台服务保持下载
2. 添加下载进度通知
3. 验证下载链接有效性

### 问题4：本地模型无法运行

**症状**：即使下载了模型，也无法使用。

**原因**：缺少GGUF模型推理引擎。

**解决方案**：
1. 集成llama.cpp Android绑定
2. 或使用MLC LLM
3. 或暂时移除本地模型功能

## 修复优先级

1. **高优先级**：修复测试连接和AI续写
2. **中优先级**：修复本地模型下载
3. **低优先级**：实现本地模型推理

## 测试清单

- [ ] 创建项目
- [ ] 进入编辑器
- [ ] 测试连接（模型页面）
- [ ] AI续写
- [ ] 添加大纲
- [ ] 添加人物
- [ ] 添加世界观
- [ ] 创建漫剧项目
- [ ] 生成剧本
- [ ] 下载本地模型

## 调试方法

### 1. 添加日志

```kotlin
import android.util.Log

fun testConnection() {
    Log.d("ModelViewModel", "开始测试连接")
    viewModelScope.launch {
        try {
            Log.d("ModelViewModel", "API Key: ${apiKey.value}")
            val result = withContext(Dispatchers.IO) {
                Log.d("ModelViewModel", "执行网络请求")
                service.chat(...)
            }
            Log.d("ModelViewModel", "结果: $result")
        } catch (e: Exception) {
            Log.e("ModelViewModel", "错误", e)
        }
    }
}
```

### 2. 使用Toast调试

```kotlin
import android.widget.Toast

fun testConnection() {
    Toast.makeText(context, "开始测试", Toast.LENGTH_SHORT).show()
    // ...
}
```

### 3. 检查网络请求

```kotlin
// 添加OkHttp日志拦截器
val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}
```
