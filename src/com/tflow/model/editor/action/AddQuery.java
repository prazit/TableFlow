package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class AddQuery extends Action {

    public AddQuery(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Add Query";
        this.description = "add query to data file";
        this.code = "AQR";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.WORKSPACE,
                CommandParamKey.DATA_FILE,
                CommandParamKey.BINARY_FILE
        );
        setCommands(
                new com.tflow.model.editor.cmd.AddQuery(),
                new com.tflow.model.editor.cmd.RearrangeQueryTable()
        );
    }

    @Override
    protected void initUndoCommands() {
        /*no undo*/
    }
}
