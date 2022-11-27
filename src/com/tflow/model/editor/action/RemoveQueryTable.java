package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class RemoveQueryTable extends Action {

    public RemoveQueryTable(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Remove Query Table";
        this.description = "remove query table from current query";
        this.code = "RQT";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.QUERY,
                CommandParamKey.QUERY_TABLE,
                CommandParamKey.WORKSPACE
        );
        setCommands(new com.tflow.model.editor.cmd.RemoveQueryTable());
    }

    @Override
    protected void initUndoCommands() {
        setUndoParams(
                CommandParamKey.QUERY,
                CommandParamKey.QUERY_TABLE,
                CommandParamKey.WORKSPACE
        );
        setUndoCommands(new com.tflow.model.editor.cmd.AddQueryTable());
    }
}
