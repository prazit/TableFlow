package com.tflow.model.editor;

import java.util.Date;

public class Package {

    private int id;

    private String name;
    private Date buildDate;
    private Date builtDate;

    private int complete;

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

    @Override
    public String toString() {
        return "{" +
                ", id:" + id +
                ", name:'" + name + '\'' +
                ", buildDate:" + buildDate +
                ", builtDate:" + builtDate +
                ", complete:" + complete +
                '}';
    }
}
