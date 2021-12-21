package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.TestCommand;

public class TestAction extends Action {
    private static final long serialVersionUID = 2021121609912360001L;

    @Override
    protected void initAction() {
        this.name = "Test Action";
        this.description = "Debug only.";
        this.code = "CODE101";
        this.icon = "";
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
