package com.tnt.APIQueuingService.service.api.client;


import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;


import java.util.Map;

@Service
public class TrackApiClient {

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    private final RestTemplate restTemplate;

    public TrackApiClient(RestTemplateBuilder restTemplateBuilder) {
        restTemplate = restTemplateBuilder.build();
    }

    public Map<String, String> getTrackJson(@RequestParam String q) {
        return restTemplate.getForObject("/track/{q}",
                Map.class, q);
    }

}

