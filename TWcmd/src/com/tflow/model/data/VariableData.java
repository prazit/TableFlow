package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/*TODO: Future Featured: need to complete all fields in VariableData Model*/
@Data
@EqualsAndHashCode(callSuper = false)
public class VariableData extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660014L;

    private String name;
}