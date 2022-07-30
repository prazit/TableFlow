package com.tflow.model.editor;

import com.tflow.system.Environment;
import com.tflow.system.constant.Theme;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;

@SessionScoped
@Named("workspace")
public class Workspace implements Serializable {
    private static final long serialVersionUID = 2021121709996660006L;

    private Environment environment;

    private Project project;
    private User user;
    private Client client;

    @PostConstruct
    public void onCreation() {
        LoggerFactory.getLogger(Workspace.class).trace("Application started.");

        // TODO: load Environment from configuration.
        environment = Environment.DEVELOPMENT;

        // TODO: do this after Authentication Module is completed, load session settings first then remove initialized below.
        user = new User();
        user.setId(1);
        user.setTheme(Theme.DARK);

        // TODO: load client information into Client instance.
        client = new Client();
        client.setId(1);

        resetProject();
    }

    public void resetProject() {
        project = null;
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

    public Environment getEnvironment() {
        return environment;
    }

}
