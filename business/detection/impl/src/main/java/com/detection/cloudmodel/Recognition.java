package com.detection.cloudmodel;

public class Recognition {
    private String diseaseName;
    private float confidence;
    private String controlPlan;

    public Recognition(String diseaseName, float confidence, String controlPlan) {
        this.diseaseName = diseaseName;
        this.confidence = confidence;
        this.controlPlan = controlPlan;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public String getControlPlan() {
        return controlPlan;
    }

    public void setControlPlan(String controlPlan) {
        this.controlPlan = controlPlan;
    }
}
