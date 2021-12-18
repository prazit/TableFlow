package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.Command;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Action implements Serializable {
    private static final long serialVersionUID = 2021121609912360000L;

    protected String icon;
    protected String name;
    protected String code;
    protected String description;
    protected Map<String, Object> paramMap;
    protected boolean canUndo;
    protected boolean canRedo;

    private List<Command> commandList;

    public void test() {

    }

    public Action(Map<String, Object> paramMap, Command... command) {
        this.commandList = Arrays.asList(command);
        this.paramMap = paramMap;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCanUndo() {
        return canUndo;
    }

    public void setCanUndo(boolean canUndo) {
        this.canUndo = canUndo;
    }

    public boolean isCanRedo() {
        return canRedo;
    }

    public void setCanRedo(boolean canRedo) {
        this.canRedo = canRedo;
    }

    public void doCommands() {
        for (Command command : commandList)
            command.doCommand(paramMap);
    }

    public void undoCommands() {
        for (Command command : commandList)
            command.undoCommand(paramMap);
    }

    @Override
    public String toString() {
        return "ActionBase{" +
                "icon='" + icon + '\'' +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", canUndo=" + canUndo +
                ", canRedo=" + canRedo +
                ", paramMap=" + paramMap +
                '}';
    }
}
