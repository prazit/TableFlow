package com.tflow.model.editor;

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

}
