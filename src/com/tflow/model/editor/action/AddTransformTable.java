package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class AddTransformTable extends Action {
    private static final long serialVersionUID = 2021122109996660003L;

    public AddTransformTable(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Add Transform Table";
        this.description = "add transform-table to current step";
        this.code = "ATT";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.TRANSFORM_TABLE,
                CommandParamKey.STEP
        );
        setCommands(new com.tflow.model.editor.cmd.AddTransformTable());
    }

    @Override
    protected void initUndoCommands() {
        /*setUndoParams(
                CommandParamKey.DATA_TABLE,
                CommandParamKey.TOWER,
        );*/
        /*setUndoCommands(new com.tflow.model.editor.cmd.RemoveTransformTable());*/
    }
}
