package com.tflow.model.mapper;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * All Mappers for ProjectDataManager need to present in this class.
 */
@SessionScoped
public class ProjectMappers implements Serializable {
    private static final long serialVersionUID = 2022070509996660001L;

    public IdListMapper idList;

    @Inject
    public ProjectMapper project;

    @Inject
    public StepMapper step;

    public ProjectMappers() {
        idList = new IdListMapper();
    }
}
