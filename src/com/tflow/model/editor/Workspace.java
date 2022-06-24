package com.tflow.model.editor;

import com.tflow.system.constant.Theme;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;

@SessionScoped
@Named("workspace")
public class Workspace implements Serializable {
    private static final long serialVersionUID = 2021121709996660006L;

    private Project project;
    private User user;
    private Client client;

    @PostConstruct
    public void onCreation() {
        // TODO: do this after Authentication Module is completed, load session settings first then remove initialized below.
        user = new User();
        user.setId(1);
        user.setTheme(Theme.DARK);

        // TODO: load client information into Client instance.
        client = new Client();
        client.setId(1);

        // TODO: do this after AddProject action is completed, remove mockup project and mockup step here.
        project = new Project("Mockup Project");
        project.setId("P1");
        project.setOwer(this);
        project.getStepList().add(new Step("Mockup Step 1", project));
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
