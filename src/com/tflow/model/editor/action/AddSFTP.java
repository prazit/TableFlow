package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class AddSFTP extends Action {
    private static final long serialVersionUID = 2021122109996660001L;

    public AddSFTP(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Add FTP Connection";
        this.description = "add ftp connection to current step";
        this.code = "AFT";
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
