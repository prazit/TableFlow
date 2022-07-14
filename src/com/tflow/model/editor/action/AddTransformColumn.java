package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class AddTransformColumn extends Action {

    public AddTransformColumn(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Add Column";
        this.description = "add column to current table";
        this.code = "ATC";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.TRANSFORM_TABLE,
                CommandParamKey.STEP
        );
        setCommands(new com.tflow.model.editor.cmd.AddTransformColumn());
    }

    @Override
    protected void initUndoCommands() {
        setUndoParams(
                CommandParamKey.TRANSFORM_COLUMN,
                CommandParamKey.STEP
        );
        setUndoCommands(new com.tflow.model.editor.cmd.RemoveTransformColumn());
    }
}
