# Elasticsearch 7.10.2 插件脚手架（照着做就能跑通）

这是一个 **Maven archetype（项目模板生成器）**。运行一条命令，它就帮你生成一个能直接编译、打包、安装到 Elasticsearch 7.10.2 的插件工程，连示例代码和构建脚本都写好了。

它能帮你做三件事：

1. **不用从零建工程**：目录结构、`pom.xml`、`build.gradle`、插件入口类、REST 示例、测试骨架全都自动生成。
2. **一个参数换插件种类**：`-DpluginType=...`，REST、analysis、ingest、mapper、script、search……甚至自定义接口都行。
3. **一套代码，两种构建方式**：既能用 Maven 打包，也能用 Elasticsearch 官方的 Gradle 插件 `elasticsearch.esplugin`（打包）和 `elasticsearch.testclusters`（起真实集群做集成测试）。

> 本仓库 fork 自 [codelibs/elasticsearch-plugin-archetype](https://github.com/codelibs/elasticsearch-plugin-archetype)，原版只支持 ES 6.6。这里已改造为 **ES 7.10.2**，并补全了上面第 2、3 点。

---

## 第 0 步：先看懂你会得到什么

跑完第 3 步后，会得到这样一个目录（默认 `buildTool=both`，以项目名 `elasticsearch-hello` 为例）：

```
elasticsearch-hello/
├── pom.xml                  ← Maven 构建文件
├── build.gradle              ← Gradle 构建文件（用官方 esplugin / testclusters）
├── gradlew / gradlew.bat     ← Gradle 6.6.1 wrapper，不用自己装 Gradle
├── settings.gradle
├── gradle.properties
├── LICENSE.txt / NOTICE.txt  ← esplugin 强制要求，别删
├── README.md                 ← 生成项目自带的说明
└── src/
    ├── main/java/com/example/HelloPlugin.java        ← 插件入口类（核心，改这里写业务逻辑）
    ├── main/java/com/example/rest/RestHelloAction.java ← REST 示例（仅 pluginType=rest）
    ├── main/plugin-metadata/                        ← Maven 版描述符模板
    ├── main/assemblies/                             ← Maven 打包配置
    ├── test/java/                                   ← Maven 测试
    └── javaRestTest/java/                           ← Gradle 真实集群测试
```

**两种构建共用同一份 `src/main/java` 源码**。你写一次插件代码，Maven 和 Gradle 都能打包。

---

## 第 1 步：准备环境

| 你要做的事 | 需要什么 | 说明 |
|:---|:---|:---|
| 只用 Maven 生成 / 打包 / 测试 | **JDK 8+**、Maven 3.3+ | 实测 JDK 1.8.0_101 + Maven 3.3.9 可以 |
| 用 Gradle 打包（`bundlePlugin`） | **JDK 11+** | 实测 corretto-11.0.22 可以 |
| 用 Gradle 跑真实集群测试（`javaRestTest`） | **JDK 14+（推荐 JDK 15）** | 官方 build-tools 里的 reaper 是 Java 14 字节码，JDK 11 会报错；ES 7.10.2 发行版自带 JDK 15，直接用它最省事 |
| 运行 Elasticsearch 7.10.2 节点 | JDK 11+ 或发行版自带 JDK 15 | 和插件里的 `elasticsearch.version` 必须同为 `7.10.2` |
| Gradle | **不用装** | 生成的项目自带 Gradle 6.6.1 wrapper |

> **不知道装哪个 JDK？装 JDK 15，上面所有场景都能跑。**
> Gradle 路线首次会下载 ES 7.10.2 发行版（约 300 MB），需要联网，请耐心等。

先检查本机环境（Windows 用 Git Bash / PowerShell 都行）：

```bash
java -version
mvn -v
```

能看到版本号就可以继续。

---

## 第 2 步：把"模板工具"装到本机

> ⚠️ 这一步不能跳。archetype 必须先安装到本机 Maven 仓库，第 3 步才能用它生成项目。

```bash
git clone https://github.com/zg900213/elasticsearch-plugin-archetype.git
cd elasticsearch-plugin-archetype
mvn clean install
```

**✅ 成功标志**：控制台出现 `BUILD SUCCESS`，并且本地 Maven 仓库里出现
`org/codelibs/elasticsearch-plugin-archetype/7.10.2/elasticsearch-plugin-archetype-7.10.2.jar`。

> 如果你之前装过旧版本，重新执行一次 `mvn clean install` 就会覆盖，不会冲突。

---

## 第 3 步：生成一个插件项目

**第一次建议原样复制、先跑通，再改名字。**

在**任意空目录**（不要在仓库目录里）执行：

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

**Windows PowerShell 用户**：把上面每行末尾的 `\` 删掉，整条命令写成一行再执行（PowerShell 的续行符是 `` ` ``，不是 `\`）。

**✅ 成功标志**：出现 `elasticsearch-hello/` 目录，且里面有 `pom.xml`、`build.gradle`、`src/main/java/com/example/HelloPlugin.java`。

### 每个参数是干什么的

| 参数 | 例子 | 大白话 |
|:---|:---|:---|
| `archetypeVersion` | `7.10.2` | 模板本身的版本，和第 2 步安装的一致，**别改** |
| `groupId` / `artifactId` / `version` / `package` | `com.example` / `elasticsearch-hello` / `1.0.0-SNAPSHOT` / `com.example` | Maven 坐标和 Java 包名，按你项目改 |
| `pluginName` | `Hello` | 类名前缀，会生成 `HelloPlugin.java`；**用大写字母开头** |
| `pluginType` | `rest` | 插件种类，见下 |
| `buildTool` | `both` | `maven`＝只生成 Maven 文件；`gradle`＝只生成 Gradle 文件；`both`＝两套都生成 |
| `restName` | `hello` | 接口地址里的名字，最终是 `/_hello` |
| `elasticsearchVersion` | `7.10.2` | 目标 ES 版本。**必须和你要安装的节点版本完全一致**，一般别改 |
| `interactiveMode` | `false` | 不让 Maven 一路问问题，脚本里必加 |

### 想换插件种类？只改 `-DpluginType`

下面每行都是**替换项**：把第 3 步那条完整命令里的 `-DpluginType=rest` 换成对应的一行，其它参数原样保留（不是单独执行的命令）。

```bash
# 1) 分析插件（分词器 / 过滤器）
-DpluginType=analysis

# 2) ingest 处理器插件
-DpluginType=ingest

# 3) 脚本 / 查询 / mapper 插件
-DpluginType=script
-DpluginType=search
-DpluginType=mapper

# 4) 简写：直接写接口名，模板自动补全包名
-DpluginType=AnalysisPlugin

# 5) 任意类型：写完整的接口名
-DpluginType=org.elasticsearch.plugins.RepositoryPlugin
-DpluginType=com.example.spi.MyPluginInterface
```

> 自定义接口需要在生成的项目里自己补依赖（Maven 加 `pom.xml`，Gradle 加 `build.gradle` 的 `dependencies`）。
> 完整类型表见文末「附录 A」。

### 只想用一种构建工具？改 `-DbuildTool`

```bash
-DbuildTool=maven    # 只有 pom.xml，适合已有 Maven 流程
-DbuildTool=gradle   # 只有 build.gradle + wrapper，适合用官方 Gradle 插件
-DbuildTool=both     # 默认，两套都要
```

生成后脚本会自动把不用的那套文件删掉，项目不会乱。

---

## 第 4 步：把项目打包成插件 zip

进入项目目录：

```bash
cd elasticsearch-hello
```

### 方式 A：Maven（最简单，JDK 8+ 就够）

```bash
mvn clean package
```

> 这条命令会顺带跑一遍测试（约半分钟，会启动一个单节点 ES 7.10.2）。只想快速出包可以写 `mvn clean package -DskipTests`。

**✅ 成功标志**：`BUILD SUCCESS`，并生成

```
target/releases/elasticsearch-hello-1.0.0-SNAPSHOT.zip
```

### 方式 B：Gradle（用官方 `elasticsearch.esplugin`）

```bash
# Windows Git Bash / Linux / macOS
./gradlew bundlePlugin

# Windows CMD / PowerShell 用这个
gradlew.bat bundlePlugin
```

**✅ 成功标志**：`BUILD SUCCESSFUL`，并生成

```
build/distributions/elasticsearch-hello-1.0.0-SNAPSHOT.zip
```

这个 zip 里的 `plugin-descriptor.properties` 是 `esplugin` **自动生成**的，不用手写。

> 第一次运行会自动下载 Gradle 6.6.1 和依赖，可能要几分钟。
> 如果报 `Reaper process died unexpectedly` 或 `UnsupportedClassVersionError`，说明你的 JDK 太旧 —— `bundlePlugin` 用 JDK 11+，跑 `javaRestTest` 要 JDK 14+（推荐 JDK 15）。

---

## 第 5 步：把 zip 装进 Elasticsearch 并验证

> 这是最容易卡住的一步，按顺序做。

### 5.1 准备一个 ES 7.10.2 节点

下载并解压官方发行版（版本必须是 **7.10.2**）：

- Windows：`elasticsearch-7.10.2-windows-x86_64.zip`（自带 JDK 15）
- Linux/macOS：`elasticsearch-7.10.2-linux-x86_64.tar.gz`

解压后，编辑 `config/elasticsearch.yml`，写入最少配置：

```yaml
cluster.name: es-plugin-verify
node.name: node-1
discovery.type: single-node
http.port: 9200
network.host: 127.0.0.1
```

> 为什么写文件而不是命令行加 `-Ediscovery.type=single-node`？Windows 的 `.bat` 脚本对该参数解析有坑，写配置文件最稳。

### 5.2 安装插件（节点必须是停止状态）

把第 4 步生成的 zip 路径换成你自己的绝对路径：

```bash
# Linux / macOS
bin/elasticsearch-plugin install -b "file:///home/me/elasticsearch-hello/target/releases/elasticsearch-hello-1.0.0-SNAPSHOT.zip"

# Windows（PowerShell 或 CMD，注意用 .bat）
bin\elasticsearch-plugin.bat install -b "file:///C:/Users/me/elasticsearch-hello/target/releases/elasticsearch-hello-1.0.0-SNAPSHOT.zip"
```

**✅ 成功标志**：输出 `-> Installed hello`（Gradle 打包的 zip 会显示项目名，如 `elasticsearch-hello`）。

> ⚠️ Windows 不要用 Git Bash 里的 `bin/elasticsearch-plugin`（POSIX 脚本），会报
> `ClassNotFoundException: JavaVersionChecker`。请用 `elasticsearch-plugin.bat`。
> `-b` 表示跳过交互确认，脚本里建议加上。

### 5.3 启动节点

```bash
# Linux / macOS（前台运行，另开一个终端做后面的验证）
bin/elasticsearch

# Windows（PowerShell）
.\bin\elasticsearch.bat
```

**✅ 成功标志**：日志出现 `started`，且下面命令能返回 JSON：

```bash
curl http://localhost:9200/
```

返回里应有 `"number" : "7.10.2"`。

### 5.4 验证插件确实生效

> Windows PowerShell 里 `curl` 是 `Invoke-WebRequest` 的别名，请写成 `curl.exe`（Git Bash / CMD 直接用 `curl` 即可）。

```bash
# 1) 看插件列表
curl "http://localhost:9200/_cat/plugins?v"

# 2) 调插件自带的 REST 接口（pluginType=rest 才有）
curl "http://localhost:9200/_hello?pretty"
curl "http://localhost:9200/my_index/_hello?pretty"
```

**✅ 期望输出**（实测结果）：

```json
// GET /_hello
{
  "description" : "This is a elasticsearch-hello response: Fri Nov 13 ..."
}

// GET /my_index/_hello
{
  "index" : "my_index",
  "description" : "This is a elasticsearch-hello response: Fri Nov 13 ..."
}
```

`_cat/plugins` 里能看到你的插件名，集群健康为 green：

```bash
curl "http://localhost:9200/_cluster/health?pretty"
```

### 5.5 想卸载

```bash
bin/elasticsearch-plugin remove hello        # Linux/macOS
bin\elasticsearch-plugin.bat remove hello    # Windows
```

> 插件必须装到**集群每个节点**上，装完要重启节点。

---

## 第 6 步（可选）：跑测试

| 想做什么 | 命令 | 环境要求 |
|:---|:---|:---|
| Maven 单元测试 / 集成测试 | `mvn test` | JDK 8+；`rest` 类型会启动单节点 ES 7.10.2 并断言接口 |
| Gradle 编译测试代码 | `./gradlew javaRestTestClasses` | JDK 11+ |
| Gradle 真实集群集成测试 | `./gradlew javaRestTest` | **JDK 14+（推荐 JDK 15）**，首次会下载 ES 7.10.2 发行版 |
| Gradle 前台启动带插件的节点 | `./gradlew run` | JDK 11+ |

用 ES 发行版自带的 JDK 15 跑 Gradle（把路径换成你自己的）：

```bash
# Linux / macOS
JAVA_HOME=/path/to/elasticsearch-7.10.2/jdk ./gradlew javaRestTest

# Windows Git Bash
JAVA_HOME="C:/path/to/elasticsearch-7.10.2/jdk" sh ./gradlew javaRestTest
```

> ⚠️ `JAVA_HOME` 不要指向 `build/testclusters/.../distro` 里面的 JDK —— 那个目录会被清理，文件锁会让构建失败。用独立解压出来的发行版 JDK。

**✅ 成功标志**：`javaRestTest` 输出 `BUILD SUCCESSFUL`，测试报告里 2 个用例全绿。

---

## 参数速查

| 参数 | 必填 | 默认 | 说明 |
|:---|:---:|:---|:---|
| `groupId` / `artifactId` / `version` / `package` | 是 | — | Maven 坐标 / Java 包名 |
| `pluginName` | 否 | `Hello` | 类名前缀，大写开头 |
| `pluginType` | 否 | `rest` | 插件种类（附录 A） |
| `buildTool` | 否 | `both` | `maven` / `gradle` / `both` |
| `elasticsearchVersion` | 否 | `7.10.2` | 目标 ES 版本 |
| `restName` | 否 | `hello` | REST 路径名 |

---

## 出问题了？对照这张表

| 报错 / 现象 | 原因 | 怎么解决 |
|:---|:---|:---|
| `NoClassDefFoundError: org/apache/logging/log4j/core/Layout` | `javaRestTest` 缺 log4j-core | 新模板已修好；旧项目手动在 `build.gradle` 加 `javaRestTestImplementation 'org.apache.logging.log4j:log4j-core:2.11.1'` |
| `Reaper process died unexpectedly` / `UnsupportedClassVersionError ... 58.0` | JDK 11 跑不了 Java 14 字节码的 reaper | `javaRestTest` 换 JDK 14+，推荐 ES 自带 JDK 15 |
| `Must specify license and notice file` | 删了 `LICENSE.txt` / `NOTICE.txt` | 别删，模板已生成 |
| `Cannot shrink cluster ... to 1 nodes` | 在 `testClusters.all` 里写了 `numberOfNodes = 1` | 删掉这一行（集群默认就是 1 个节点） |
| 下载依赖失败（`shadow 6.0.0` / `nebula-core 3.0.0` / `gradle-docker-compose-plugin`） | JCenter 已关闭 | 模板已配 Maven Central + Gradle Plugin Portal + 公共镜像，换成你网络能访问的镜像即可 |
| `ClassNotFoundException: JavaVersionChecker` | Windows 用了 Git Bash 的 POSIX 脚本装插件 | 改用 `bin\elasticsearch-plugin.bat` |
| `ERROR: setting [discovery] must not be empty` | `.bat` 解析 `-E` 参数出错 | 把配置写进 `config/elasticsearch.yml` |
| 安装插件报版本不一致 | descriptor 的 `elasticsearch.version` 与节点不同 | 保证两边都是 `7.10.2` |
| 自定义接口编译报"找不到类" | 该接口所在依赖没加 | 在 `pom.xml` / `build.gradle` 补依赖 |
| `JAVA_HOME` 指向 testclusters 内部 JDK 后目录删不掉 | 文件被占用 | 用独立目录的 JDK |
| Windows 上 Maven 测试结束打印"文件被占用" | cluster-runner 清理临时目录 | 不影响测试结果，可忽略 |

---

## 附录 A：`pluginType` 完整列表

这些都是 ES 7.10.2 `org.elasticsearch.plugins` 包里真实存在的接口。

| 填什么 | 生成类实现什么接口 | 典型用途 |
|:---|:---|:---|
| `rest`（默认） | `ActionPlugin` | 自带完整 REST 示例 + 两种集成测试 |
| `action` | `ActionPlugin` | 自定义 Action / REST handler |
| `analysis` | `AnalysisPlugin` | 分词器、token filter、char filter |
| `ingest` | `IngestPlugin` | ingest processor |
| `mapper` | `MapperPlugin` | 自定义字段类型、metadata mapper |
| `script` | `ScriptPlugin` | 脚本引擎 / 自定义脚本上下文 |
| `search` | `SearchPlugin` | 自定义查询、聚合、suggester |
| `repository` | `RepositoryPlugin` | 快照仓库 |
| `discovery` | `DiscoveryPlugin` | 发现 / seed provider |
| `engine` | `EnginePlugin` | 自定义 Engine |
| `indexstore` | `IndexStorePlugin` | 自定义 Directory |
| `systemindex` | `SystemIndexPlugin` | 系统索引 |
| `circuitbreaker` | `CircuitBreakerPlugin` | 熔断器 |
| `cluster` | `ClusterPlugin` | 分配策略 / decider |
| `network` | `NetworkPlugin` | HTTP transport / 拦截器 |
| `persistent` | `PersistentTaskPlugin` | 持久任务 |
| `reloadable` | `ReloadablePlugin` | 可重载设置 |
| `extensible` | `ExtensiblePlugin` | SPI 扩展 |
| `basic` / `generic` / `none` | 仅 `Plugin` | 最简骨架 |

补充规则：

- 写 `-DpluginType=AnalysisPlugin`（带 `Plugin` 后缀）会自动补成 `org.elasticsearch.plugins.AnalysisPlugin`。
- 写全限定名（含 `.`）就按你写的来，自定义 SPI 也可以。
- 未知别名不会让生成失败，会退回纯 `Plugin`，并在生成类的 TODO 注释里提醒。
- 非 `rest` 类型不生成 REST 示例；Maven 测试变成不启集群的冒烟测试。

---

## 附录 B：Gradle 构建到底做了什么（进阶，可跳过）

生成的 `build.gradle` 引入官方构建工具 `org.elasticsearch.gradle:build-tools:7.10.2`，里面就带了这两个插件：

```groovy
buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven { url 'https://maven.aliyun.com/repository/public' } // JCenter 历史依赖镜像
    }
    dependencies { classpath 'org.elasticsearch.gradle:build-tools:7.10.2' }
}

apply plugin: 'elasticsearch.esplugin'        // 自动生成 descriptor + bundlePlugin zip + run 任务
apply plugin: 'elasticsearch.testclusters'    // 起真实 ES 集群
apply plugin: 'elasticsearch.java-rest-test'  // 自动把 bundlePlugin 的 zip 装进集群并跑测试

esplugin {
    name 'elasticsearch-hello'
    description 'Hello Plugin for Elasticsearch 7.10.2'
    classname 'com.example.HelloPlugin'
    licenseFile file('LICENSE.txt')
    noticeFile file('NOTICE.txt')
}

testClusters.all { testDistribution = 'DEFAULT' }

dependencies {
    javaRestTestImplementation 'org.apache.logging.log4j:log4j-core:2.11.1'
}
```

几个设计点：

- `build-tools:7.10.2` 在 Maven Central 上，**独立 Gradle 工程就能用**，不必把项目放进 Elasticsearch 源码树。
- Maven 的 descriptor 模板特意改名为 `plugin-descriptor.properties.tmpl`，打包时由 assembly 重命名；Gradle 的 `bundlePlugin` 排除 `.tmpl`，所以 `both` 模式下 zip 里也不会出现两个 descriptor。
- `META-INF/archetype-post-generate.groovy` 负责按 `pluginType` / `buildTool` 清理多余文件。
- `META-INF/maven/archetype.xml` 是上游遗留的 1.x 描述符，只为兼容保留；真正生效的是 `archetype-metadata.xml`。

---

## 实测记录

在 Windows 上完整跑过一轮（clone → 安装模板 → 生成 → 两种构建 → 两个 zip 装进真实节点 → curl 验证）：

| 环节 | 结果 |
|:---|:---|
| 环境 | JDK 1.8.0_101 / corretto-11.0.22 / ES 自带 JDK 15、Maven 3.3.9、Gradle 6.6.1、ES 7.10.2 |
| `mvn clean install` | BUILD SUCCESS |
| 生成 `maven` / `gradle` / `both` × `rest` / `analysis` / `basic` | 全部生成成功，文件清理正确 |
| `mvn test` / `mvn package` | 1 test / 0 failure；zip 内 descriptor 属性正确 |
| `./gradlew bundlePlugin` | BUILD SUCCESSFUL，zip 含 esplugin 生成的 descriptor |
| `./gradlew javaRestTest`（JDK 15） | BUILD SUCCESSFUL，2 tests / 0 failures，真实集群自动装好插件 |
| 真实节点装 Maven zip 与 Gradle zip | `version.number=7.10.2`、`_cat/plugins` 可见、`/_hello` 与 `/my_index/_hello` 返回预期 JSON、集群 green |

---

## 许可

Apache License 2.0，见 [LICENSE](LICENSE)。原作者 codelibs / Shinsuke Sugaya 的版权声明保留；Gradle wrapper 脚本来自 Elasticsearch 仓库（Apache 2.0）。
