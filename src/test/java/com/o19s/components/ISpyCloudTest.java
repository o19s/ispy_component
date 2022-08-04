package com.o19s.components;

import org.apache.lucene.util.TestUtil;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.cloud.SolrCloudTestCase;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CoreAdminParams;
import org.junit.After;
import org.junit.BeforeClass;

import java.nio.file.Paths;
import java.util.Collections;
import org.apache.solr.client.solrj.request.UpdateRequest;

public class ISpyCloudTest extends SolrCloudTestCase {
    private static final String COLLECTION = "ispy_test";
    private static final String CONF = "ispy";
    private static final String LOG_COLLECTION = "ispy_log";


    @BeforeClass
    public static void setupCluster() throws Exception {
        final int numShards = TestUtil.nextInt(random(), 1, 5);
        final int numReplicas = 1;
        final int nodeCount = numShards * numReplicas;

        configureCluster(nodeCount)
                .addConfig(CONF, Paths.get(TEST_HOME(), "collection1"))
                .configure();

        assertEquals(
                0,
                (CollectionAdminRequest.createCollection(COLLECTION, CONF, numShards, numReplicas))
                .setProperties(
                        Collections.singletonMap(CoreAdminParams.CONFIG, "solrconfig.xml"))
                        .process(cluster.getSolrClient()
                ).getStatus());

        cluster.getSolrClient().setDefaultCollection(COLLECTION);

        SolrInputDocument doc = sdoc("id", "1", "content", "One doc");
        assertEquals(0, (new UpdateRequest().add(doc)).process(cluster.getSolrClient()).getStatus());
        assertEquals(0, cluster.getSolrClient().commit().getStatus());
    }

    @After
    @Override
    public void tearDown() throws Exception {
        CollectionAdminRequest.deleteCollection(COLLECTION).process(cluster.getSolrClient());
        super.tearDown();

    }

    public void testFunctionality() throws Exception {
        final SolrQuery query = new SolrQuery(
                "q", "one",
                "debug", "true",
                "debug.explain.structured", "true"
        );

        final QueryResponse rsp = cluster.getSolrClient().query(query);
        assertEquals(1, rsp.getResults().getNumFound());

        // TODO: Hit configured collection and check for doc?
    }
}
