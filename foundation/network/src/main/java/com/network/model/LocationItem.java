// LocationItem.java
package com.network.model;
public class LocationItem {
    private String name;
    private String adm2;
    private String adm1;
    private String country;
    // 可选字段
    private String id;
    private String lat;
    private String lon;
    private String tz;
    private String utcOffset;
    private String isDst;
    private String type;
    private String rank;
    private String fxLink;

    // getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAdm2() { return adm2; }
    public void setAdm2(String adm2) { this.adm2 = adm2; }
    public String getAdm1() { return adm1; }
    public void setAdm1(String adm1) { this.adm1 = adm1; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    // 可选字段的getter/setter省略，可按需添加
}
