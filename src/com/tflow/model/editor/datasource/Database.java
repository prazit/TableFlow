package com.tflow.model.editor.datasource;

import javafx.util.Pair;

import java.util.List;

public class Database extends DataSource {
    private static final long serialVersionUID = 2021121709996660011L;

    private DBMS dbms;
    private String url;
    private String driver;
    private String user;
    private boolean userEncrypted;
    private String password;
    private boolean passwordEncrypted;
    private int retry;
    private String quotesForName;
    private String quotesForValue;
    private List<Pair<String, String>> propList;

    public DBMS getDbms() {
        return dbms;
    }

    public void setDbms(DBMS dbms) {
        this.dbms = dbms;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isUserEncrypted() {
        return userEncrypted;
    }

    public void setUserEncrypted(boolean userEncrypted) {
        this.userEncrypted = userEncrypted;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isPasswordEncrypted() {
        return passwordEncrypted;
    }

    public void setPasswordEncrypted(boolean passwordEncrypted) {
        this.passwordEncrypted = passwordEncrypted;
    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public String getQuotesForName() {
        return quotesForName;
    }

    public void setQuotesForName(String quotesForName) {
        this.quotesForName = quotesForName;
    }

    public String getQuotesForValue() {
        return quotesForValue;
    }

    public void setQuotesForValue(String quotesForValue) {
        this.quotesForValue = quotesForValue;
    }

    public List<Pair<String, String>> getPropList() {
        return propList;
    }

    public void setPropList(List<Pair<String, String>> propList) {
        this.propList = propList;
    }
}
