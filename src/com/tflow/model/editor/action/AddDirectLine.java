package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class AddDirectLine extends Action {
    private static final long serialVersionUID = 2021122109996660014L;

    public AddDirectLine(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Add Line";
        this.description = "add a line to current step";
        this.code = "ADL";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.LINE,
                CommandParamKey.STEP
        );
        setCommands(new com.tflow.model.editor.cmd.AddDirectLine());
    }

    @Override
    protected void initUndoCommands() {
        setUndoParams(
                CommandParamKey.LINE,
                CommandParamKey.STEP
        );
        setUndoCommands(new com.tflow.model.editor.cmd.RemoveDirectLine());
    }
}
