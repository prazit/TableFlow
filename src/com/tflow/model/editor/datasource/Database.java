package com.tflow.model.editor.datasource;

import com.clevel.dconvers.ngin.Crypto;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.Dbms;
import com.tflow.model.data.IDPrefix;
import com.tflow.model.editor.*;
import com.tflow.model.editor.Properties;
import com.tflow.model.editor.room.RoomType;
import com.tflow.model.editor.view.PropertyView;

import java.util.*;

public class Database extends DataSource implements Selectable, HasEvent {

    private Dbms dbms;
    private String url;
    private String driver;
    private String user;
    private String password;
    private int retry;

    private String host;
    private String port;
    private String schema;

    /*always encrypted*/
    private boolean userEncrypted;
    private boolean passwordEncrypted;

    /*quotes depend on selected dbms*/
    private String quotesForName;
    private String quotesForValue;

    private List<NameValue> propList;

    private EventManager eventManager;

    /* for DataSourceMapper*/
    public Database() {
        setImage("database.png");
        init();
    }

    /* for ProjectMapper only */
    public Database(int id) {
        this.id = id;
        setImage("database.png");
        init();
    }

    public Database(String name, Dbms dbms) {
        this.dbms = dbms;
        setImage(dbms.getImage());
        setName(name);
        init();
    }

    private void init() {
        setType(DataSourceType.DATABASE);
        userEncrypted = true;
        passwordEncrypted = true;
        propList = new ArrayList<>();
        addProp();
        this.setRoomType(RoomType.DATA_SOURCE);
        eventManager = new EventManager(this);
        createEventHandlers();
    }

    private void createEventHandlers() {
        eventManager.addHandler(EventName.PROPERTY_CHANGED, new EventHandler() {
            @Override
            public void handle(Event event) {
                url = dbms.getURL(host, port, schema);
            }
        });
    }

    public void addProp() {
        for (NameValue prop : propList) {
            prop.setLast(false);
        }

        propList.add(new NameValue(true));
    }

    @Override
    public ProjectFileType getProjectFileType() {
        return ProjectFileType.DB;
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

    public String getDecryptedUser() {
        return Crypto.decrypt(user);
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

    public String getDecryptedPassword() {
        return Crypto.decrypt(password);
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

    public List<NameValue> getPropList() {
        return propList;
    }

    public void setPropList(List<NameValue> propList) {
        this.propList = propList;
        if (propList.size() > 0) {
            propList.get(propList.size() - 1).setLast(true);
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public EventManager getEventManager() {
        return eventManager;
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
        return Properties.valueOf(dbms.name());
    }

    @Override
    public String getSelectableId() {
        return IDPrefix.DB.getPrefix() + id;
    }
}
