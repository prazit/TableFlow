package com.tflow.model.mapper;

import com.tflow.model.data.*;
import com.tflow.model.editor.*;
import com.tflow.model.editor.datasource.DataSource;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.datasource.SFTP;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Tower;
import org.jboss.weld.manager.Transform;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "default",
        imports = {
                Collectors.class,

                Step.class,
                Database.class,
                SFTP.class,
                Local.class,
                Variable.class,

                StepData.class,
                DatabaseData.class,
                SFTPData.class,
                LocalData.class,
                VariableData.class,
        }
)
public interface ProjectMapper {

    /*---- MAP BETWEEN OBJECT ----*/

    @Mappings({
            @Mapping(target = "databaseList", expression = "java(project.getDatabaseMap().values().stream().map(Database::getId).collect(Collectors.toList()))"),
            @Mapping(target = "sftpList", expression = "java(project.getSftpMap().values().stream().map(SFTP::getId).collect(Collectors.toList()))"),
            @Mapping(target = "localList", expression = "java(project.getLocalMap().values().stream().map(Local::getId).collect(Collectors.toList()))"),
            @Mapping(target = "variableList", expression = "java(project.getVariableMap().values().stream().map(Variable::getName).collect(Collectors.toList()))"),
    })
    ProjectData map(Project project);

    @Mappings({
            @Mapping(target = "dataTower", source = "dataTower.id"),
            @Mapping(target = "transformTower", source = "transformTower.id"),
            @Mapping(target = "outputTower", source = "outputTower.id"),
            @Mapping(target = "activeObject", source = "activeObject.selectableId"),
    })
    StepData map(Step step);

    DatabaseData map(Database database);

    SFTPData map(SFTP sftp);

    LocalData map(Local local);

    DataFileData map(DataFile dataFile);

    VariableData map(Variable variableData);

    DataTableData map(DataTable dataTable);

    DataColumnData map(DataColumn dataColumn);

    TransformTableData map(TransformTable transformTable);

    TransformColumnData map(TransformColumn transformColumn);

    ColumnFxData map(ColumnFx columnFx);

    TableFxData map(TableFx tableFx);

    LineData map(Line newLine);

    TowerData map(Tower tower);

    FloorData map(Floor floor);


    @Mappings({
            @Mapping(target = "databaseMap", expression = "java(projectData.getDatabaseList().stream().map(Database::new).collect(Collectors.toMap(Database::getId,item->{return item;})))"),
            @Mapping(target = "sftpMap", expression = "java(projectData.getSftpList().stream().map(SFTP::new).collect(Collectors.toMap(SFTP::getId,item->{return item;})))"),
            @Mapping(target = "localMap", expression = "java(projectData.getLocalList().stream().map(Local::new).collect(Collectors.toMap(Local::getId,item->{return item;})))"),
            @Mapping(target = "variableMap", expression = "java(projectData.getVariableList().stream().map(Variable::new).collect(Collectors.toMap(Variable::getName,item->{return item;})))"),
    })
    Project map(ProjectData projectData);

    @Mappings({
            @Mapping(target = "name", ignore = true),
            @Mapping(target = "index", expression = "java(-1)")
    })
    Step map(StepItemData stepItemData);

    Step map(StepData stepData);

    Database map(DatabaseData databaseData);

    SFTP map(SFTPData sftpData);

    Local map(LocalData localData);

    Variable map(VariableData variableData);

    DataFile map(DataFileData dataFileData);

    DataTable map(DataTableData dataTableData);

    DataColumn map(DataColumnData dataColumnData);

    TransformTable map(TransformTableData transformTableData);

    TransformColumn map(TransformColumnData transformColumnData);

    ColumnFx map(ColumnFxData columnFxData);

    TableFx map(TableFxData tableFxData);

    Tower map(TowerData towerData);

    Floor map(FloorData floorData);

    Line map(LineData lineData);

    List<Step> toStepList(List<StepItemData> stepItemDataList);

    List<StepItemData> toStepItemDataList(List<Step> stepList);



    /*---- ALL ABOUT ID ----*/

    default Integer id(Line line) {
        return line.getId();
    }

    default Integer id(DataSource dataSource) {
        return dataSource.getId();
    }

    default Integer id(DataFile dataFile) {
        return dataFile.getId();
    }

    default Integer id(DataTable dataTable) {
        return dataTable.getId();
    }

    default Integer id(DataColumn dataColumn) {
        return dataColumn.getId();
    }

    default Integer id(ColumnFx columnFx) {
        return columnFx.getId();
    }

    default Integer id(Tower tower) {
        return tower.getId();
    }

    default Integer id(Floor floor) {
        return floor.getId();
    }

    default String selectableId(Selectable selectable) {
        return selectable.getSelectableId();
    }



    /*TODO: where to call this function*/
    String selectableId(LinePlugData linePlugData);

    /*TODO: where to call this function*/
    default LinePlug toLinePlug(boolean startPlug) {
        return new LinePlug(String.valueOf(startPlug));
    }

    /*TODO: where to call this function*/
    default boolean id(LinePlug linePlug) {
        return false;
    }



    default DataSource toDataSource(Integer id) {
        return new Local(id);
    }

    default DataFile toDataFile(Integer id) {
        return new DataFile(id);
    }

    default ColumnFx toColumnFx(Integer id) {return new ColumnFx(id);}

    default TableFx toTableFx(Integer id) {return new TableFx(id);}

    default Tower toTower(Integer id) {
        return new Tower(id);
    }

    default Floor toFloor(Integer id) {
        return new Floor(id);
    }

    default Selectable toSelectable(String selectableId) {
        return new SelectableIdOnly(selectableId);
    }

    default Line toLine(Integer id) {
        return new Line(id);
    }



    List<Integer> fromStepList(List<Step> stepList);

    List<Integer> fromDataTableList(List<DataTable> dataList);

    List<Integer> fromDataFileList(List<DataFile> dataFileList);

    List<Integer> fromDataColumnList(List<DataColumn> columnList);

    List<Integer> fromTransformTableList(List<TransformTable> transformList);

    List<Integer> fromTableFxList(List<TableFx> tableFxList);

    List<Integer> fromLineList(List<Line> lineList);

    List<Integer> fromFloorList(List<Floor> floorList);

    default List<Integer> fromMap(Map<Integer, ? extends Object> map) {
        return new ArrayList<>(map.keySet());
    }



    /*----*/
}
