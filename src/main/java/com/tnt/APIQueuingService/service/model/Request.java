package com.tnt.APIQueuingService.service.model;

public class Request {

    String url;
    String id;

    public Request(String url, String id) {
        this.url = url;
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public String getId() {
        return id;
    }
}
