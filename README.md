# Elasticsearch Plugin Archetype (7.10.2)

Fork 自 [codelibs/elasticsearch-plugin-archetype](https://github.com/codelibs/elasticsearch-plugin-archetype)，已改造为面向 **Elasticsearch 7.10.2** 的 Maven archetype，并新增 `pluginType` 参数：只用一个参数即可生成**任意类型**的 ES 插件工程（REST、analysis、ingest、mapper、script、search……甚至任意自定义插件接口）。

| 项目 | 值 |
|:---|:---|
| archetypeGroupId | `org.codelibs` |
| archetypeArtifactId | `elasticsearch-plugin-archetype` |
| archetypeVersion | `7.10.2` |
| 默认 elasticsearchVersion | `7.10.2` |
| 默认 pluginType | `rest` |
| 许可证 | Apache License 2.0（沿用上游） |

## 环境要求

- JDK：编译与测试用 JDK 8+ 即可（实测 JDK 1.8.0_101 可编译并运行单节点 ES 7.10.2 集成测试，ES 7.10.2 与 cluster-runner 7.10.2.0 均为 Java 8 字节码）；生产集群仍建议使用 JDK 11+ 或 ES 自带 JDK 15。生成工程 pom 默认 `maven.compiler.source/target=1.8`，若集群只允许 JDK 11+ 可改为 `11`。
- Maven 3.3+（archetype 本身固定使用 `maven-archetype-plugin 3.2.1` / `archetype-packaging 3.2.1`）。

## 快速开始

先把 archetype 安装到本地仓库：

```bash
git clone https://github.com/zg900213/elasticsearch-plugin-archetype.git
cd elasticsearch-plugin-archetype
mvn clean install
```

生成一个默认（REST）插件工程：

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
  -DrestName=hello \
  -DelasticsearchVersion=7.10.2 \
  -DinteractiveMode=false
```

构建与安装：

```bash
cd elasticsearch-hello
mvn clean package
# 产物：target/releases/elasticsearch-hello-1.0.0-SNAPSHOT.zip
bin/elasticsearch-plugin install file:///绝对路径/target/releases/elasticsearch-hello-1.0.0-SNAPSHOT.zip
```

REST 类型会生成可直接调用的接口：

```bash
curl 'http://localhost:9200/_hello?pretty'
curl 'http://localhost:9200/my_index/_hello?pretty'
```

## 参数

| 参数 | 必填 | 默认值 | 说明 |
|:---|:---:|:---|:---|
| `pluginType` | 否 | `rest` | **新增**：插件类型（见下表）。可填内置别名、带 `Plugin` 后缀的接口名，或任意全限定接口名 |
| `pluginName` | 否 | `Hello` | 类名前缀，生成 `${pluginName}Plugin`、`Rest${pluginName}Action` |
| `elasticsearchVersion` | 否 | `7.10.2` | 生成工程依赖的 ES 版本；测试依赖自动拼为 `<版本>.0`（codelibs cluster-runner 的版本规则） |
| `restName` | 否 | `hello` | 仅 `pluginType=rest` 有效，REST 路径中的名字 |
| `groupId` / `artifactId` / `version` / `package` | 是 | — | Maven 坐标与 Java 包名 |

## pluginType：一个参数生成任意类型插件

内置别名（均映射到 ES 7.10.2 `org.elasticsearch.plugins` 包中的真实接口）：

| pluginType | 生成的接口 | 典型扩展点 |
|:---|:---|:---|
| `rest`（默认） | `ActionPlugin` | 附带完整 REST 示例（`routes()` + `getRestHandlers()`）+ 集成测试 |
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

另外三种写法让"任意类型"真正无上限：

1. **接口名简写**：`-DpluginType=AnalysisPlugin` → 自动补全为 `org.elasticsearch.plugins.AnalysisPlugin`。
2. **全限定接口名**：`-DpluginType=org.elasticsearch.plugins.RepositoryPlugin`。
3. **自定义 SPI 接口**：`-DpluginType=com.example.spi.MyPluginInterface`（生成 `implements MyPluginInterface`，需自行提供该依赖）。

未知别名不会让生成失败：会退回为纯 `Plugin` 并在生成类的 TODO 注释里给出警告。

示例：

```bash
# 分析插件（analysis）
mvn org.apache.maven.plugins:maven-archetype-plugin:3.2.1:generate \
  -DarchetypeGroupId=org.codelibs -DarchetypeArtifactId=elasticsearch-plugin-archetype \
  -DarchetypeVersion=7.10.2 -DgroupId=com.example -DartifactId=es-my-analyzer \
  -Dversion=1.0.0-SNAPSHOT -Dpackage=com.example -DpluginName=MyAnalyzer \
  -DpluginType=analysis -DinteractiveMode=false

# 自定义插件接口（任意类型）
# -DpluginType=com.example.spi.MyPluginInterface

# ingest 插件
# -DpluginType=ingest
```

非 `rest` 类型不会生成 REST handler（`META-INF/archetype-post-generate.groovy` 会清理占位文件），测试改为不启动集群的冒烟测试，`pom.xml` 也不会引入 cluster-runner。生成类的 TODO 注释会列出该类型常用扩展点。

## 相比上游 6.6 版的改动

- `archetype-metadata.xml`：`elasticsearchVersion` 默认改为 `7.10.2`，新增 `pluginType` 参数并给所有参数补默认值（便于 `-DinteractiveMode=false`）。
- archetype 自身 pom：显式 `maven-archetype` packaging，固定 archetype 插件 3.2.1，scm/url 指向 fork。
- 生成的插件类按 `pluginType` 实现对应 7.10.2 插件接口（Velocity 映射），rest 类型给出完整可用示例。
- REST 示例改用 7.x API：`BaseRestHandler` 无参构造 + `getName()` / `routes()` / `Route`，插件通过 `getRestHandlers(...)` 返回 handler（6.x 的 `RestController.registerHandler()` 已不适用）。
- 生成工程 pom 修正 cluster-runner 的 groupId 为 `org.codelibs`（上游误写为用户 groupId），版本改为 `7.10.2.0`，junit 升到 4.13.2，插件版本更新。
- `plugin-descriptor.properties` 使用 7.10.2 所需字段（`java.version` 取 `maven.compiler.target`），assembly 增加 `plugin-security.policy` 用法说明。
- 集成测试：由 3 节点 + `discovery.zen.ping.unicast.hosts`（7.x 已移除）改为单节点 + `discovery.type=single-node`；JSON 解析改用 cluster-runner 7.10.2.0 的 `EcrCurl.jsonParser()` 方法（6.x 是字段 `EcrCurl.jsonParser`）。
- 新增 `archetype-post-generate.groovy`（按类型清理 REST 占位文件）与生成工程自带的 `README.md`；在 `archetype-metadata.xml` 中为根目录 `README.md` 补充 fileSet。
- `pluginType` 与类名同名时（例如 `pluginName=Ingest` + `pluginType=IngestPlugin`）自动改用全限定接口名，避免 `class IngestPlugin implements IngestPlugin` 的导入冲突。
- 移除仅适用于上游 6.x sample 工程的维护脚本 `update_src.sh`；模板现在直接维护在 `src/main/resources/archetype-resources/`。

## 验证记录（本机实测）

环境：Windows / JDK 1.8.0_101 / Maven 3.3.9。

- `mvn clean install`：archetype 构建、安装成功（archetype-plugin 3.2.1）。
- 生成 6 种工程并核对映射：`rest` → `ActionPlugin`、`analysis` → `AnalysisPlugin`、`IngestPlugin`（简写自动补全）、`org.elasticsearch.plugins.SearchPlugin`（FQCN）、`basic` → 纯 `Plugin`、未知别名 → 纯 `Plugin` + 警告。
- 6 个工程 `mvn test-compile` 全部通过（依赖 `org.elasticsearch:elasticsearch:7.10.2`）。
- `rest`：`mvn test` 通过（单节点 ES 7.10.2 启动、插件加载、`/_hello` 与 `/{index}/_hello` 断言通过，1 test / 0 failure）；`basic`：`mvn test` 通过。
- `mvn -DskipTests package` 产出 `target/releases/es-rest-1.0.0-SNAPSHOT.zip`，内含 `plugin-descriptor.properties`（`version=1.0.0-SNAPSHOT`、`classname=com.example.HelloPlugin`、`elasticsearch.version=7.10.2`、`java.version=1.8`）与插件 jar。
- 非 `rest` 类型不生成 REST handler、不引入 cluster-runner，测试为轻量冒烟测试。

## 已知限制

- `elasticsearchVersion` 传非 `7.10.2` 时，测试依赖 `<版本>.0` 需要在 Maven Central 上存在对应的 codelibs cluster-runner 版本（仅 `rest` 类型需要）；也可用 `-DpluginType=basic` 规避。
- ES 7.10 插件接口的方法全部是 `default`，因此生成的骨架可以直接编译；具体扩展点需要自行实现并补依赖。传入自定义全限定接口（如 `com.example.spi.MyPluginInterface`）时，需要在生成工程 pom 中自行加入该接口所在依赖，否则编译会报找不到类。
- 生成的插件 zip 只包含工程自身与运行时依赖，ES / log4j 由节点提供（`provided`）。
- `META-INF/maven/archetype.xml` 是上游遗留的 1.x 描述符，仅为兼容保留；权威配置是 `archetype-metadata.xml`，请按上文命令固定使用 archetype-plugin 3.2.1。
- Windows 上运行 REST 集成测试后，cluster-runner 清理临时目录时可能打印"文件被占用"的警告，不影响测试结果。

## 许可

Apache License 2.0，见 [LICENSE](LICENSE)。原作者 codelibs / Shinsuke Sugaya 的版权声明保留。
