package org.logicahealth.sandboxmanagerapi.services.impl;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.logicahealth.sandboxmanagerapi.controllers.UnauthorizedException;
import org.logicahealth.sandboxmanagerapi.model.DataSet;
import org.logicahealth.sandboxmanagerapi.model.Sandbox;
import org.logicahealth.sandboxmanagerapi.model.SandboxImport;
import org.logicahealth.sandboxmanagerapi.services.DataManagerService;
import org.logicahealth.sandboxmanagerapi.services.SandboxService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.IntStream;

@Service
public class DataMangerServiceImpl implements DataManagerService {

    private static Logger LOGGER = LoggerFactory.getLogger(DataMangerServiceImpl.class.getName());

    private SandboxService sandboxService;
    private CloseableHttpClient httpClient;

    public DataMangerServiceImpl() {
    }

    @Inject
    public void setSandboxService(SandboxService sandboxService) {
        this.sandboxService = sandboxService;
    }

    @Inject
    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    private RestTemplate simpleRestTemplate;

    @Inject
    public void setRestTemplate(RestTemplate simpleRestTemplate) {
        this.simpleRestTemplate = simpleRestTemplate;
    }

    @Override
    public String importPatientData(final Sandbox sandbox, final String bearerToken, final String endpoint, final String patientId, final String fhirIdPrefix) throws UnsupportedEncodingException {

        LOGGER.info("importPatientData");

        LOGGER.debug("importPatientData: "
        +"(BEFORE) Parameters: sandbox = "+sandbox+", bearerToken = "+bearerToken+", endpoint = "+endpoint
        +", patientId = "+patientId+", fhirIdPrefix = "+fhirIdPrefix);

        SandboxImport sandboxImport = new SandboxImport();
        Timestamp start = new Timestamp(new Date().getTime());
        sandboxImport.setTimestamp(start);
        sandboxImport.setSuccessCount("0");
        sandboxImport.setFailureCount("0");
        sandboxImport.setImportFhirUrl(endpoint + "/Patient/" + patientId + "/$everything");
        String success = getEverythingForPatient(patientId, endpoint, fhirIdPrefix, sandbox, bearerToken, sandboxImport);

        long seconds = (new Date().getTime()-start.getTime())/1000;
        sandboxImport.setDurationSeconds(Long.toString(seconds));
        sandboxService.addSandboxImport(sandbox, sandboxImport);

        LOGGER.debug("importPatientData: "
        +"(AFTER) Parameters: sandbox = "+sandbox+", bearerToken = "+bearerToken+", endpoint = "+endpoint
        +", patientId = "+patientId+", fhirIdPrefix = "+fhirIdPrefix+"; Return value = "+success);

        return success;
    }

    @Override
    public String reset(final Sandbox sandbox, final String bearerToken) throws UnsupportedEncodingException {
        
        LOGGER.info("reset");

        String retVal = resetSandboxFhirData(sandbox, bearerToken ) ? "SUCCESS" : "FAILED";

        LOGGER.debug("reset: "
        +"Parameters: sandbox = "+sandbox+", bearerToken = "+bearerToken
        +"; Return value = "+retVal);

        return retVal;
    }

    private String getEverythingForPatient(String patientId, String endpoint, String fhirIdPrefix, Sandbox sandbox, String bearerToken, SandboxImport sandboxImport) throws UnsupportedEncodingException {
        
        LOGGER.info("getEverythingForPatient");

        LOGGER.debug("getEverythingForPatient: "
        +"(BEFORE) Parameters: patientId = "+patientId+", endpoint = "+endpoint
        +", fhirIdPrefix = "+fhirIdPrefix+", sandbox = "+sandbox+", bearerToken = "+bearerToken
        +", sandboxImport = "+sandboxImport);

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
        sandboxImport.setSuccessCount(Integer.toString(resourceIds.size()));

        LOGGER.debug("getEverythingForPatient: "
        +"(AFTER) Parameters: patientId = "+patientId+", endpoint = "+endpoint
        +", fhirIdPrefix = "+fhirIdPrefix+", sandbox = "+sandbox+", bearerToken = "+bearerToken
        +", sandboxImport = "+sandboxImport+"; Return value = SUCCESS");

        return "SUCCESS";
    }

    private String buildTransactionBundle(List<JSONObject> resources, String fhirIdPrefix) {
        
        LOGGER.info("buildTransactionBundle");

        LOGGER.debug("buildTransactionBundle: "
        +"(BEFORE) Parameters: resources = "+resources+", fhirIdPrefix = "+fhirIdPrefix);

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

        LOGGER.debug("buildTransactionBundle: "
        +"(AFTER) Parameters: resources = "+resources+", fhirIdPrefix = "+fhirIdPrefix
        +"; Return value = "+transactionBundle.toString());

        return transactionBundle.toString();
    }

    // Prefix resource ids - used for the transaction bundle
    private void fixupIDs(Object json, String fhirIdPrefix) {

        LOGGER.info("fixupIDs");

        if (json instanceof JSONObject) {
            fixupJsonObjectIds((JSONObject) json, fhirIdPrefix);
        } else if (json instanceof JSONArray){
            JSONArray jsonArray = (JSONArray) json;
            IntStream.range(0, jsonArray.length()).forEach(i -> fixupIDs(jsonArray.get(i), fhirIdPrefix));
        }

        LOGGER.debug("fixupIDs: "
        +"Parameters: json = "+json+", fhirIdPrefix = "+fhirIdPrefix
        +"; No return value");

    }

    private void fixupJsonObjectIds(JSONObject json, String fhirIdPrefix) {
        
        LOGGER.info("fixupJsonObjectIds");

        final String reference = "reference";
        JSONObject jsonObject = json;
        if (jsonObject.has(reference)) {
            String ref = jsonObject.getString(reference);
            String[] resourceAndId = ref.split("/");
            if (resourceAndId.length == 2) {
                ref = resourceAndId[0] + "/" + fhirIdPrefix + resourceAndId[1];
                jsonObject.put(reference, ref);
            }
        } else {
            for (Object object : jsonObject.keySet()) {
                if (object instanceof String) {
                    fixupIDs(jsonObject.get((String) object), fhirIdPrefix);
                }
            }
        }

        LOGGER.debug("fixupJsonObjectIds: "
        +"Parameters: json = "+json+", fhirIdPrefix = "+fhirIdPrefix
        +"; No return value");

    }

    private List<JSONObject> getResourcesFromSearch(JSONObject jsonObject) {
        
        LOGGER.info("getResourcesFromSearch");

        List<JSONObject> resources = new ArrayList<>();

        JSONArray entries = jsonObject.getJSONArray("entry");

        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i);

            JSONObject resource = entry.getJSONObject("resource");
            resources.add(resource);
        }

        LOGGER.debug("getResourcesFromSearch: "
        +"Parameters: jsonObject = "+jsonObject+"; Return value = "+resources);

        return resources;
    }

    private String getNextPageLink(JSONObject jsonObject) {
        
        LOGGER.info("getNextPageLink");

        JSONArray links = jsonObject.getJSONArray("link");

        for (int i = 0; i < links.length(); i++) {
            JSONObject link = links.getJSONObject(i);

            if ("next".equalsIgnoreCase(link.getString("relation"))) {

                LOGGER.debug("getNextPageLink: "
                +"Parameters: jsonObject = "+jsonObject
                +"; Return value = "+link.getString("url"));

                return link.getString("url");
            }
        }

        LOGGER.debug("getNextPageLink: "
        +"Parameters: jsonObject = "+jsonObject
        +"; Return value = "+null);

        return null;
    }

    private String queryFHIRServer(final String endpoint, final String query)  {
        
        LOGGER.info("queryFHIRServer");

        String url = endpoint + query;

        // TODO: change to using 'simpleRestTemplate'
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader("Accept", "application/json");

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

            LOGGER.debug("queryFHIRServer: "
            +"Parameters: endpoint = "+endpoint+", query = "+query
            +"; Return value = "+EntityUtils.toString(httpEntity));

            return EntityUtils.toString(httpEntity);
        } catch (IOException e) {
            LOGGER.error("Error posting to " + url, e);
            throw new RuntimeException(e);
        } finally {
//            try {
//                httpClient.close();
//            }catch (IOException e) {
//                LOGGER.error("Error closing HttpClient", e);
//            }
        }
    }

    private boolean resetSandboxFhirData(final Sandbox sandbox, final String bearerToken ) throws UnsupportedEncodingException {
        
        LOGGER.info("resetSandboxFhirData");

        String jsonString = "{}";
        if (sandbox.getDataSet().equals(DataSet.DEFAULT)) {
            jsonString = "{\"dataSet\": \"" + sandbox.getDataSet() + "\"}";
        }

        if (postToSandbox(sandbox, jsonString, "/sandbox/reset", bearerToken )) {
            sandboxService.reset(sandbox, bearerToken);

            LOGGER.debug("resetSandboxFhirData: "
            +"Parameters: sandbox = "+sandbox+", bearerToken = "+bearerToken
            +"; Return value = true");

            return true;
        }
        
        LOGGER.debug("resetSandboxFhirData: "
        +"Parameters: sandbox = "+sandbox+", bearerToken = "+bearerToken
        +"; Return value = false");

        return false;
    }

    private boolean postFHIRBundle(final Sandbox sandbox, final String jsonString, final String bearerToken ) throws UnsupportedEncodingException {
        
        LOGGER.info("postFHIRBundle");

        boolean retVal = postToSandbox(sandbox, jsonString, "/data", bearerToken );

        LOGGER.debug("postFHIRBundle: "
        +"Parameters: sandbox = "+sandbox+", jsonString = "+jsonString+", bearerToken = "+bearerToken
        +"; Return value = "+retVal);

        return retVal;
    }

    private boolean postToSandbox(final Sandbox sandbox, final String jsonString, final String requestStr, final String bearerToken ) throws UnsupportedEncodingException {
        
        LOGGER.info("postToSandbox");

        String url = sandboxService.getSandboxApiURL(sandbox) + requestStr;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "BEARER " + bearerToken);
        headers.set("Content-Type", "application/json");

        org.springframework.http.HttpEntity entity = new org.springframework.http.HttpEntity(jsonString, headers);

        try {
            simpleRestTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            LOGGER.debug("postToSandbox: "
            +"Parameters: sandbox = "+sandbox+", jsonString = "+jsonString+", requestStr = "+requestStr
            +", bearerToken = "+bearerToken+"; Return value = true");

            return true;
        } catch (HttpClientErrorException e) {
            if (e.getRawStatusCode() == 401) {
                throw new UnauthorizedException(String.format("Response Status : %s.\n" +
                                "Response Detail : User not authorized to perform this action."
                        , HttpStatus.SC_UNAUTHORIZED));
            } else {
                throw new UnknownError("There was a problem posting to the sandbox.");
            }
        }
    }

}
