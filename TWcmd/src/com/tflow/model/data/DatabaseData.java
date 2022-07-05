package com.tflow.model.data;

import lombok.Data;

import java.util.Map;

/*TODO: complete all Fields in DatabaseData Model*/
@Data
public class DatabaseData extends DataSourceData {
    private static final long serialVersionUID = 2021121709996660011L;

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

