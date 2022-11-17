package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class RemoveQueryColumn extends Action {

    public RemoveQueryColumn(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Remove Query Column";
        this.description = "remove query column from current query";
        this.code = "RQC";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.QUERY,
                CommandParamKey.COLUMN_ID,
                CommandParamKey.STEP
        );
        setCommands(new com.tflow.model.editor.cmd.RemoveQueryColumn());
    }

    @Override
    protected void initUndoCommands() {
        setUndoParams(
                CommandParamKey.QUERY,
                CommandParamKey.QUERY_COLUMN,
                CommandParamKey.STEP
        );
        setUndoCommands(new com.tflow.model.editor.cmd.AddQueryColumn());
    }
}
