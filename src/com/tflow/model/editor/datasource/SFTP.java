package com.tflow.model.editor.datasource;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.IDPrefix;
import com.tflow.model.data.verify.Verifiers;
import com.tflow.model.editor.LinePlug;
import com.tflow.model.editor.Properties;
import com.tflow.model.editor.Selectable;
import com.tflow.model.editor.room.RoomType;
import com.tflow.model.mapper.ProjectMapper;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SFTP extends DataSource implements Selectable {

    private List<String> pathHistory;
    private String rootPath;

    private String host;
    private int port;
    private String user;
    private String password;
    private int retry;

    private boolean userEncrypted;
    private boolean passwordEncrypted;

    /*tmp=/ftp/<id>/<root-path>/*/
    private String tmp;

    /* for DataSourceMapper*/
    public SFTP() {/*nothing*/}

    /* for DataManager.getProject only */
    public SFTP(int id) {
        this.id = id;
    }

    public SFTP(String name, String rootPath) {
        setName(name);
        setType(DataSourceType.SFTP);
        setImage("ftp.png");
        this.rootPath = rootPath;
        pathHistory = new ArrayList<>();
        this.setRoomType(RoomType.DATA_SOURCE);
    }

    @Override
    public ProjectFileType getProjectFileType() {
        return ProjectFileType.SFTP;
    }

    public List<String> getPathHistory() {
        return pathHistory;
    }

    public void setPathHistory(List<String> pathHistory) {
        this.pathHistory = pathHistory;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public boolean isUserEncrypted() {
        return userEncrypted;
    }

    public void setUserEncrypted(boolean userEncrypted) {
        this.userEncrypted = userEncrypted;
    }

    public boolean isPasswordEncrypted() {
        return passwordEncrypted;
    }

    public void setPasswordEncrypted(boolean passwordEncrypted) {
        this.passwordEncrypted = passwordEncrypted;
    }

    public String getTmp() {
        return tmp;
    }

    public void setTmp(String tmp) {
        this.tmp = tmp;
    }

    public boolean isTestConnectionEnabled() {
        return Verifiers.getVerifier(Mappers.getMapper(ProjectMapper.class).map(this)).verify();
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
        return Properties.SFTP;
    }

    @Override
    public String getSelectableId() {
        return IDPrefix.SFTP.getPrefix() + id;
    }
}
