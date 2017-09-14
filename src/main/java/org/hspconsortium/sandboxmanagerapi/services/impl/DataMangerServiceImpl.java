package org.hspconsortium.sandboxmanagerapi.services.impl;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.hspconsortium.sandboxmanagerapi.model.DataSet;
import org.hspconsortium.sandboxmanagerapi.model.Sandbox;
import org.hspconsortium.sandboxmanagerapi.model.SandboxImport;
import org.hspconsortium.sandboxmanagerapi.services.DataManagerService;
import org.hspconsortium.sandboxmanagerapi.services.SandboxService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.*;

@Service
public class DataMangerServiceImpl implements DataManagerService {

    private static Logger LOGGER = LoggerFactory.getLogger(SandboxServiceImpl.class.getName());

    private final SandboxService sandboxService;

    @Inject
    public DataMangerServiceImpl(final SandboxService sandboxService) {
        this.sandboxService = sandboxService;
    }

    @Override
    public String importPatientData(final Sandbox sandbox, final String bearerToken, final String endpoint, final String patientId, final String fhirIdPrefix) throws UnsupportedEncodingException {

        SandboxImport sandboxImport = new SandboxImport();
        Timestamp start = new Timestamp(new Date().getTime());
        sandboxImport.setTimestamp(start);
        sandboxImport.setSuccessCount("0");
        sandboxImport.setFailureCount("0");
        sandboxImport.setImportFhirUrl(endpoint + "/Patient/" + patientId + "/$everything");
        String success = getEverythingForPatient(patientId, endpoint, fhirIdPrefix, sandbox, bearerToken, sandboxImport);

        long seconds = (new Date().getTime()-start.getTime())/1000;
        sandboxImport.setDurationSeconds("" + seconds);
        sandboxService.addSandboxImport(sandbox, sandboxImport);
        return success;
    }

    @Override
    public String reset(final Sandbox sandbox, final String bearerToken) throws UnsupportedEncodingException {
        return resetSandboxFhirData(sandbox, bearerToken ) ? "SUCCESS" : "FAILED";
    }

    private String getEverythingForPatient(String patientId, String endpoint, String fhirIdPrefix, Sandbox sandbox, String bearerToken, SandboxImport sandboxImport) throws UnsupportedEncodingException {
        int success = 0;
        int failure = 0;

        String nextPage;
        List<JSONObject> everythingResources = new ArrayList<>();
        String query = "/Patient/" + patientId + "/$everything";
        do {
            String everything = queryFHIRServer(endpoint, query);
            JSONObject everythingJsonObj = new JSONObject(everything);
            everythingResources.addAll(getResourcesFromSearch(everythingJsonObj));
            nextPage = getNextPageLink(everythingJsonObj);
            if (nextPage != null) {
                String[] urlAndQuery = nextPage.split("\\?");
                query = "?" + urlAndQuery[1];
            }
        } while (nextPage != null);

        // Hack to remove the duplicate resources in the collection
        // TODO figure out why we have dups
        Set<String> resourceIds = new HashSet<>();
        Iterator iterator = everythingResources.iterator();
        while (iterator.hasNext()) {
            JSONObject jsonObject = (JSONObject)iterator.next();
            String id = jsonObject.getString("id");
            if (resourceIds.contains(id)) {
                iterator.remove();
            } else {
                resourceIds.add(id);
            }
        }

        String bundleString = buildTransactionBundle(everythingResources, fhirIdPrefix);

        postFHIRBundle(sandbox, bundleString, bearerToken);
        sandboxImport.setSuccessCount("" + resourceIds.size());
        return "SUCCESS";
    }

    private String buildTransactionBundle(List<JSONObject> resources, String fhirIdPrefix) {
        JSONObject transactionBundle = new JSONObject();
        JSONArray resourcesArray = new JSONArray();
        transactionBundle.put("resourceType", "Bundle");
        transactionBundle.put("type", "transaction");

        for (JSONObject resource : resources) {
            JSONObject entry = new JSONObject();

            String resourceType = resource.getString("resourceType");
            String resourceId = resource.getString("id");
            resource.put("id","/" + fhirIdPrefix + resourceId);
            entry.put("resource", resource);

            JSONObject request = new JSONObject();
            request.put("method", "PUT");
            request.put("url", resourceType + "/" + fhirIdPrefix + resourceId);
            entry.put("request", request);
            resourcesArray.put(entry);
        }

        transactionBundle.put("entry", resourcesArray);
        fixupIDs(transactionBundle, fhirIdPrefix);
        return transactionBundle.toString();
    }

    // Prefix resource ids - used for the transaction bundle
    private void fixupIDs(Object json, String fhirIdPrefix) {

        if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) json;
            if (jsonObject.has("reference")) {
                String ref = jsonObject.getString("reference");
                String[] resourceAndId = ref.split("/");
                if (resourceAndId.length == 2) {
                    ref = resourceAndId[0] + "/" + fhirIdPrefix + resourceAndId[1];
                    jsonObject.put("reference", ref);
                }
            } else {
                for (Object object : jsonObject.keySet()) {
                    if (object instanceof String) {
                        fixupIDs(jsonObject.get((String) object), fhirIdPrefix);
                    }
                }
            }
        } else if (json instanceof JSONArray){
            JSONArray jsonArray = (JSONArray) json;
            for (int i = 0; i < jsonArray.length(); i++) {
                fixupIDs(jsonArray.get(i), fhirIdPrefix);
            }
        }
    }

    private List<JSONObject> getResourcesFromSearch(JSONObject jsonObject) {
        List<JSONObject> resources = new ArrayList<>();

        JSONArray entries = jsonObject.getJSONArray("entry");

        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i);

            JSONObject resource = entry.getJSONObject("resource");
            resources.add(resource);
        }

        return resources;
    }

    private String getNextPageLink(JSONObject jsonObject) {
        JSONArray links = jsonObject.getJSONArray("link");

        for (int i = 0; i < links.length(); i++) {
            JSONObject link = links.getJSONObject(i);

            if (link.getString("relation").equalsIgnoreCase("next")) {
                return link.getString("url");
            }
        }

        return null;
    }

    private String queryFHIRServer(final String endpoint, final String query)  {
        String url = endpoint + query;

        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader("Accept", "application/json");

        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).useSSL().build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            LOGGER.error("Error loading ssl context", e);
            throw new RuntimeException(e);
        }
        HttpClientBuilder builder = HttpClientBuilder.create();
        SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        builder.setSSLSocketFactory(sslConnectionFactory);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionFactory)
                .register("http", new PlainConnectionSocketFactory())
                .build();
        HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
        builder.setConnectionManager(ccm);

        CloseableHttpClient httpClient = builder.build();

        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(getRequest)) {
            if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
                HttpEntity rEntity = closeableHttpResponse.getEntity();
                String responseString = EntityUtils.toString(rEntity, StandardCharsets.UTF_8);
                String errorMsg = String.format("There was a problem calling the source FHIR server.\n" +
                                "Response Status : %s .\nResponse Detail :%s. \nUrl: :%s",
                        closeableHttpResponse.getStatusLine(),
                        responseString,
                        url);
                LOGGER.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            HttpEntity httpEntity = closeableHttpResponse.getEntity();
            return EntityUtils.toString(httpEntity);
        } catch (IOException e) {
            LOGGER.error("Error posting to " + url, e);
            throw new RuntimeException(e);
        } finally {
            try {
                httpClient.close();
            }catch (IOException e) {
                LOGGER.error("Error closing HttpClient");
            }
        }
    }

    private boolean resetSandboxFhirData(final Sandbox sandbox, final String bearerToken ) throws UnsupportedEncodingException {
        String jsonString = "{}";
        if (!sandbox.getDataSet().equals(DataSet.NA)) {
            jsonString = "{\"dataSet\": \"" + sandbox.getDataSet() + "\"}";
        }

        if (postToSandbox(sandbox, jsonString, "/sandbox/reset", bearerToken )) {
            sandboxService.reset(sandbox, bearerToken);
            return true;
        }
        return false;
    }

    private boolean postFHIRBundle(final Sandbox sandbox, final String jsonString, final String bearerToken ) throws UnsupportedEncodingException {
        return postToSandbox(sandbox, jsonString, "/data", bearerToken );
    }

    private boolean postToSandbox(final Sandbox sandbox, final String jsonString, final String requestStr, final String bearerToken ) throws UnsupportedEncodingException {
        String url = sandboxService.getSandboxApiURL(sandbox) + requestStr;

        HttpPost postRequest = new HttpPost(url);
        postRequest.addHeader("Content-Type", "application/json");
        if (jsonString != null) {
            StringEntity entity = new StringEntity(jsonString);

            postRequest.setEntity(entity);
        }
        postRequest.setHeader("Authorization", "BEARER " + bearerToken);

        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).useSSL().build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            LOGGER.error("Error loading ssl context", e);
            throw new RuntimeException(e);
        }
        HttpClientBuilder builder = HttpClientBuilder.create();
        SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        builder.setSSLSocketFactory(sslConnectionFactory);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionFactory)
                .register("http", new PlainConnectionSocketFactory())
                .build();
        HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
        builder.setConnectionManager(ccm);

        CloseableHttpClient httpClient = builder.build();

        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(postRequest)) {
            if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
                HttpEntity rEntity = closeableHttpResponse.getEntity();
                String responseString = EntityUtils.toString(rEntity, StandardCharsets.UTF_8);
                String errorMsg = String.format("There was a problem posting to the sandbox.\n" +
                                "Response Status : %s .\nResponse Detail :%s. \nUrl: :%s",
                        closeableHttpResponse.getStatusLine(),
                        responseString,
                        url);
                LOGGER.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

            return true;
        } catch (IOException e) {
            LOGGER.error("Error posting to " + url, e);
            throw new RuntimeException(e);
        } finally {
            try {
                httpClient.close();
            }catch (IOException e) {
                LOGGER.error("Error closing HttpClient");
            }
        }
    }

}
