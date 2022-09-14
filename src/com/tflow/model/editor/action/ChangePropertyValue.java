package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class ChangePropertyValue extends Action {

    public ChangePropertyValue(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Change Property Value";
        this.description = "change property value to active object";
        this.code = "CPV";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.WORKSPACE,
                CommandParamKey.PROJECT_FILE_TYPE,
                CommandParamKey.DATA,
                CommandParamKey.PROPERTY
        );
        setCommands(
                new com.tflow.model.editor.cmd.ChangePropertyValue()
        );
    }

    @Override
    protected void initUndoCommands() {
        setUndoParams(
                CommandParamKey.WORKSPACE,
                CommandParamKey.PROJECT_FILE_TYPE,
                CommandParamKey.DATA,
                CommandParamKey.PROPERTY
        );
        /*setUndoCommands(
                new com.tflow.model.editor.cmd.ChangePropertyValue()
        );*/
    }
}
