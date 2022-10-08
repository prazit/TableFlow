package com.tflow.model.editor;

/**
 * Variable name for event PROPERTY_CHANGED.
 */
public enum PropertyVar {

    columnList,
    dataSourceId,
    fixedLengthFormatList,
    format,
    function,
    name,
    quickColumnList,
    columns,
    dir,
    quotesName,
    quotesValue,
    activeObject,
    showPropertyList, showActionButtons, showColumnNumbers, showStepList;

    public boolean equals(String varName) {
        return varName.compareTo(name()) == 0;
    }

}
