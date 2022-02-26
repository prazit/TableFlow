package com.tflow.model.editor;

public class Event {

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
