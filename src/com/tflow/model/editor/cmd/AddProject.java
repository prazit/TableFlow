package com.tflow.model.editor.cmd;

import com.tflow.model.data.ProjectDataException;
import com.tflow.model.data.ProjectDataManager;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.ProjectManager;
import com.tflow.model.editor.Workspace;

import java.util.Map;

public class AddProject extends Command {
    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Workspace workspace = (Workspace) paramMap.get(CommandParamKey.WORKSPACE);
        ProjectManager projectManager = new ProjectManager(workspace.getEnvironment());
        ProjectDataManager dataManager = new ProjectDataManager(workspace.getEnvironment(), "TFlow");

        Project project = new Project("", "Untitled");
        project.setOwner(workspace);
        project.setDataManager(dataManager);
        project.setManager(projectManager);
        workspace.setProject(project);

        String id = null;
        try {
            id = projectManager.getNewProjectId(workspace, dataManager);
        } catch (ProjectDataException ex) {
            throw new UnsupportedOperationException("getNewProjectId failed:", ex);
        }

        // save Project
        projectManager.saveProjectAs(id, project);
    }
}
