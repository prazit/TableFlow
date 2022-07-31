package com.tflow.model.data;

public class ProjectUser {

    private String id;
    private int userId;
    private int clientId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
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
