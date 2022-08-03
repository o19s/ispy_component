package com.o19s.components;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.response.JSONWriter;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.JsonTextWriter;
import org.apache.solr.core.PluginInfo;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.handler.component.ShardRequest;
import org.apache.solr.search.SolrCache;
import org.apache.solr.util.plugin.PluginInfoInitialized;
import org.apache.solr.util.plugin.SolrCoreAware;

import java.io.IOException;
import java.io.StringWriter;

public class ISpyComponent extends SearchComponent implements PluginInfoInitialized, SolrCoreAware {
    private PluginInfo info = PluginInfo.EMPTY_INFO;

    @Override
    public void init(PluginInfo pluginInfo) {
        this.info = pluginInfo;
    }

    @Override
    public void prepare(ResponseBuilder responseBuilder) throws IOException {
        // No-op 
    }

    @Override
    public void process(ResponseBuilder responseBuilder) throws IOException {
        SolrParams params = responseBuilder.req.getParams();

        if(debugEnabled(params)){
            SolrCache cache = responseBuilder.req.getSearcher().getCache("ispy");
            if (cache != null) {
                String key = responseBuilder.req.getOriginalParams().get("q");
                if (key == null) return;

                final String namedListStyle = params.get(JsonTextWriter.JSON_NL_STYLE, JsonTextWriter.JSON_NL_FLAT).intern();
                final String wrapperFunction = params.get("json.wrf", "");
                StringWriter writer = new StringWriter();
                JSONWriter jsonWriter = new JSONWriter(writer, responseBuilder.req, responseBuilder.rsp, wrapperFunction, namedListStyle) {
                    public void writeResponse() throws IOException {
                        if (wrapperFunction != null) {
                            _writeStr(wrapperFunction + "(");
                        }
                        writeNamedList(null, rsp.getValues());
                        if (wrapperFunction != null) {
                            _writeChar(')');
                        }
                        _writeChar('\n'); // ending with a newline looks much better from the command line
                        writer.flush(); // TODO: Needed to extend to add this line, how does it work without this?
                    }
                };

                try {
                    jsonWriter.writeResponse();
                } catch (IOException ex) {
                    // No-op
                } finally {
                    jsonWriter.close();
                    cache.put(key, writer.toString());
                }
            }
        }
    }

    @Override
    public void finishStage(ResponseBuilder responseBuilder) {
      // TODO: Need logic here to support cloud
    }

    @Override
    public String getDescription() {
      return "Caches explain responses for relevance debugging.";
    }

    @Override
    public void inform(SolrCore core) {
      // No-op
    }

    @Override
    public void modifyRequest(ResponseBuilder responseBuilder, SearchComponent who, ShardRequest shardRequest) {
      // No-op
    }

    @Override
    public void handleResponses(ResponseBuilder responseBuilder, ShardRequest shardRequest){
      // No-op
    }

    private boolean debugEnabled(SolrParams params) {
        // TODO: Other cases to check for?
        return params.getBool(CommonParams.DEBUG, false);
    }
}
