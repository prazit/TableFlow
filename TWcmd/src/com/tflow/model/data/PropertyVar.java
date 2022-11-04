package com.tflow.model.data;

import javax.jws.HandlerChain;

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
    queryId,
    ;

    public boolean equals(String varName) {
        return varName.compareTo(name()) == 0;
    }

}
