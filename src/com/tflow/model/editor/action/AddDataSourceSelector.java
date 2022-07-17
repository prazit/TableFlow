package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class AddDataSourceSelector extends Action {

    public AddDataSourceSelector(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Add Data Source Selector";
        this.description = "add data-source-selector to current step";
        this.code = "ADS";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.DATA_SOURCE_SELECTOR,
                CommandParamKey.STEP
        );
        setCommands(new com.tflow.model.editor.cmd.AddDataSourceSelector());
    }

    @Override
    protected void initUndoCommands() {
        setUndoParams(
                CommandParamKey.DATA_SOURCE_SELECTOR,
                CommandParamKey.STEP
        );
        /*TODO: need undo for AddDataSourceSelector*/
        /*setUndoCommands(new com.tflow.model.editor.cmd.RemoveDataSourceSelector());*/
    }
}
