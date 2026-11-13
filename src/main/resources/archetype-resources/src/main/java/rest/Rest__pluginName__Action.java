#if(!$pluginType || $pluginType.trim() == "")##
#set($pluginType = "rest")##
#end
#set($pt = $pluginType.trim().toLowerCase())##
#if($pt == "rest")##
package ${package}.rest;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestStatus.OK;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestHandler.Route;
import org.elasticsearch.rest.RestRequest;

/**
 * Example REST action generated for pluginType=rest (Elasticsearch 7.10.2).
 *
 * <p>Since 7.x a handler declares its routes via {@code routes()} and is
 * returned from {@code ActionPlugin.getRestHandlers(...)}. The old 6.x style
 * ({@code RestController.registerHandler(...)} in the constructor) no longer
 * applies.</p>
 */
public class Rest${pluginName}Action extends BaseRestHandler {

    @Override
    public String getName() {
        return "${restName}_get";
    }

    @Override
    public List<Route> routes() {
        return Arrays.asList(
                new Route(GET, "/{index}/_${restName}"),
                new Route(GET, "/_${restName}"));
    }

    @Override
    protected RestChannelConsumer prepareRequest(final RestRequest request,
            final NodeClient client) throws IOException {
        final boolean isPretty = request.hasParam("pretty");
        final String index = request.param("index");
        return channel -> {
            final XContentBuilder builder = JsonXContent.contentBuilder();
            if (isPretty) {
                builder.prettyPrint().lfAtEnd();
            }
            builder.startObject();
            if (index != null) {
                builder.field("index", index);
            }
            builder.field("description",
                    "This is a ${artifactId} response: " + new Date().toString());
            builder.endObject();
            channel.sendResponse(new BytesRestResponse(OK, builder));
        };
    }
}
#else
// pluginType=${pluginType}: no REST handler is generated for this plugin type.
// This placeholder is removed by META-INF/archetype-post-generate.groovy.
#end
