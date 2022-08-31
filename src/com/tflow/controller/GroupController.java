package com.tflow.controller;

import com.tflow.model.PageParameter;
import com.tflow.model.data.IDPrefix;
import com.tflow.model.data.ProjectDataException;
import com.tflow.model.editor.*;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;

import javax.el.MethodExpression;
import javax.faces.event.ActionListener;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@ViewScoped
@Named("groupCtl")
public class GroupController extends Controller {

    private String name;

    private ProjectGroupList groupList;
    private GroupItem selectedGroup;

    private ProjectGroup projectList;
    private ProjectItem selectedProject;

    private String openSectionUpdate;

    @Override
    void onCreation() {
        log.trace("onCreation.");
        selectedProject = null;
        selectedGroup = null;
        projectList = new ProjectGroup();
        projectList.setProjectList(new ArrayList<>());
        groupList = new ProjectGroupList();
        groupList.setGroupList(new ArrayList<>());
    }

    @Override
    protected Page getPage() {
        return Page.GROUP;
    }

    public ProjectGroupList getGroupList() {
        return groupList;
    }

    public ProjectGroup getProjectList() {
        return projectList;
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

    public void throwException() {
        throw new NullPointerException("Unexpected error occurred in the test section.");
    }

    public void throwException2() throws Exception {
        throw new Exception("Unknown error occurred.");
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

        groupList = workspace.getProjectManager().loadGroupList(workspace);

        return GroupSection.EXISTING_PROJECT.getUpdate();
    }

    private void projectGroupSelected(SelectEvent event) throws ProjectDataException {
        if (selectedGroup.getId() == projectList.getId()) {
            log.debug("projectGroupSelected: on the same group ({}).", selectedGroup.getId());
            return;
        }

        log.debug("projectGroupSelected: event = {}", event);
        log.debug("projectGroupSelected: selectedGroup = {}", selectedGroup);

        selectedProject = null;
        projectList = workspace.getProjectManager().loadProjectGroup(workspace, selectedGroup.getId());
    }

    private String openTemplateSection() {
        return GroupSection.PROJECT_TEMPLATE.getUpdate();
    }

    public String getOpenSectionUpdate() {
        return openSectionUpdate;
    }
}
