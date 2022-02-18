package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class RemoveColumnFx extends Action {
    private static final long serialVersionUID = 2021122109996660006L;

    public RemoveColumnFx(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Remove Column Function";
        this.description = "Remove column function from current step";
        this.code = "RCF";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.COLUMN_FX,
                CommandParamKey.STEP
        );
        setCommands(new com.tflow.model.editor.cmd.RemoveColumnFx());
    }

    @Override
    protected void initUndoCommands() {
        /*TODO: need undo for unexpected remove, how to keep existing lines and properties*/

        /*setUndoParams(
                CommandParamKey.COLUMN_FUNCTION,
                CommandParamKey.STEP
        );
        setUndoCommands(new com.tflow.model.editor.cmd.AddColumnFx());*/
    }
}
