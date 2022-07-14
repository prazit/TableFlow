package com.tflow.model.editor.cmd;

import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.Map;

public class TestCommand extends Command {

    @Inject
    Logger log;

    public void execute(Map<CommandParamKey, Object> paramMap) {
        String data1 = (String) paramMap.get(CommandParamKey.DATA_TEST1);
        String data2 = (String) paramMap.get(CommandParamKey.DATA_TEST2);
        log.debug("TestCommand.execute(DATA_TEST1:{},DATA_TEST2:{})", data1, data2);
    }
}
