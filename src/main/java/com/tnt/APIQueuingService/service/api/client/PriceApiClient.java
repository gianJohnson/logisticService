package com.tnt.APIQueuingService.service.api.client;


import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class PriceApiClient {

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    private final RestTemplate restTemplate;

    public PriceApiClient(RestTemplateBuilder restTemplateBuilder) {
        restTemplate = restTemplateBuilder.build();
    }

    public Map<String, String> getPriceJson(@RequestParam String q) {
        return restTemplate.getForObject("/pricing/{q}",
                Map.class, q);

    }
}

