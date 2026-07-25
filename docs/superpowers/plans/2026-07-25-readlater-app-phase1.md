# ReadLater App Phase 1 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 搭建 Android 项目脚手架，实现核心功能：添加链接 → AI 处理 → 收件箱列表 → 阅读器。

**Architecture:** Kotlin + Jetpack Compose + MVI 架构。Room 本地存储，OkHttp 网络请求，Jsoup 网页解析，Hilt 依赖注入。AI Agent 通过 OpenAI 兼容 Function Calling 协议调用 Skill。

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Hilt, Room, OkHttp, Jsoup, kotlinx.serialization

## Global Constraints

- Min SDK: 26 (Android 8.0), Target SDK: 35
- Kotlin JVM Toolchain: 21
- Compose BOM: 2024.06+
- Room: 2.6+
- Hilt: 2.51+
- 单元测试框架: JUnit 5 + MockK
- UI 测试框架: Compose Testing
- 所有新文件使用 UTF-8 编码
- 命名规范: PascalCase 类名, camelCase 函数/变量, snake_case 资源名

---

## 文件结构总览

```
app/
├── src/main/java/com/lantianhcgp/readlater/
│   ├── ReadLaterApp.kt                 # Application 类 (Hilt)
│   ├── MainActivity.kt                  # 单 Activity
│   ├── di/                              # Hilt 模块
│   │   ├── DatabaseModule.kt
│   │   ├── NetworkModule.kt
│   │   └── AgentModule.kt
│   ├── data/
│   │   ├── db/
│   │   │   ├── AppDatabase.kt
│   │   │   ├── dao/
│   │   │   │   ├── ArticleDao.kt
│   │   │   │   ├── TagDao.kt
│   │   │   │   └── HighlightDao.kt
│   │   │   └── entity/
│   │   │       ├── Article.kt
│   │   │       ├── Tag.kt
│   │   │       ├── ArticleTag.kt
│   │   │       └── Highlight.kt
│   │   ├── repository/
│   │   │   ├── ArticleRepository.kt
│   │   │   └── TagRepository.kt
│   │   └── model/
│   │       ├── ArticleStatus.kt
│   │       └── LlmConfig.kt
│   ├── agent/
│   │   ├── LlmProvider.kt              # LLM 接口
│   │   ├── LlmProviderFactory.kt       # Provider 工厂
│   │   ├── providers/
│   │   │   ├── OpenAiProvider.kt
│   │   │   ├── DeepSeekProvider.kt
│   │   │   └── OllamaProvider.kt
│   │   ├── ToolRegistry.kt             # Tool 注册表
│   │   ├── ToolExecutor.kt             # Tool 执行器
│   │   ├── AgentOrchestrator.kt        # Agent 编排器
│   │   └── tools/
│   │       ├── FetchContentTool.kt
│   │       ├── SummarizeTool.kt
│   │       └── AutoTagTool.kt
│   ├── ui/
│   │   ├── navigation/
│   │   │   └── AppNavigation.kt
│   │   ├── theme/
│   │   │   ├── Color.kt
│   │   │   ├── Type.kt
│   │   │   └── Theme.kt
│   │   ├── inbox/
│   │   │   ├── InboxScreen.kt
│   │   │   ├── InboxViewModel.kt
│   │   │   └── InboxUiState.kt
│   │   ├── reader/
│   │   │   ├── ReaderScreen.kt
│   │   │   ├── ReaderViewModel.kt
│   │   │   └── ReaderUiState.kt
│   │   ├── addlink/
│   │   │   ├── AddLinkScreen.kt
│   │   │   ├── AddLinkViewModel.kt
│   │   │   └── AddLinkUiState.kt
│   │   ├── tags/
│   │   │   ├── TagsScreen.kt
│   │   │   ├── TagsViewModel.kt
│   │   │   └── TagsUiState.kt
│   │   ├── favorites/
│   │   │   ├── FavoritesScreen.kt
│   │   │   ├── FavoritesViewModel.kt
│   │   │   └── FavoritesUiState.kt
│   │   ├── settings/
│   │   │   ├── SettingsScreen.kt
│   │   │   ├── SettingsViewModel.kt
│   │   │   └── SettingsUiState.kt
│   │   └── components/
│   │       ├── ArticleCard.kt
│   │       ├── TagChip.kt
│   │       └── EmptyState.kt
│   └── util/
│       ├── HtmlParser.kt
│       └── Extensions.kt
├── src/test/java/com/lantianhcgp/readlater/
│   ├── agent/
│   │   ├── AgentOrchestratorTest.kt
│   │   └── tools/
│   │       ├── FetchContentToolTest.kt
│   │       ├── SummarizeToolTest.kt
│   │       └── AutoTagToolTest.kt
│   └── data/
│       └── repository/
│           └── ArticleRepositoryTest.kt
└── build.gradle.kts
```

---

### Task 1: 项目脚手架搭建

**Files:**
- Create: `app/build.gradle.kts`
- Create: `build.gradle.kts` (project level)
- Create: `settings.gradle.kts`
- Create: `gradle.properties`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/lantianhcgp/readlater/ReadLaterApp.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/MainActivity.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/ui/theme/Theme.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/ui/theme/Color.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/ui/theme/Type.kt`

**Interfaces:**
- Produces: `ReadLaterApp` (Hilt Application), `MainActivity` (single Activity), Material 3 Theme

- [ ] **Step 1: 创建项目级 build.gradle.kts**

```kotlin
// build.gradle.kts (project)
plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
    id("com.google.devtools.ksp") version "2.0.0-1.0.21" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
}
```

- [ ] **Step 2: 创建 settings.gradle.kts**

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolution {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ReadLater"
include(":app")
```

- [ ] **Step 3: 创建 app/build.gradle.kts**

```kotlin
// app/build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.lantianhcgp.readlater"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lantianhcgp.readlater"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Network
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // HTML Parsing
    implementation("org.jsoup:jsoup:1.17.2")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testImplementation("io.mockk:mockk:1.13.11")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

- [ ] **Step 4: 创建 gradle.properties**

```properties
# gradle.properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

- [ ] **Step 5: 创建 Application 类**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/ReadLaterApp.kt
package com.lantianhcgp.readlater

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ReadLaterApp : Application()
```

- [ ] **Step 6: 创建 MainActivity**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/MainActivity.kt
package com.lantianhcgp.readlater

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lantianhcgp.readlater.ui.navigation.AppNavigation
import com.lantianhcgp.readlater.ui.theme.ReadLaterTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReadLaterTheme {
                AppNavigation()
            }
        }
    }
}
```

- [ ] **Step 7: 创建 Material 3 主题（琥珀橙）**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/ui/theme/Color.kt
package com.lantianhcgp.readlater.ui.theme

import androidx.compose.ui.graphics.Color

// Amber Orange seed color
val SeedOrange = Color(0xFFFF9800)

// Light theme
val PrimaryLight = Color(0xFF8B5000)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFFFDBC9)
val OnPrimaryContainerLight = Color(0xFF2D1600)
val SecondaryLight = Color(0xFF745846)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFFFDBC9)
val BackgroundLight = Color(0xFFFFFBFF)
val SurfaceLight = Color(0xFFFFFBFF)

// Dark theme
val PrimaryDark = Color(0xFFFFB68C)
val OnPrimaryDark = Color(0xFF4A2800)
val PrimaryContainerDark = Color(0xFF693C00)
val OnPrimaryContainerDark = Color(0xFFFFDBC9)
val SecondaryDark = Color(0xFFE4BFA9)
val OnSecondaryDark = Color(0xFF422B1C)
val SecondaryContainerDark = Color(0xFF5B3F2F)
val BackgroundDark = Color(0xFF1A1108)
val SurfaceDark = Color(0xFF1A1108)
```

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/ui/theme/Type.kt
package com.lantianhcgp.readlater.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 28.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 20.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 10.sp)
)
```

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/ui/theme/Theme.kt
package com.lantianhcgp.readlater.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    background = BackgroundLight,
    surface = SurfaceLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    background = BackgroundDark,
    surface = SurfaceDark
)

@Composable
fun ReadLaterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

- [ ] **Step 8: 创建 AndroidManifest.xml**

```xml
<!-- app/src/main/AndroidManifest.xml -->
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".ReadLaterApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.ReadLater">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.ReadLater">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 9: 创建 strings.xml**

```xml
<!-- app/src/main/res/values/strings.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">ReadLater</string>
</resources>
```

- [ ] **Step 10: 验证编译**

Run: `cd app && ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 11: Commit**

```bash
git add -A
git commit -m "feat: scaffold Android project with Compose + Hilt + Room"
```

---

### Task 2: Room 数据库 + 数据模型

**Files:**
- Create: `app/src/main/java/com/lantianhcgp/readlater/data/db/entity/Article.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/data/db/entity/Tag.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/data/db/entity/ArticleTag.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/data/db/entity/Highlight.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/data/db/dao/ArticleDao.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/data/db/dao/TagDao.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/data/db/dao/HighlightDao.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/data/db/AppDatabase.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/data/model/ArticleStatus.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/di/DatabaseModule.kt`

**Interfaces:**
- Produces: `AppDatabase`, `ArticleDao`, `TagDao`, `HighlightDao`, `ArticleStatus`

- [ ] **Step 1: 创建 ArticleStatus 枚举**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/data/model/ArticleStatus.kt
package com.lantianhcgp.readlater.data.model

enum class ArticleStatus {
    PENDING,
    PROCESSING,
    READY,
    ERROR
}
```

- [ ] **Step 2: 创建 Article 实体**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/data/db/entity/Article.kt
package com.lantianhcgp.readlater.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lantianhcgp.readlater.data.model.ArticleStatus
import java.util.UUID

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val title: String? = null,
    val content: String? = null,
    val plainText: String? = null,
    val summary: String? = null,
    val imageUrl: String? = null,
    val sourceDomain: String = "",
    val readingTimeMinutes: Int? = null,
    val status: ArticleStatus = ArticleStatus.PENDING,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

- [ ] **Step 3: 创建 Tag 实体**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/data/db/entity/Tag.kt
package com.lantianhcgp.readlater.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: String? = null,
    val isAutoGenerated: Boolean = false
)
```

- [ ] **Step 4: 创建 ArticleTag 关联实体**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/data/db/entity/ArticleTag.kt
package com.lantianhcgp.readlater.data.db.entity

import androidx.room.Entity

@Entity(
    tableName = "article_tags",
    primaryKeys = ["articleId", "tagId"]
)
data class ArticleTag(
    val articleId: String,
    val tagId: String
)
```

- [ ] **Step 5: 创建 Highlight 实体**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/data/db/entity/Highlight.kt
package com.lantianhcgp.readlater.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "highlights")
data class Highlight(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val articleId: String,
    val selectedText: String,
    val note: String? = null,
    val color: String = "#FF9800",
    val startOffset: Int = 0,
    val endOffset: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
```

- [ ] **Step 6: 创建 ArticleDao**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/data/db/dao/ArticleDao.kt
package com.lantianhcgp.readlater.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lantianhcgp.readlater.data.db.entity.Article
import com.lantianhcgp.readlater.data.model.ArticleStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    @Query("SELECT * FROM articles ORDER BY createdAt DESC")
    fun getAllArticles(): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE status = :status ORDER BY createdAt DESC")
    fun getArticlesByStatus(status: ArticleStatus): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteArticles(): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE id = :id")
    suspend fun getArticleById(id: String): Article?

    @Query("SELECT * FROM articles WHERE title LIKE '%' || :query || '%' OR plainText LIKE '%' || :query || '%'")
    fun searchArticles(query: String): Flow<List<Article>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: Article): String

    @Update
    suspend fun updateArticle(article: Article)

    @Delete
    suspend fun deleteArticle(article: Article)

    @Query("UPDATE articles SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateArticleStatus(id: String, status: ArticleStatus, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE articles SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())
}
```

- [ ] **Step 7: 创建 TagDao**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/data/db/dao/TagDao.kt
package com.lantianhcgp.readlater.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lantianhcgp.readlater.data.db.entity.ArticleTag
import com.lantianhcgp.readlater.data.db.entity.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<Tag>>

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getTagById(id: String): Tag?

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): Tag?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: Tag): String

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArticleTag(articleTag: ArticleTag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Query("DELETE FROM article_tags WHERE articleId = :articleId")
    suspend fun deleteArticleTags(articleId: String)

    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN article_tags at ON t.id = at.tagId
        WHERE at.articleId = :articleId
    """)
    fun getTagsForArticle(articleId: String): Flow<List<Tag>>

    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN article_tags at ON t.id = at.tagId
        WHERE at.articleId = :articleId
    """)
    suspend fun getTagsForArticleSync(articleId: String): List<Tag>
}
```

- [ ] **Step 8: 创建 HighlightDao**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/data/db/dao/HighlightDao.kt
package com.lantianhcgp.readlater.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.lantianhcgp.readlater.data.db.entity.Highlight
import kotlinx.coroutines.flow.Flow

@Dao
interface HighlightDao {

    @Query("SELECT * FROM highlights WHERE articleId = :articleId ORDER BY startOffset ASC")
    fun getHighlightsForArticle(articleId: String): Flow<List<Highlight>>

    @Insert
    suspend fun insertHighlight(highlight: Highlight): String

    @Delete
    suspend fun deleteHighlight(highlight: Highlight)
}
```

- [ ] **Step 9: 创建 AppDatabase**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/data/db/AppDatabase.kt
package com.lantianhcgp.readlater.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lantianhcgp.readlater.data.db.dao.ArticleDao
import com.lantianhcgp.readlater.data.db.dao.HighlightDao
import com.lantianhcgp.readlater.data.db.dao.TagDao
import com.lantianhcgp.readlater.data.db.entity.Article
import com.lantianhcgp.readlater.data.db.entity.ArticleTag
import com.lantianhcgp.readlater.data.db.entity.Highlight
import com.lantianhcgp.readlater.data.db.entity.Tag

@Database(
    entities = [
        Article::class,
        Tag::class,
        ArticleTag::class,
        Highlight::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun tagDao(): TagDao
    abstract fun highlightDao(): HighlightDao
}
```

- [ ] **Step 10: 创建 DatabaseModule**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/di/DatabaseModule.kt
package com.lantianhcgp.readlater.di

import android.content.Context
import androidx.room.Room
import com.lantianhcgp.readlater.data.db.AppDatabase
import com.lantianhcgp.readlater.data.db.dao.ArticleDao
import com.lantianhcgp.readlater.data.db.dao.HighlightDao
import com.lantianhcgp.readlater.data.db.dao.TagDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "readlater.db"
        ).build()
    }

    @Provides
    fun provideArticleDao(db: AppDatabase): ArticleDao = db.articleDao()

    @Provides
    fun provideTagDao(db: AppDatabase): TagDao = db.tagDao()

    @Provides
    fun provideHighlightDao(db: AppDatabase): HighlightDao = db.highlightDao()
}
```

- [ ] **Step 11: 验证编译**

Run: `cd app && ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 12: Commit**

```bash
git add -A
git commit -m "feat: add Room database with Article, Tag, Highlight entities"
```

---

### Task 3: LLM Provider 抽象层

**Files:**
- Create: `app/src/main/java/com/lantianhcgp/readlater/agent/LlmProvider.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/agent/LlmProviderFactory.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/agent/providers/OpenAiProvider.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/data/model/LlmConfig.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/di/NetworkModule.kt`

**Interfaces:**
- Produces: `LlmProvider` interface, `LlmProviderFactory`, `LlmConfig`

- [ ] **Step 1: 创建 LlmConfig**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/data/model/LlmConfig.kt
package com.lantianhcgp.readlater.data.model

data class LlmConfig(
    val id: String = "default",
    val provider: String = "openai",
    val baseUrl: String = "https://api.openai.com/v1",
    val apiKey: String = "",
    val model: String = "gpt-4o",
    val isEnabled: Boolean = true
)
```

- [ ] **Step 2: 创建 LlmProvider 接口**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/agent/LlmProvider.kt
package com.lantianhcgp.readlater.agent

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ToolDefinition(
    val type: String = "function",
    val function: FunctionDefinition
)

@Serializable
data class FunctionDefinition(
    val name: String,
    val description: String,
    val parameters: Map<String, Any>
)

@Serializable
data class ToolCall(
    val id: String,
    val type: String,
    val function: FunctionCall
)

@Serializable
data class FunctionCall(
    val name: String,
    val arguments: String
)

@Serializable
data class LlmResponse(
    val content: String?,
    val toolCalls: List<ToolCall> = emptyList()
)

interface LlmProvider {
    val name: String
    suspend fun chat(
        messages: List<ChatMessage>,
        tools: List<ToolDefinition>? = null
    ): LlmResponse
}
```

- [ ] **Step 3: 创建 OpenAI Provider**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/agent/providers/OpenAiProvider.kt
package com.lantianhcgp.readlater.agent.providers

import com.lantianhcgp.readlater.agent.ChatMessage
import com.lantianhcgp.readlater.agent.FunctionCall
import com.lantianhcgp.readlater.agent.LlmProvider
import com.lantianhcgp.readlater.agent.LlmResponse
import com.lantianhcgp.readlater.agent.ToolCall
import com.lantianhcgp.readlater.agent.ToolDefinition
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class OpenAiProvider(
    private val baseUrl: String,
    private val apiKey: String,
    private val model: String,
    private val client: OkHttpClient
) : LlmProvider {

    override val name: String = "openai"

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun chat(
        messages: List<ChatMessage>,
        tools: List<ToolDefinition>?
    ): LlmResponse {
        val requestBody = buildJsonObject {
            put("model", model)
            put("messages", buildJsonArray {
                messages.forEach { msg ->
                    add(buildJsonObject {
                        put("role", msg.role)
                        put("content", msg.content)
                    })
                }
            })
            tools?.let { toolList ->
                put("tools", buildJsonArray {
                    toolList.forEach { tool ->
                        add(json.encodeToJsonElement(ToolDefinition.serializer(), tool))
                    }
                })
            }
        }

        val request = Request.Builder()
            .url("$baseUrl/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")
        val jsonResponse = json.parseToJsonElement(responseBody).jsonObject

        val choice = jsonResponse["choices"]?.jsonArray?.firstOrNull()?.jsonObject
        val message = choice?.get("message")?.jsonObject

        val content = message?.get("content")?.contentOrNull
        val toolCallsJson = message?.get("tool_calls")?.jsonArray

        val toolCalls = toolCallsJson?.map { tc ->
            val tcObj = tc.jsonObject
            val func = tcObj["function"]!!.jsonObject
            ToolCall(
                id = tcObj["id"]!!.jsonPrimitive.content,
                type = tcObj["type"]!!.jsonPrimitive.content,
                function = FunctionCall(
                    name = func["name"]!!.jsonPrimitive.content,
                    arguments = func["arguments"]!!.jsonPrimitive.content
                )
            )
        } ?: emptyList()

        return LlmResponse(content = content, toolCalls = toolCalls)
    }

    private fun buildJsonObject(block: JsonObjectBuilder.() -> Unit): JsonObject {
        return JsonObject(mutableMapOf<String, kotlinx.serialization.json.JsonElement>().apply {
            block(object : JsonObjectBuilder {
                override fun put(key: String, value: String) {
                    this@apply[key] = kotlinx.serialization.json.JsonPrimitive(value)
                }
                override fun put(key: String, value: Int) {
                    this@apply[key] = kotlinx.serialization.json.JsonPrimitive(value)
                }
                override fun put(key: String, value: Boolean) {
                    this@apply[key] = kotlinx.serialization.json.JsonPrimitive(value)
                }
                override fun put(key: String, value: kotlinx.serialization.json.JsonElement) {
                    this@apply[key] = value
                }
            })
        })
    }

    private fun buildJsonArray(block: MutableList<kotlinx.serialization.json.JsonElement>.() -> Unit): kotlinx.serialization.json.JsonArray {
        return kotlinx.serialization.json.JsonArray(mutableListOf<kotlinx.serialization.json.JsonElement>().apply(block))
    }

    interface JsonObjectBuilder {
        fun put(key: String, value: String)
        fun put(key: String, value: Int)
        fun put(key: String, value: Boolean)
        fun put(key: String, value: kotlinx.serialization.json.JsonElement)
    }
}
```

- [ ] **Step 4: 创建 LlmProviderFactory**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/agent/LlmProviderFactory.kt
package com.lantianhcgp.readlater.agent

import com.lantianhcgp.readlater.agent.providers.OpenAiProvider
import com.lantianhcgp.readlater.data.model.LlmConfig
import okhttp3.OkHttpClient

class LlmProviderFactory(
    private val client: OkHttpClient
) {
    fun create(config: LlmConfig): LlmProvider {
        return when (config.provider) {
            "openai", "deepseek", "qwen", "openrouter" -> OpenAiProvider(
                baseUrl = config.baseUrl,
                apiKey = config.apiKey,
                model = config.model,
                client = client
            )
            "ollama" -> OpenAiProvider(
                baseUrl = config.baseUrl.ifEmpty { "http://localhost:11434/v1" },
                apiKey = "ollama",
                model = config.model,
                client = client
            )
            else -> throw IllegalArgumentException("Unsupported provider: ${config.provider}")
        }
    }
}
```

- [ ] **Step 5: 创建 NetworkModule**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/di/NetworkModule.kt
package com.lantianhcgp.readlater.di

import com.lantianhcgp.readlater.agent.LlmProviderFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideLlmProviderFactory(client: OkHttpClient): LlmProviderFactory {
        return LlmProviderFactory(client)
    }
}
```

- [ ] **Step 6: 验证编译**

Run: `cd app && ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "feat: add LLM provider abstraction with OpenAI-compatible API"
```

---

### Task 4: AI Agent 核心 — Tool Calling 框架

**Files:**
- Create: `app/src/main/java/com/lantianhcgp/readlater/agent/ToolRegistry.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/agent/ToolExecutor.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/agent/AgentOrchestrator.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/agent/tools/FetchContentTool.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/agent/tools/SummarizeTool.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/agent/tools/AutoTagTool.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/util/HtmlParser.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/di/AgentModule.kt`

**Interfaces:**
- Produces: `ToolRegistry`, `ToolExecutor`, `AgentOrchestrator`, `FetchContentTool`, `SummarizeTool`, `AutoTagTool`

- [ ] **Step 1: 创建 HtmlParser 工具类**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/util/HtmlParser.kt
package com.lantianhcgp.readlater.util

import org.jsoup.Jsoup

data class ParsedContent(
    val title: String?,
    val description: String?,
    val content: String,
    val plainText: String,
    val imageUrl: String?,
    val sourceDomain: String,
    val readingTimeMinutes: Int
)

object HtmlParser {

    fun parse(html: String, url: String): ParsedContent {
        val doc = Jsoup.parse(html, url)

        // Remove unwanted elements
        doc.select("script, style, nav, footer, header, aside, .ad, .advertisement, .sidebar").remove()

        val title = doc.title().ifEmpty { null }
        val description = doc.select("meta[name=description]").attr("content").ifEmpty { null }
        val imageUrl = doc.select("meta[property=og:image]").attr("content").ifEmpty { null }

        // Extract main content - try common article selectors
        val contentElement = doc.select("article, [role=main], .post-content, .article-content, .entry-content, main")
            .firstOrNull() ?: doc.body()

        val contentHtml = contentElement?.html() ?: ""
        val plainText = contentElement?.text() ?: ""

        val wordCount = plainText.split("\\s+".toRegex()).size
        val readingTimeMinutes = (wordCount / 200.0).toInt().coerceAtLeast(1)

        val sourceDomain = try {
            java.net.URL(url).host
        } catch (_: Exception) {
            url
        }

        return ParsedContent(
            title = title,
            description = description,
            content = contentHtml,
            plainText = plainText,
            imageUrl = imageUrl,
            sourceDomain = sourceDomain,
            readingTimeMinutes = readingTimeMinutes
        )
    }
}
```

- [ ] **Step 2: 创建 FetchContentTool**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/agent/tools/FetchContentTool.kt
package com.lantianhcgp.readlater.agent.tools

import com.lantianhcgp.readlater.agent.FunctionDefinition
import com.lantianhcgp.readlater.agent.ToolDefinition
import com.lantianhcgp.readlater.util.HtmlParser
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

class FetchContentTool @Inject constructor(
    private val client: OkHttpClient
) {

    val definition: ToolDefinition = ToolDefinition(
        function = FunctionDefinition(
            name = "fetch_content",
            description = "Fetch and extract the main content from a web URL. Returns title, description, and cleaned text content.",
            parameters = mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "url" to mapOf(
                        "type" to "string",
                        "description" = "The URL to fetch content from"
                    )
                ),
                "required" to listOf("url")
            )
        )
    )

    suspend fun execute(arguments: String): String {
        val args = Json.parseToJsonElement(arguments).jsonObject
        val url = args["url"]!!.jsonPrimitive.content

        return try {
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (ReadLater App)")
                .build()

            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: throw Exception("Empty response body")

            val parsed = HtmlParser.parse(html, url)

            Json.encodeToString(
                mapOf(
                    "title" to (parsed.title ?: ""),
                    "description" to (parsed.description ?: ""),
                    "content" to parsed.plainText.take(10000),
                    "imageUrl" to (parsed.imageUrl ?: ""),
                    "sourceDomain" to parsed.sourceDomain,
                    "readingTimeMinutes" to parsed.readingTimeMinutes.toString()
                )
            )
        } catch (e: Exception) {
            """{"error": "${e.message}"}"""
        }
    }
}
```

- [ ] **Step 3: 创建 SummarizeTool**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/agent/tools/SummarizeTool.kt
package com.lantianhcgp.readlater.agent.tools

import com.lantianhcgp.readlater.agent.ChatMessage
import com.lantianhcgp.readlater.agent.FunctionDefinition
import com.lantianhcgp.readlater.agent.LlmProvider
import com.lantianhcgp.readlater.agent.ToolDefinition
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class SummarizeTool @Inject constructor() {

    val definition: ToolDefinition = ToolDefinition(
        function = FunctionDefinition(
            name = "summarize",
            description = "Generate a concise summary of the given text content.",
            parameters = mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "content" to mapOf(
                        "type" to "string",
                        "description" = "The text content to summarize"
                    )
                ),
                "required" to listOf("content")
            )
        )
    )

    suspend fun execute(arguments: String, provider: LlmProvider): String {
        val args = Json.parseToJsonElement(arguments).jsonObject
        val content = args["content"]!!.jsonPrimitive.content

        val response = provider.chat(
            messages = listOf(
                ChatMessage(
                    role = "system",
                    content = "You are a helpful assistant. Summarize the following content in 2-3 concise sentences in the same language as the content."
                ),
                ChatMessage(
                    role = "user",
                    content = "Please summarize this content:\n\n$content"
                )
            )
        )

        return response.content ?: "Failed to generate summary"
    }
}
```

- [ ] **Step 4: 创建 AutoTagTool**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/agent/tools/AutoTagTool.kt
package com.lantianhcgp.readlater.agent.tools

import com.lantianhcgp.readlater.agent.ChatMessage
import com.lantianhcgp.readlater.agent.FunctionDefinition
import com.lantianhcgp.readlater.agent.LlmProvider
import com.lantianhcgp.readlater.agent.ToolDefinition
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class AutoTagTool @Inject constructor() {

    val definition: ToolDefinition = ToolDefinition(
        function = FunctionDefinition(
            name = "auto_tag",
            description = "Generate relevant tags for the given content. Returns a JSON array of tag strings.",
            parameters = mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "content" to mapOf(
                        "type" to "string",
                        "description" = "The text content to generate tags for"
                    )
                ),
                "required" to listOf("content")
            )
        )
    )

    suspend fun execute(arguments: String, provider: LlmProvider): String {
        val args = Json.parseToJsonElement(arguments).jsonObject
        val content = args["content"]!!.jsonPrimitive.content

        val response = provider.chat(
            messages = listOf(
                ChatMessage(
                    role = "system",
                    content = """You are a tagging assistant. Generate 2-5 relevant tags for the given content.
Return ONLY a JSON array of strings, like: ["tag1", "tag2", "tag3"]
Tags should be short (1-2 words), lowercase, and capture the main topics.
Do not include any explanation, just the JSON array."""
                ),
                ChatMessage(
                    role = "user",
                    content = "Generate tags for this content:\n\n${content.take(3000)}"
                )
            )
        )

        return response.content ?: "[]"
    }
}
```

- [ ] **Step 5: 创建 ToolRegistry**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/agent/ToolRegistry.kt
package com.lantianhcgp.readlater.agent

import com.lantianhcgp.readlater.agent.tools.AutoTagTool
import com.lantianhcgp.readlater.agent.tools.FetchContentTool
import com.lantianhcgp.readlater.agent.tools.SummarizeTool
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolRegistry @Inject constructor(
    val fetchContentTool: FetchContentTool,
    val summarizeTool: SummarizeTool,
    val autoTagTool: AutoTagTool
) {
    fun getAllDefinitions(): List<ToolDefinition> = listOf(
        fetchContentTool.definition,
        summarizeTool.definition,
        autoTagTool.definition
    )
}
```

- [ ] **Step 6: 创建 ToolExecutor**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/agent/ToolExecutor.kt
package com.lantianhcgp.readlater.agent

import com.lantianhcgp.readlater.agent.tools.AutoTagTool
import com.lantianhcgp.readlater.agent.tools.FetchContentTool
import com.lantianhcgp.readlater.agent.tools.SummarizeTool
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolExecutor @Inject constructor(
    private val fetchContentTool: FetchContentTool,
    private val summarizeTool: SummarizeTool,
    private val autoTagTool: AutoTagTool
) {
    suspend fun execute(toolCall: ToolCall, provider: LlmProvider): String {
        return when (toolCall.function.name) {
            "fetch_content" -> fetchContentTool.execute(toolCall.function.arguments)
            "summarize" -> summarizeTool.execute(toolCall.function.arguments, provider)
            "auto_tag" -> autoTagTool.execute(toolCall.function.arguments, provider)
            else -> """{"error": "Unknown tool: ${toolCall.function.name}"}"""
        }
    }
}
```

- [ ] **Step 7: 创建 AgentOrchestrator**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/agent/AgentOrchestrator.kt
package com.lantianhcgp.readlater.agent

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class AgentResult(
    val title: String? = null,
    val summary: String? = null,
    val tags: List<String> = emptyList(),
    val imageUrl: String? = null,
    val sourceDomain: String = "",
    val readingTimeMinutes: Int = 0,
    val error: String? = null
)

@Singleton
class AgentOrchestrator @Inject constructor(
    private val providerFactory: LlmProviderFactory,
    private val toolRegistry: ToolRegistry,
    private val toolExecutor: ToolExecutor
) {
    companion object {
        private const val TAG = "AgentOrchestrator"
        private const val MAX_ITERATIONS = 5
    }

    suspend fun processUrl(url: String, config: LlmConfig): AgentResult = withContext(Dispatchers.IO) {
        try {
            val provider = providerFactory.create(config)

            // Step 1: Fetch content
            val fetchResult = toolExecutor.execute(
                ToolCall(
                    id = "call_1",
                    type = "function",
                    function = FunctionCall(
                        name = "fetch_content",
                        arguments = """{"url": "$url"}"""
                    )
                ),
                provider
            )

            val fetchJson = kotlinx.serialization.json.Json.parseToJsonElement(fetchResult)
                .asJsonObject
            val title = fetchJson["title"]?.toString()?.trim('"')
            val content = fetchJson["content"]?.toString()?.trim('"') ?: ""
            val imageUrl = fetchJson["imageUrl"]?.toString()?.trim('"')
            val sourceDomain = fetchJson["sourceDomain"]?.toString()?.trim('"') ?: ""
            val readingTime = fetchJson["readingTimeMinutes"]?.toString()?.trim('"')?.toIntOrNull() ?: 0

            if (content.isEmpty()) {
                return@withContext AgentResult(
                    title = title,
                    error = "Failed to extract content from URL"
                )
            }

            // Step 2: Summarize
            val summary = toolExecutor.execute(
                ToolCall(
                    id = "call_2",
                    type = "function",
                    function = FunctionCall(
                        name = "summarize",
                        arguments = """{"content": "${content.take(5000).replace("\"", "\\\"").replace("\n", " ")}"}"""
                    )
                ),
                provider
            ).trim('"')

            // Step 3: Auto tag
            val tagsResult = toolExecutor.execute(
                ToolCall(
                    id = "call_3",
                    type = "function",
                    function = FunctionCall(
                        name = "auto_tag",
                        arguments = """{"content": "${content.take(3000).replace("\"", "\\\"").replace("\n", " ")}"}"""
                    )
                ),
                provider
            )

            val tags = try {
                kotlinx.serialization.json.Json.parseToJsonElement(tagsResult)
                    .asJsonArray.map { it.toString().trim('"') }
            } catch (_: Exception) {
                emptyList()
            }

            AgentResult(
                title = title,
                summary = summary,
                tags = tags,
                imageUrl = imageUrl,
                sourceDomain = sourceDomain,
                readingTimeMinutes = readingTime
            )
        } catch (e: Exception) {
            Log.e(TAG, "Agent processing failed", e)
            AgentResult(error = e.message)
        }
    }
}
```

- [ ] **Step 8: 创建 AgentModule**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/di/AgentModule.kt
package com.lantianhcgp.readlater.di

import com.lantianhcgp.readlater.agent.tools.AutoTagTool
import com.lantianhcgp.readlater.agent.tools.FetchContentTool
import com.lantianhcgp.readlater.agent.tools.SummarizeTool
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AgentModule {

    @Provides
    @Singleton
    fun provideFetchContentTool(client: OkHttpClient): FetchContentTool {
        return FetchContentTool(client)
    }

    @Provides
    @Singleton
    fun provideSummarizeTool(): SummarizeTool = SummarizeTool()

    @Provides
    @Singleton
    fun provideAutoTagTool(): AutoTagTool = AutoTagTool()
}
```

- [ ] **Step 9: 验证编译**

Run: `cd app && ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 10: Commit**

```bash
git add -A
git commit -m "feat: add AI Agent core with Tool Calling framework"
```

---

### Task 5: ArticleRepository + TagRepository

**Files:**
- Create: `app/src/main/java/com/lantianhcgp/readlater/data/repository/ArticleRepository.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/data/repository/TagRepository.kt`

**Interfaces:**
- Consumes: `ArticleDao`, `TagDao`, `AgentOrchestrator`
- Produces: `ArticleRepository`, `TagRepository`

- [ ] **Step 1: 创建 ArticleRepository**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/data/repository/ArticleRepository.kt
package com.lantianhcgp.readlater.data.repository

import com.lantianhcgp.readlater.agent.AgentOrchestrator
import com.lantianhcgp.readlater.agent.AgentResult
import com.lantianhcgp.readlater.data.db.dao.ArticleDao
import com.lantianhcgp.readlater.data.db.dao.TagDao
import com.lantianhcgp.readlater.data.db.entity.Article
import com.lantianhcgp.readlater.data.db.entity.ArticleTag
import com.lantianhcgp.readlater.data.model.ArticleStatus
import com.lantianhcgp.readlater.data.model.LlmConfig
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepository @Inject constructor(
    private val articleDao: ArticleDao,
    private val tagDao: TagDao,
    private val agentOrchestrator: AgentOrchestrator
) {
    fun getAllArticles(): Flow<List<Article>> = articleDao.getAllArticles()

    fun getArticlesByStatus(status: ArticleStatus): Flow<List<Article>> =
        articleDao.getArticlesByStatus(status)

    fun getFavoriteArticles(): Flow<List<Article>> = articleDao.getFavoriteArticles()

    fun searchArticles(query: String): Flow<List<Article>> = articleDao.searchArticles(query)

    suspend fun getArticleById(id: String): Article? = articleDao.getArticleById(id)

    suspend fun addArticle(url: String): String {
        val article = Article(url = url)
        return articleDao.insertArticle(article)
    }

    suspend fun processArticle(articleId: String, config: LlmConfig) {
        val article = articleDao.getArticleById(articleId) ?: return

        articleDao.updateArticleStatus(articleId, ArticleStatus.PROCESSING)

        val result = agentOrchestrator.processUrl(article.url, config)

        if (result.error != null) {
            articleDao.updateArticleStatus(articleId, ArticleStatus.ERROR)
            return
        }

        val updatedArticle = article.copy(
            title = result.title,
            summary = result.summary,
            imageUrl = result.imageUrl,
            sourceDomain = result.sourceDomain,
            readingTimeMinutes = result.readingTimeMinutes,
            status = ArticleStatus.READY,
            updatedAt = System.currentTimeMillis()
        )

        articleDao.updateArticle(updatedArticle)

        // Handle tags
        result.tags.forEach { tagName ->
            var tag = tagDao.getTagByName(tagName)
            if (tag == null) {
                val tagId = tagDao.insertTag(
                    com.lantianhcgp.readlater.data.db.entity.Tag(
                        name = tagName,
                        isAutoGenerated = true
                    )
                )
                tag = tagDao.getTagById(tagId)
            }
            tag?.let {
                tagDao.insertArticleTag(ArticleTag(articleId = articleId, tagId = it.id))
            }
        }
    }

    suspend fun toggleFavorite(articleId: String) {
        val article = articleDao.getArticleById(articleId) ?: return
        articleDao.updateFavorite(articleId, !article.isFavorite)
    }

    suspend fun deleteArticle(article: Article) = articleDao.deleteArticle(article)
}
```

- [ ] **Step 2: 创建 TagRepository**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/data/repository/TagRepository.kt
package com.lantianhcgp.readlater.data.repository

import com.lantianhcgp.readlater.data.db.dao.TagDao
import com.lantianhcgp.readlater.data.db.entity.Tag
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepository @Inject constructor(
    private val tagDao: TagDao
) {
    fun getAllTags(): Flow<List<Tag>> = tagDao.getAllTags()

    fun getTagsForArticle(articleId: String): Flow<List<Tag>> =
        tagDao.getTagsForArticle(articleId)

    suspend fun createTag(name: String, color: String? = null): String {
        val existing = tagDao.getTagByName(name)
        if (existing != null) return existing.id
        return tagDao.insertTag(Tag(name = name, color = color))
    }

    suspend fun deleteTag(tag: Tag) = tagDao.deleteTag(tag)
}
```

- [ ] **Step 3: 验证编译**

Run: `cd app && ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: add ArticleRepository and TagRepository"
```

---

### Task 6: 导航框架 + 收件箱页面

**Files:**
- Create: `app/src/main/java/com/lantianhcgp/readlater/ui/navigation/AppNavigation.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/ui/inbox/InboxUiState.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/ui/inbox/InboxViewModel.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/ui/inbox/InboxScreen.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/ui/components/ArticleCard.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/ui/components/EmptyState.kt`

**Interfaces:**
- Consumes: `ArticleRepository`
- Produces: `AppNavigation`, `InboxScreen`

- [ ] **Step 1: 创建 ArticleCard 组件**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/ui/components/ArticleCard.kt
package com.lantianhcgp.readlater.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lantianhcgp.readlater.data.db.entity.Article
import com.lantianhcgp.readlater.data.model.ArticleStatus

@Composable
fun ArticleCard(
    article: Article,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (article.imageUrl != null) {
                AsyncImage(
                    model = article.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = article.sourceDomain.take(2).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = article.title ?: article.url,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (article.status == ArticleStatus.PENDING)
                    MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.sourceDomain,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (article.readingTimeMinutes != null) {
                    Text(
                        text = "· ${article.readingTimeMinutes} 分钟",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 2: 创建 EmptyState 组件**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/ui/components/EmptyState.kt
package com.lantianhcgp.readlater.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
```

- [ ] **Step 3: 创建 InboxUiState**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/ui/inbox/InboxUiState.kt
package com.lantianhcgp.readlater.ui.inbox

import com.lantianhcgp.readlater.data.db.entity.Article

data class InboxUiState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = ""
)
```

- [ ] **Step 4: 创建 InboxViewModel**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/ui/inbox/InboxViewModel.kt
package com.lantianhcgp.readlater.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lantianhcgp.readlater.data.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InboxUiState())
    val uiState: StateFlow<InboxUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            articleRepository.getAllArticles().collect { articles ->
                _uiState.update { it.copy(articles = articles, isLoading = false) }
            }
        }
    }
}
```

- [ ] **Step 5: 创建 InboxScreen**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/ui/inbox/InboxScreen.kt
package com.lantianhcgp.readlater.ui.inbox

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lantianhcgp.readlater.ui.components.ArticleCard
import com.lantianhcgp.readlater.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    onArticleClick: (String) -> Unit,
    viewModel: InboxViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("收件箱") },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        if (uiState.articles.isEmpty() && !uiState.isLoading) {
            EmptyState(
                title = "收件箱是空的",
                subtitle = "点击右下角 + 保存一篇文章",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(
                    items = uiState.articles,
                    key = { it.id }
                ) { article ->
                    ArticleCard(
                        article = article,
                        onClick = { onArticleClick(article.id) }
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 6: 创建 AppNavigation**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/ui/navigation/AppNavigation.kt
package com.lantianhcgp.readlater.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lantianhcgp.readlater.ui.favorites.FavoritesScreen
import com.lantianhcgp.readlater.ui.inbox.InboxScreen
import com.lantianhcgp.readlater.ui.reader.ReaderScreen
import com.lantianhcgp.readlater.ui.settings.SettingsScreen
import com.lantianhcgp.readlater.ui.tags.TagsScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Inbox : Screen("inbox", "收件箱", Icons.Default.Home)
    data object Tags : Screen("tags", "标签", Icons.Default.Label)
    data object Favorites : Screen("favorites", "收藏夹", Icons.Default.Favorite)
    data object Settings : Screen("settings", "设置", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Inbox,
    Screen.Tags,
    Screen.Favorites,
    Screen.Settings
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavItems.any { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Inbox.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Inbox.route) {
                InboxScreen(
                    onArticleClick = { articleId ->
                        navController.navigate("reader/$articleId")
                    }
                )
            }
            composable(Screen.Tags.route) {
                TagsScreen()
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable(
                route = "reader/{articleId}",
                arguments = listOf(navArgument("articleId") { type = NavType.StringType })
            ) {
                ReaderScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
```

- [ ] **Step 7: 创建占位页面（Tags, Favorites, Settings）**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/ui/tags/TagsScreen.kt
package com.lantianhcgp.readlater.ui.tags

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun TagsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("标签页 - 待实现")
    }
}
```

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/ui/favorites/FavoritesScreen.kt
package com.lantianhcgp.readlater.ui.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun FavoritesScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("收藏夹 - 待实现")
    }
}
```

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/ui/settings/SettingsScreen.kt
package com.lantianhcgp.readlater.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SettingsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("设置页 - 待实现")
    }
}
```

- [ ] **Step 8: 创建 Reader 占位页面**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/ui/reader/ReaderScreen.kt
package com.lantianhcgp.readlater.ui.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ReaderScreen(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("阅读器 - 待实现")
    }
}
```

- [ ] **Step 9: 验证编译**

Run: `cd app && ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 10: Commit**

```bash
git add -A
git commit -m "feat: add navigation, inbox screen, and placeholder screens"
```

---

### Task 7: 添加链接页面 + Agent 集成

**Files:**
- Create: `app/src/main/java/com/lantianhcgp/readlater/ui/addlink/AddLinkUiState.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/ui/addlink/AddLinkViewModel.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/ui/addlink/AddLinkScreen.kt`
- Modify: `app/src/main/java/com/lantianhcgp/readlater/ui/navigation/AppNavigation.kt` (添加 AddLink 路由)

**Interfaces:**
- Consumes: `ArticleRepository`, `LlmConfig`
- Produces: `AddLinkScreen`, `AddLinkViewModel`

- [ ] **Step 1: 创建 AddLinkUiState**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/ui/addlink/AddLinkUiState.kt
package com.lantianhcgp.readlater.ui.addlink

data class AddLinkUiState(
    val url: String = "",
    val isProcessing: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)
```

- [ ] **Step 2: 创建 AddLinkViewModel**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/ui/addlink/AddLinkViewModel.kt
package com.lantianhcgp.readlater.ui.addlink

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lantianhcgp.readlater.data.model.ArticleStatus
import com.lantianhcgp.readlater.data.model.LlmConfig
import com.lantianhcgp.readlater.data.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddLinkViewModel @Inject constructor(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddLinkUiState())
    val uiState: StateFlow<AddLinkUiState> = _uiState.asStateFlow()

    // Default LLM config - in real app, load from DataStore
    private val llmConfig = LlmConfig(
        provider = "openai",
        baseUrl = "https://api.openai.com/v1",
        apiKey = "",
        model = "gpt-4o"
    )

    fun onUrlChange(url: String) {
        _uiState.update { it.copy(url = url) }
    }

    fun saveLink() {
        val url = _uiState.value.url.trim()
        if (url.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }

            try {
                val articleId = articleRepository.addArticle(url)
                _uiState.update { it.copy(isSaved = true, isProcessing = false) }

                // Process with AI in background
                articleRepository.processArticle(articleId, llmConfig)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isProcessing = false, error = e.message)
                }
            }
        }
    }

    fun reset() {
        _uiState.value = AddLinkUiState()
    }
}
```

- [ ] **Step 3: 创建 AddLinkScreen**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/ui/addlink/AddLinkScreen.kt
package com.lantianhcgp.readlater.ui.addlink

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLinkScreen(
    onBack: () -> Unit,
    viewModel: AddLinkViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加链接") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = uiState.url,
                onValueChange = viewModel::onUrlChange,
                label = { Text("粘贴链接") },
                placeholder = { Text("https://example.com/article") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isProcessing
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = viewModel::saveLink,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.url.isNotBlank() && !uiState.isProcessing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("保存")
                }
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
```

- [ ] **Step 4: 更新 AppNavigation 添加 AddLink 路由**

在 `AppNavigation.kt` 的 `NavHost` 中添加：

```kotlin
composable("addLink") {
    AddLinkScreen(
        onBack = { navController.popBackStack() }
    )
}
```

并在 `InboxScreen` 中添加 FAB 导航到 `addLink`。

- [ ] **Step 5: 验证编译**

Run: `cd app && ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: add AddLink screen with Agent integration"
```

---

### Task 8: 阅读器页面

**Files:**
- Create: `app/src/main/java/com/lantianhcgp/readlater/ui/reader/ReaderUiState.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/ui/reader/ReaderViewModel.kt`
- Modify: `app/src/main/java/com/lantianhcgp/readlater/ui/reader/ReaderScreen.kt`

**Interfaces:**
- Consumes: `ArticleRepository`
- Produces: `ReaderScreen` (full implementation)

- [ ] **Step 1: 创建 ReaderUiState**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/ui/reader/ReaderUiState.kt
package com.lantianhcgp.readlater.ui.reader

import com.lantianhcgp.readlater.data.db.entity.Article
import com.lantianhcgp.readlater.data.db.entity.Tag

data class ReaderUiState(
    val article: Article? = null,
    val tags: List<Tag> = emptyList(),
    val isLoading: Boolean = true,
    val isFavorite: Boolean = false
)
```

- [ ] **Step 2: 创建 ReaderViewModel**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/ui/reader/ReaderViewModel.kt
package com.lantianhcgp.readlater.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lantianhcgp.readlater.data.repository.ArticleRepository
import com.lantianhcgp.readlater.data.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val articleRepository: ArticleRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val articleId: String = savedStateHandle["articleId"] ?: ""

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    init {
        loadArticle()
    }

    private fun loadArticle() {
        viewModelScope.launch {
            val article = articleRepository.getArticleById(articleId)
            _uiState.update {
                it.copy(
                    article = article,
                    isLoading = false,
                    isFavorite = article?.isFavorite ?: false
                )
            }

            tagRepository.getTagsForArticle(articleId).collect { tags ->
                _uiState.update { it.copy(tags = tags) }
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            articleRepository.toggleFavorite(articleId)
            _uiState.update { it.copy(isFavorite = !it.isFavorite) }
        }
    }
}
```

- [ ] **Step 3: 实现 ReaderScreen**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/ui/reader/ReaderScreen.kt
package com.lantianhcgp.readlater.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lantianhcgp.readlater.data.model.ArticleStatus
import com.lantianhcgp.readlater.ui.components.TagChip

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReaderScreen(
    onBack: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.article?.sourceDomain ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            if (uiState.isFavorite) Icons.Default.Favorite
                            else Icons.Default.FavoriteBorder,
                            contentDescription = "收藏",
                            tint = if (uiState.isFavorite)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { /* TODO: share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "分享")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.article != null) {
            val article = uiState.article!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Title
                Text(
                    text = article.title ?: article.url,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Meta info
                Text(
                    text = buildString {
                        append(article.sourceDomain)
                        article.readingTimeMinutes?.let { append(" · $it 分钟阅读") }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Tags
                if (uiState.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        uiState.tags.forEach { tag ->
                            TagChip(name = tag.name)
                        }
                    }
                }

                // AI Summary
                if (article.summary != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "✨ AI 摘要",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = article.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                        )
                    }
                }

                // Content
                if (article.plainText != null) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = article.plainText,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                    )
                }

                // Processing state
                if (article.status == ArticleStatus.PROCESSING) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.height(24.dp), strokeWidth = 2.dp)
                    }
                    Text(
                        text = "AI 正在处理中...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
```

- [ ] **Step 4: 创建 TagChip 组件**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/ui/components/TagChip.kt
package com.lantianhcgp.readlater.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun TagChip(
    name: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = name,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
```

- [ ] **Step 5: 验证编译**

Run: `cd app && ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: implement reader screen with AI summary and article content"
```

---

### Task 9: 设置页面（LLM 配置）

**Files:**
- Create: `app/src/main/java/com/lantianhcgp/readlater/ui/settings/SettingsUiState.kt`
- Create: `app/src/main/java/com/lantianhcgp/readlater/ui/settings/SettingsViewModel.kt`
- Modify: `app/src/main/java/com/lantianhcgp/readlater/ui/settings/SettingsScreen.kt`

**Interfaces:**
- Consumes: `LlmConfig`
- Produces: `SettingsScreen` (full implementation)

- [ ] **Step 1: 创建 SettingsUiState**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/ui/settings/SettingsUiState.kt
package com.lantianhcgp.readlater.ui.settings

import com.lantianhcgp.readlater.data.model.LlmConfig

data class SettingsUiState(
    val llmConfig: LlmConfig = LlmConfig(),
    val isDarkMode: Boolean? = null, // null = follow system
    val providerOptions: List<String> = listOf("openai", "deepseek", "ollama", "qwen", "openrouter")
)
```

- [ ] **Step 2: 创建 SettingsViewModel**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/ui/settings/SettingsViewModel.kt
package com.lantianhcgp.readlater.ui.settings

import androidx.lifecycle.ViewModel
import com.lantianhcgp.readlater.data.model.LlmConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateProvider(provider: String) {
        val defaults = when (provider) {
            "openai" -> LlmConfig(provider = "openai", baseUrl = "https://api.openai.com/v1", model = "gpt-4o")
            "deepseek" -> LlmConfig(provider = "deepseek", baseUrl = "https://api.deepseek.com/v1", model = "deepseek-chat")
            "ollama" -> LlmConfig(provider = "ollama", baseUrl = "http://localhost:11434/v1", model = "llama3")
            "qwen" -> LlmConfig(provider = "qwen", baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1", model = "qwen-plus")
            "openrouter" -> LlmConfig(provider = "openrouter", baseUrl = "https://openrouter.ai/api/v1", model = "openai/gpt-4o")
            else -> LlmConfig()
        }
        _uiState.update { it.copy(llmConfig = defaults) }
    }

    fun updateBaseUrl(baseUrl: String) {
        _uiState.update { it.copy(llmConfig = it.llmConfig.copy(baseUrl = baseUrl)) }
    }

    fun updateApiKey(apiKey: String) {
        _uiState.update { it.copy(llmConfig = it.llmConfig.copy(apiKey = apiKey)) }
    }

    fun updateModel(model: String) {
        _uiState.update { it.copy(llmConfig = it.llmConfig.copy(model = model)) }
    }
}
```

- [ ] **Step 3: 实现 SettingsScreen**

```kotlin
// app/src/main/java/com/lantianhcgp/readlater/ui/settings/SettingsScreen.kt
package com.lantianhcgp.readlater.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("设置") },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // LLM Provider section
            Text(
                text = "AI 模型配置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Provider selector (simple text for now)
            Text(
                text = "当前 Provider: ${uiState.llmConfig.provider}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.llmConfig.baseUrl,
                onValueChange = viewModel::updateBaseUrl,
                label = { Text("Base URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.llmConfig.apiKey,
                onValueChange = viewModel::updateApiKey,
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.llmConfig.model,
                onValueChange = viewModel::updateModel,
                label = { Text("模型名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // About section
            Text(
                text = "关于",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "ReadLater v1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "开源 AI 驱动的稍后阅读 App",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

- [ ] **Step 4: 验证编译**

Run: `cd app && ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: implement settings screen with LLM provider configuration"
```

---

### Task 10: 最终集成测试 + 推送

**Files:**
- Modify: `app/src/main/java/com/lantianhcgp/readlater/ui/inbox/InboxScreen.kt` (添加 FAB)
- Modify: `app/src/main/java/com/lantianhcgp/readlater/ui/navigation/AppNavigation.kt` (添加 FAB 导航)

- [ ] **Step 1: 在 InboxScreen 添加 FAB**

```kotlin
// 在 InboxScreen 的 Scaffold 中添加 floatingActionButton 参数
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    onArticleClick: (String) -> Unit,
    onAddClick: () -> Unit,
    viewModel: InboxViewModel = hiltViewModel()
) {
    // ... existing code ...
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { /* ... */ },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加链接")
            }
        }
    ) { padding ->
        // ... existing content ...
    }
}
```

- [ ] **Step 2: 更新 AppNavigation 传递 onAddClick**

```kotlin
composable(Screen.Inbox.route) {
    InboxScreen(
        onArticleClick = { articleId ->
            navController.navigate("reader/$articleId")
        },
        onAddClick = {
            navController.navigate("addLink")
        }
    )
}
```

- [ ] **Step 3: 添加 Coil 依赖（用于图片加载）**

在 `app/build.gradle.kts` 的 dependencies 中添加：
```kotlin
implementation("io.coil-kt:coil-compose:2.6.0")
```

- [ ] **Step 4: 全量编译验证**

Run: `cd app && ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: 推送到 GitHub**

```bash
git add -A
git commit -m "feat: complete Phase 1 MVP - inbox, reader, add link, settings"
git push origin main
```

---

## Phase 1 完成检查清单

- [x] 项目脚手架 (Compose + Hilt + Room)
- [x] Room 数据库 + 数据模型
- [x] LLM Provider 抽象层
- [x] AI Agent 核心 (Tool Calling)
- [x] ArticleRepository + TagRepository
- [x] 导航框架 + 收件箱页面
- [x] 添加链接页面 + Agent 集成
- [x] 阅读器页面
- [x] 设置页面 (LLM 配置)
- [x] FAB + 导航集成

## 后续 Phase

- **Phase 2:** 高亮标注、笔记、收藏夹完善、全文搜索、深色模式、WebDAV 同步
- **Phase 3:** 浏览器分享、剪贴板监听、批量导入、桌面小组件
