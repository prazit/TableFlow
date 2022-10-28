package com.tflow.model.data.verify;

import com.tflow.model.data.TWData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ExceptionData extends TWData {
    private Exception exception;
    private TWData data;

    public ExceptionData(Exception exception, TWData data) {
        this.exception = exception;
        this.data = data;
    }
}
