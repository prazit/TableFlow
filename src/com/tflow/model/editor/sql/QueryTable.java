package com.tflow.model.editor.sql;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.IDPrefix;
import com.tflow.model.data.query.TableJoinType;
import com.tflow.model.editor.LinePlug;
import com.tflow.model.editor.Properties;
import com.tflow.model.editor.Selectable;
import com.tflow.model.editor.room.Room;
import com.tflow.model.editor.room.RoomType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class QueryTable extends Room implements Selectable {

    private int id;
    private String name;
    private String alias;
    private String schema;

    private List<QueryColumn> columnList;

    private LinePlug startPlug;
    private LinePlug endPlug;

    /*Join*/
    private TableJoinType joinType;
    private String joinTable;
    private int joinTableId;
    private String joinCondition;

    /*for Mapper*/
    public QueryTable() {
        this.joinType = TableJoinType.NONE;
        init();
    }

    /*for Mapper*/
    public QueryTable(int id) {
        this.id = id;
        this.joinType = TableJoinType.NONE;
        init();
    }

    public QueryTable(int id, String name) {
        this.id = id;
        this.schema = "";
        this.name = name;
        this.alias = name;
        this.joinType = TableJoinType.NONE;
        this.joinTable = "";
        init();
    }

    public QueryTable(int id, String name, String schema, String alias) {
        this.id = id;
        this.name = name;
        this.alias = alias;
        this.schema = schema;
        this.joinType = TableJoinType.NONE;
        this.joinTable = "";
        init();
    }

    public QueryTable(int id, String schema, String name, String alias, String joinType, String joinTable, String joinCondition) {
        this.id = id;
        this.schema = schema;
        this.name = name;
        this.alias = alias;
        this.joinType = TableJoinType.valueOf(joinType);
        this.joinTable = joinTable;
        this.joinCondition = joinCondition;
        init();
    }

    public void init() {
        this.setRoomType(RoomType.QUERY_TABLE);
        this.columnList = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public List<QueryColumn> getColumnList() {
        return columnList;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public void setColumnList(List<QueryColumn> columnList) {
        this.columnList = columnList;
    }

    @Override
    public ProjectFileType getProjectFileType() {
        return ProjectFileType.QUERY_TABLE;
    }

    @Override
    public Properties getProperties() {
        return null;
    }

    @Override
    public String getSelectableId() {
        return IDPrefix.QUERY_TABLE.getPrefix() + id;
    }

    public LinePlug getStartPlug() {
        return startPlug;
    }

    public void setStartPlug(LinePlug startPlug) {
        this.startPlug = startPlug;
    }

    @Override
    public Map<String, Object> getPropertyMap() {
        return null;
    }

    public LinePlug getEndPlug() {
        return endPlug;
    }

    public void setEndPlug(LinePlug endPlug) {
        this.endPlug = endPlug;
    }

    public TableJoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(TableJoinType joinType) {
        this.joinType = joinType;
    }

    public String getJoinTable() {
        return joinTable;
    }

    public void setJoinTable(String joinTable) {
        this.joinTable = joinTable;
    }

    public int getJoinTableId() {
        return joinTableId;
    }

    public void setJoinTableId(int joinTableId) {
        this.joinTableId = joinTableId;
    }

    public String getJoinCondition() {
        return joinCondition;
    }

    public void setJoinCondition(String joinCondition) {
        this.joinCondition = joinCondition;
    }

    @Override
    public String toString() {
        return "{" +
                "id:" + id +
                ", name:'" + name + '\'' +
                ", alias:'" + alias + '\'' +
                ", schema:'" + schema + '\'' +
                ", columnList:" + Arrays.toString(columnList.toArray()) +
                ", joinType:" + joinType +
                ", joinTable:'" + joinTable + '\'' +
                ", joinTableId:'" + joinTableId + '\'' +
                ", joinCondition:'" + joinCondition + '\'' +
                ", startPlug:" + startPlug +
                ", endPlug:" + endPlug +
                '}';
    }
}
