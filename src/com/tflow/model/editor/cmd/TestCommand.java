package com.tflow.model.editor.cmd;

import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.Map;

public class TestCommand extends Command {
    private static final long serialVersionUID = 2021121699912360001L;

    @Inject
    Logger log;

    public void doCommand(Map<String, Object> paramMap) {
        String data1 = (String) paramMap.get("data1");
        String data2 = (String) paramMap.get("data1");
        log.debug("TestCommand.doCommand(data1:{},data2:{})", data1, data2);
    }

    public void undoCommand(Map<String, Object> paramMap) {

    }
}
