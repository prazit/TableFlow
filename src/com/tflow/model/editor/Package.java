package com.tflow.model.editor;

import java.util.Date;

public class Package {

    private int projectId;
    private int packageId;

    private String name;
    private Date buildDate;
    private Date builtDate;

    private int complete;

    public Package() {
        /*nothing*/
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getPackageId() {
        return packageId;
    }

    public void setPackageId(int packageId) {
        this.packageId = packageId;
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

    @Override
    public String toString() {
        return "{" +
                "projectId:" + projectId +
                ", packageId:" + packageId +
                ", name:'" + name + '\'' +
                ", buildDate:" + buildDate +
                ", builtDate:" + builtDate +
                ", complete:" + complete +
                '}';
    }
}
