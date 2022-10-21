package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class HeaderData implements Serializable {
    private static final transient long serialVersionUID = 2022070109996660003L;

    private long responseCode;
    private long more;

    private long transactionId;
    private long time;
    private long userId;
    private long clientId;
    private String projectId;
}
