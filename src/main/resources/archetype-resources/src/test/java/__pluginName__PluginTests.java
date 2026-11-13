#if(!$pluginType || $pluginType.trim() == "")##
#set($pluginType = "rest")##
#end
#set($pt = $pluginType.trim().toLowerCase())##
#if($pt == "rest")##
package ${package};

import static org.codelibs.elasticsearch.runner.ElasticsearchClusterRunner.newConfigs;

import java.util.Map;

import org.codelibs.curl.CurlResponse;
import org.codelibs.elasticsearch.runner.ElasticsearchClusterRunner;
import org.codelibs.elasticsearch.runner.net.EcrCurl;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.node.Node;

import junit.framework.TestCase;

/** Integration test that boots a single ES 7.10.2 node with this plugin. */
public class ${pluginName}PluginTests extends TestCase {

    private ElasticsearchClusterRunner runner;

    @Override
    protected void setUp() throws Exception {
        runner = new ElasticsearchClusterRunner();
        runner.onBuild(new ElasticsearchClusterRunner.Builder() {
            @Override
            public void build(final int number, final Builder settingsBuilder) {
                settingsBuilder.put("http.cors.enabled", true);
                settingsBuilder.put("http.cors.allow-origin", "*");
                // 7.x no longer uses discovery.zen.ping.unicast.hosts.
                // single-node discovery bootstraps the cluster without
                // cluster.initial_master_nodes, which keeps this test fast.
                settingsBuilder.put("discovery.type", "single-node");
            }
        }).build(newConfigs()
                .clusterName("es-cl-run-" + System.currentTimeMillis())
                .pluginTypes("${package}.${pluginName}Plugin")
                .numOfNode(1));

        runner.ensureYellow();
    }

    @Override
    protected void tearDown() throws Exception {
        if (runner != null) {
            runner.close();
            runner.clean();
        }
    }

    public void test_plugin() throws Exception {
        final Node node = runner.node();
        final String index = "test_index";

        try (CurlResponse curlResponse = EcrCurl
                .get(node, "/" + index + "/_${restName}").execute()) {
            final String content = curlResponse.getContentAsString();
            assertNotNull(content);
            final Map<String, Object> contentMap = curlResponse
                    .getContent(EcrCurl.jsonParser());
            assertEquals(index, contentMap.get("index"));
            assertTrue(contentMap.get("description").toString()
                    .startsWith("This is a ${artifactId} response:"));
        }

        try (CurlResponse curlResponse = EcrCurl.get(node, "/_${restName}")
                .execute()) {
            final String content = curlResponse.getContentAsString();
            assertNotNull(content);
            final Map<String, Object> contentMap = curlResponse
                    .getContent(EcrCurl.jsonParser());
            assertFalse(contentMap.containsKey("index"));
            assertTrue(contentMap.get("description").toString()
                    .startsWith("This is a ${artifactId} response:"));
        }
    }
}
#else
package ${package};

import org.elasticsearch.plugins.Plugin;

import junit.framework.TestCase;

/** Smoke test for a pluginType=${pluginType} plugin; no cluster is started. */
public class ${pluginName}PluginTests extends TestCase {

    public void test_pluginIsLoadable() {
        final ${pluginName}Plugin plugin = new ${pluginName}Plugin();
        assertNotNull(plugin);
        assertTrue(plugin instanceof Plugin);
    }
}
#end
