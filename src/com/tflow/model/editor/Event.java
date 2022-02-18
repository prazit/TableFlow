package com.tflow.model.editor;

import com.tflow.model.editor.view.PropertyView;

public class Event {

    private EventName eventName;
    private Selectable target;
    private PropertyView property;

    public Event(EventName eventName, Selectable target) {
        this.eventName = eventName;
        this.target = target;
    }

    public EventName getEventName() {
        return eventName;
    }

    public void setEventName(EventName eventName) {
        this.eventName = eventName;
    }

    public Selectable getTarget() {
        return target;
    }

    public void setTarget(Selectable target) {
        this.target = target;
    }

    public PropertyView getProperty() {
        return property;
    }

    public void setProperty(PropertyView property) {
        this.property = property;
    }
}
