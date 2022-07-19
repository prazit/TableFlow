package com.tflow.model.editor;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventManager {

    private Selectable target;
    private Map<EventName, List<EventHandler>> eventHandlerMap;

    public EventManager(Selectable target) {
        eventHandlerMap = new HashMap<>();
        this.target = target;
    }

    public EventManager addHandler(EventName event, EventHandler handler) {
        List<EventHandler> eventHandlerList = eventHandlerMap.computeIfAbsent(event, k -> new ArrayList<>());
        eventHandlerList.add(handler);
        handler.setManager(this);
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

        for (EventHandler handler : eventHandlerList) {
            if (handler.isHandling()) {
                LoggerFactory.getLogger(getClass()).warn("dead loop event occurred in fireEvent(event:{}, target:{})", event, target);
                continue;
            }

            handler.setHandling(true);
            Event ev = new Event(event, target);
            if (data != null) ev.setData(data);
            LoggerFactory.getLogger(getClass()).warn("fireEvent(event:{}, target:{}, data:{})", event, target, data);
            handler.handle(ev);
            handler.setHandling(false);
        }

        return this;
    }

}
