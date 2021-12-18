package com.tflow.model.editor.datasource;

import java.util.List;

public class SFTP extends DataSource {
    private static final long serialVersionUID = 2021121709996660012L;

    private List<String> pathHistory;
    private String rootPath;

    private String host;
    private String port;
    private String user;
    private String password;
    private int retry;
    private String tmp;

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

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
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

    public String getTmp() {
        return tmp;
    }

    public void setTmp(String tmp) {
        this.tmp = tmp;
    }
}
