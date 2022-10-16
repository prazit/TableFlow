package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class AddVariable extends Action {

    public AddVariable(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Add Variable";
        this.description = "add user variable to current project";
        this.code = "AVR";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.PROJECT
        );
        setCommands(new com.tflow.model.editor.cmd.AddVariable());
    }

    @Override
    protected void initUndoCommands() {
        /*setUndoParams(
                CommandParamKey.STEP
        );*/
        /*setUndoCommands(new com.tflow.model.editor.cmd.RemoveStep());*/
    }
}
