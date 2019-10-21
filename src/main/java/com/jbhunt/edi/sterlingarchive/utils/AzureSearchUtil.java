/*
package com.jbhunt.edi.sterlingarchive.utils;

import com.jbhunt.edi.sterlingarchive.dto.AzureSearchResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class AzureSearchUtil {
    private static final String url = "https://edi.search.windows.net/indexes/azureblob-indexmetadata/docs?api-version=2017-11-11&search=";
    private static final String apiKey = "0EC02D290B2F63B9FC755444DCA1086C";

    private final RestTemplate restTemplate;

    public AzureSearchUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AzureSearchResponseDTO performSearch(String searchString) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", apiKey);
        HttpEntity<String> httpEntity = new HttpEntity<>("parameters", headers);
        ParameterizedTypeReference<AzureSearchResponseDTO> responseType =
                new ParameterizedTypeReference<AzureSearchResponseDTO>() {};
        ResponseEntity<AzureSearchResponseDTO> response =
                restTemplate.exchange(url + searchString, HttpMethod.GET, httpEntity, responseType);
        return response.getBody();
    }
}
*/
