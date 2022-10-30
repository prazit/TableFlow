package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class LoadStep extends Action {

    public LoadStep(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Load Step";
        this.description = "Load step and initial some data for UI";
        this.code = "LOS";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.PROJECT,
                CommandParamKey.INDEX
        );
        setCommands(new com.tflow.model.editor.cmd.LoadStep());
    }

    @Override
    protected void initUndoCommands() {
        /*no undo*/
    }
}
