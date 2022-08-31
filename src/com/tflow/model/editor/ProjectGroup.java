package com.tflow.model.editor;

import java.util.Arrays;
import java.util.List;

public class ProjectGroup {

    private int id;
    private String name;
    private List<ProjectItem> projectList;

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

    public List<ProjectItem> getProjectList() {
        return projectList;
    }

    public void setProjectList(List<ProjectItem> projectList) {
        this.projectList = projectList;
    }

    @Override
    public String toString() {
        return "{" +
                "id:" + id +
                ", name:'" + name + '\'' +
                ", projectList:" + Arrays.toString(projectList.toArray()) +
                '}';
    }
}
