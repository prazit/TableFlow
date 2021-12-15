package com.tflow.controller;

import com.tflow.model.editor.Workspace;
import com.tflow.system.Application;
import com.tflow.system.constant.Theme;
import com.tflow.util.FacesUtil;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

@ViewScoped
@Named("editorCtl")
public class EditorController extends BaseController {

    @Inject
    private Workspace workspace;

    @PostConstruct
    public void onCreation() {

    }

    public void lightTheme() {
        workspace.getUser().setTheme(Theme.LIGHT);
        FacesUtil.redirect("/editor.xhtml");
    }

    public void darkTheme() {
        workspace.getUser().setTheme(Theme.DARK);
        FacesUtil.redirect("/editor.xhtml");
    }

}
