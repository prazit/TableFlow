package com.tflow.model.editor;

import com.tflow.model.editor.view.PropertyView;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventManager implements Serializable {

    private Map<EventName, List<EventHandler>> eventHandlerMap;

    public EventManager() {
        eventHandlerMap = new HashMap<>();
    }

    public void addHandler(EventName event, EventHandler handler) {
        List<EventHandler> eventHandlerList = eventHandlerMap.computeIfAbsent(event, k -> new ArrayList<>());
        eventHandlerList.add(handler);
    }

    public void removeHandlers(EventName event) {
        eventHandlerMap.remove(event);
    }

    public void removeHandler(EventHandler handler) {
        List<EventHandler> eventHandlerList = eventHandlerMap.get(handler.getEventName());
        if(eventHandlerList == null) return;
        eventHandlerList.remove(handler);
    }

    public void fireEvent(EventName event) {
        fireEvent(event, null);
    }

    public void fireEvent(EventName event, PropertyView property) {
        List<EventHandler> eventHandlerList = eventHandlerMap.get(event);
        if(eventHandlerList == null || eventHandlerList.size() == 0) return;

        for (EventHandler handler : eventHandlerList) {
            Selectable target = handler.getTarget();
            if(handler.isHandling()) {
                LoggerFactory.getLogger(getClass()).warn("dead loop event occurred in fireEvent(event:{}, target:{})", event, target);
                continue;
            }

            handler.setHandling(true);
            Event ev = new Event(event, target);
            if(property != null) ev.setProperty(property);
            LoggerFactory.getLogger(getClass()).warn("fireEvent(event:{}, target:{})", event, target);
            handler.handle(ev);
            handler.setHandling(false);
        }
    }

}
