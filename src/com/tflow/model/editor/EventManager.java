package com.tflow.model.editor;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventManager {

    private Object target;
    private Map<EventName, List<EventHandler>> eventHandlerMap;

    private Exception lastEventException;

    public EventManager(Object target) {
        eventHandlerMap = new HashMap<>();
        this.target = target;
    }

    public EventManager addHandler(EventName event, EventHandler handler) {
        List<EventHandler> eventHandlerList = eventHandlerMap.computeIfAbsent(event, k -> new ArrayList<>());
        eventHandlerList.add(handler);
        handler.setManager(this);
        handler.setHandling(false);
        return this;
    }

    public EventManager removeHandlers(EventName event) {
        eventHandlerMap.remove(event);
        return this;
    }

    public EventManager removeHandler(EventHandler handler) {
        List<EventHandler> eventHandlerList = eventHandlerMap.get(handler.getEventName());
        if (eventHandlerList == null) return this;
        eventHandlerList.remove(handler);
        return this;
    }

    public EventManager fireEvent(EventName event) {
        return fireEvent(event, null);
    }

    public EventManager fireEvent(EventName event, Object data) {
        List<EventHandler> eventHandlerList = eventHandlerMap.get(event);
        if (eventHandlerList == null || eventHandlerList.size() == 0) return this;

        lastEventException = null;
        for (EventHandler handler : eventHandlerList) {
            if (handler.isHandling()) {
                LoggerFactory.getLogger(getClass()).debug("dead loop event occurred in fireEvent(event:{}, target:{})", event, target);
                continue;
            }

            handler.setHandling(true);
            Event ev = new Event(event, target, data);
            LoggerFactory.getLogger(getClass()).debug("fireEvent(event:{}, target:{}, data:{})", event, target, data);
            try {
                handler.handle(ev);
            } catch (Exception ex) {
                lastEventException = ex;
            }
            handler.setHandling(false);
        }

        return this;
    }

    public int countEventHandler(EventName event) {
        List<EventHandler> eventHandlerList = eventHandlerMap.get(event);
        if (eventHandlerList == null) return 0;
        return eventHandlerList.size();
    }

    public Exception getLastEventException() {
        return lastEventException;
    }
}
