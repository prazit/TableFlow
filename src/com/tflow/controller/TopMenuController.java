package com.tflow.controller;

import com.tflow.model.PageParameter;
import com.tflow.system.Environment;
import com.tflow.system.constant.Theme;
import com.tflow.util.FacesUtil;

import javax.faces.event.ActionListener;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

@ViewScoped
@Named("topMenuCtl")
public class TopMenuController extends Controller {

    private boolean inDevelopment;

    @Override
    protected Page getPage() {
        return workspace.getCurrentPage();
    }

    public boolean isInDevelopment() {
        return inDevelopment;
    }

    @Override
    void onCreation() {
        Environment currentEnvironment = workspace.getEnvironment();
        inDevelopment = Environment.DEVELOPMENT == currentEnvironment;
    }

    public void openPlayground(int sectionIndex) {
        workspace.openPage(Page.PLAYGROUND, new Parameter(PageParameter.SECTION_INDEX, String.valueOf(sectionIndex)));
    }

    public void newEmptyProject() {
        workspace.openPage(Page.GROUP, new Parameter(PageParameter.SECTION_INDEX, "1"));
    }

    public void openProject() {
        workspace.openPage(Page.GROUP);
    }

    public void lightTheme() {
        workspace.getUser().setTheme(Theme.LIGHT);
        workspace.openPage(workspace.getCurrentPage());
    }

    public void darkTheme() {
        workspace.getUser().setTheme(Theme.DARK);
        workspace.openPage(workspace.getCurrentPage());
    }

    public void dummy() {
        log.debug("dummy called");
    }
}
