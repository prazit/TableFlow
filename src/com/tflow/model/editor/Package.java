package com.tflow.model.editor;

import com.tflow.model.data.IDPrefix;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Package implements Selectable {

    private int id;

    private String name;
    private Date buildDate;
    private Date builtDate;

    private int complete;

    private List<PackageFile> fileList;
    private int lastFileId;

    public Package() {
        /*nothing*/
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBuildDate() {
        return buildDate;
    }

    public void setBuildDate(Date buildDate) {
        this.buildDate = buildDate;
    }

    public Date getBuiltDate() {
        return builtDate;
    }

    public void setBuiltDate(Date builtDate) {
        this.builtDate = builtDate;
    }

    public int getComplete() {
        return complete;
    }

    public void setComplete(int complete) {
        this.complete = complete;
    }

    public List<PackageFile> getFileList() {
        return fileList;
    }

    public void setFileList(List<PackageFile> fileList) {
        this.fileList = fileList;
    }

    public int getLastFileId() {
        return lastFileId;
    }

    public void setLastFileId(int lastFileId) {
        this.lastFileId = lastFileId;
    }

    @Override
    public String toString() {
        return "{" +
                ", id:" + id +
                ", name:'" + name + '\'' +
                ", buildDate:" + buildDate +
                ", builtDate:" + builtDate +
                ", complete:" + complete +
                ", lastFileId:" + lastFileId +
                ", fileList:" + (fileList == null ? 0 : fileList.size()) +
                '}';
    }

    @Override
    public Properties getProperties() {
        return Properties.PACKAGE;
    }

    @Override
    public String getSelectableId() {
        return IDPrefix.PACKAGE.getPrefix() + id;
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
}
