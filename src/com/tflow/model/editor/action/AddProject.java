package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class AddProject extends Action {

    public AddProject(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Create New Project";
        this.description = "add new project to current workspace";
        this.code = "APJ";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.WORKSPACE,
                CommandParamKey.GROUP_ID,
                CommandParamKey.TEMPLATE_ID
        );
        setCommands(new com.tflow.model.editor.cmd.AddProject());
    }

    @Override
    protected void initUndoCommands() {
        /*setUndoParams(
                [nothing]
        );*/
        /*setUndoCommands(nothing);*/
    }
}
