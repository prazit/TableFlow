package com.tflow.model.editor;

import com.tflow.kafka.ProjectFileType;

import java.util.HashMap;
import java.util.Map;

/* Notice: value of TransformColumn has many cases please find Properties.TRANSFORM_COLUMN for more detailed */
public class TransformColumn extends DataColumn implements HasEndPlug {

    private int sourceColumnId;
    private String dynamicExpression;
    private boolean useDynamic;

    /*Notice: function, propertyMap used as fx replacement*/
    private boolean useFunction;
    private ColumnFunction function;
    private Map<String, Object> propertyMap;
    private String propertyOrder;

    @Deprecated
    private ColumnFx fx;

    @Deprecated
    private LinePlug endPlug;

    /*for projectMapper*/
    public TransformColumn() {
        init("");
    }

    public TransformColumn(DataColumn sourceColumn, String endPlug, String startPlug, DataTable owner) {
        super(sourceColumn.getIndex(), sourceColumn.getType(), sourceColumn.getName(), startPlug, owner);
        sourceColumnId = sourceColumn.getId();
        init(endPlug);
    }

    public TransformColumn(int index, DataType type, String name, String endPlug, String startPlug, DataTable owner) {
        super(index, type, name, startPlug, owner);
        sourceColumnId = -1;
        init(endPlug);
    }

    private void init(String endPlug) {
        function = ColumnFunction.TRANSFER;
        createEndPlug(endPlug);
        propertyMap = new HashMap<>();
        propertyOrder = getProperties().initPropertyMap(propertyMap);
    }

    private void createEndPlug(String endPlugId) {
        endPlug = new EndPlug(endPlugId);
        createEndPlugListener();
    }

    private void createEndPlugListener() {
        endPlug.setListener(new PlugListener(endPlug) {
            @Override
            public void plugged(Line line) {
                plug.setPlugged(true);
                plug.setRemoveButton(true);
                owner.connectionCreated();
            }

            @Override
            public void unplugged(Line line) {
                boolean plugged = plug.getLineList().size() > 0;
                plug.setPlugged(plugged);
                plug.setRemoveButton(plugged);
                owner.connectionRemoved();
            }
        });
    }

    /*call after projectMapper*/
    @Override
    public void createPlugListeners() {
        super.createPlugListeners();
        createEndPlugListener();
    }

    public int getSourceColumnId() {
        return sourceColumnId;
    }

    public void setSourceColumnId(int sourceColumnId) {
        this.sourceColumnId = sourceColumnId;
    }

    public String getDynamicExpression() {
        return dynamicExpression;
    }

    public void setDynamicExpression(String dynamicExpression) {
        this.dynamicExpression = dynamicExpression;
    }

    public ColumnFx getFx() {
        return fx;
    }

    public void setFx(ColumnFx fx) {
        this.fx = fx;
    }

    public boolean isUseDynamic() {
        return useDynamic;
    }

    public void setUseDynamic(boolean useDynamic) {
        this.useDynamic = useDynamic;
    }

    public String getPropertyOrder() {
        return propertyOrder;
    }

    public void setPropertyOrder(String propertyOrder) {
        this.propertyOrder = propertyOrder;
    }

    public boolean isUseFunction() {
        return useFunction;
    }

    public void setUseFunction(boolean useFunction) {
        this.useFunction = useFunction;
    }

    public ColumnFunction getFunction() {
        return function;
    }

    public void setFunction(ColumnFunction function) {
        this.function = function;
    }

    @Override
    public ProjectFileType getProjectFileType() {
        return ProjectFileType.TRANSFORM_COLUMN;
    }

    @Override
    public Map<String, Object> getPropertyMap() {
        return propertyMap;
    }

    public void setPropertyMap(Map<String, Object> propertyMap) {
        this.propertyMap = propertyMap;
    }

    @Override
    public LinePlug getEndPlug() {
        return endPlug;
    }

    @Override
    public void setEndPlug(LinePlug endPlug) {
        this.endPlug = endPlug;
    }

    @Override
    public Properties getProperties() {
        return Properties.TRANSFORM_COLUMN;
    }

    @Override
    public String toString() {
        return "{" +
                "id:" + id +
                ", index:" + index +
                ", type:" + type +
                ", name:'" + name + '\'' +
                ", sourceColumnId:'" + sourceColumnId + '\'' +
                ", dynamicExpression:'" + dynamicExpression + '\'' +
                ", useDynamic:" + useDynamic +
                ", useFunction:" + useFunction +
                ", function:" + function +
                ", propertyMap:" + propertyMap +
                ", propertyOrder:'" + propertyOrder + '\'' +
                ", selectableId:'" + getSelectableId() + '\'' +
                '}';
    }
}
