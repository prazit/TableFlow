package com.tflow.model.editor.action;

import com.tflow.model.editor.Step;
import com.tflow.model.editor.cmd.Command;
import com.tflow.model.editor.cmd.CommandParamKey;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
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

    private Map<String, Object> resultMap;

    protected abstract void initAction();

    protected abstract void initCommands();

    protected abstract void initUndoCommands();

    public Action() {
        resultMap = new HashMap<>();
        initAction();
    }

    public void setActionParameters(Map<CommandParamKey, Object> paramMap) {
        this.paramMap = paramMap;
        this.paramMap.put(CommandParamKey.ACTION, this);
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
        if (commandList == null) initCommandsWrapper();
        return canRedo;
    }

    public boolean isCanUndo() {
        if (commandList == null) initUndoCommandsWrapper();
        return canUndo;
    }

    /**
     * Some commands create result in the Action-Result-Map, use this function to get the map after execution completed.
     */
    public Map<String, Object> getResultMap() {
        return resultMap;
    }

    /*-- Public Methods --*/

    public void execute() throws RequiredParamException, UnsupportedOperationException {
        if (commandList == null) initCommandsWrapper();
        requiredParam(paramList, paramMap, false);
        for (Command command : commandList) command.execute(paramMap);

        /*add this Action to history*/
        @SuppressWarnings("unchecked")
        List<Action> history = getHistory();
        history.add(this);
    }

    public void executeUndo() throws RequiredParamException, UnsupportedOperationException {
        if (commandList == null) initUndoCommandsWrapper();
        requiredParam(undoParamList, paramMap, true);

        List<Action> history = getHistory();
        int lastActionIndex = history.size() - 1;
        if(!history.get(lastActionIndex).equals(this))
            throw new UnsupportedOperationException("Action '" + getName() + "' is not last action in the history. " + toString());
        if (!isCanUndo())
            throw new UnsupportedOperationException("Action '" + getName() + "' is not support UNDO. " + toString());

        for (Command command : undoCommandList) command.execute(paramMap);

        /*remove Action from history (FILO)*/
        history.remove(lastActionIndex);
    }

    private List<Action> getHistory() {
        return paramMap.containsKey(CommandParamKey.HISTORY) ? ((List<Action>) paramMap.get(CommandParamKey.HISTORY)) : ((Step) paramMap.get(CommandParamKey.STEP)).getHistory();
    }

    private void requiredParam(List<CommandParamKey> paramList, Map<CommandParamKey, Object> paramMap,
                               boolean undo) throws RequiredParamException {
        for (CommandParamKey required : paramList) {
            if (!required.isOptional() && !paramMap.containsKey(required)) {
                throw new RequiredParamException(required, this, undo);
            }
        }
        if (!paramMap.containsKey(CommandParamKey.HISTORY)) {
            if (!paramMap.containsKey(CommandParamKey.STEP)) {
                throw new RequiredParamException(CommandParamKey.HISTORY, this, undo);
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getName() + "{" +
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
