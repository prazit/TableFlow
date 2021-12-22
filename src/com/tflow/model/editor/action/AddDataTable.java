package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class AddDataTable extends Action {
    private static final long serialVersionUID = 2021122109996660002L;

    public AddDataTable(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Add Data Table";
        this.description = "add data table to current step";
        this.code = "ADT";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.DATA_TABLE,
                CommandParamKey.TOWER,
                CommandParamKey.LINE
        );
        setCommands(new com.tflow.model.editor.cmd.AddDataTable());
    }

    @Override
    protected void initUndoCommands() {
        /*setUndoParams(
                CommandParamKey.DATA_TABLE,
                CommandParamKey.TOWER,
        );*/
        /*setUndoCommands(new com.tflow.model.editor.cmd.RemoveDataTable());*/
    }
}
