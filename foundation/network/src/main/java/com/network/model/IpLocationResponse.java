package com.network.model;

import com.google.gson.annotations.SerializedName;

public class IpLocationResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("info")
    private String info;

    @SerializedName("infocode")
    private String infocode;

    @SerializedName("province")
    private String province;

    @SerializedName("city")
    private String city;

    @SerializedName("adcode")
    private String adcode;

    @SerializedName("rectangle")
    private String rectangle;

    public IpLocationResponse() {
    }

    public IpLocationResponse(String adcode, String city, String info, String infocode, String province, String rectangle, String status) {
        this.adcode = adcode;
        this.city = city;
        this.info = info;
        this.infocode = infocode;
        this.province = province;
        this.rectangle = rectangle;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getInfocode() {
        return infocode;
    }

    public void setInfocode(String infocode) {
        this.infocode = infocode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAdcode() {
        return adcode;
    }

    public void setAdcode(String adcode) {
        this.adcode = adcode;
    }

    public String getRectangle() {
        return rectangle;
    }

    public void setRectangle(String rectangle) {
        this.rectangle = rectangle;
    }

    @Override
    public String toString() {
        return "AmapIpLocation{" +
                "status='" + status + '\'' +
                ", info='" + info + '\'' +
                ", infocode='" + infocode + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", adcode='" + adcode + '\'' +
                ", rectangle='" + rectangle + '\'' +
                '}';
    }
}
