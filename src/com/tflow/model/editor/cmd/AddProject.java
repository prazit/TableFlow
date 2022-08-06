package com.tflow.model.editor.cmd;

import com.tflow.model.data.ProjectDataManager;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.ProjectManager;
import com.tflow.model.editor.Workspace;

import java.util.Map;

public class AddProject extends Command {
    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Workspace workspace = (Workspace) paramMap.get(CommandParamKey.WORKSPACE);

        /*TODO: need to get new project id from Project List*/
        Project project = new Project("NewProject", "Untitled");
        project.setOwner(workspace);
        workspace.setProject(project);

        ProjectManager projectManager = new ProjectManager();
        project.setDataManager(new ProjectDataManager(workspace.getEnvironment()));

        // save Project
        projectManager.saveProjectAs(project.getId(), project);
    }
}
