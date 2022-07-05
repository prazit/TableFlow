package com.tflow.model.editor;

/*TODO: need Variable List Data File in ProjectFileType*/
/*TODO: need Variable Data File in ProjectFileType*/
/**
 * A function item that execute(translation) at the first access.
 */
public class Variable {
    private static final long serialVersionUID = 2021121709996660014L;

    private String name;

    /* for ProjectDataManager.getProject only */
    public Variable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
