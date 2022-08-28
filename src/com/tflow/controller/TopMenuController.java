package com.tflow.controller;

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


    public void lightTheme() {
        workspace.getUser().setTheme(Theme.LIGHT);
        FacesUtil.redirect("/" + workspace.getCurrentPage().getName());
    }

    public void darkTheme() {
        workspace.getUser().setTheme(Theme.DARK);
        FacesUtil.redirect("/" + workspace.getCurrentPage().getName());
    }

}
