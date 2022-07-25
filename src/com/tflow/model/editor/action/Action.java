package com.tflow.model.editor.action;

import com.tflow.model.editor.Project;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.cmd.Command;
import com.tflow.model.editor.cmd.CommandParamKey;
import com.tflow.util.ProjectUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Action {

    protected int id;
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

    private Map<ActionResultKey, Object> resultMap;

    private Action previousChain;
    private Action nextChain;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
        if (undoCommandList == null) initUndoCommandsWrapper();
        return canUndo;
    }

    public Action getPreviousChain() {
        return previousChain;
    }

    public void setPreviousChain(Action previousChain) {
        this.previousChain = previousChain;
    }

    public Action getNextChain() {
        return nextChain;
    }

    public void setNextChain(Action nextChain) {
        this.nextChain = nextChain;
    }

    /**
     * Some commands create result in the Action-Result-Map, use this function to get the map after execution completed.
     */
    public Map<ActionResultKey, Object> getResultMap() {
        return resultMap;
    }

    /*-- Public Methods --*/

    public void execute() throws RequiredParamException, UnsupportedOperationException {
        execute(false);
    }

    /**
     * @param chain this Action is chain of previous Action in the history.
     */
    public void execute(boolean chain) throws RequiredParamException, UnsupportedOperationException {
        if (commandList == null) initCommandsWrapper();
        requiredParam(paramList, paramMap, false);

        /*this action need ID before add to history*/
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        if (step != null) {
            Project project = step.getOwner();
            List<Action> history = step.getHistory();
            setId(ProjectUtil.newUniqueId(project));
            if (chain) {
                previousChain = history.get(history.size() - 1);
                previousChain.setNextChain(this);
            }
            history.add(this);
        }

        resultMap.clear();

        /*notice: add to history before execute to avoid invalid order in history when the action create a chain*/
        for (Command command : commandList) {
            command.setStep(step);
            command.execute(paramMap);
        }
    }

    public void executeUndo() throws RequiredParamException, UnsupportedOperationException {
        if (commandList == null) initUndoCommandsWrapper();
        requiredParam(paramList, paramMap, true);

        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        List<Action> history = step.getHistory();

        if (!isCanUndo())
            throw new UnsupportedOperationException("Action '" + getName() + "' is not support UNDO. " + toString());

        int lastActionIndex = history.size() - 1;
        if (history.get(lastActionIndex).getId() != getId())
            throw new UnsupportedOperationException("Action '" + getName() + "' is not last action in the history. " + toString());

        resultMap.clear();

        for (Command command : undoCommandList) {
            command.setStep(step);
            command.execute(paramMap);
        }

        /*remove Action from history (FILO)*/
        history.remove(lastActionIndex);

        if (previousChain != null) {
            previousChain.setNextChain(null);
            previousChain.executeUndo();
        }
    }

    private void requiredParam(List<CommandParamKey> paramList, Map<CommandParamKey, Object> paramMap, boolean undo) throws RequiredParamException {
        for (CommandParamKey required : paramList) {
            if (!required.isOptional() && !paramMap.containsKey(required)) {
                throw new RequiredParamException(required, this, undo);
            }
        }
        if (!paramMap.containsKey(CommandParamKey.STEP)
                && !paramMap.containsKey(CommandParamKey.PROJECT)
                && !paramMap.containsKey(CommandParamKey.WORKSPACE)) {
            throw new RequiredParamException(CommandParamKey.STEP, this, undo);
        }
    }

    @Override
    public String toString() {
        return "{" +
                "className:'" + getClass().getName() + "'" +
                ", id:" + id +
                ", icon:'" + image + '\'' +
                ", code:'" + code + '\'' +
                ", name:'" + name + '\'' +
                ", description:'" + description + '\'' +
                ", canUndo:" + canUndo +
                ", canRedo:" + canRedo +
                ", paramMap:" + Arrays.toString(paramMap.keySet().toArray()) +
                '}';
    }
}
