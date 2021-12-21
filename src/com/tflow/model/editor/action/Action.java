package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.Command;
import com.tflow.model.editor.cmd.CommandParamKey;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class Action implements Serializable {
    private static final long serialVersionUID = 2021121609912360000L;

    protected String icon;
    protected String name;
    protected String code;
    protected String description;
    protected Map<CommandParamKey, Object> paramMap;
    protected boolean canUndo;
    protected boolean canRedo;

    private List<Command> commandList;
    private List<Command> undoCommandList;

    protected abstract void initAction();

    protected abstract void initCommands();

    protected abstract void initUndoCommands();

    public Action() {
        initAction();
    }

    public void setActionParameters(Map<CommandParamKey, Object> paramMap) {
        this.paramMap = paramMap;
    }

    private void initCommandsWrapper() {
        initCommands();
        canRedo = commandList != null && commandList.size() > 0;
    }

    private void initUndoCommandsWrapper() {
        initUndoCommands();
        canUndo = undoCommandList != null && undoCommandList.size() > 0;
    }

    protected void setCommands(Command... commands) {
        this.commandList = Arrays.asList(commands);
    }

    protected void setUndoCommands(Command... commands) {
        this.undoCommandList = Arrays.asList(commands);
    }

    public String getIcon() {
        return icon;
    }

    protected void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    public boolean isCanRedo() {
        if (commandList == null)
            initCommandsWrapper();
        return canRedo;
    }

    public boolean isCanUndo() {
        if (commandList == null)
            initUndoCommandsWrapper();
        return canUndo;
    }

    public void execute() {
        if (commandList == null)
            initCommandsWrapper();
        for (Command command : commandList)
            command.execute(paramMap);
    }

    public void executeUndo() {
        if (commandList == null)
            initUndoCommandsWrapper();
        for (Command command : undoCommandList)
            command.execute(paramMap);
    }

    @Override
    public String toString() {
        return "ActionBase{" +
                "icon='" + icon + '\'' +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", canUndo=" + canUndo +
                ", canRedo=" + canRedo +
                ", paramMap=" + paramMap +
                '}';
    }
}
