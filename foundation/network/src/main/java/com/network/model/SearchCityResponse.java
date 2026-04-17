package com.network.model;

import java.util.List;

public class SearchCityResponse {
    private String code;
    private List<LocationItem> location;
    private Refer refer;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<LocationItem> getLocation() {
        return location;
    }

    public void setLocation(List<LocationItem> location) {
        this.location = location;
    }

    public Refer getRefer() {
        return refer;
    }

    public void setRefer(Refer refer) {
        this.refer = refer;
    }
}
