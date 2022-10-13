package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class AddVersioned extends Action {

    public AddVersioned(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Update Library File";
        this.description = "update library file";
        this.code = "AVS";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.WORKSPACE,
                CommandParamKey.BINARY_FILE,
                CommandParamKey.PROPERTY,
                CommandParamKey.SELECTABLE
        );
        setCommands(
                new com.tflow.model.editor.cmd.AddVersioned(),
                new com.tflow.model.editor.cmd.ChangePropertyValue()
        );
    }

    @Override
    protected void initUndoCommands() {
        setUndoParams(
                CommandParamKey.WORKSPACE,
                CommandParamKey.BINARY_FILE,
                CommandParamKey.PROPERTY,
                CommandParamKey.SELECTABLE
        );
        /*setUndoCommands(
                new com.tflow.model.editor.cmd.ChangePropertyValue()
        );*/
    }
}
