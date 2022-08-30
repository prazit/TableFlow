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

    @Override
    protected Page getPage() {
        return Page.EDITOR;
    }

    @Override
    public void onCreation() {
        log.trace("onCreation.");
        project = workspace.getProject();
        createPackageEventHandlers();
    }

    /**
     * TODO: Test me now!!!
     */
    private void createPackageEventHandlers() {
        if (activePackage == null) return;

        activePackage.getEventManager()
                .removeHandlers(EventName.NAME_CHANGED)
                .addHandler(EventName.NAME_CHANGED, new EventHandler() {
                    @Override
                    public void handle(Event event) {
                        log.debug("package.NAME_CHANGED: event = {}", event);
                        /*TODO: package name on the dropdown list need to update to new value from the event/or do this in the Property-Changed-Action-Command*/
                    }
                });
    }

    public void openSection(TabChangeEvent event) {
        String title = event.getTab().getTitle();
        //log.debug("openSection: selectedTitle={}, event={}", title, event);
        if (title.compareTo(ProjectSection.PACKAGE.getTitle()) == 0) {
            openPackage();
        }
    }

    public void openPackage() {
        log.trace("openPackage.");
        reloadPackageList();
        selectPackage(packageList.size() - 1);
    }

    public void selectPackage(int packageListIndex) {
        if (packageListIndex < 0) {
            selectedPackageId = -1;
        } else {
            selectedPackageId = packageList.get(packageListIndex).getId();
        }
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

    private void reloadPackage() {
        log.trace("reloadPackage.");
        try {
            ProjectManager manager = project.getManager();
            activePackage = manager.loadPackage(selectedPackageId, project);
            manager.addSeletable(activePackage, project);
        } catch (ProjectDataException ex) {
            String msg = "Reload package " + selectedPackageId + " failed: ";
            log.error(msg, ex);
            FacesUtil.addError(msg + ex.getMessage());
        }
    }

    public void buildPackage() {
        log.trace("buildPackage.");
        Package buildPackage = project.getManager().buildPackage(workspace.getProject());
        if (buildPackage == null) {
            String msg = "Unexpected Error Occurred, try to build-package few minutes later";
            FacesUtil.addError(msg);
            log.error(msg);
            return;
        }

        activePackage = buildPackage;
        pleaseSelectPackage = false;
        jsBuilder.post(JavaScript.updateEm, "PackageTab").runOnClient();
    }

    /**
     * IMPORTANT: call this function at least 2 seconds after buildPackage.
     */
    public void refreshBuildingPackage() {
        if (activePackage == null || activePackage.getId() < 0) {
            /*case: mockup package for building process*/
            int countBefore = packageList == null ? 0 : packageList.size();
            log.debug("refreshBuildingPackage: countBefore = {}", countBefore);

            reloadPackageList();
            int countAfter = packageList == null ? 0 : packageList.size();
            log.debug("refreshBuildingPackage: countAfter = {}", countAfter);
            if (countBefore < countAfter) {
                selectPackage(countBefore);
            }

        } else {
            openPackage();
        }
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
