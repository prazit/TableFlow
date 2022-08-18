package com.tflow.model.data;

public class ProjectUser {

    private String id;
    private long userId;
    private long clientId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    @Override
    public String toString() {
        return "{" +
                "id:" + id +
                ", userId:" + userId +
                ", clientId:" + clientId +
                '}';
    }
}
