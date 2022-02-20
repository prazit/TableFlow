package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class AddOutputFile extends Action {
    private static final long serialVersionUID = 2021122109996660008L;

    public AddOutputFile(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Add Output File";
        this.description = "add output file to current table";
        this.code = "AOF";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.DATA_TABLE,
                CommandParamKey.STEP
        );
        setCommands(new com.tflow.model.editor.cmd.AddOutputFile());
    }

    @Override
    protected void initUndoCommands() {
        setUndoParams(
                CommandParamKey.DATA_FILE,
                CommandParamKey.STEP
        );
        //setUndoCommands(new com.tflow.model.editor.cmd.RemoveOutputFile());
    }
}