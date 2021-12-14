package com.tflow.model.editor;

import com.tflow.system.constant.Theme;

public class User {
    private Theme theme;

    public Theme getTheme() {
        if (theme == null)
            return Theme.DARK;
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }
}
