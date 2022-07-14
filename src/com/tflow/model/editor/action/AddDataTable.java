package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class AddDataTable extends Action {

    public AddDataTable(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Extract Data File";
        this.description = "Extract data file and then create data table to current step";
        this.code = "EDF";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.DATA_FILE,
                CommandParamKey.STEP
        );
        setCommands(new com.tflow.model.editor.cmd.AddDataTable());
    }

    @Override
    protected void initUndoCommands() {
        setUndoParams(
                CommandParamKey.DATA_TABLE,
                CommandParamKey.STEP
        );
        setUndoCommands(new com.tflow.model.editor.cmd.RemoveDataTable());
    }
}
