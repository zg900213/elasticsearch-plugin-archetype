#if(!$pluginType || $pluginType.trim() == "")##
#set($pluginType = "rest")##
#end
#set($pt = $pluginType.trim().toLowerCase())##
package ${package};

import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.test.rest.ESRestTestCase;

/**
 * Integration test driven by elasticsearch.testclusters.
 *
 * <p>The javaRestTest task boots a real Elasticsearch ${elasticsearchVersion}
 * node, installs the bundlePlugin zip (wired automatically by
 * {@code elasticsearch.java-rest-test}) and runs this class against it.</p>
 */
public class ${pluginName}RestIT extends ESRestTestCase {

    public void testPluginIsLoaded() throws Exception {
        final Response response = client().performRequest(new Request("GET", "/_cat/plugins"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        // Add stronger assertions here, e.g. check the response body contains '${artifactId}'.
    }
#if($pt == "rest")
    public void testPluginRestEndpoint() throws Exception {
        final Response response = client().performRequest(new Request("GET", "/_${restName}"));
        assertEquals(200, response.getStatusLine().getStatusCode());
    }
#end
}
