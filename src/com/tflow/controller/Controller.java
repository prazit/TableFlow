package com.tflow.controller;

import com.tflow.model.editor.JavaScriptBuilder;
import com.tflow.model.editor.Selectable;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.Workspace;
import com.tflow.util.DateTimeUtil;
import com.tflow.util.FacesUtil;
import org.slf4j.Logger;
import com.tflow.system.Application;

import javax.annotation.PostConstruct;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.inject.Inject;
import java.awt.print.Pageable;
import java.io.Serializable;
import java.util.Date;

public abstract class Controller implements Serializable {

    @Inject
    Workspace workspace;

    @Inject
    Logger log;

    @Inject
    Application application;

    JavaScriptBuilder jsBuilder;

    private boolean init = true;

    private Date timestamp;

    abstract void onCreation();

    @PostConstruct
    public void postConstruct() {
        timestamp = DateTimeUtil.now();
        workspace.setCurrentPage(getPage());
        jsBuilder = workspace.getJavaScriptBuilder();

        /*TODO: need to parse parameters from URL-GET into the same parameterMap*/

        onCreation();
    }

    public Date getTimestamp() {
        return timestamp;
    }

    protected abstract Page getPage();

    public Page getCurrentPage() {
        return workspace.getCurrentPage();
    }

    public String getForceReloadResources() {
        if (init) {
            init = false;
            application.refreshForceReloadResources();
        }
        return application.getForceReloadResources();
    }

    public Step getStep() {
        return workspace.getProject().getActiveStep();
    }

    /**
     * Get active class for css.
     *
     * @return " active" or empty string
     */
    public String active(Selectable selectable) {
        Step step = getStep();
        Selectable activeObject = step.getActiveObject();
        if (activeObject == null) return "";

        String selectableId = selectable.getSelectableId();
        String activeSelectableId = activeObject.getSelectableId();

        return selectableId.compareTo(activeSelectableId) == 0 ? " active" : "";
    }

    public String getFormattedStackTrace(Exception exception, String filter) {
        String formattedStackTrace = FacesUtil.getFormattedStackTrace(exception, filter, "<br/>");
        if (formattedStackTrace.isEmpty()) {
            formattedStackTrace = FacesUtil.getFormattedStackTrace(exception, ".", "<br/>");
        }
        return formattedStackTrace;
    }

    public void showNoti() {
        jsBuilder.runNoti();
    }

}
