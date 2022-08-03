package com.o19s.components;

import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.util.TestHarness;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;

public class ISpyComponentTest extends SolrTestCaseJ4 {
    @BeforeClass
    public static void beforeClass() throws Exception {
        initCore("solrconfig.xml", "schema.xml");
    }

    @After
    @Override
    public void tearDown() throws Exception {
        clearIndex();
        super.tearDown();
    }

    @Test
    public void testFunctionality() {
        assertU(adoc("content", "One doc", "id", "1"));
        assertU(commit());
        assertU(optimize());

        HashMap<String, String> args = new HashMap<>();
        args.put("debug", "true");
        args.put("debug.explain.structured", "true");

        TestHarness.LocalRequestFactory init = h.getRequestFactory("standard", 0, 200, args);

        assertQ("Verify local test",
                init.makeRequest("doc"),
                        "//*[@numFound='1']");

        assertQ("Explain was cached",
                req("qt", "/admin/ispy", "action", "spy", "q", "doc"),
                "//*[@numFound='1']");


    }
}
