package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

import java.util.Map;

public class SetQuickColumnList extends Action {

    public SetQuickColumnList(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Set QuickColumnList";
        this.description = "Set value to property quickColumnList of TransformTable";
        this.code = "SQL";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setParams(
                CommandParamKey.TRANSFORM_TABLE
        );
        setCommands(new com.tflow.model.editor.cmd.SetQuickColumnList());
    }

    @Override
    protected void initUndoCommands() {
        /*setUndoParams(
                CommandParamKey.TRANSFORM_TABLE
        );*/
        /*setUndoCommands(new com.tflow.model.editor.cmd.SetQuickColumnList());*/
    }
}
