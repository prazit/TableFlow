package com.tflow.model.editor;

import com.tflow.kafka.ProjectFileType;

public class OutputFile extends DataFile {

    /*for ProjectMapper*/
    public OutputFile() {
        super();
    }

    public OutputFile(DataFileType type, String path, String endPlug, String startPlug) {
        super(type, path, endPlug, startPlug);
    }

    @Override
    public String getSelectableId() {
        return "of" + id;
    }

    @Override
    public ProjectFileType getProjectFileType() {
        if (owner instanceof TransformTable)
            return ProjectFileType.TRANSFORM_OUTPUT;
        else
            return ProjectFileType.DATA_OUTPUT;
    }
}
