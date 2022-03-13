package com.tflow.model.editor;

import java.io.Serializable;

public class Event implements Serializable {
    private static final long serialVersionUID = 2021121709996660061L;

    private EventName eventName;
    private Selectable target;
    private Object data;

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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
