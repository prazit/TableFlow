package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class AddQueryColumn extends Action {

    public AddQueryColumn(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Add Query Column";
        this.description = "add query column to current query";
        this.code = "AQC";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.QUERY,
                CommandParamKey.QUERY_COLUMN,
                CommandParamKey.SWITCH_ON,
                CommandParamKey.STEP
        );
        setCommands(new com.tflow.model.editor.cmd.AddQueryColumn());
    }

    @Override
    protected void initUndoCommands() {
        setUndoParams(
                CommandParamKey.QUERY,
                CommandParamKey.COLUMN_ID,
                CommandParamKey.SWITCH_ON,
                CommandParamKey.STEP
        );
        setUndoCommands(new com.tflow.model.editor.cmd.RemoveQueryColumn());
    }
}
