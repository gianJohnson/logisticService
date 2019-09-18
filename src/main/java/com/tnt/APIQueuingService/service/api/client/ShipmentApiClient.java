package com.tnt.APIQueuingService.service.api.client;


import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ShipmentApiClient {

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    private final RestTemplate restTemplate;

    public ShipmentApiClient(RestTemplateBuilder restTemplateBuilder) {
        restTemplate = restTemplateBuilder.build();
    }

    public Map<String, List<String>> getShipmentsJson(@RequestParam String q) {
        return restTemplate.getForObject("/shipments/{q}",
                Map.class, q);

    }
}

