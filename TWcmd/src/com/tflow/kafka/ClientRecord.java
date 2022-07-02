package com.tflow.kafka;

import java.io.Serializable;

public class ClientRecord implements Serializable {
    private static final long serialVersionUID = 2022070109996660001L;

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

    @Override
    public String toString() {
        return "{" +
                "userId:" + userId +
                ", clientId:" + clientId +
                '}';
    }
}
