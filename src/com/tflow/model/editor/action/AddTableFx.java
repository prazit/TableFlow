package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class AddTableFx extends Action {

    public AddTableFx(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Add Table Function";
        this.description = "add table function to current table";
        this.code = "ATF";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.TRANSFORM_TABLE,
                CommandParamKey.STEP
        );
        setCommands(new com.tflow.model.editor.cmd.AddTableFx());
    }

    @Override
    protected void initUndoCommands() {
        setUndoParams(
                CommandParamKey.TABLE_FX,
                CommandParamKey.STEP
        );
        setUndoCommands(new com.tflow.model.editor.cmd.RemoveTableFx());
    }
}
