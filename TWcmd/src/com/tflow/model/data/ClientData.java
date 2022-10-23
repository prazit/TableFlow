package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class ClientData extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660063L;

    /**
     * map to Client-ComputerName-Ip
     */
    private String id;

    /**
     * map to Client.Id
     */
    private long uniqueNumber;

}
