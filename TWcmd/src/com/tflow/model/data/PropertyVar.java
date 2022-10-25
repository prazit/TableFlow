package com.tflow.model.data;

/**
 * Variable name for event PROPERTY_CHANGED.
 */
public enum PropertyVar {

    lock,
    columnList,
    dataSourceId,
    fixedLengthFormatList,
    format,
    function,
    type,
    name,
    quickColumnList,
    columns,
    dir,
    quotesName,
    quotesValue,
    activeObject,
    showPropertyList,
    showActionButtons,
    showColumnNumbers,
    showStepList,
    ;

    public boolean equals(String varName) {
        return varName.compareTo(name()) == 0;
    }

}
