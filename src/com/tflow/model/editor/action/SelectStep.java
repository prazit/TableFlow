package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class SelectStep extends Action {

    public SelectStep(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Select Step";
        this.description = "Set active step to current project";
        this.code = "SLS";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.PROJECT,
                CommandParamKey.INDEX
        );
        setCommands(
                new com.tflow.model.editor.cmd.LoadStep(),
                new com.tflow.model.editor.cmd.SelectStep()
        );
    }

    @Override
    protected void initUndoCommands() {
        /*setUndoParams(
                CommandParamKey.PROJECT,
                CommandParamKey.INDEX,
                CommandParamKey.STEP
        );*/
        /*setUndoCommands(new com.tflow.model.editor.cmd.UnselectStep());*/
    }
}
