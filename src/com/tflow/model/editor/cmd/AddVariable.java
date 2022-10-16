package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.DataManager;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.ProjectUtil;
import org.mapstruct.factory.Mappers;

import java.util.Map;

public class AddVariable extends Command {
    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Project project = (Project) paramMap.get(CommandParamKey.PROJECT);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);

        Map<String, Variable> variableMap = project.getVariableMap();
        String name = "NEW_VARIABLE";
        while (variableMap.containsKey(name)) {
            name += "_";
        }

        Variable newVariable = new Variable(variableMap.size() + 1, VariableType.USER, name, "description");
        newVariable.setId(ProjectUtil.newUniqueId(project));

        /*put to variableMap*/
        variableMap.put(name, newVariable);

        /*put to selectableMap of projectStep*/
        Map<String, Selectable> selectableMap = project.getStepList().get(-1).getSelectableMap();
        selectableMap.put(newVariable.getSelectableId(), newVariable);

        /*for Action.executeUndo()*/

        /*Action Result*/
        Map<ActionResultKey, Object> resultMap = action.getResultMap();
        resultMap.put(ActionResultKey.VARIABLE, newVariable);

        // save Variable List
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        DataManager dataManager = project.getDataManager();
        ProjectUser projectUser = mapper.toProjectUser(project);
        dataManager.addData(ProjectFileType.VARIABLE_LIST, mapper.fromVariableList(getUserVariableList(project.getVariableMap())), projectUser);

        // save Variable data
        dataManager.addData(ProjectFileType.VARIABLE, mapper.map(newVariable), projectUser, newVariable.getId());

        // save Project data: need to update Project record every Action that call the newUniqueId*/
        dataManager.addData(ProjectFileType.PROJECT, mapper.map(project), projectUser, project.getId());

        // need to wait commit thread after addData.
        dataManager.waitAllTasks();

    }
}
