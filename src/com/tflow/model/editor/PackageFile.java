package com.tflow.model.editor;

import com.tflow.model.data.FileNameExtension;
import com.tflow.model.data.FileType;

import java.util.Date;

public class PackageFile {

    private int id;
    private String name;
    private FileNameExtension ext;

    private FileType type;
    private int fileId;

    private Date buildDate;
    private String buildPath;

    private boolean updated;

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

    public FileNameExtension getExt() {
        return ext;
    }

    public void setExt(FileNameExtension ext) {
        this.ext = ext;
    }

    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public Date getBuildDate() {
        return buildDate;
    }

    public void setBuildDate(Date buildDate) {
        this.buildDate = buildDate;
    }

    public String getBuildPath() {
        return buildPath;
    }

    public void setBuildPath(String buildPath) {
        this.buildPath = buildPath;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    @Override
    public String toString() {
        return "{" +
                "id:" + id +
                ", name:'" + name + '\'' +
                ", ext:" + ext +
                ", type:" + type +
                ", fileId:" + fileId +
                ", buildDate:" + buildDate +
                ", buildPath:'" + buildPath + '\'' +
                ", updated:" + updated +
                '}';
    }
}
