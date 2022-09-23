package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class AddUploaded extends Action {

    public AddUploaded(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Add Uploaded File";
        this.description = "add uploaded file to the project";
        this.code = "AUP";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.WORKSPACE,
                CommandParamKey.BINARY_FILE,
                CommandParamKey.PROPERTY,
                CommandParamKey.SELECTABLE
        );
        setCommands(
                new com.tflow.model.editor.cmd.AddUploaded(),
                new com.tflow.model.editor.cmd.ChangePropertyValue()
                /*TODO: new com.tflow.model.editor.cmd.UpdateDataTable() // do this after AddDataTable already extract real data from file*/
        );
    }

    @Override
    protected void initUndoCommands() {
        setUndoParams(
                CommandParamKey.WORKSPACE,
                CommandParamKey.BINARY_FILE,
                CommandParamKey.PROPERTY,
                CommandParamKey.SELECTABLE
        );
        /*setUndoCommands(
                new com.tflow.model.editor.cmd.ChangePropertyValue()
        );*/
    }
}
