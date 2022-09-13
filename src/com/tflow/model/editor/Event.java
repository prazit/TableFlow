package com.tflow.model.editor;

public class Event {

    private EventName eventName;
    private Object target;
    private Object data;

    public Event(EventName eventName, Object target, Object data) {
        this.eventName = eventName;
        this.target = target;
        this.data = data;
    }

    public EventName getEventName() {
        return eventName;
    }

    public Object getTarget() {
        return target;
    }

    public Object getData() {
        return data;
    }
}
