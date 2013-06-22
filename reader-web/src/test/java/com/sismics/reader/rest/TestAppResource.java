package com.sismics.reader.rest;

import junit.framework.Assert;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import com.sismics.reader.rest.filter.CookieAuthenticationFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;

/**
 * Test the app resource.
 * 
 * @author jtremeaux
 */
public class TestAppResource extends BaseJerseyTest {
    /**
     * Test the API resource.
     * 
     * @throws JSONException
     */
    @Test
    public void testAppResource() throws JSONException {
        // Check the application info
        WebResource appResource = resource().path("/app");
        ClientResponse response = appResource.get(ClientResponse.class);
        response = appResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        String currentVersion = json.getString("current_version");
        Assert.assertNotNull(currentVersion);
        String minVersion = json.getString("min_version");
        Assert.assertNotNull(minVersion);
        Long freeMemory = json.getLong("free_memory");
        Assert.assertTrue(freeMemory > 0);
        Long totalMemory = json.getLong("total_memory");
        Assert.assertTrue(totalMemory > 0 && totalMemory > freeMemory);
        
        // Login admin
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);
        
        // Rebuild articles index
        appResource = resource().path("/app/batch/reindex");
        appResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = appResource.post(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
    }

    /**
     * Test the map port resource.
     * 
     * @throws JSONException
     */
    @Test
    @Ignore
    public void testMapPortResource() throws JSONException {
        // Login admin
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);
        
        // Map port using UPnP
        WebResource appResource = resource().path("/app/map_port");
        appResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        appResource.post(ClientResponse.class);
    }
    
    /**
     * Test the log resource.
     * 
     * @throws JSONException
     */
    @Test
    public void testLogResource() throws JSONException {
        // Login admin
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);
        
        // Check the logs (page 1)
        WebResource appResource = resource()
                .path("/app/log")
                .queryParam("level", "INFO");
        ClientResponse response = appResource.get(ClientResponse.class);
        appResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = appResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        JSONArray logs = json.getJSONArray("logs");
        Assert.assertTrue(logs.length() == 10);
        Long date1 = logs.optJSONObject(0).optLong("date");
        Long date2 = logs.optJSONObject(9).optLong("date");
        Assert.assertTrue(date1 > date2);
        
        // Check the logs (page 2)
        appResource = resource()
                .path("/app/log")
                .queryParam("offset",  "10")
                .queryParam("level", "INFO");
        response = appResource.get(ClientResponse.class);
        appResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        response = appResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        logs = json.getJSONArray("logs");
        Assert.assertTrue(logs.length() == 10);
        Long date3 = logs.optJSONObject(0).optLong("date");
        Long date4 = logs.optJSONObject(9).optLong("date");
        Assert.assertTrue(date3 > date4);
        Assert.assertTrue(date2 > date3);
    }
}