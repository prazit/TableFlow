package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.TestCommand;

import java.util.Map;

public class TestAction extends Action {
    private static final long serialVersionUID = 2021121609912360001L;

    public TestAction(Map<String, Object> paramMap) {
        super(paramMap, new TestCommand());
        this.name = "Test Action";
        this.description = "Debug only.";
        this.code = "CODE101";
        this.icon = "";
    }

}
