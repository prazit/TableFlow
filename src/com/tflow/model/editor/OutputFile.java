package com.tflow.model.editor;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.datasource.NameValue;
import com.tflow.model.editor.view.PropertyView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OutputFile extends DataFile implements HasEvent, HasSelected {

    private EventManager eventManager;

    /*view only*/
    private List<NameValue> fixedLengthFormatList;

    public List<NameValue> getFixedLengthFormatList() {
        return fixedLengthFormatList;
    }

    public void setFixedLengthFormatList(List<NameValue> fixedLengthFormatList) {
        this.fixedLengthFormatList = fixedLengthFormatList;
    }

    public void refreshFixedLengthFormatList() {
        if (DataFileType.OUT_TXT != type) return;

        if (fixedLengthFormatList != null) {
            correctFixedLengthFormatList();
            return;
        }

        Object object = propertyMap.get(PropertyVar.format.name());
        String[] formats;
        if (object == null) {
            formats = new String[]{};
        } else {
            formats = ((String) object).split(",");
        }

        List<DataColumn> columnList = ((DataTable) owner).getColumnList();
        fixedLengthFormatList = new ArrayList<>();
        for (int index = 0; index < columnList.size(); index++) {
            DataColumn column = columnList.get(index);
            fixedLengthFormatList.add(new NameValue(column.getName(), index < formats.length ? formats[index] : column.getType().getShorten() + ":1"));
        }
        fixedLengthFormatList.get(columnList.size() - 1).setLast(true);
    }

    private void correctFixedLengthFormatList() {
        List<DataColumn> columnList = ((DataTable) owner).getColumnList();
        HashMap<String, String> hashMap = new HashMap<>();
        for (NameValue fixed : fixedLengthFormatList) {
            hashMap.put(fixed.getName(), fixed.getValue());
        }
        fixedLengthFormatList.clear();
        for (DataColumn column : columnList) {
            String formatted = hashMap.get(column.getName());
            fixedLengthFormatList.add(new NameValue(column.getName(), formatted == null ? "STR:1" : formatted));
        }
        fixedLengthFormatList.get(columnList.size() - 1).setLast(true);
    }

    private String getFixedLengthFormat() {
        StringBuilder stringBuilder = new StringBuilder();
        for (NameValue nameValue : fixedLengthFormatList) {
            stringBuilder.append(",").append(nameValue.getValue());
        }
        return stringBuilder.substring(1);
    }

    /*for ProjectMapper*/
    public OutputFile() {
        super();
        outputInit();
    }

    public OutputFile(DataFileType type, String path, String endPlug, String startPlug) {
        super(type, path, endPlug, startPlug);
        outputInit();
    }

    private void outputInit() {
        eventManager = new EventManager(this);
        createEventHandlers();
    }

    public void createEventHandlers() {
        eventManager.addHandler(EventName.PROPERTY_CHANGED, new EventHandler() {
            @Override
            public void handle(Event event) {
                PropertyView property = (PropertyView) event.getData();
                if (PropertyVar.fixedLengthFormatList.equals(property.getVar())) {
                    propertyMap.put(PropertyVar.format.name(), getFixedLengthFormat());
                }
            }
        });
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

    @Override
    public EventManager getEventManager() {
        return eventManager;
    }

    @Override
    public void selected() {
        refreshFixedLengthFormatList();
    }
}
