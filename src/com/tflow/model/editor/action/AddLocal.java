package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class AddLocal extends Action {
    private static final long serialVersionUID = 2021122109996660012L;

    public AddLocal(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Add Local Root Directory";
        this.description = "add local root directory to current step";
        this.code = "ALC";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.DATA_SOURCE,
                CommandParamKey.STEP
        );
        setCommands(new com.tflow.model.editor.cmd.AddDataSource());
    }

    @Override
    protected void initUndoCommands() {
        /*setUndoParams(
                CommandParamKey.DATA_SOURCE,
                CommandParamKey.TOWER,
                CommandParamKey.PROJECT
        );*/
        /*setUndoCommands(new com.tflow.model.editor.cmd.RemoveDataSource());*/
    }
}
