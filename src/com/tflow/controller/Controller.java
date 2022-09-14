package com.tflow.controller;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.ChangePropertyValue;
import com.tflow.model.editor.cmd.CommandParamKey;
import com.tflow.model.editor.view.PropertyView;
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
import java.util.HashMap;
import java.util.Map;

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

    protected String propertyToMethod(String propertyName) {
        return "get" +
                propertyName.substring(0, 1).toUpperCase()
                + propertyName.substring(1);
    }

    protected Object getPropertyValue(Selectable selectable, String propertyName) {
        Object value = selectable.getPropertyMap().get(propertyName);
        if (value != null) return value;

        try {
            /*by getValue() method*/
            value = selectable.getClass().getMethod(propertyToMethod(propertyName)).invoke(selectable);
        } catch (Exception e) {
            /*by property.var*/
            value = getPropertyValue(selectable, selectable.getProperties().getPropertyView(propertyName));
        }

        return value == null ? "" : value;
    }

    protected Object getPropertyValue(Selectable selectable, PropertyView property) {
        Object value = null;
        if (property == null) {
            return value;
        }

        if (property.hasParent())
            /*by getParent().getValue() method, the parent always be the PropertyMap*/
            value = selectable.getPropertyMap().get(property.getVar());
        else
            try {
                /*by getValue() method without parent*/
                value = selectable.getClass().getMethod(propertyToMethod(property.getVar())).invoke(selectable);
            } catch (Exception e) {
                /*no property*/
                log.warn("getPropertyValue: no compatible method to get value from property({})", property);
                log.error("this is debug information", e);
            }

        return value == null ? "" : value;
    }

    public void propertyChanged(PropertyView property) {
        log.debug("propertyChanged:fromClient");
        Selectable activeObject = workspace.getProject().getActiveStep().getActiveObject();
        propertyChanged(activeObject.getProjectFileType(), activeObject, property);
    }

    protected void propertyChanged(ProjectFileType projectFileType, Object data, PropertyView property) {
        if (data instanceof Selectable) {
            property.setNewValue(getPropertyValue(((Selectable) data), property));
        }
        log.debug("propertyChanged(type:{}, data:{}, property:{})", projectFileType, data.getClass().getSimpleName(), property);

        Map<CommandParamKey, Object> paramMap = new HashMap<>();
        paramMap.put(CommandParamKey.WORKSPACE, workspace);
        paramMap.put(CommandParamKey.PROJECT_FILE_TYPE, projectFileType);
        paramMap.put(CommandParamKey.DATA, data);
        paramMap.put(CommandParamKey.PROPERTY, property);

        try {
            new ChangePropertyValue(paramMap).execute();
        } catch (Exception ex) {
            jsBuilder.pre(JavaScript.notiError, "Change property value failed by ChangePropertyValue command!");
            log.error("Change Property Value Failed!, type:{}, data:{}, property:{}", projectFileType, data.getClass().getSimpleName(), property);
            log.error("", ex);
        }
    }

}
