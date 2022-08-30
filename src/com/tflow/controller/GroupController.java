package com.tflow.controller;

import com.tflow.model.PageParameter;
import com.tflow.model.data.ProjectDataException;
import com.tflow.model.editor.Item;
import com.tflow.model.editor.ProjectGroup;
import com.tflow.model.editor.ProjectGroupList;
import org.primefaces.event.TabChangeEvent;

import javax.el.MethodExpression;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.List;

@ViewScoped
@Named("groupCtl")
public class GroupController extends Controller {

    private String name;
    private ProjectGroupList groupList;
    private String openSectionUpdate;

    @Override
    void onCreation() {
        log.trace("onCreation.");
    }

    @Override
    protected Page getPage() {
        return Page.GROUP;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void throwException() {
        throw new NullPointerException("Unexpected error occurred in the test section.");
    }

    public void throwException2() throws Exception {
        throw new Exception("Unknown error occurred.");
    }

    public void openProject(String projectId) {
        workspace.openPage(Page.EDITOR, new Parameter(PageParameter.PROJECT_ID, projectId));
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
        /*TODO: clear selected project list*/
        /*TODO: clear project list*/
        /*TODO: clear selected group*/

        /*TODO: load group list*/
        groupList = workspace.getProjectManager().loadGroupList(workspace);

        return GroupSection.EXISTING_PROJECT.getUpdate();
    }

    private String openTemplateSection() {
        return GroupSection.PROJECT_TEMPLATE.getUpdate();
    }

    public String getOpenSectionUpdate() {
        return openSectionUpdate;
    }
}
