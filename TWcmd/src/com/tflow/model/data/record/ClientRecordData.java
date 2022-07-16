package com.tflow.model.data.record;

import lombok.Data;

import java.io.Serializable;

@Data
public class ClientRecordData implements Serializable {
    private static final transient long serialVersionUID = 2022070109996660001L;

    private long userId;
    private long clientId;
    private long expiredDate;
}
