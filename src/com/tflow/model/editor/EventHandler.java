package com.tflow.model.editor;

public abstract class EventHandler {

    protected EventName eventName;
    private boolean handling;
    private EventManager manager;

    public EventHandler() {
        /*nothing*/
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

    public EventManager getManager() {
        return manager;
    }

    public void setManager(EventManager manager) {
        this.manager = manager;
    }

    public void remove() {
        manager.removeHandler(this);
    }

    public abstract void handle(Event event) throws Exception;
}
