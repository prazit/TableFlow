package com.tflow.controller;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.IDPrefix;
import com.tflow.model.data.IssueType;
import com.tflow.model.data.ProjectDataException;
import com.tflow.model.data.PropertyVar;
import com.tflow.model.editor.Package;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.*;
import com.tflow.model.editor.cmd.CommandParamKey;
import com.tflow.model.editor.datasource.DataSource;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.datasource.SFTP;
import com.tflow.model.editor.view.PropertyView;
import com.tflow.model.editor.view.UploadedFileView;
import com.tflow.model.editor.view.VersionedFile;
import com.tflow.util.DateTimeUtil;
import org.apache.tika.Tika;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

@ViewScoped
@Named("projectCtl")
public class ProjectController extends Controller {

    private Project project;

    private List<Database> databaseList;
    private List<Local> localList;
    private List<SFTP> sftpList;

    private List<Variable> variableList;
    private List<Variable> systemVariableList;
    private List<UploadedFileView> uploadedList;
    private List<VersionedFile> versionedList;

    private List<Item> packageList;
    private int selectedPackageId;
    private Package activePackage;
    private boolean pleaseSelectPackage;
    private boolean verified;
    private boolean verifying;
    private boolean building;
    private Date startDate;

    @Override
    public Page getPage() {
        return Page.EDITOR;
    }

    @Override
    public void onCreation() {
        log.debug("onCreation.");
        project = workspace.getProject();
        verified = false;
        verifying = false;
        createEventHandlers();
    }

    private void createEventHandlers() {
        project.getEventManager().addHandler(EventName.NAME_CHANGED, new EventHandler() {
            @Override
            public void handle(Event event) {
                PropertyView property = (PropertyView) event.getData();

                Project target = (Project) event.getTarget();
                String projectId = target.getId();
                ProjectGroup group;
                try {
                    group = target.getManager().loadProjectGroup(workspace, target.getGroupId());
                    ProjectItem targetProjectItem = group.getProjectList().stream().filter(projectItem -> projectItem.getId().compareTo(projectId) == 0).collect(Collectors.toList()).get(0);
                    targetProjectItem.setName(target.getName());
                } catch (ProjectDataException ex) {
                    String msg = "Project Name '" + target.getName() + "' is changed, but the name in group still unchanged by Internal Error!";
                    jsBuilder.pre(JavaScript.notiError, msg);
                    log.error(msg + ex.getMessage());
                    log.trace("", ex);
                    return;
                }

                propertyChanged(ProjectFileType.GROUP, group, property);
            }
        });
    }

    private void createPackageEventHandlers() {
        if (activePackage == null) return;

        activePackage.getEventManager()
                .removeHandlers(EventName.NAME_CHANGED)
                .addHandler(EventName.NAME_CHANGED, new EventHandler() {
                    @Override
                    public void handle(Event event) {
                        PropertyView property = (PropertyView) event.getData();
                        Package target = (Package) event.getTarget();
                        packageList.stream().filter(item -> item.getId() == target.getId()).collect(Collectors.toList()).get(0).setName(target.getName());
                        propertyChanged(ProjectFileType.PACKAGE_LIST, packageList, property);
                    }
                });

    }

    public void openSection(TabChangeEvent event) {
        String title = event.getTab().getTitle();
        ProjectSection section = ProjectSection.parse(title);
        if (section == null) {
            String message = "Unknown section with title: {}";
            jsBuilder.pre(JavaScript.notiError, message, title);
            log.error(message, title);
            return;
        }

        switch (section) {
            case UPLOADED:
                openUploaded();
                break;
            case PACKAGE:
                openPackage();
                break;
            case VERSIONED:
                openVersioned();
                break;
            case VARIABLE:
                openVariable();
                break;
            /*case DATA_SOURCE:
                break;
            */
        }
    }

    private void openVariable() {
        log.debug("openVariable.");
        if (variableList != null) return;

        Map<String, Variable> variableMap = project.getVariableMap();
        log.debug("openVariable: variableMap = {}", variableMap);

        variableList = new ArrayList<>(variableMap.values());
        variableList.sort(Comparator.comparing(Variable::getIndex));

        systemVariableList = variableList.stream().filter(item -> VariableType.SYSTEM == item.getType()).collect(Collectors.toList());
        variableList = variableList.stream().filter(item -> VariableType.USER == item.getType()).collect(Collectors.toList());

        createVariableEventHandlers();
    }

    private void createVariableEventHandlers() {
        for (Variable variable : variableList) {
            if (variable.getType() == VariableType.SYSTEM) continue;

            variable.getEventManager()
                    .removeHandlers(EventName.NAME_CHANGED)
                    .addHandler(EventName.NAME_CHANGED, new EventHandler() {
                        @Override
                        public void handle(Event event) throws Exception {
                            PropertyView property = (PropertyView) event.getData();
                            Variable target = (Variable) event.getTarget();
                            String oldName = (String) property.getOldValue();
                            String newName = target.getName();

                            /*cancel change when new name is duplicate*/
                            Map<String, Variable> variableMap = project.getVariableMap();
                            if (variableMap.containsKey(newName)) {
                                log.debug("variable({}) NAME_CHANGED is cancelled by duplicated name '{}'", target.getSelectableId(), newName);
                                target.setName(oldName);
                                jsBuilder.pre(JavaScript.focusProperty, 100, property.getVar()).runOnClient();
                                throw new Exception("Duplicate Variable Name '" + newName + "'");
                            }

                            /*update view only: variable map is needed by Dynamic Value Expression*/
                            log.debug("variable({}) NAME_CHANGED from '{}' to '{}'", target.getSelectableId(), oldName, newName);
                            variableMap.remove(oldName);
                            variableMap.put(target.getName(), target);
                        }
                    });
        }
    }

    private void openVersioned() {
        log.debug("openVersioned.");
        if (versionedList != null) return;

        try {
            versionedList = project.getManager().loadVersionedList(project);
        } catch (ProjectDataException ex) {
            String message = "Load Library list failed! " + ex.getMessage();
            jsBuilder.pre(JavaScript.notiError, message);
            log.error(message);
            log.trace("", ex);
            return;
        }

        /*need selectable and eventHandler*/
        Map<String, Selectable> selectableMap = project.getActiveStep().getSelectableMap();
        for (VersionedFile versionedFile : versionedList) {
            selectableMap.put(versionedFile.getSelectableId(), versionedFile);
            createVersionedFileEventHandlers(versionedFile);
        }
    }

    private void createVersionedFileEventHandlers(VersionedFile versionedFile) {
        versionedFile.getEventManager().removeHandlers(EventName.NAME_CHANGED)
                .addHandler(EventName.NAME_CHANGED, new EventHandler() {
                    @Override
                    public void handle(Event event) {
                        PropertyView property = (PropertyView) event.getData();
                        VersionedFile target = (VersionedFile) event.getTarget();
                        target.setUploadedDate(DateTimeUtil.now());
                        //versionedList.stream().filter(item -> item.getId() == target.getId()).collect(Collectors.toList()).get(0).setName(target.getName());
                        propertyChanged(ProjectFileType.VERSIONED_LIST, versionedList, property);
                    }
                });
    }

    private void openUploaded() {
        log.debug("openUploaded.");
        if (uploadedList != null) return;

        try {
            uploadedList = project.getManager().loadUploadedList(project);
        } catch (ProjectDataException ex) {
            String message = "Load uploaded list failed! " + ex.getMessage();
            jsBuilder.pre(JavaScript.notiError, message);
            log.error(message);
            log.trace("", ex);
        }
    }

    public void openPackage() {
        log.debug("openPackage.");
        reloadPackageList();
        selectPackage(0);
    }

    public void selectPackage(int packageListIndex) {
        if (packageListIndex < 0) {
            selectedPackageId = -1;
        } else if (packageListIndex >= packageList.size()) {
            selectedPackageId = packageList.size() - 1;
        } else {
            selectedPackageId = packageList.get(packageListIndex).getId();
        }
        selectedPackageChanged();
    }

    public void reloadPackageList() {
        try {
            packageList = project.getManager().loadPackageList(project);
            packageList.sort((s, t) -> s.getId() == 1 ? 1 : Integer.compare(t.getId(), s.getId()));
        } catch (ProjectDataException ex) {
            packageList = new ArrayList<>();

            String msg = "Load package list failed: ";
            log.error(msg + ex.getMessage());
            log.trace("", ex);
            jsBuilder.pre(JavaScript.notiError, msg + ex.getMessage());
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

        jsBuilder.post(JavaScript.selectObject, activePackage.getSelectableId()).runOnClient();
    }

    private void reloadPackage() {
        log.debug("reloadPackage.");
        try {
            ProjectManager manager = project.getManager();
            activePackage = manager.loadPackage(selectedPackageId, project);
            createPackageEventHandlers();
            manager.addSeletable(activePackage, project);
        } catch (ProjectDataException ex) {
            String msg = "Reload package " + selectedPackageId + " failed: ";
            log.error(msg + ex.getMessage());
            log.trace("", ex);
            jsBuilder.pre(JavaScript.notiError, msg + ex.getMessage());
        }
    }

    public StreamedContent downloadPackage() {
        log.debug("downloadPackage.");

        BinaryFile binaryFile = null;
        try {
            binaryFile = project.getManager().loadPackaged(activePackage.getId(), workspace.getProject());
        } catch (Exception ex) {
            String msg = "Download Error: {}";
            jsBuilder.pre(JavaScript.notiError, msg, ex.getMessage());
            log.error(msg, ex.getMessage());
            return null;
        }

        if (binaryFile == null) {
            String msg = "Unexpected Error Occurred, try to download-package few minutes later";
            jsBuilder.pre(JavaScript.notiError, msg);
            log.error(msg);
            return null;
        }

        Tika tika = new Tika();
        byte[] content = binaryFile.getContent();
        String mimeType = tika.detect(content);
        StreamedContent streamedContent = DefaultStreamedContent.builder()
                .contentType(mimeType)
                .contentLength(content.length)
                .name(binaryFile.getName())
                .stream(() -> new ByteArrayInputStream(content))
                .build();

        log.debug("mimetype:{}, binaryFile:{}", mimeType, binaryFile);
        return streamedContent;
    }

    public void buildPackage() {
        log.debug("buildPackage.");
        building = true;

        Package rebuild;
        if (activePackage == null || activePackage.isLock()) {
            /*build new package*/
            rebuild = null;
        } else {
            /*rebuild selected package*/
            rebuild = activePackage;
            activePackage.getEventManager().removeHandlers(EventName.NAME_CHANGED);
            packageList.remove(getPackageListIndex(activePackage.getId()));
        }

        Package buildPackage = project.getManager().buildPackage(workspace.getProject(), rebuild);
        if (buildPackage == null) {
            String msg = "Unexpected Error Occurred, try to build-package few minutes later";
            jsBuilder.pre(JavaScript.notiError, msg);
            log.error(msg);
            building = false;
            return;
        }

        activePackage = buildPackage;
        createPackageEventHandlers();

        pleaseSelectPackage = false;
        /*jsBuilder.post(JavaScript.updateEmByClass, "package-panel").runOnClient();*/
    }

    public void verifyProject() {
        /*show progressbar like building*/
        startDate = DateTimeUtil.now();
        Issues issues = project.getIssues();
        issues.setFinished(false);
        issues.setComplete(0);
        issues.setStartDate(startDate);
        verifying = true;

        if (!project.getManager().verifyProject(project)) {
            jsBuilder.pre(JavaScript.notiError, "Can't verify project, try again few minutes later.");
            log.error("verifyProject return false, project is not verified!");
        }
    }

    public synchronized int refreshIssueList() {
        if (!verifying) {
            log.debug("refreshIssueList: called before start verifying will ignored");
            return 0;
        }

        Issues issues = project.getIssues();
        try {
            Issues newIssues = project.getManager().loadIssues(project);
            if (newIssues != null && ((issues.getStartDate() == null) || newIssues.getStartDate().compareTo(issues.getStartDate()) >= 0)) {
                /*new issues only, will ignore for null and older issues*/
                setIssueDisplay(newIssues);
                project.setIssues(newIssues);
                issues = newIssues;
            }
        } catch (ProjectDataException ex) {
            log.error("refreshIssueList: error during verify process! {}", ex.getMessage());
            verifying = false;
            return 100;
        }

        int complete = issues.getComplete();
        if (issues.isFinished()) {
            complete = 100;
            verifying = false;
            verified = issues.getIssueList().size() == 0;
            jsBuilder.post(JavaScript.notiInfo, "Verify Project Completed");
        }

        log.debug("refreshIssueList: complete: {}", complete);
        return complete;
    }

    public synchronized Integer refreshBuildingPackage() {
        if (!building) {
            log.debug("refreshBuildingPackage: called before start build-package will ignored");
            return 0;
        }

        reloadPackageList();
        selectPackage(0);

        int complete = activePackage.getComplete();
        if (activePackage.isFinished()) {
            building = false;
            complete = 100;
            jsBuilder.post(JavaScript.notiInfo, "Package Creation Completed");
        }
        return complete;
    }

    private void setIssueDisplay(Issues issues) {
        String stepName;
        String objectType;
        String objectName;
        String propertyLabel;
        String propertyRange;
        String display;
        Step step;
        for (Issue issue : issues.getIssueList()) {

            /*step name*/
            if (issue.getStepId() <= 0) {
                step = project.getStepList().get(-1);
                stepName = null;
            } else {
                step = findStep(issue.getStepId());
                if (step.getIndex() < 0) loadStep(project.getStepList().indexOf(step));
                stepName = "step:" + step.getName();
            }

            /*get object name from issue.objectType*/
            String selectableId = IDPrefix.valueOf(issue.getObjectType().name()).getPrefix() + issue.getObjectId();
            Selectable selectableObject = step.getSelectableMap().get(selectableId);
            log.debug("selectableId:{}, selectableObject:{}, step:{}", selectableId, selectableObject, step.getName());
            if (selectableObject == null) {
                objectName = "unknown";
                propertyLabel = issue.getPropertyVar();
            } else {
                objectName = (String) selectableObject.getProperties().getPropertyValue(selectableObject, "name", log);

                /*property label*/
                PropertyView property = selectableObject.getProperties().getPropertyView(issue.getPropertyVar());
                propertyLabel = property.getLabel();
            }

            /*get object type from issue.objectType*/
            objectType = issue.getObjectType().name().toLowerCase().replace("_", "-");

            /*TODO: need to improve message for types of child of table [OUTPUT,COLUMN,TRANSFORMATION]*/

            /*message pattern from IssueType*/
            if (IssueType.REQUIRED == issue.getType()) {
                display = "Required value for " + (propertyLabel == null ? issue.getPropertyVar() : propertyLabel) + " of " + objectType + ":" + objectName + (stepName == null ? "" : " in " + stepName);
            } else {/*IssueType.OUT_OF_RANGE*/
                propertyRange = "";
                display = "Value out of range(" + propertyRange + ") for " + propertyLabel + " of " + objectType + ":" + objectName + (stepName == null ? "" : " in " + stepName);
            }
            issue.setDisplay(display);
        }
    }

    private void loadStep(int stepIndex) {
        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.PROJECT, project);
        paramMap.put(CommandParamKey.INDEX, stepIndex);

        try {
            new LoadStep(paramMap).execute();
        } catch (RequiredParamException ex) {
            jsBuilder.pre(JavaScript.notiError, "Load Step Failed {}", ex.getMessage());
            log.error("Load Step Failed! {}", ex.getMessage());
        }

    }

    private Step findStep(int stepId) {
        for (Step step : project.getStepList()) {
            if (step.getId() == stepId) {
                return step;
            }
        }
        return null;
    }

    public void lockPackage() {
        activePackage.setLock(!activePackage.isLock());
        propertyChanged(ProjectFileType.PACKAGE, activePackage, activePackage.getProperties().getPropertyView(PropertyVar.lock.name()));
    }

    private int getPackageListIndex(int packageId) {
        Item item;
        for (int pIndex = 0; pIndex < packageList.size(); pIndex++) {
            item = packageList.get(pIndex);
            if (item.getId() == packageId) {
                return pIndex;
            }
        }
        return -1;
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

    public boolean isLastPackage() {
        if (packageList == null || activePackage == null) return false;
        return packageList.get(0).getId() == activePackage.getId();
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

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isVerifying() {
        return verifying;
    }

    public void setVerifying(boolean verifying) {
        this.verifying = verifying;
    }

    public boolean isBuilding() {
        return building;
    }

    public void setBuilding(boolean building) {
        this.building = building;
    }

    public List<UploadedFileView> getUploadedList() {
        return uploadedList;
    }

    public void setUploadedList(List<UploadedFileView> uploadedList) {
        this.uploadedList = uploadedList;
    }

    public List<VersionedFile> getVersionedList() {
        return versionedList;
    }

    public void setVersionedList(List<VersionedFile> versionedList) {
        this.versionedList = versionedList;
    }

    public List<Variable> getVariableList() {
        return variableList;
    }

    public List<Variable> getSystemVariableList() {
        return systemVariableList;
    }

    public void addVariable() {
        HashMap<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.PROJECT, project);

        Action action = null;
        try {
            action = new AddVariable(paramMap);
            action.execute();
        } catch (Exception ex) {
            jsBuilder.pre(JavaScript.notiError, "Add variable failed by internal command!");
            log.error("Add variable Failed!, {}", ex.getMessage());
            log.trace("", ex);
            return;
        }

        Variable newVariable = (Variable) action.getResultMap().get(ActionResultKey.VARIABLE);
        variableList.add(newVariable);

        jsBuilder.pre(JavaScript.selectObject, newVariable.getSelectableId()).runOnClient();
    }
}
