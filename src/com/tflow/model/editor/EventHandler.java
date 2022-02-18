package com.tflow.model.editor;

public abstract class EventHandler {

    protected Selectable target;
    protected EventName eventName;
    private boolean handling;

    public EventHandler(Selectable target) {
        this.target = target;
    }

    public Selectable getTarget() {
        return target;
    }

    public EventName getEventName() {
        return eventName;
    }

    public void setEventName(EventName eventName) {
        this.eventName = eventName;
    }

    public boolean isHandling() {
        return handling;
    }

    public void setHandling(boolean handling) {
        this.handling = handling;
    }

    public abstract void handle(Event event);
}
