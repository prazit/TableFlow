package com.tflow.controller;

import com.tflow.kafka.EnvironmentConfigs;
import com.tflow.model.PageParameter;
import com.tflow.model.data.IDPrefix;
import com.tflow.model.data.ProjectDataException;
import com.tflow.model.editor.*;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.UnselectEvent;

import javax.el.MethodExpression;
import javax.faces.event.ActionListener;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ViewScoped
@Named("groupCtl")
public class GroupController extends Controller {

    private int sectionIndex;

    private ProjectGroupList groupList;
    private GroupItem selectedGroup;

    private ProjectGroup projectList;
    private ProjectItem selectedProject;

    private ProjectGroup templateList;
    private ProjectItem selectedTemplate;

    private String openSectionUpdate;

    private int templateGroupId;
    private int selectedGroupId;

    @Override
    protected Page getPage() {
        return Page.GROUP;
    }

    @Override
    void onCreation() {
        log.trace("onCreation.");

        /*Open Group Cases.
         * 1. hasParameter(SectionIndex): switch to SECTION by index
         * 2. noParameter: Normal Working user click refresh button
         **/
        Map<PageParameter, String> parameterMap = workspace.getParameterMap();
        String sectionIndexString = parameterMap.get(PageParameter.SECTION_INDEX);
        if (sectionIndexString != null) {
            /*case 1.*/
            log.info("Open-Page:Group: switch to section({})", sectionIndexString);
            sectionIndex = Integer.parseInt(sectionIndexString);
        } else if (templateList != null) {
            /*case 2.*/
            return;
        }

        log.info("Open-Page:Group: first time");
        sectionIndex = 0;
        selectedProject = null;
        selectedGroup = null;
        groupList = null;

        templateGroupId = EnvironmentConfigs.valueOf(workspace.getEnvironment().name()).getTemplateGroupId();
        templateList = new ProjectGroup();
        templateList.setId(-1);
        templateList.setProjectList(new ArrayList<>());
        selectedGroupId = -1;

        try {
            openProjectSection();
        } catch (ProjectDataException ex) {
            log.error("onCreation.openProjectSection", ex);
        }

    }

    public int getSectionIndex() {
        return sectionIndex;
    }

    public void setSectionIndex(int sectionIndex) {
        this.sectionIndex = sectionIndex;
    }

    public ProjectGroupList getGroupList() {
        return groupList;
    }

    public void setGroupList(ProjectGroupList groupList) {
        this.groupList = groupList;
    }

    public ProjectGroup getProjectList() {
        return projectList;
    }

    public void setProjectList(ProjectGroup projectList) {
        this.projectList = projectList;
    }

    public ProjectGroup getTemplateList() {
        return templateList;
    }

    public void setTemplateList(ProjectGroup templateList) {
        this.templateList = templateList;
    }

    public GroupItem getSelectedGroup() {
        return selectedGroup;
    }

    public void setSelectedGroup(GroupItem selectedGroup) {
        this.selectedGroup = selectedGroup;
    }

    public ProjectItem getSelectedProject() {
        return selectedProject;
    }

    public void setSelectedProject(ProjectItem selectedProject) {
        this.selectedProject = selectedProject;
    }

    public ProjectItem getSelectedTemplate() {
        return selectedTemplate;
    }

    public void setSelectedTemplate(ProjectItem selectedTemplate) {
        this.selectedTemplate = selectedTemplate;
    }

    public int getSelectedGroupId() {
        return selectedGroupId;
    }

    public void setSelectedGroupId(int selectedGroupId) {
        this.selectedGroupId = selectedGroupId;
    }

    public String getOpenSectionUpdate() {
        return openSectionUpdate;
    }

    public void setOpenSectionUpdate(String openSectionUpdate) {
        this.openSectionUpdate = openSectionUpdate;
    }

    public void openSelectedProject() {
        openProject(selectedProject.getId());
    }

    public void openProject(String projectId) {
        workspace.openPage(Page.EDITOR, new Parameter(PageParameter.PROJECT_ID, projectId));
    }

    public void cloneSelectedProject() {
        cloneProject(selectedGroup.getId(), selectedProject.getId());
    }

    public void cloneProject(int groupId, String projectId) {
        workspace.openPage(Page.EDITOR,
                new Parameter(PageParameter.GROUP_ID, String.valueOf(groupId)),
                new Parameter(PageParameter.PROJECT_ID, IDPrefix.TEMPLATE.getPrefix() + projectId));
    }

    public void openSection(TabChangeEvent event) throws ProjectDataException {
        String title = event.getTab().getTitle();
        log.debug("openSection: selectedTitle={}, event={}", title, event);
        if (title.compareTo(GroupSection.EXISTING_PROJECT.getTitle()) == 0) {
            openSectionUpdate = openProjectSection();
        } else if (title.compareTo(GroupSection.PROJECT_TEMPLATE.getTitle()) == 0) {
            openSectionUpdate = openTemplateSection();
        } else {
            openSectionUpdate = "";
            log.warn("openSection() with Unknown Section:{}", title);
        }
    }

    private String openProjectSection() throws ProjectDataException {
        if (groupList != null) {
            log.trace("openProjectSection Again.");
            return "";
        }

        log.trace("openProjectSection.loadGroupList");
        groupList = workspace.getProjectManager().loadGroupList(workspace);

        selectedGroup = null;
        selectedProject = null;
        projectList = new ProjectGroup();
        projectList.setId(-1);
        projectList.setProjectList(new ArrayList<>());

        return GroupSection.EXISTING_PROJECT.getUpdate();
    }

    public void onGroupSelect(SelectEvent event) throws ProjectDataException {
        log.debug("onGroupSelect: selectedGroup = {}", selectedGroup);
        if (selectedGroup.getId() == projectList.getId()) {
            log.debug("projectGroupSelected: on the same group ({}).", selectedGroup.getId());
            return;
        }

        log.debug("projectGroupSelected: selectedGroup = {}", selectedGroup);

        projectList = workspace.getProjectManager().loadProjectGroup(workspace, selectedGroup.getId());
        selectedProject = null;
    }

    private String openTemplateSection() throws ProjectDataException {
        if (templateList != null) {
            log.trace("openTemplateSection Again.");
            return "";
        }

        templateList = workspace.getProjectManager().loadProjectGroup(workspace, templateGroupId);
        selectedTemplate = null;

        return GroupSection.PROJECT_TEMPLATE.getUpdate();
    }

    public void newProject() {
        workspace.openPage(Page.EDITOR, new Parameter(PageParameter.GROUP_ID, String.valueOf(selectedGroupId)));
    }

}
