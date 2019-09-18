package com.tnt.APIQueuingService.service;

import com.tnt.APIQueuingService.service.api.client.PriceApiClient;
import com.tnt.APIQueuingService.service.api.client.TrackApiClient;
import com.tnt.APIQueuingService.service.model.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ApiQueuingService {

    @Autowired
    TrackApiClient trackApiClient;

    @Autowired
    PriceApiClient priceApiClient;

    @Value("#{new Integer('${cap}')}")
    Integer cap;

    @Value("${trackApiUri}")
    String trackApiUri;

    @Value("${pricingApiUri}")
    String pricingApiUri;


    AtomicBoolean trackCap;
    AtomicBoolean priceCap;
    Queue<String> trackRequestQueue = new LinkedList<>();
    Queue<String> priceRequestQueue = new LinkedList<>();

    //return a list of json {key,value}
    public Optional<List<Map<String, String>>> process(List<Request> requests) {
         trackCap = new AtomicBoolean(false);
         priceCap = new AtomicBoolean(false);
         List<Map<String, String>> finalResponse = new ArrayList<>();

        requests.forEach(request -> {
            //tack api
            if (trackApiUri.equals(request.getUrl())) {
                if (trackRequestQueue.size() == cap-1) {
                    trackRequestQueue.add(request.getId());
                    finalResponse.add(processTrackRequestQueue(trackRequestQueue));

                } else
                    trackRequestQueue.add(request.getId());
            }
            //price api
            if (pricingApiUri.equals(request.getUrl())) {
                if (priceRequestQueue.size() == cap-1) {
                    priceRequestQueue.add(request.getId());
                    finalResponse.add(processPriceRequestQueue(priceRequestQueue));
                } else
                    priceRequestQueue.add(request.getId());
            }
        });

        if (requests.stream().filter(r -> r.getUrl().equals("/track")).findFirst().isPresent())
            if (trackCap.get() == false)
                return Optional.empty();

        if (requests.stream().filter(r -> r.getUrl().equals("/price")).findFirst().isPresent())
            if (priceCap.get() == false)
                return Optional.empty();

        return Optional.ofNullable(finalResponse);
    }

    private Map<String, String> processTrackRequestQueue(Queue<String> requestQueue ){

        TreeSet<String> queryString = new TreeSet<>();
        requestQueue.iterator().forEachRemaining(id -> queryString.add(id));
        trackRequestQueue.clear();
        trackCap.set(true);
        return trackApiClient.getTrackJson(String.join(",", queryString));

    }
    private Map<String, String> processPriceRequestQueue(Queue<String> requestQueue ){

        TreeSet<String> queryString = new TreeSet<>();
        requestQueue.iterator().forEachRemaining(id -> queryString.add(id));
        priceRequestQueue.clear();
        priceCap.set(true);
        return priceApiClient.getPriceJson(String.join(",", queryString));

    }

}
