package com.tflow.model.editor.action;

import com.tflow.model.editor.cmd.Command;
import com.tflow.model.editor.cmd.CommandParamKey;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class Action implements Serializable {
    private static final long serialVersionUID = 2021122109996660000L;

    protected String image;
    protected String name;
    protected String code;
    protected String description;
    protected Map<CommandParamKey, Object> paramMap;
    protected boolean canUndo;
    protected boolean canRedo;

    private List<CommandParamKey> paramList;
    private List<CommandParamKey> undoParamList;
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

    protected void setParams(CommandParamKey... params) {
        this.paramList = Arrays.asList(params);
    }

    protected void setUndoCommands(Command... commands) {
        this.undoCommandList = Arrays.asList(commands);
    }

    protected void setUndoParams(CommandParamKey... params) {
        this.undoParamList = Arrays.asList(params);
    }

    public String getImage() {
        return image;
    }

    protected void setImage(String image) {
        this.image = image;
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

    public void execute() throws RequiredParamException, UnsupportedOperationException {
        if (commandList == null)
            initCommandsWrapper();
        requiredParam(paramList, paramMap, false);
        for (Command command : commandList)
            command.execute(paramMap);

        /*TODO: add Action to history*/
    }

    public void executeUndo() throws RequiredParamException, UnsupportedOperationException {
        if (commandList == null)
            initUndoCommandsWrapper();
        requiredParam(undoParamList, paramMap, true);

        /*TODO: check allowed to undo or not (last action in the history)*/

        for (Command command : undoCommandList)
            command.execute(paramMap);

        /*TODO: remove Action from history (FILO)*/
    }

    private void requiredParam(List<CommandParamKey> paramList, Map<CommandParamKey, Object> paramMap, boolean undo) throws RequiredParamException {
        for (CommandParamKey required : paramList) {
            if (!paramMap.containsKey(required)) {
                throw new RequiredParamException(required, this, undo);
            }
        }
        if (!paramMap.containsKey(CommandParamKey.HISTORY)) {
            throw new RequiredParamException(CommandParamKey.HISTORY, this, undo);
        }
    }

    @Override
    public String toString() {
        return "ActionBase{" +
                "icon='" + image + '\'' +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", canUndo=" + canUndo +
                ", canRedo=" + canRedo +
                ", paramMap=" + paramMap +
                '}';
    }
}
