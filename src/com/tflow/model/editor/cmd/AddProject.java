package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectDataManager;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Workspace;

import java.util.Map;

public class AddProject extends Command {
    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Workspace workspace = (Workspace) paramMap.get(CommandParamKey.WORKSPACE);
        ProjectDataManager projectDataManager = (ProjectDataManager) paramMap.get(CommandParamKey.DATA_MANAGER);

        /*TODO: need to get new project id from Project List*/
        Project project = new Project("NewProject", "Untitled");
        project.setDataManager(projectDataManager);
        project.setOwer(workspace);
        workspace.setProject(project);

        // save Project
        projectDataManager.addProjectAs(project.getId(), project);
    }
}
