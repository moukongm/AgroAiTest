package com.network.model;

import java.util.List;

public class Refer {
    private List<String> sources;
    private List<String> license;

    public List<String> getLicense() {
        return license;
    }

    public void setLicense(List<String> license) {
        this.license = license;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }
}
