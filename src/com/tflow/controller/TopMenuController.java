package com.tflow.controller;

import com.tflow.model.PageParameter;
import com.tflow.system.constant.Theme;
import com.tflow.util.FacesUtil;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

@ViewScoped
@Named("topMenuCtl")
public class TopMenuController extends Controller {

    @Override
    void onCreation() {
        /*nothing*/
    }

    @Override
    protected Page getPage() {
        return workspace.getCurrentPage();
    }

    public void newEmptyProject() {
        workspace.openPage(Page.EDITOR, new Parameter(PageParameter.GROUP_ID, "0"));
    }

    public void lightTheme() {
        workspace.getUser().setTheme(Theme.LIGHT);
        workspace.openPage(workspace.getCurrentPage());
    }

    public void darkTheme() {
        workspace.getUser().setTheme(Theme.DARK);
        workspace.openPage(workspace.getCurrentPage());
    }

}