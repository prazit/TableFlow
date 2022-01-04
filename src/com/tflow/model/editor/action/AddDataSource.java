package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class AddDataSource extends Action {
    private static final long serialVersionUID = 2021122109996660001L;

    public AddDataSource(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Add DB Connection";
        this.description = "add datasource/database connection to current step";
        this.code = "ADB";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.DATA_SOURCE,
                CommandParamKey.TOWER,
                CommandParamKey.PROJECT
        );
        setCommands(new com.tflow.model.editor.cmd.AddDataSource());
    }

    @Override
    protected void initUndoCommands() {
        /*setUndoParams(
                CommandParamKey.DATA_SOURCE,
                CommandParamKey.TOWER,
                CommandParamKey.PROJECT
        );*/
        /*setUndoCommands(new com.tflow.model.editor.cmd.RemoveDataSource());*/
    }
}
