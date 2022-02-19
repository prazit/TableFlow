package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.action.Action;
import com.tflow.model.editor.view.PropertyView;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AddColumnFx extends Command {

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        DataColumn sourceColumn = (DataColumn) paramMap.get(CommandParamKey.DATA_COLUMN);
        TransformColumn targetColumn = (TransformColumn) paramMap.get(CommandParamKey.TRANSFORM_COLUMN);
        ColumnFunction columnFunction = (ColumnFunction) paramMap.get(CommandParamKey.COLUMN_FUNCTION);
        Step step = (Step) paramMap.get(CommandParamKey.STEP);
        Action action = (Action) paramMap.get(CommandParamKey.ACTION);
        Map<String, Selectable> selectableMap = step.getSelectableMap();
        Project project = step.getOwner();

        /*optional JAVASCRIPT_BUILDER*/
        StringBuilder jsBuilder = null;
        Object obj = paramMap.get(CommandParamKey.JAVASCRIPT_BUILDER);
        if (obj != null) {
            jsBuilder = (StringBuilder) obj;
        }

        ColumnFx columnFx = new ColumnFx((DataColumn) targetColumn, columnFunction, columnFunction.getName(), project.newElementId());
        columnFx.setId(project.newUniqueId());
        initPropertyMap(columnFx.getPropertyMap(), sourceColumn);

        TransformTable transformTable = (TransformTable) targetColumn.getOwner();
        List<ColumnFx> columnFxList = transformTable.getColumnFxTable().getColumnFxList();
        columnFxList.add(columnFx);
        columnFxList.sort(Comparator.comparingInt(columnFx2 -> columnFx2.getOwner().getIndex()));

        selectableMap.put(columnFx.getSelectableId(), columnFx);

        /*when property 'function' is changed need to re-create endPlugList again*/
        EventManager eventManager = columnFx.getEventManager();
        eventManager.addHandler(EventName.PROPERTY_CHANGED, new EventHandler(columnFx) {
            @Override
            public void handle(Event event) {
                if (PropertyType.COLUMNFUNCTION != event.getProperty().getType()) return;
                createEndPlugList((ColumnFx) event.getTarget());
            }
        });
        createEndPlugList(columnFx);

        /*Notice: draw lines below tested on ColumnFunction.LOOKUP and expect to work for all ColumnFunction*/

        /*line between sourceColumn and columnFx*/
        Line line1 = step.addLine(sourceColumn.getSelectableId(), columnFx.getSelectableId());

        /*line between columnFx and targetColumn*/
        Line line2 = step.addLine(columnFx.getSelectableId(), targetColumn.getSelectableId());

        if (jsBuilder != null) {
            jsBuilder.append(line1.getJsAdd());
            jsBuilder.append(line2.getJsAdd());
        }

        /*for Action.executeUndo()*/
        paramMap.put(CommandParamKey.COLUMN_FX, columnFx);

        /*Action Result*/
        action.getResultMap().put("columnFx", columnFx);
    }

    private void initPropertyMap(Map<String, Object> propertyMap, DataColumn sourceColumn) {
        propertyMap.put("sourceTable", sourceColumn.getOwner().getSelectableId());
        propertyMap.put("sourceColumn", sourceColumn.getSelectableId());
    }

    private void createEndPlugList(ColumnFx columnFx) {
        List<ColumnFxPlug> plugList = columnFx.getEndPlugList();

        Step step = columnFx.getOwner().getOwner().getOwner();
        Map<String, Selectable> selectableMap = step.getSelectableMap();
        Project project = step.getOwner();

        if (plugList.size() > 0) {
            /*need to remove old list from selectableMap before reset the list*/
            for (ColumnFxPlug columnFxPlug : plugList) {
                selectableMap.remove(columnFxPlug.getSelectableId());
            }
            plugList.clear();
        }

        String endPlugId;
        for (PropertyView propertyView : columnFx.getFunction().getProperties().getPlugPropertyList()) {
            endPlugId = project.newElementId();
            ColumnFxPlug columnFxPlug = new ColumnFxPlug(project.newUniqueId(), propertyView.getType().getDataType(), propertyView.getLabel(), endPlugId, columnFx);
            plugList.add(columnFxPlug);
            /*need to update selectableMap for each*/
            selectableMap.put(columnFxPlug.getSelectableId(), columnFxPlug);
        }
    }

}
