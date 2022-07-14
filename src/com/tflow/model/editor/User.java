package com.tflow.model.editor;

import com.tflow.system.constant.Theme;

public class User {
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
