package com.tflow.model.data.verify;

import com.tflow.model.data.TWData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ExceptionData extends TWData {
    private Exception exception;

    public ExceptionData(Exception exception) {
        this.exception = exception;
    }
}
