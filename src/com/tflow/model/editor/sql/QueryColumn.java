package com.tflow.model.editor.sql;

import com.tflow.model.data.query.ColumnType;
import com.tflow.model.editor.DataType;
import com.tflow.model.editor.LinePlug;

public class QueryColumn {

    private int id;
    private int index;
    private ColumnType type;
    private DataType dataType;
    private String name;
    private String value;

    /*JOIN*/
    private boolean pk;
    private boolean fk;
    private String fkSchema;
    private String fkTable;

    private boolean selected;

    private QueryTable owner;

    /*for Mapper*/
    public QueryColumn() {
        /*nothing*/
    }

    public QueryColumn(int index, int id, String name, QueryTable owner) {
        this.index = index;
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.dataType = DataType.STRING;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public ColumnType getType() {
        return type;
    }

    public void setType(ColumnType type) {
        this.type = type;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isPk() {
        return pk;
    }

    public void setPk(boolean pk) {
        this.pk = pk;
    }

    public boolean isFk() {
        return fk;
    }

    public void setFk(boolean fk) {
        this.fk = fk;
    }

    public String getFkSchema() {
        return fkSchema;
    }

    public void setFkSchema(String fkSchema) {
        this.fkSchema = fkSchema;
    }

    public String getFkTable() {
        return fkTable;
    }

    public void setFkTable(String fkTable) {
        this.fkTable = fkTable;
    }

    public QueryTable getOwner() {
        return owner;
    }

    public void setOwner(QueryTable owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "{" +
                "id:" + id +
                ", index:" + index +
                ", type:" + type +
                ", dataType: " + dataType +
                ", name:'" + name + '\'' +
                ", value:'" + value + '\'' +
                ", selected:" + selected +
                ", pk:" + pk +
                ", fk:" + fk +
                ", fkSchema:'" + fkSchema + '\'' +
                ", fkTable:'" + fkTable + '\'' +
                ", owner:" + (owner == null ? "null" : "'" + owner.getId() + ":" + owner.getName() + "'") +
                '}';
    }
}
