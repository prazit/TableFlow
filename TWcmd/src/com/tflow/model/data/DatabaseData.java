package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class DatabaseData extends DataSourceData {
    private static final transient long serialVersionUID = 2021121709996660011L;

    private String dbms;
    private String url;
    private String driver;

    private String user;
    private String password;
    private int retry;

    /*always encrypted*/
    private boolean userEncrypted;
    private boolean passwordEncrypted;

    /*quotes depend on selected dbms*/
    private String quotesForName;
    private String quotesForValue;

    private Map<String, String> propList;

}

