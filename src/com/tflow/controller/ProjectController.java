package com.tflow.controller;

import com.tflow.model.editor.*;
import com.tflow.model.editor.datasource.DataSource;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.datasource.SFTP;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@ViewScoped
@Named("projectCtl")
public class ProjectController extends Controller {

    private Project project;

    private List<Database> databaseList;
    private List<Local> localList;
    private List<SFTP> sftpList;

    @PostConstruct
    public void onCreation() {
        project = workspace.getProject();
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<Database> getDatabaseList() {
        Map<Integer, Database> databaseMap = project.getDatabaseMap();
        if (databaseList == null || databaseList.size() != databaseMap.size()) {
            databaseList = databaseMap.values().stream().sorted(Comparator.comparingInt(DataSource::getId)).collect(Collectors.toList());
        }
        return databaseList;
    }

    public List<SFTP> getSftpList() {
        Map<Integer, SFTP> sftpMap = project.getSftpMap();
        if (sftpList == null || sftpList.size() != sftpMap.size()) {
            sftpList = sftpMap.values().stream().sorted(Comparator.comparingInt(DataSource::getId)).collect(Collectors.toList());
        }
        return sftpList;
    }

    public List<Local> getLocalList() {
        Map<Integer, Local> localMap = project.getLocalMap();
        if (localList == null || localList.size() != localMap.size()) {
            localList = localMap.values().stream().sorted(Comparator.comparingInt(DataSource::getId)).collect(Collectors.toList());
        }
        return localList;
    }

    /*==== PUBLIC METHOD ====*/

}
