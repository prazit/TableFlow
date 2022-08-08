package com.tflow.model.editor.datasource;

import com.tflow.model.data.IDPrefix;
import com.tflow.model.editor.LinePlug;
import com.tflow.model.editor.Properties;
import com.tflow.model.editor.Selectable;
import com.tflow.model.editor.room.RoomType;

import java.util.HashMap;
import java.util.Map;

public class Database extends DataSource implements Selectable {

    private Dbms dbms;
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

    /* for DataSourceMapper*/
    public Database() {/*nothing*/}

    /* for ProjectMapper only */
    public Database(int id) {
        this.id = id;
    }

    public Database(String name, Dbms dbms) {
        this.dbms = dbms;
        setType(DataSourceType.DATABASE);
        setImage("database.png");
        setName(name);
        userEncrypted = true;
        passwordEncrypted = true;
        propList = new HashMap<>();
        this.setRoomType(RoomType.DATA_SOURCE);
    }

    public Dbms getDbms() {
        return dbms;
    }

    public void setDbms(Dbms dbms) {
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

    @Override
    public LinePlug getStartPlug() {
        return null;
    }

    @Override
    public void setStartPlug(LinePlug startPlug) {
        /*nothing*/
    }

    @Override
    public Map<String, Object> getPropertyMap() {
        return new HashMap<>();
    }

    @Override
    public Properties getProperties() {
        return Properties.DATA_BASE;
    }

    @Override
    public String getSelectableId() {
        return IDPrefix.DB.getPrefix() + id;
    }
}
