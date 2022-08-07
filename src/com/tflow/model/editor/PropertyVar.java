package com.tflow.model.editor;

/**
 * Variable name for event PROPERTY_CHANGED.
 */
public enum PropertyVar {

    dataSourceId,
    function,
    ;

    public boolean equals(String varName) {
        return varName.compareTo(name()) == 0;
    }

}