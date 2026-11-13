# Elasticsearch Plugin Archetype (7.10.2)

Fork 自 [codelibs/elasticsearch-plugin-archetype](https://github.com/codelibs/elasticsearch-plugin-archetype)，已改造为面向 **Elasticsearch 7.10.2** 的 Maven archetype：

- `pluginType` 参数：一个参数生成**任意类型**的 ES 插件工程（REST、analysis、ingest、mapper、script、search……甚至任意自定义插件接口）。
- `buildTool` 参数：接入 **Elasticsearch 官方 Gradle 插件 `elasticsearch.esplugin`（打包 / descriptor / run）与 `elasticsearch.testclusters`（真实集群集成测试）**，同时保留经典 Maven 构建。

能力矩阵（本次把后两列补进 archetype 生成的项目）：

| 方案 | 脚手架生成 | 基础代码 | 官方打包 / descriptor | 真实集群集成测试 |
|:---|:---:|:---:|:---:|:---:|
| Maven Archetype（本仓库） | 支持 | 支持 | Maven assembly | cluster-runner |
| `elasticsearch.esplugin` | — | — | 支持 | — |
| `elasticsearch.testclusters` | — | — | — | 支持 |
| **本仓库生成的项目** | **支持** | **支持** | **支持（Gradle）** | **支持（Gradle）** |

| 项目 | 值 |
|:---|:---|
| archetypeGroupId | `org.codelibs` |
| archetypeArtifactId | `elasticsearch-plugin-archetype` |
| archetypeVersion | `7.10.2` |
| 默认 elasticsearchVersion | `7.10.2` |
| 默认 pluginType | `rest` |
| 默认 buildTool | `both`（Maven + Gradle 都生成） |
| 许可证 | Apache License 2.0（沿用上游） |

## 环境要求

- JDK：Maven 编译/测试用 JDK 8+ 即可；`./gradlew bundlePlugin` 用 JDK 11+（实测 corretto-11.0.22）；**`./gradlew javaRestTest` 必须 JDK 14+** —— build-tools 的 reaper 辅助进程是 Java 14 字节码（class 58.0），testclusters 用 Gradle 同一个 JVM 启动它，JDK 11 会报 `UnsupportedClassVersionError` / "Reaper process died unexpectedly"。ES 7.10.2 发行版自带 JDK 15，可直接用它。生产集群建议 JDK 11+。生成工程 pom 默认 `maven.compiler.source/target=1.8`。
- Maven 3.3+（archetype 本身固定 `maven-archetype-plugin 3.2.1`）。
- Gradle 6.6.1（**wrapper 已内置到生成工程**，首次运行自动下载发行版）。

## 快速开始

先把 archetype 安装到本地仓库：

```bash
git clone https://github.com/zg900213/elasticsearch-plugin-archetype.git
cd elasticsearch-plugin-archetype
mvn clean install
```

生成一个默认（REST 插件、Maven + Gradle 双构建）工程：

```bash
mvn org.apache.maven.plugins:maven-archetype-plugin:3.2.1:generate \
  -DarchetypeGroupId=org.codelibs \
  -DarchetypeArtifactId=elasticsearch-plugin-archetype \
  -DarchetypeVersion=7.10.2 \
  -DgroupId=com.example \
  -DartifactId=elasticsearch-hello \
  -Dversion=1.0.0-SNAPSHOT \
  -Dpackage=com.example \
  -DpluginName=Hello \
  -DpluginType=rest \
  -DbuildTool=both \
  -DrestName=hello \
  -DelasticsearchVersion=7.10.2 \
  -DinteractiveMode=false
```

构建（任选其一，或两套都用）：

```bash
cd elasticsearch-hello

# Maven：产物 target/releases/elasticsearch-hello-1.0.0-SNAPSHOT.zip
mvn clean package

# Gradle：esplugin 自动生成 descriptor，产物 build/distributions/*.zip
./gradlew bundlePlugin
./gradlew javaRestTest     # testclusters 起真实 ES 7.10.2 节点并安装插件
./gradlew run              # 前台启动带插件的节点
```

安装到节点：

```bash
bin/elasticsearch-plugin install file:///绝对路径/elasticsearch-hello-1.0.0-SNAPSHOT.zip
```

REST 类型会生成可直接调用的接口 `http://localhost:9200/_hello` 与 `/{index}/_hello`。

## 参数

| 参数 | 必填 | 默认值 | 说明 |
|:---|:---:|:---|:---|
| `pluginType` | 否 | `rest` | 插件类型（见下表）：内置别名、`XxxPlugin` 简写或任意全限定接口名 |
| `buildTool` | 否 | `both` | **新增**：`maven`（仅 pom + assembly）、`gradle`（仅 esplugin + testclusters）、`both`（两套都生成） |
| `pluginName` | 否 | `Hello` | 类名前缀，生成 `${pluginName}Plugin`、`Rest${pluginName}Action` |
| `elasticsearchVersion` | 否 | `7.10.2` | ES 版本；Maven 测试依赖拼为 `<版本>.0`；Gradle 用它选 `build-tools` 版本 |
| `restName` | 否 | `hello` | 仅 `pluginType=rest` 有效 |
| `groupId` / `artifactId` / `version` / `package` | 是 | — | Maven 坐标与 Java 包名 |

`META-INF/archetype-post-generate.groovy` 会按 `buildTool` 清理另一套构建的文件：`maven` 删掉 Gradle wrapper / build.gradle / javaRestTest，`gradle` 删掉 pom.xml / assembly / Maven 测试；`both` 全部保留。两套构建**共用同一份 `src/main/java` 插件源码**。

## 接入 elasticsearch.esplugin / elasticsearch.testclusters

生成的 `build.gradle` 做法（已实测可运行）：

```groovy
buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven { url 'https://maven.aliyun.com/repository/public' }  // JCenter 历史依赖镜像
    }
    dependencies { classpath 'org.elasticsearch.gradle:build-tools:7.10.2' }
}
apply plugin: 'elasticsearch.esplugin'        // descriptor + bundlePlugin + run
apply plugin: 'elasticsearch.testclusters'    // 真实集群
apply plugin: 'elasticsearch.java-rest-test'  // 创建 javaRestTest 任务并自动 useCluster

esplugin {
    name 'elasticsearch-hello'
    description 'Hello Plugin for Elasticsearch 7.10.2'
    classname 'com.example.HelloPlugin'
    licenseFile file('LICENSE.txt')   // BuildPlugin 强制要求
    noticeFile file('NOTICE.txt')
}
testClusters.all { testDistribution = 'DEFAULT' }
```

要点：

- `build-tools:7.10.2` 在 Maven Central 上有，且**jar 内确实注册了** `elasticsearch.esplugin`（`PluginBuildPlugin`）与 `elasticsearch.testclusters`（`TestClustersPlugin`），独立工程（非 ES 源码树）也能用；其 `version.properties` 写死 `elasticsearch=7.10.2`，descriptor 由 esplugin 自动生成，无需手写。
- 三个仓库缺一不可：`shadow 6.0.0` 在 Gradle Plugin Portal，`nebula-core 3.0.0`、`gradle-docker-compose-plugin 0.12.1` 只在 JCenter 镜像（JCenter 已关闭），所以模板额外配了一个公共镜像，可自行替换。
- `BuildPlugin` 要求 `licenseFile` / `noticeFile`，模板已生成 `LICENSE.txt` / `NOTICE.txt`；`testClusters.all` 不要写 `numberOfNodes = 1`（集群默认已有 1 节点，会被判定为"shrink"而报错）。
- `elasticsearch.java-rest-test` 会创建 `javaRestTest` sourceSet/`RestIntegTestTask`，并自动把 `bundlePlugin` 的 zip 装进 testclusters 集群；测试类在 `src/javaRestTest/java/.../${pluginName}RestIT.java`（继承 `ESRestTestCase`）。
- 独立工程里 `org.elasticsearch.test:framework:7.10.2` 不会把 `log4j-core` 带到 javaRestTest 的 runtime classpath，`ESRestTestCase` 初始化会抛 `NoClassDefFoundError: org/apache/logging/log4j/core/Layout`；模板已显式加 `javaRestTestImplementation 'org.apache.logging.log4j:log4j-core:2.11.1'`。
- 为避免两套构建各写一个 `plugin-descriptor.properties`，Maven 的描述符模板改名为 `plugin-descriptor.properties.tmpl`，由 assembly 的 `<destName>` 在打包时重命名；Gradle 的 `bundlePlugin` 用 `exclude '**/*.tmpl'` 排除它。

## pluginType：一个参数生成任意类型插件

内置别名（均映射到 ES 7.10.2 `org.elasticsearch.plugins` 包中的真实接口）：

| pluginType | 生成的接口 | 典型扩展点 |
|:---|:---|:---|
| `rest`（默认） | `ActionPlugin` | 完整 REST 示例（`routes()` + `getRestHandlers()`）+ Maven/Gradle 两种集成测试 |
| `action` | `ActionPlugin` | `getActions()`、`getActionFilters()`、`getRestHandlers()` |
| `analysis` | `AnalysisPlugin` | `getCharFilters()`、`getTokenFilters()`、`getAnalyzers()` |
| `ingest` | `IngestPlugin` | `getProcessors(Processor.Parameters)` |
| `mapper` | `MapperPlugin` | `getMappers()`、`getMetadataMappers()`、`getFieldFilter()` |
| `script` | `ScriptPlugin` | `getScriptEngine(...)`、`getContexts()` |
| `search` | `SearchPlugin` | `getQueries()`、`getAggregations()`、`getSuggesters()` |
| `repository` | `RepositoryPlugin` | `getRepositories(...)` |
| `discovery` | `DiscoveryPlugin` | `getDiscoveryTypes(...)`、`getSeedProviders(...)` |
| `engine` | `EnginePlugin` | `getEngineFactory(...)` |
| `indexstore` | `IndexStorePlugin` | `getDirectoryFactories(...)` |
| `systemindex` | `SystemIndexPlugin` | `getSystemIndexDescriptors(...)` |
| `circuitbreaker` | `CircuitBreakerPlugin` | `getCircuitBreaker(...)` |
| `cluster` | `ClusterPlugin` | `getAllocationDeciders(...)` |
| `network` | `NetworkPlugin` | `getHttpTransports(...)` |
| `persistent` | `PersistentTaskPlugin` | `getPersistentTasksExecutor(...)` |
| `reloadable` | `ReloadablePlugin` | `reload(Settings)` |
| `extensible` | `ExtensiblePlugin` | `loadExtensions(...)` |
| `basic` / `generic` / `none` | 仅 `Plugin` | 最简插件骨架 |

另外三种写法让"任意类型"无上限：`-DpluginType=AnalysisPlugin`（简写自动补全）、`-DpluginType=org.elasticsearch.plugins.RepositoryPlugin`（FQCN）、`-DpluginType=com.example.spi.MyPluginInterface`（自定义 SPI，需自行补依赖）。未知别名退回纯 `Plugin` 并在 TODO 注释里给出警告。

非 `rest` 类型不生成 REST handler（`archetype-post-generate.groovy` 清理占位文件），Maven 测试改为不启动集群的冒烟测试，pom 不引入 cluster-runner；Gradle 的 `javaRestTest` 仍会启动集群验证插件能被加载。

## 相比上游 6.6 版的改动

- `archetype-metadata.xml`：`elasticsearchVersion` 默认 `7.10.2`，新增 `pluginType` / `buildTool` 参数并补默认值；新增根目录 Gradle 文件、wrapper、`src/javaRestTest` 的 fileSet。
- archetype 自身 pom：显式 `maven-archetype` packaging，固定 archetype 插件 3.2.1。
- 生成的插件类按 `pluginType` 实现对应 7.10.2 插件接口（Velocity 映射），rest 类型给出完整可用示例；类名与接口同名时自动回退全限定名。
- REST 示例改用 7.x API：`BaseRestHandler` 无参构造 + `getName()` / `routes()` / `Route`，插件通过 `getRestHandlers(...)` 返回 handler。
- 生成工程 pom 修正 cluster-runner 的 groupId 为 `org.codelibs`（上游误写为用户 groupId），版本 `7.10.2.0`，junit 升到 4.13.2。
- 集成测试：3 节点 + `discovery.zen.ping.unicast.hosts` → 单节点 `discovery.type=single-node`；`EcrCurl.jsonParser` → `jsonParser()`。
- **新增 Gradle 路线**：`build.gradle` / `settings.gradle` / `gradle.properties` / Gradle 6.6.1 wrapper / `LICENSE.txt` / `NOTICE.txt` / `src/javaRestTest` 模板，接入官方 `esplugin` + `testclusters`。
- Maven descriptor 模板改名 `.tmpl` + assembly `destName`，避免双构建下 zip 内出现两个 descriptor。
- 新增 `archetype-post-generate.groovy`（按 pluginType / buildTool 清理文件）与生成工程自带的 `README.md`。
- 移除仅适用于上游 6.x sample 工程的维护脚本 `update_src.sh`。

## 验证记录（本机实测）

环境：Windows / JDK 1.8.0_101（Maven）、corretto-11.0.22（Gradle）/ Maven 3.3.9 / Gradle 6.6.1。

- `mvn clean install`：archetype 构建、安装成功。
- 生成 6 种 pluginType 工程并核对映射：`rest`→`ActionPlugin`、`analysis`→`AnalysisPlugin`、`IngestPlugin` 简写、FQCN、`basic`、未知别名，全部正确；6 个工程 `mvn test-compile` 通过。
- `rest`：`mvn test` 通过（单节点 ES 7.10.2 启动、插件加载、REST 断言，1 test / 0 failure）；`basic`：`mvn test` 通过；`mvn -DskipTests package` 产出 zip，descriptor 属性（`classname` / `elasticsearch.version=7.10.2` / `java.version=1.8`）正确。
- **独立 Gradle 工程实测**（`build-tools:7.10.2`）：`gradle tasks --all` 成功加载 `elasticsearch.esplugin`、`elasticsearch.testclusters`、`elasticsearch.java-rest-test`，可见 `bundlePlugin` / `javaRestTest` / `pluginProperties` 等任务；`gradle bundlePlugin` **BUILD SUCCESSFUL**，产出 zip 内含 esplugin 自动生成的 `plugin-descriptor.properties`（`elasticsearch.version=7.10.2`）、插件 jar、`LICENSE.txt`、`NOTICE.txt`。
- **生成工程实测**（自带 wrapper，Gradle 6.6.1 + JDK 11）：`./gradlew bundlePlugin` 在 `buildTool=gradle`（analysis）与 `buildTool=both`（rest）下均 BUILD SUCCESSFUL；both 模式的 zip 只有一个 descriptor、无 `.tmpl` 泄漏；`./gradlew javaRestTestClasses` 编译通过（`ESRestTestCase` 依赖可用）。
- Maven 侧：descriptor 模板改名 `.tmpl` 后，`mvn test-compile` 与 `mvn -DskipTests package` 仍通过，zip 内 `plugin-descriptor.properties` 属性正确（assembly `destName` 生效）。
- 三个 buildTool 值（maven / gradle / both）均能生成且文件清理正确；wrapper jar 生成后与源文件 SHA-256 一致，gradlew 的 shell 变量未被 Velocity 破坏。
- 未实际执行 `./gradlew javaRestTest` 启动集群（首次需下载 ES 7.10.2 发行版，数百 MB）；任务与测试类已验证存在且可编译。

## 已知限制

- Gradle 路线需 Gradle 6.6.1；`javaRestTest` 另需 JDK 14+（reaper 字节码限制，详见上文），首次还会下载 ES 7.10.2 发行版（数百 MB），耗时取决于网络。`build-tools` 的 JCenter 历史依赖依赖模板中的公共镜像，离线环境需提前缓存。Windows 上若 JAVA_HOME 指向 testclusters 目录内的 JDK，会因文件锁导致清理失败，应把 JDK 复制到独立目录。
- `elasticsearchVersion` 传非 `7.10.2` 时：Maven 测试依赖 `<版本>.0` 需要有对应 cluster-runner；Gradle 的 `build-tools:<版本>` 也需要存在。
- 自定义全限定接口需自行在生成工程补依赖（Maven 加 pom，Gradle 加 `dependencies { compileOnly ... }`）。
- `META-INF/maven/archetype.xml` 是上游遗留的 1.x 描述符，仅为兼容保留；权威配置是 `archetype-metadata.xml`，请使用 archetype-plugin 3.2.1。
- Windows 上 Maven REST 集成测试结束后，cluster-runner 清理临时目录可能打印"文件被占用"，不影响结果。

## 许可

Apache License 2.0，见 [LICENSE](LICENSE)。原作者 codelibs / Shinsuke Sugaya 的版权声明保留；Gradle wrapper 脚本来自 Elasticsearch 仓库（Apache 2.0）。
