package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class RemoveTransformTable extends Action {
    private static final long serialVersionUID = 2021122109996660016L;

    public RemoveTransformTable(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Remove Transformation Table";
        this.description = "Remove transformation table from current step";
        this.code = "RTT";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.TRANSFORM_TABLE,
                CommandParamKey.STEP
        );
        setCommands(new com.tflow.model.editor.cmd.RemoveTransformTable());
    }

    @Override
    protected void initUndoCommands() {
        setUndoParams(
                CommandParamKey.DATA_TABLE,
                CommandParamKey.STEP
        );
        setUndoCommands(new com.tflow.model.editor.cmd.AddTransformTable());
    }
}
