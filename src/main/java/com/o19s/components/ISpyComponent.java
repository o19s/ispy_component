package com.o19s.components;

import com.google.common.annotations.VisibleForTesting;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.response.JSONResponseWriter;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.PluginInfo;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.handler.component.ShardRequest;
import org.apache.solr.util.plugin.SolrCoreAware;

import java.io.IOException;
import java.io.StringWriter;

public class ISpyComponent extends SearchComponent implements SolrCoreAware {
    private PluginInfo info = PluginInfo.EMPTY_INFO;

    // Constants used in solrconfig.xml
    @VisibleForTesting static final String LOG_COLLECTION = "ispyCollection";


    protected SolrParams initArgs;
    @Override
    public void init(NamedList args) {
        this.initArgs = args.toSolrParams();
    }

    @Override
    public void prepare(ResponseBuilder responseBuilder) throws IOException {
        // No-op 
    }

    @Override
    public void process(ResponseBuilder responseBuilder) throws IOException {
        // TODO: Single node support is pending
    }

    @Override
    public void finishStage(ResponseBuilder responseBuilder) {
        SolrParams params = responseBuilder.req.getParams();

        if((responseBuilder.stage == responseBuilder.STAGE_GET_FIELDS) && responseBuilder.isDebug()){
            String key = responseBuilder.req.getOriginalParams().get("q");

            // Exit conditions
            if (key == null) return;
            if (initArgs.get(LOG_COLLECTION) == null) return;

            StringWriter writer = new StringWriter();
            JSONResponseWriter jsonWriter = new JSONResponseWriter();

            // Opted for cleaner option above but loses json.wrf support and maybe the named list style
            // Leaving this here in case those options are needed
            /*
            final String namedListStyle = params.get(JsonTextWriter.JSON_NL_STYLE, JsonTextWriter.JSON_NL_FLAT).intern();
            final String wrapperFunction = params.get("json.wrf", null);
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
            */

            HttpSolrClient solr = null;
            try {
                jsonWriter.write(writer, responseBuilder.req, responseBuilder.rsp);
                String baseUrl = initArgs.get(LOG_COLLECTION);
                solr = new HttpSolrClient.Builder(baseUrl).build();
                SolrInputDocument doc = new SolrInputDocument(
                        "id", key,
                        "raw_s", writer.toString()
                );
                solr.add(doc);
            } catch (IOException ex) {
                // No-op
            } catch (SolrServerException ex) {
                // No-op
            } finally {
                try {
                    if (solr != null) {
                        solr.close();
                    }
                } catch (IOException ex) {
                    // No-op
                }
            }
        }
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
}
