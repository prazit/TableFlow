package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.CommandParamKey;

public class RequiredParamException extends Exception {

    public RequiredParamException(CommandParamKey param, Action action, boolean undo) {
        super("Parameter '" + param.name().toLowerCase() + "' is required for action '" + action.getName() + "'" + (undo ? "(undo)" : "") + "!");
    }

}
