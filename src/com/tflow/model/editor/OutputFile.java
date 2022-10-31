package com.tflow.model.editor;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.FileNameExtension;
import com.tflow.model.data.IDPrefix;
import com.tflow.model.data.PropertyVar;
import com.tflow.model.editor.datasource.NameValue;
import com.tflow.model.editor.view.PropertyView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class OutputFile extends DataFile implements HasEvent, HasSelected {

    private Logger log = LoggerFactory.getLogger(OutputFile.class);

    private EventManager eventManager;
    private List<EventHandler> columnNameHandlerList;

    /*view only*/
    private List<NameValue> fixedLengthFormatList;

    public List<NameValue> getFixedLengthFormatList() {
        return fixedLengthFormatList;
    }

    public void setFixedLengthFormatList(List<NameValue> fixedLengthFormatList) {
        this.fixedLengthFormatList = fixedLengthFormatList;
    }

    /**
     * @param property not null only when column-name has changed.
     */
    private void refreshColumns(PropertyView property) {
        if (DataFileType.OUT_SQL != type) return;

        Object properti = propertyMap.get(PropertyVar.columns.name());
        if (properti == null) return;
        List<String> columnList = (List<String>) properti;

        /*case: column name changed*/
        if (property != null) {
            String oldName = (String) property.getOldValue();
            int index = columnList.indexOf(oldName);
            if (index > 0) {
                columnList.remove(oldName);
                columnList.add(index, (String) property.getNewValue());
            }
            return;
        }

        /*case: column removed*/
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

    /**
     * @param property not null only when column-name has changed.
     */
    public void refreshFixedLengthFormatList(PropertyView property) {
        if (DataFileType.OUT_TXT != type) return;

        Object object = propertyMap.get(PropertyVar.format.name());
        if (log.isDebugEnabled()) log.debug("refreshFixedLengthFormatList: format:{}, fixedLengthFormatList:{}", object, fixedLengthFormatList == null ? "null" : Arrays.toString(fixedLengthFormatList.toArray()));
        if ((object == null || ((String) object).isEmpty()) && fixedLengthFormatList == null) {
            fixedLengthFormatList = new ArrayList<>();
            correctFixedLengthFormatList(property);
            return;
        }

        if (fixedLengthFormatList != null) {
            correctFixedLengthFormatList(property);
            return;
        }

        String formatString = (String) object;
        String[] formats = formatString.split(",");
        if (log.isDebugEnabled()) log.debug("refreshFixedLengthFormatList: formats:{}", Arrays.toString(formats));

        fixedLengthFormatList = new ArrayList<>();
        int index = 0;
        if (formats.length > 0 && formatString.contains("=")) {
            for (String format : formats) {
                String[] value = format.split("=");
                fixedLengthFormatList.add(new NameValue(value[0], value[1], index++));
            }
        }

        if (fixedLengthFormatList.size() == 0) {
            fixedLengthFormatList.add(new NameValue());
        }

        fixedLengthFormatList.get(fixedLengthFormatList.size() - 1).setLast(true);
    }

    private void correctFixedLengthFormatList(PropertyView property) {

        String oldName = null;
        String newName = null;
        if (property != null) {
            oldName = (String) property.getOldValue();
            newName = (String) property.getNewValue();
        }

        /*collect existing-values to map*/
        HashMap<String, String> hashMap = new HashMap<>();
        String name;
        for (NameValue fixed : fixedLengthFormatList) {
            name = fixed.getName().equals(oldName) ? newName : fixed.getName();
            hashMap.put(name, fixed.getValue());
        }
        fixedLengthFormatList.clear();

        /*recreate value list with existing-values and default-value for new-column*/
        List<DataColumn> columnList = ((DataTable) owner).getColumnList();
        String formatted;
        int index = 0;
        for (DataColumn column : columnList) {
            formatted = hashMap.get(column.getName());
            fixedLengthFormatList.add(new NameValue(column.getName(), formatted == null ? "STR:10" : formatted, index++));
        }
        fixedLengthFormatList.get(columnList.size() - 1).setLast(true);
    }

    private String acceptFixedLengthFormat() {
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
                    propertyMap.put(PropertyVar.format.name(), acceptFixedLengthFormat());
                } else if (PropertyVar.type.equals(property.getVar())) {
                    /*need corrected extension of name*/
                    FileNameExtension ext = FileNameExtension.forName(type.getDefaultName());
                    if (ext != null) {
                        int extIndex = name.lastIndexOf(".");
                        name = name.substring(0, extIndex + 1) + ext.name().toLowerCase();
                    }
                }
            }
        });
    }

    public void createOwnerEventHandlers() {
        if (!(owner instanceof TransformTable)) return;

        TransformTable transformTable = (TransformTable) owner;
        transformTable.getEventManager().addHandler(EventName.COLUMN_LIST_CHANGED, new EventHandler() {
            @Override
            public void handle(Event event) {
                columnChanged(null);
            }
        });
        createColumnNameHandlers();
    }

    private void createColumnNameHandlers() {
        /*clear all handlers*/
        if (columnNameHandlerList == null) {
            columnNameHandlerList = new ArrayList<>();
        } else {
            for (EventHandler handler : columnNameHandlerList) {
                handler.remove();
            }
            columnNameHandlerList.clear();
        }

        /*recreate handler for all columns*/
        TransformTable transformTable = (TransformTable) owner;
        for (DataColumn column : transformTable.getColumnList()) {
            TransformColumn transformColumn = (TransformColumn) column;
            EventHandler columnNameHandler = new EventHandler() {
                @Override
                public void handle(Event event) {
                    columnChanged((PropertyView) event.getData());
                }
            };
            transformColumn.getEventManager().addHandler(EventName.NAME_CHANGED, columnNameHandler);
        }
    }

    /**
     * @param property not null only when column-name has changed.
     */
    private void columnChanged(PropertyView property) {
        boolean hasChanges = false;
        String propertyVar = null;

        /*OUTPUT_TXT.format*/
        if (DataFileType.OUT_TXT == type) {
            hasChanges = true;
            propertyVar = PropertyVar.format.name();
            refreshFixedLengthFormatList(property);
            propertyMap.put(PropertyVar.format.name(), acceptFixedLengthFormat());
        }

        /*OUTPUT_SQL.columns*/
        else if (DataFileType.OUT_SQL == type) {
            hasChanges = true;
            propertyVar = PropertyVar.columns.name();
            refreshColumns(property);
        }

        if (hasChanges) {
            eventManager.fireEvent(EventName.COLUMN_LIST_CHANGED, getProperties().getPropertyView(propertyVar));
            if (property == null) createColumnNameHandlers();
        }
    }

    @Override
    public String getSelectableId() {
        return IDPrefix.DATA_OUTPUT.getPrefix() + id;
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
        refreshFixedLengthFormatList(null);
    }
}
