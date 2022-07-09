package com.tflow.kafka;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ClientRecord implements Serializable {
    private static final long serialVersionUID = 2022070109996660001L;

    private long userId;
    private long clientId;
    private LocalDateTime expiredDate;

    public ClientRecord(KafkaTWAdditional additional) {
        this.userId = additional.getModifiedUserId();
        this.clientId = additional.getModifiedClientId();
        /*TODO: need to load timeout from configuration*/
        this.expiredDate = LocalDateTime.now().plusSeconds(3600);
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

    public LocalDateTime getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(LocalDateTime expiredDate) {
        this.expiredDate = expiredDate;
    }

    public boolean isMe(KafkaTWAdditional additional) {
        return additional.getModifiedClientId() == clientId &&
                additional.getModifiedUserId() == userId;
    }

    public boolean isTimeout() {
        return LocalDateTime.now().isAfter(expiredDate);
    }

    @Override
    public String toString() {
        return "{" +
                "userId: " + userId +
                ", clientId: " + clientId +
                ", expiredDate: '" + expiredDate + "'" +
                '}';
    }
}
