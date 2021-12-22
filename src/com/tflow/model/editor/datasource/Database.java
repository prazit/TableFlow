package com.tflow.model.editor.datasource;

import com.tflow.model.editor.room.RoomType;

import java.util.HashMap;
import java.util.Map;

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
    private Map<String, String> propList;

    public Database(String name, DBMS dbms, String plug) {
        this.dbms = dbms;
        setType("Database");
        setImage("database.png");
        setName(name);
        setPlug(plug);
        propList = new HashMap<>();
        this.setRoomType(RoomType.DATA_SOURCE);
    }

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

    public Map<String, String> getPropList() {
        return propList;
    }

    public void setPropList(Map<String, String> propList) {
        this.propList = propList;
    }
}
