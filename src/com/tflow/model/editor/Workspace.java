package com.tflow.model.editor;

import com.tflow.system.constant.Theme;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;

@SessionScoped
@Named("workspace")
public class Workspace implements Serializable {

    private Project project;
    private User user;
    private Client client;

    @PostConstruct
    public void onCreation() {
        // TODO: load session settings first then remove initialized below, test only.
        user = new User();
        user.setTheme(Theme.DARK);

        // TODO: remove mockup project and mockup step here.
        project = new Project("Mockup Project");
        project.getStepList().add(new Step(1, "Mockup Step 1", 0, project));
        project.getStepList().add(new Step(2, "Mockup Step 2", 1, project));
        project.getStepList().add(new Step(3, "Mockup Step 3", 2, project));
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
