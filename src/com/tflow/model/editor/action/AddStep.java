package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class AddStep  extends Action {

    public AddStep(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Add Step";
        this.description = "add step to current project";
        this.code = "AST";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.STEP
        );
        setCommands(new com.tflow.model.editor.cmd.AddStep());
    }

    @Override
    protected void initUndoCommands() {
        /*setUndoParams(
                CommandParamKey.STEP
        );*/
        /*setUndoCommands(new com.tflow.model.editor.cmd.RemoveStep());*/
    }
}
