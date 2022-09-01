package com.tflow.model.editor.cmd;

import com.tflow.model.data.IDPrefix;
import com.tflow.model.data.ProjectDataException;
import com.tflow.model.data.DataManager;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.ProjectManager;
import com.tflow.model.editor.Workspace;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.action.ActionResultKey;

import java.util.Map;

public class AddProject extends Command {
    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Workspace workspace = (Workspace) paramMap.get(CommandParamKey.WORKSPACE);
        int groupId = (Integer) paramMap.get(CommandParamKey.GROUP_ID);
        String templateId = (String) paramMap.get(CommandParamKey.TEMPLATE_ID);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);

        /* TEMPLATE_ID:
         * empty string = Add New Empty Project
         * projectID = Copy Project to New Project
         * templateID = Copy Template to New Project
         */
        if (templateId.startsWith(IDPrefix.PROJECT.getPrefix())) {
            templateId = IDPrefix.TEMPLATE.getPrefix() + templateId;
        }

        ProjectManager projectManager = workspace.getProjectManager();
        DataManager dataManager = workspace.getDataManager();

        Project project = new Project(templateId, "Untitled");
        project.setOwner(workspace);
        project.setDataManager(dataManager);
        project.setManager(projectManager);
        workspace.setProject(project);

        String newProjectId = null;
        try {
            newProjectId = projectManager.getNewProjectId(groupId, workspace, dataManager);
            project.setId(newProjectId);
        } catch (ProjectDataException ex) {
            throw new UnsupportedOperationException("getNewProjectId failed:", ex);
        }

        // for Action.executeUndo

        // result map
        action.getResultMap().put(ActionResultKey.PROJECT_ID, newProjectId);

        // save Project
        projectManager.saveProjectAs(newProjectId, project);

        // need to wait commit thread after addData.
        dataManager.waitAllTasks();
    }
}
