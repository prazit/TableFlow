package com.tflow.model.data;

import lombok.Data;

@Data
public class NameValueData {
    private static final transient long serialVersionUID = 2021121709996660016L;

    private String name;
    private String value;
}
