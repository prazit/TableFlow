package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class AddColumnFx extends Action {
    private static final long serialVersionUID = 2021122109996660006L;

    public AddColumnFx(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Add Column Function";
        this.description = "add column function to current step";
        this.code = "ACF";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.COLUMN_FUNCTION,
                CommandParamKey.STEP
        );
        setCommands(new com.tflow.model.editor.cmd.AddColumnFx());
    }

    @Override
    protected void initUndoCommands() {
        /*setUndoParams(
                CommandParamKey.COLUMN_FX,
                CommandParamKey.STEP
        );*/
        /*setUndoCommands(new com.tflow.model.editor.cmd.RemoveColumnFx());*/
    }
}
