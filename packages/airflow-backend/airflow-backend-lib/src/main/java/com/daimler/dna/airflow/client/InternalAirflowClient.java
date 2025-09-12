
package com.daimler.dna.airflow.client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;



import com.daimler.dna.airflow.exceptions.GenericMessage;

import org.springframework.http.HttpHeaders; 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
public class InternalAirflowClient {

    @Value("${dag.run.authToken}")
	private String secret;

    @Value("${dag.run.Url}")
    private String baseURL;

    @Autowired
    private RestTemplate proxyRestTemplate;


    private static final Logger LOGGER = LoggerFactory.getLogger(InternalAirflowClient.class);

    public GenericMessage triggerDag(String dagid) {
        try {
            LOGGER.info("Triggering DAG: " + dagid);

            String airflowUrl = baseURL + dagid + "/dagRuns";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.set("Authorization", "Basic "+secret);
            String body = "{}";

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            proxyRestTemplate.postForEntity(airflowUrl, entity, String.class);

            return new GenericMessage("DAG triggered successfully.");
        } catch (Exception e) {
            LOGGER.error("Error triggering DAG: {}", e.getMessage());
            return new GenericMessage("Failed to trigger DAG: " + e.getMessage());
        }
    }
}
