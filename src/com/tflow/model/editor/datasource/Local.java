package com.tflow.model.editor.datasource;

import java.util.List;

public class Local  extends DataSource {
    private static final long serialVersionUID = 2021121709996660013L;

    private List<String> pathHistory;
    private String rootPath;

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
}
