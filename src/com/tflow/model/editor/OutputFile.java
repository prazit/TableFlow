package com.tflow.model.editor;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.editor.datasource.NameValue;
import com.tflow.model.editor.view.PropertyView;
import org.apache.commons.lang3.StringUtils;

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

    private void refreshColumns() {
        if (DataFileType.OUT_SQL != type) return;

        Object properti = propertyMap.get(PropertyVar.columns.name());
        if (properti == null) return;

        /*incase: column removed*/
        List<String> columnList = (List<String>) properti;
        TransformTable transformTable = (TransformTable) owner;
        StringBuilder stringBuilder = new StringBuilder();
        for (DataColumn dataColumn : transformTable.getColumnList()) {
            stringBuilder.append(",").append(dataColumn.getName());
        }
        String csv = stringBuilder.toString();
        int index = 0;
        while (index < columnList.size()) {
            String column = "," + columnList.get(index);
            if (!csv.contains(column)) {
                columnList.remove(index);
                continue;
            }
            index++;
        }
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

        fixedLengthFormatList = new ArrayList<>();
        for (String format : formats) {
            String[] value = format.split("=");
            fixedLengthFormatList.add(new NameValue(value[0], value[1]));
        }

        if (fixedLengthFormatList.size() == 0) {
            fixedLengthFormatList.add(new NameValue());
        }

        fixedLengthFormatList.get(fixedLengthFormatList.size() - 1).setLast(true);
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
            stringBuilder.append(",").append(nameValue.getName()).append("=").append(nameValue.getValue());
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

    /**
     * TODO: need to capture event NAMED_CHANGED of column and table.
     */
    public void createOwnerEventHandlers() {
        if (!(owner instanceof TransformTable)) return;

        TransformTable transformTable = (TransformTable) owner;
        transformTable.getEventManager().addHandler(EventName.COLUMN_LIST_CHANGED, new EventHandler() {
            @Override
            public void handle(Event event) {
                /*OUTPUT_TXT.format*/
                if (DataFileType.OUT_TXT == type) {
                    refreshFixedLengthFormatList();
                    propertyMap.put(PropertyVar.format.name(), getFixedLengthFormat());
                }

                /*OUTPUT_SQL.columns*/
                else if (DataFileType.OUT_SQL == type) {
                    refreshColumns();
                }

                eventManager.fireEvent(EventName.COLUMN_LIST_CHANGED, getProperties().getPropertyView(PropertyVar.format.name()));
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
