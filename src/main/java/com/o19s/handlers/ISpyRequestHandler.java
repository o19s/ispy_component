/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.o19s.handlers;

import org.apache.solr.common.SolrException;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;

import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.SolrCache;
import org.apache.solr.security.AuthorizationContext;
import org.apache.solr.security.PermissionNameProvider;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.lang.invoke.MethodHandles;

import java.util.Locale;

import static org.apache.solr.common.params.CommonParams.ACTION;
import static org.apache.solr.common.params.CommonParams.DISABLE;

import static org.apache.solr.common.params.CommonParams.ENABLE;

/**
 * Ping Request Handler for reporting SolrCore health to a Load Balancer.
 *
 * <p>This handler is designed to be used as the endpoint for an HTTP Load-Balancer to use when
 * checking the "health" or "up status" of a Solr server.
 *
 * <p>In its simplest form, the PingRequestHandler should be configured with some defaults
 * indicating a request that should be executed. If the request succeeds, then the
 * PingRequestHandler will respond back with a simple "OK" status. If the request fails, then the
 * PingRequestHandler will respond back with the corresponding HTTP Error code. Clients (such as
 * load balancers) can be configured to poll the PingRequestHandler monitoring for these types of
 * responses (or for a simple connection failure) to know if there is a problem with the Solr
 * server.
 *
 * <p>Note in case isShard=true, PingRequestHandler respond back with what the delegated handler
 * returns (by default it's /select handler).
 *
 * <pre class="prettyprint">
 * &lt;requestHandler name="/admin/bob" class="solr.ISpyRequestHandler"&gt;
 *   &lt;lst name="invariants"&gt;
 *     &lt;str name="qt"&gt;/search&lt;/str&gt;&lt;!-- handler to delegate to --&gt;
 *     &lt;str name="q"&gt;some test query&lt;/str&gt;
 *   &lt;/lst&gt;
 * &lt;/requestHandler&gt;
 * </pre>
 *
 * <p>The health check file may be created/deleted by any external system, or the PingRequestHandler
 * itself can be used to create/delete the file by specifying an "action" param in a request:
 *
 * <ul>
 *   <li><code>http://.../ping?action=enable</code> - creates the health check file if it does not
 *       already exist
 *   <li><code>http://.../ping?action=disable</code> - deletes the health check file if it exists
 *   <li><code>http://.../ping?action=status</code> - returns a status code indicating if the
 *       healthcheck file exists ("<code>enabled</code>") or not ("<code>disabled</code>")
 * </ul>
 *
 */
public class ISpyRequestHandler extends RequestHandlerBase implements SolrCoreAware, PermissionNameProvider {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static boolean ENABLED = false;

    @Override
    public Name getPermissionName(AuthorizationContext request) {
        String action = request.getParams().get(ACTION, "").strip().toLowerCase(Locale.ROOT);
        // Modifying the health check file requires more permission than just doing a ping
        switch (action) {
            case ENABLE:
            case DISABLE:
                return Name.CONFIG_EDIT_PERM;
            default:
                return Name.CONFIG_EDIT_PERM;
        }
    }

    protected enum ACTIONS {
        STATUS,
        ENABLE,
        DISABLE,
        LIST,
        SPY,

    };


    @Override
    public void inform(SolrCore core) {

    }


    @Override
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {

        SolrParams params = req.getParams();

        String actionParam = params.get("action");
        ISpyRequestHandler.ACTIONS action = null;
        try {
            action = ISpyRequestHandler.ACTIONS.valueOf(actionParam.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException iae) {
            throw new SolrException(
                    SolrException.ErrorCode.BAD_REQUEST, "Unknown action: " + actionParam);
        }

        switch (action) {
            case ENABLE:
                handleEnable(true);
                break;
            case DISABLE:
                handleEnable(false);
                break;
            case LIST:
                handleList(req);
                break;
            case SPY:
                handleSpy(req, rsp);
                break;
            case STATUS:
                rsp.add("status", ENABLED? "enabled" : "disabled");
                SolrCache<?, ?> cache= req.getSearcher().getCache("ispy");
                if (cache != null){
                    rsp.add("name", cache.getClass().getName());
                    rsp.add("cache", cache.toString());
                }


        }

    }

    @SuppressWarnings("unchecked")
    private void handleSpy(SolrQueryRequest req, SolrQueryResponse rsp) {
        String key = req.getParams().get(CommonParams.Q);
        SolrCache<String, String> cache= req.getSearcher().getCache("ispy");
        String value = cache.get(key);
        rsp.add("query", key);
        rsp.add("spy", value);


    }

    @SuppressWarnings("unchecked")
    private void handleList(SolrQueryRequest req) {
        SolrCache<String, String> cache= req.getSearcher().getCache("ispy");
        if (cache != null){

        }
    }

    protected void handleEnable(boolean enable) throws SolrException {
        ENABLED = enable;
        //if (healthcheck == null) {
        // throw new SolrException(
        //        SolrException.ErrorCode.SERVICE_UNAVAILABLE, "No healthcheck file defined.");
        //}
        if (enable) {
      /*try {
        // write out when the file was created
        Files.write(healthcheck, Instant.now().toString().getBytes(StandardCharsets.UTF_8));
      } catch (IOException e) {
        throw new SolrException(
                SolrException.ErrorCode.SERVER_ERROR, "Unable to write healthcheck flag file", e);
      }

       */
        } else {
      /*
      try {
        Files.deleteIfExists(healthcheck);
      } catch (Throwable cause) {
        throw new SolrException(
                SolrException.ErrorCode.NOT_FOUND,
                "Did not successfully delete healthcheck file: " + healthcheck.toAbsolutePath(),
                cause);
      }

       */
        }
    }

    //////////////////////// SolrInfoMBeans methods //////////////////////

    @Override
    public String getDescription() {
        return "Reports what a query response was";
    }

    @Override
    public Boolean registerV2() {
        return Boolean.TRUE;
    }

    @Override
    public Category getCategory() {
        return Category.ADMIN;
    }
}