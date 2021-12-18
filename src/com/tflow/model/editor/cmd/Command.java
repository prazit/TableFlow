package com.tflow.model.editor.cmd;

import java.io.Serializable;
import java.util.Map;

public abstract class Command implements Serializable {
    public abstract void doCommand(Map<String, Object> paramMap);

    public abstract void undoCommand(Map<String, Object> paramMap);
}
