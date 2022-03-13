package com.tflow.model.editor.view;

import com.tflow.model.editor.action.Action;

import java.io.Serializable;

public class ActionView implements Serializable {
    private static final long serialVersionUID = 2021121709996660004L;

    /*TODO: need to add Log information such as Time, Duration...*/

    private int id;
    private String image;
    private String code;
    private String name;
    private String description;
    private boolean undo;

    public ActionView(Action action) {
        id = action.getId();
        image = action.getImage();
        code = action.getCode();
        name = action.getName();
        description = action.getDescription();
        undo = action.isCanUndo();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isUndo() {
        return undo;
    }

    public void setUndo(boolean undo) {
        this.undo = undo;
    }
}
