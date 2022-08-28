package com.tflow.controller;

import com.tflow.util.FacesUtil;

import javax.faces.event.ActionListener;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

@ViewScoped
@Named("unexpectedCtl")
public class UnexpectedExceptionController extends Controller {

    @Override
    void onCreation() {
        /*nothing*/
    }

    @Override
    protected Page getPage() {
        return workspace.getCurrentPage();
    }

    public void noException() {
        log.warn("Open Unexpected Exception Page without Exception.");
        FacesUtil.redirect("/" + Page.GROUP.getName());
    }
}
