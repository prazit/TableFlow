package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class AddQueryTable extends Action {

    public AddQueryTable(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Add Query Table";
        this.description = "add query table to current query";
        this.code = "AQT";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.QUERY,
                CommandParamKey.QUERY_TABLE,
                CommandParamKey.WORKSPACE
        );
        setCommands(new com.tflow.model.editor.cmd.AddQueryTable());
    }

    @Override
    protected void initUndoCommands() {
        setUndoParams(
                CommandParamKey.QUERY,
                CommandParamKey.QUERY_TABLE,
                CommandParamKey.WORKSPACE
        );
        setUndoCommands(new com.tflow.model.editor.cmd.RemoveQueryTable());
    }
}
