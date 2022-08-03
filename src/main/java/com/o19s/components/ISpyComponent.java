package com.o19s.payloads.component;

import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.PluginInfo;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.handler.component.ShardRequest;
import org.apache.solr.handler.component.ShardResponse;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.util.SolrPluginUtils;
import org.apache.solr.util.plugin.PluginInfoInitialized;
import org.apache.solr.util.plugin.SolrCoreAware;

import java.io.IOException;
import java.util.List;

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
      // No-op
    }

    @Override
    public void finishStage(ResponseBuilder responseBuilder) {
        SolrParams params = responseBuilder.req.getParams();

        // TODO: Check stage and add response to cache if debug was turned on

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
