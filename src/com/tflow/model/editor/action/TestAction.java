package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;
import com.tflow.model.editor.cmd.TestCommand;

import java.util.Map;

public class TestAction extends Action {

    public TestAction(Map<CommandParamKey, Object> paramMap) {
        setActionParameters(paramMap);
    }

    @Override
    protected void initAction() {
        this.name = "Test Action";
        this.description = "Test in development only.";
        this.code = "TA";
        this.image = "action.png";
    }

    @Override
    protected void initCommands() {
        setCommands(new TestCommand());
    }

    @Override
    protected void initUndoCommands() {
        /*no undo*/
    }
}
