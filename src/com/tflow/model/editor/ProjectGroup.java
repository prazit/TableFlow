package com.tflow.model.editor;

import com.tflow.model.data.PropertyVar;
import com.tflow.model.editor.view.PropertyView;

import java.util.Arrays;
import java.util.List;

public class ProjectGroup implements HasEvent {

    private int id;
    private String name;
    private List<ProjectItem> projectList;

    private EventManager eventManager;

    public ProjectGroup() {
        eventManager = new EventManager(this);
        createEventHandlers();
    }

    private void createEventHandlers() {
        eventManager.addHandler(EventName.PROPERTY_CHANGED, new EventHandler() {
            @Override
            public void handle(Event event) {
                PropertyView property = (PropertyView) event.getData();
                if (PropertyVar.name.equals(property.getVar())) {
                    eventManager.fireEvent(EventName.NAME_CHANGED, property);
                }
            }
        });
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ProjectItem> getProjectList() {
        return projectList;
    }

    public void setProjectList(List<ProjectItem> projectList) {
        this.projectList = projectList;
    }

    @Override
    public EventManager getEventManager() {
        return eventManager;
    }

    @Override
    public String toString() {
        return "{" +
                "id:" + id +
                ", name:'" + name + '\'' +
                ", projectList:" + Arrays.toString(projectList.toArray()) +
                '}';
    }
}
