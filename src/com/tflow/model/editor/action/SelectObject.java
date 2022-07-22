package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class SelectObject extends Action {

    public SelectObject(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Select Object";
        this.description = "set active object to current step";
        this.code = "SOB";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.SELECTABLE,
                CommandParamKey.STEP
        );
        setCommands(new com.tflow.model.editor.cmd.SelectObject());
    }

    @Override
    protected void initUndoCommands() {
        /*setUndoParams(
                CommandParamKey.STEP
        );*/
        /*setUndoCommands(new com.tflow.model.editor.cmd.UnselectObject());*/
    }
}
