package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class RemoveDirectLine extends Action {
    private static final long serialVersionUID = 2021122109996660013L;

    public RemoveDirectLine(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Remove Line";
        this.description = "remove a line from current step";
        this.code = "RML";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.LINE,
                CommandParamKey.STEP
        );
        setCommands(new com.tflow.model.editor.cmd.RemoveDirectLine());
    }

    @Override
    protected void initUndoCommands() {
        setUndoParams(
                CommandParamKey.LINE,
                CommandParamKey.STEP
        );
        setUndoCommands(new com.tflow.model.editor.cmd.AddDirectLine());
    }
}
