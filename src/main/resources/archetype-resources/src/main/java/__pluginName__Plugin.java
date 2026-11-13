#if(!$pluginType || $pluginType.trim() == "")##
#set($pluginType = "rest")##
#end
#set($pt = $pluginType.trim().toLowerCase())##
#set($pluginInterfaceSimple = "Plugin")##
#set($pluginInterface = "")##
#if($pt.indexOf(".") >= 0)##
#set($pluginInterface = $pluginType.trim())##
#elseif($pt == "rest" || $pt == "action")##
#set($pluginInterface = "org.elasticsearch.plugins.ActionPlugin")##
#elseif($pt == "analysis")##
#set($pluginInterface = "org.elasticsearch.plugins.AnalysisPlugin")##
#elseif($pt == "ingest")##
#set($pluginInterface = "org.elasticsearch.plugins.IngestPlugin")##
#elseif($pt == "mapper")##
#set($pluginInterface = "org.elasticsearch.plugins.MapperPlugin")##
#elseif($pt == "script")##
#set($pluginInterface = "org.elasticsearch.plugins.ScriptPlugin")##
#elseif($pt == "search")##
#set($pluginInterface = "org.elasticsearch.plugins.SearchPlugin")##
#elseif($pt == "repository")##
#set($pluginInterface = "org.elasticsearch.plugins.RepositoryPlugin")##
#elseif($pt == "discovery")##
#set($pluginInterface = "org.elasticsearch.plugins.DiscoveryPlugin")##
#elseif($pt == "engine")##
#set($pluginInterface = "org.elasticsearch.plugins.EnginePlugin")##
#elseif($pt == "indexstore")##
#set($pluginInterface = "org.elasticsearch.plugins.IndexStorePlugin")##
#elseif($pt == "systemindex")##
#set($pluginInterface = "org.elasticsearch.plugins.SystemIndexPlugin")##
#elseif($pt == "circuitbreaker")##
#set($pluginInterface = "org.elasticsearch.plugins.CircuitBreakerPlugin")##
#elseif($pt == "cluster")##
#set($pluginInterface = "org.elasticsearch.plugins.ClusterPlugin")##
#elseif($pt == "network")##
#set($pluginInterface = "org.elasticsearch.plugins.NetworkPlugin")##
#elseif($pt == "persistent")##
#set($pluginInterface = "org.elasticsearch.plugins.PersistentTaskPlugin")##
#elseif($pt == "reloadable")##
#set($pluginInterface = "org.elasticsearch.plugins.ReloadablePlugin")##
#elseif($pt == "extensible")##
#set($pluginInterface = "org.elasticsearch.plugins.ExtensiblePlugin")##
#elseif($pt == "basic" || $pt == "generic" || $pt == "none" || $pt == "plugin")##
#set($pluginInterface = "")##
#elseif($pt.endsWith("plugin"))##
#set($pluginInterface = "org.elasticsearch.plugins.${pluginType}")##
#else##
#set($pluginInterface = "")##
#set($unknownType = true)##
#end
#if($pluginInterface != "")##
#set($idx = $pluginInterface.lastIndexOf(".") + 1)##
#set($pluginInterfaceSimple = $pluginInterface.substring($idx))##
#end
#set($interfaceRef = "")##
#set($skipInterfaceImport = "false")##
#if($pluginInterface != "")##
#set($interfaceRef = $pluginInterfaceSimple)##
#if($pluginInterfaceSimple == "${pluginName}Plugin")##
## Avoid "class IngestPlugin implements IngestPlugin" (simple name collision):
## implement the interface by its fully qualified name instead of importing it.
#set($interfaceRef = $pluginInterface)##
#set($skipInterfaceImport = "true")##
#end##
#end##
#if($pt == "rest")##
#set($isRest = "true")##
#else##
#set($isRest = "false")##
#end
package ${package};

#if($isRest == "true")
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

#end
#if($pluginInterface != "" && $skipInterfaceImport != "true")
import ${pluginInterface};
#end
#if($isRest == "true")
import ${package}.rest.Rest${pluginName}Action;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.IndexScopedSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;
#end
import org.elasticsearch.plugins.Plugin;

/**
 * ${pluginName} plugin for Elasticsearch ${elasticsearchVersion}.
 *
 * <p>Generated with pluginType=${pluginType}; implemented interface:
 * ${pluginInterfaceSimple}. Every extension point of an Elasticsearch 7.10.2
 * plugin interface has a default implementation, so this class compiles as-is.
#if($isRest == "true")##
 * The REST example below is ready to run; replace it with your own handlers.
#else##
 * Override the extension points you need (see the TODO block in the class body).
#end##
 */
public class ${pluginName}Plugin extends Plugin#if($interfaceRef != "") implements ${interfaceRef}#end {
#if($isRest == "true")
    @Override
    public List<RestHandler> getRestHandlers(final Settings settings,
            final RestController restController,
            final ClusterSettings clusterSettings,
            final IndexScopedSettings indexScopedSettings,
            final SettingsFilter settingsFilter,
            final IndexNameExpressionResolver indexNameExpressionResolver,
            final Supplier<DiscoveryNodes> nodesInCluster) {
        // ES 7.x registers the routes declared by Rest${pluginName}Action.routes()
        // from this list; do not call RestController.registerHandler() yourself.
        return Collections.singletonList(new Rest${pluginName}Action());
    }
#else
    // TODO: implement the extension points of ${pluginInterfaceSimple}.
#if($unknownType)
    // WARNING: unknown pluginType alias "${pluginType}". A plain Plugin was generated.
    // Pass a fully qualified interface name (e.g. org.elasticsearch.plugins.AnalysisPlugin)
    // or one of the aliases listed in README.md.
#end
#if($pt == "action")
    //   getActions(), getClientActions(), getActionFilters(), getRestHandlers(...)
#elseif($pt == "analysis")
    //   getCharFilters(), getTokenFilters(), getTokenizers(), getAnalyzers(),
    //   getHunspellDictionaries(), getPreConfigured*()
#elseif($pt == "ingest")
    //   getProcessors(Processor.Parameters parameters)
#elseif($pt == "mapper")
    //   getMappers(), getMetadataMappers(), getFieldFilter(), getFieldNameAnalyzerProvider()
#elseif($pt == "script")
    //   getScriptEngine(Settings, Collection<ScriptContext<?>>), getContexts()
#elseif($pt == "search")
    //   getQueries(), getAggregations(), getPipelineAggregations(), getSuggesters(),
    //   getHighlighters(), getRescorers(), getFetchSubPhases(...)
#elseif($pt == "repository")
    //   getRepositories(Environment, NamedXContentRegistry, ClusterService, RecoverySettings),
    //   getInternalRepositories(...)
#elseif($pt == "discovery")
    //   getDiscoveryTypes(...), getZenHostsProviders(...), getSeedProviders(...)
#elseif($pt == "engine")
    //   getEngineFactory(...), getCustomCodecServiceFactory(...)
#elseif($pt == "indexstore")
    //   getDirectoryFactories(...), getRecoveryStateFactories(...), getSnapshotCommitSuppliers(...)
#elseif($pt == "systemindex")
    //   getSystemIndexDescriptors(Settings), getAssociatedIndexPatterns()
#elseif($pt == "circuitbreaker")
    //   getCircuitBreaker(Settings), setCircuitBreaker(CircuitBreakerService)
#elseif($pt == "cluster")
    //   getAllocationDeciders(...), getShardsAllocator(...), getClusterDecoratorService(...)
#elseif($pt == "network")
    //   getHttpTransports(...), getTransportInterceptors(...), getTaskHeaders()
#elseif($pt == "persistent")
    //   getPersistentTasksExecutor(...)
#elseif($pt == "reloadable")
    //   reload(Settings)
#elseif($pt == "extensible")
    //   loadExtensions(ExtensiblePlugin.ExtensionLoader)
#elseif($pt == "basic" || $pt == "generic" || $pt == "none")
    //   A plain Plugin: override getSettings(), getGuiceServiceClasses(), etc.
#else
    //   See the Javadoc of ${pluginInterfaceSimple} for its extension points.
#end
#end
}
