package com.tflow.model.editor;

import com.tflow.system.constant.Theme;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 2021121709996660008L;

    private long id;
    private Theme theme;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Theme getTheme() {
        if (theme == null)
            return Theme.DARK;
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }
}
