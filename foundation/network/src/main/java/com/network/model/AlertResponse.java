package com.network.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 预警响应根类
 */
public class AlertResponse {

    @SerializedName("metadata")
    private Metadata metadata;

    @SerializedName("alerts")
    private List<Alert> alerts;

    // getter / setter
    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public List<Alert> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<Alert> alerts) {
        this.alerts = alerts;
    }

    /**
     * 元数据（可忽略，但为完整保留）
     */
    public static class Metadata {
        @SerializedName("tag")
        private String tag;
        @SerializedName("zeroResult")
        private Boolean zeroResult;
        // 可根据需要添加其他字段
        // getter/setter 省略
    }

    /**
     * 预警信息，只包含需要的字段
     */
    public static class Alert {
        @SerializedName("id")
        private String id;

        @SerializedName("senderName")
        private String senderName;

        @SerializedName("issuedTime")
        private String issuedTime;

        @SerializedName("description")
        private String description;

        @SerializedName("eventType")
        private EventType eventType;

        @SerializedName("color")
        private Color color;

        // 以下为可选的其他基本字段，如需可添加
        // @SerializedName("headline") private String headline;

        // getter / setter
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSenderName() {
            return senderName;
        }

        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }

        public String getIssuedTime() {
            return issuedTime;
        }

        public void setIssuedTime(String issuedTime) {
            this.issuedTime = issuedTime;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public EventType getEventType() {
            return eventType;
        }

        public void setEventType(EventType eventType) {
            this.eventType = eventType;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }
    }

    public static class EventType {
        @SerializedName("name")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Color {
        @SerializedName("code")
        private String code;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}