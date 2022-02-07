package com.tflow.model.editor;

import com.tflow.model.editor.view.PropertyView;

public abstract class PropertiesListener {

    /**
     * for Property Validation process.
     * @param propertyView the property that receive new value.
     * @param oldValue
     * @param newValue
     * @return false to cancel the change.
     */
    abstract public boolean changing(PropertyView propertyView, Object oldValue, Object newValue);

    /**
     * for the post process after the Property is changed, such as 'update list of related field'.
     * @param propertyView the property that already changed.
     */
    abstract public void changed(PropertyView propertyView);

}
