package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class AddDataFile extends Action {

    public AddDataFile(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Add Data File";
        this.description = "add data file to current step";
        this.code = "ADF";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.STEP
        );
        setCommands(
                new com.tflow.model.editor.cmd.AddDataFile()
        );
    }

    @Override
    protected void initUndoCommands() {
        setUndoParams(
                CommandParamKey.DATA_FILE,
                CommandParamKey.STEP
        );
        setUndoCommands(new com.tflow.model.editor.cmd.RemoveDataFile());
    }
}
