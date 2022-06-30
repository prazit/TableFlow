package com.tflow.kafka;

public class ClientRecord {

    private long userId;
    private long clientId;

    public ClientRecord() {
        /*nothing*/
    }

    public ClientRecord(KafkaTWAdditional additional) {
        this.userId = additional.getModifiedUserId();
        this.clientId = additional.getModifiedClientId();
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

    public boolean isMe(KafkaTWAdditional additional) {
        return additional.getModifiedClientId() == clientId &&
                additional.getModifiedUserId() == userId;
    }
}
