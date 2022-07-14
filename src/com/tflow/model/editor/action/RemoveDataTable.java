package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class RemoveDataTable extends Action {

    public RemoveDataTable(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Remove Data Table";
        this.description = "Remove data table from current step";
        this.code = "RDT";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.DATA_TABLE,
                CommandParamKey.STEP
        );
        setCommands(new com.tflow.model.editor.cmd.RemoveDataTable());
    }

    @Override
    protected void initUndoCommands() {
        setUndoParams(
                CommandParamKey.DATA_FILE,
                CommandParamKey.STEP
        );
        setUndoCommands(new com.tflow.model.editor.cmd.AddDataTable());
    }
}
