# 笔核 - 技术文档

## 项目概述

笔核是一款AI辅助写作工具，支持小说创作、漫剧制作、推文视频生成等功能。

## 技术架构

### 1. 技术栈
- **语言**: Kotlin
- **UI框架**: Jetpack Compose + Material3
- **架构**: MVVM
- **数据库**: Room
- **网络**: Retrofit + OkHttp
- **异步**: Kotlin Coroutines + Flow
- **AI**: DeepSeek API

### 2. 项目结构

```
app/src/main/java/com/bihe/app/
├── BiHeApplication.kt          # Application入口
├── MainActivity.kt             # 主Activity
├── data/
│   ├── database/              # Room数据库
│   │   ├── AppDatabase.kt     # 数据库定义
│   │   └── Dao.kt             # 数据访问对象
│   ├── model/                 # 数据模型
│   │   ├── Project.kt         # 项目
│   │   ├── Chapter.kt         # 章节
│   │   ├── Character.kt       # 人物
│   │   ├── WorldSetting.kt    # 世界观
│   │   └── ...
│   └── repository/            # 数据仓库
│       └── SettingsRepository.kt
├── domain/
│   └── ai/
│       └── DeepSeekService.kt # AI服务
├── ui/
│   ├── screens/               # UI页面
│   │   ├── MainScreen.kt      # 主页面
│   │   ├── CreationScreen.kt  # 创作页面
│   │   ├── EditorScreen.kt    # 编辑器
│   │   ├── OutlineScreen.kt   # 大纲
│   │   ├── CharactersScreen.kt # 人物
│   │   ├── WorldSettingScreen.kt # 世界观
│   │   ├── DramaScreen.kt     # 漫剧
│   │   ├── PromoScreen.kt     # 推文
│   │   ├── ModelScreen.kt     # 模型管理
│   │   └── SettingsScreen.kt  # 设置
│   └── viewmodel/             # ViewModel
│       ├── CreationViewModel.kt
│       ├── EditorViewModel.kt
│       └── ...
└── service/
    └── WritingService.kt      # 后台写作服务
```

## 功能模块

### 1. 创作中枢
- 项目管理（创建、删除、分类）
- 四种项目类型：小说、短剧、漫剧、推文

### 2. 编辑器
- 文本编辑
- AI续写（调用DeepSeek API）
- 章节管理
- 自动保存

### 3. 大纲系统
- 卷管理
- 章节大纲
- 拖拽排序（待实现）

### 4. 人物管理
- 人物创建
- 属性设置（姓名、别名、外貌、性格、背景）

### 5. 世界观设定
- 分类管理（背景、势力、地理、魔法等）
- 详细描述

### 6. 漫剧制作
- 剧集管理
- AI生成剧本
- 分镜生成（待实现）

### 7. 模型管理
- 在线模型（DeepSeek API）
- 本地模型下载（开发中）

## API集成

### DeepSeek API

```kotlin
// 初始化
val service = DeepSeekService(apiKey, baseUrl)

// 聊天
val result = service.chat(
    messages = listOf(
        Message(role = "user", content = "你好")
    )
)

// 续写
val result = service.continueWriting(
    context = "前文内容...",
    outline = "章节大纲",
    characters = "人物设定",
    worldSetting = "世界观",
    targetWords = 500
)
```

### API配置
- 默认API Key: 已内置
- 默认Base URL: https://api.deepseek.com
- 模型: deepseek-chat

## 数据库设计

### 表结构

#### projects（项目表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| name | String | 项目名 |
| type | ProjectType | 类型 |
| wordCount | Int | 当前字数 |
| totalWordGoal | Int | 目标字数 |
| createdAt | Long | 创建时间 |
| updatedAt | Long | 更新时间 |

#### chapters（章节表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| projectId | Long | 项目ID |
| title | String | 标题 |
| content | String | 内容 |
| outline | String | 大纲 |
| wordCount | Int | 字数 |
| orderIndex | Int | 排序 |

#### characters（人物表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| projectId | Long | 项目ID |
| name | String | 姓名 |
| alias | String | 别名 |
| description | String | 外貌描述 |
| personality | String | 性格 |
| background | String | 背景 |

#### world_settings（世界观表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| projectId | Long | 项目ID |
| title | String | 标题 |
| category | String | 分类 |
| content | String | 内容 |

## 已知问题

### 1. 网络请求问题
- 症状：测试连接无反应
- 原因：需要确保在IO线程执行
- 解决：使用 withContext(Dispatchers.IO)

### 2. 本地模型
- 症状：下载卡在0%
- 原因：需要前台服务保持下载
- 状态：开发中

### 3. GGUF模型运行
- 需要：llama.cpp Android绑定
- 状态：待实现

## 构建说明

### 环境要求
- JDK 17
- Android SDK 34
- Gradle 8.2

### 构建命令
```bash
./gradlew assembleDebug
```

### CI/CD
- GitHub Actions自动构建
- 推送到main分支触发
- 输出APK到Artifacts

## 更新日志

### v1.0.0
- 初始版本
- 基础写作功能
- AI续写集成
- 大纲/人物/世界观管理
