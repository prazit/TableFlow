package com.tflow.controller;

import com.tflow.model.editor.JavaScriptBuilder;
import com.tflow.model.editor.Selectable;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.Workspace;
import org.slf4j.Logger;
import com.tflow.system.Application;

import javax.inject.Inject;
import java.io.Serializable;

public class Controller implements Serializable {

    @Inject
    Workspace workspace;

    @Inject
    Logger log;

    @Inject
    Application application;

    JavaScriptBuilder jsBuilder = new JavaScriptBuilder();

    private boolean init = true;

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

}
