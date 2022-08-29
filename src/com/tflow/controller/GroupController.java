package com.tflow.controller;

import com.tflow.model.PageParameter;
import com.tflow.model.editor.Project;
import com.tflow.util.FacesUtil;

import javax.faces.event.ActionListener;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.Map;

@ViewScoped
@Named("groupCtl")
public class GroupController extends Controller {

    private String name;

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

    public void openProject80() {
        workspace.openPage(Page.EDITOR, new Parameter(PageParameter.PROJECT_ID, "P80"));
    }
}
