package com.tflow.controller;

import com.tflow.model.data.*;
import com.tflow.model.editor.*;
import com.tflow.model.editor.Package;
import com.tflow.model.editor.datasource.DataSource;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.datasource.SFTP;
import com.tflow.util.FacesUtil;
import org.primefaces.event.TabChangeEvent;

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

    /*TODO: Uploaded File List here*/

    private List<Item> packageList;
    private int selectedPackageId;
    private Package activePackage;
    private boolean pleaseSelectPackage;

    @PostConstruct
    public void onCreation() {
        log.warn("ProjectController.onCreation.", new Exception("onCreation"));
        project = workspace.getProject();
    }

    public void openSection(TabChangeEvent event) {
        String title = event.getTab().getTitle();
        log.debug("openSection: selectedTitle={}, event={}", title, event);
        if (title.compareTo(ProjectSection.PACKAGE.getTitle()) == 0) {
            openPackage();
        }
    }

    public void openPackage() {
        log.trace("openPackage");
        reloadPackageList();
        selectPackage(packageList.size() - 1);
    }

    public void selectPackage(int packageId) {
        selectedPackageId = packageId;
        selectedPackageChanged();
    }

    public void reloadPackageList() {
        try {
            packageList = project.getManager().loadPackageList(project);
        } catch (ProjectDataException ex) {
            packageList = new ArrayList<>();

            String msg = "Load package list failed: ";
            log.error(msg, ex);
            FacesUtil.addError(msg + ex.getMessage());
        }
    }

    public void selectedPackageChanged() {
        log.debug("selectedPackageChanged: selectedPackageId={}", selectedPackageId);
        if (selectedPackageId < 0) {
            /*show something to user 'lets select package from the list' */
            pleaseSelectPackage = true;
            return;
        }
        pleaseSelectPackage = false;

        reloadPackage();

        /* TODO: need to call EditorController.setActive by javascript same as flowchart page.
            active-object = package-view,
            properties = changable-property of package-data */
    }

    public void reloadPackage() {
        log.trace("reloadPackage.");
        try {
            activePackage = project.getManager().loadPackage(selectedPackageId, project);
            if (activePackage.getComplete() != 100) {
                /*TODO: put javascript to call reloadPackage() on next 5 seconds until percent complete == 100 or has error occurred*/
            }
        } catch (ProjectDataException ex) {
            String msg = "Reload package " + selectedPackageId + " failed: ";
            log.error(msg, ex);
            FacesUtil.addError(msg + ex.getMessage());
        }
    }

    public void buildPackage() {
        log.trace("buildPackage.");
        if (project.getManager().buildPackage(workspace.getProject())) {
            reloadPackageList();
            selectPackage(packageList.size() - 1);
        }
        log.trace("buildPackage completed.");
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

    public List<Item> getPackageList() {
        return packageList;
    }

    public void setPackageList(List<Item> packageList) {
        this.packageList = packageList;
    }

    public int getSelectedPackageId() {
        return selectedPackageId;
    }

    public void setSelectedPackageId(int selectedPackageId) {
        this.selectedPackageId = selectedPackageId;
    }

    public Package getActivePackage() {
        return activePackage;
    }

    public void setActivePackage(Package activePackage) {
        this.activePackage = activePackage;
    }

    public boolean isPleaseSelectPackage() {
        return pleaseSelectPackage;
    }

    public void setPleaseSelectPackage(boolean pleaseSelectPackage) {
        this.pleaseSelectPackage = pleaseSelectPackage;
    }

}
