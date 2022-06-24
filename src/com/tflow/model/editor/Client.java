package com.tflow.model.editor;

import java.io.Serializable;

public class Client implements Serializable {
    private static final long serialVersionUID = 2021121709996660007L;

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
