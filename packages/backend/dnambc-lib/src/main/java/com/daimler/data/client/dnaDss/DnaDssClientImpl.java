/* LICENSE START
 *
 * MIT License
 *
 * Copyright (c) 2019 Daimler TSS GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * LICENSE END
 */
package com.daimler.data.client.dnaDss;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Component
public class DnaDssClientImpl implements DnaDssClient {

    private Logger LOGGER = LoggerFactory.getLogger(DnaDssClientImpl.class);

    @Autowired
    HttpServletRequest httpRequest;

    @Value("${dnadss.provisionSolutionuri}")
    private String provisionSolutionUri;

    @Autowired
    RestTemplate restTemplate;

    @Override
    public GenericMessage provisionSolutionToDataikuProject(String projectName, String cloudProfile, String solutionId) {
        GenericMessage dataikuResponse = new GenericMessage();
        String jwt = httpRequest.getHeader("Authorization");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", jwt);
        JSONObject res = null;

        // Create a JSON object with the solutionId as a body.
        JSONObject body = new JSONObject();
        body.put("solutionId", solutionId);
        String url = provisionSolutionUri + "/" + cloudProfile + "/" + projectName;
        try {
            HttpEntity entity = new HttpEntity<>(body.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);
            if (response != null && response.hasBody()) {
                res = (JSONObject) new JSONObject(response.getBody()).get("data");
                if (res.get("id") != null) {
                    LOGGER.info("Successfully updated dataiku project {} of {} with solutionId {} ", projectName, cloudProfile, solutionId);
                    dataikuResponse.setSuccess("success");
                } else {
                    dataikuResponse.setSuccess("Failed");
                }
            }
            return dataikuResponse;
        } catch (Exception e) {
            LOGGER.error("Failed at dataiku solution for project {} with solution {}, with an exception, {}", projectName, solutionId, e.getMessage());
            dataikuResponse.setSuccess("Failed");
            List<MessageDescription> errors = new ArrayList<>();
            MessageDescription errMsg = new MessageDescription("Failed while updating solution " + solutionId + " to Project with name " + projectName + " and profile " + cloudProfile);
            errors.add(errMsg);
            dataikuResponse.setErrors(errors);
        }
        return dataikuResponse;
    }
}
