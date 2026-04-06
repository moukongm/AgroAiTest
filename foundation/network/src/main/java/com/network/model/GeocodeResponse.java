package com.network.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GeocodeResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("count")
    private String count;

    @SerializedName("info")
    private String info;

    @SerializedName("geocodes")
    private List<Geocode> geocodes;

    // 无参构造
    public GeocodeResponse() {
    }

    // getter/setter
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public List<Geocode> getGeocodes() {
        return geocodes;
    }

    public void setGeocodes(List<Geocode> geocodes) {
        this.geocodes = geocodes;
    }

    // 内部类 Geocode
    public static class Geocode {

        @SerializedName("location")
        private String location;  // 格式："经度,纬度"

        public Geocode() {
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }
}