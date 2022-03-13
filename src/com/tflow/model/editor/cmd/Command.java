package com.tflow.model.editor.cmd;

import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;

public abstract class Command  implements Serializable {
    private static final long serialVersionUID = 2022031309996660003L;

    public abstract void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException;

    /**
     * function to support error: Required Parameter
     **/
    protected void required(CommandParamKey paramKey) {
        LoggerFactory.getLogger(this.getClass()).error("Required parameter '{}'.", paramKey);
    }
}
