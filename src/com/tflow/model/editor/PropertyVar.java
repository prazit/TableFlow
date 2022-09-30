package com.tflow.model.editor;

/**
 * Variable name for event PROPERTY_CHANGED.
 */
public enum PropertyVar {

    name,
    dataSourceId,
    function,
    quickColumnList,
    fixedLengthFormatList,
    format,
    ;

    public boolean equals(String varName) {
        return varName.compareTo(name()) == 0;
    }

}
