package com.tflow.model.mapper;

import com.google.gson.internal.LinkedTreeMap;
import com.tflow.kafka.KafkaRecordAttributes;
import com.tflow.model.data.*;
import com.tflow.model.editor.*;
import com.tflow.model.editor.Package;
import com.tflow.model.editor.datasource.*;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Room;
import com.tflow.model.editor.room.Tower;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/* Notice: modify mapper during running need to manual Rebuild Artifact before redeploy again */
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
        },
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProjectMapper {

    /*---- MAP BETWEEN OBJECT ----*/

    ProjectData map(Project project);

    @Mappings({
            @Mapping(target = "dataTower", source = "dataTower.id"),
            @Mapping(target = "transformTower", source = "transformTower.id"),
            @Mapping(target = "outputTower", source = "outputTower.id"),
            @Mapping(target = "activeObject", source = "activeObject.selectableId"),
    })
    StepData map(Step step);

    DataSourceSelectorData map(DataSourceSelector dataSourceSelector);

    DatabaseData map(Database database);

    SFTPData map(SFTP sftp);

    LocalData map(Local local);

    DataFileData map(DataFile dataFile);

    OutputFileData map(OutputFile dataFile);

    VariableData map(Variable variableData);

    DataTableData map(DataTable dataTable);

    DataColumnData map(DataColumn dataColumn);

    @Mapping(target = "dataFile", ignore = true)
    TransformTableData map(TransformTable transformTable);

    TransformColumnData map(TransformColumn transformColumn);

    ColumnFxData map(ColumnFx columnFx);

    TableFxData map(TableFx tableFx);

    LineData map(Line newLine);

    /*@Mapping(target = "roomsOnAFloor", expression = "java(tower.getFloorList().size() == 0 ? 0 : tower.getFloor(0).getRoomList().size())")*/
    TowerData map(Tower tower);

    FloorData map(Floor floor);

    RoomData map(Room room);

    Project map(ProjectData projectData);

    @Mapping(target = "index", expression = "java(-1)" /* Notice: index >= 0 = Working Data, index < 0 = Label Data */)
    Step map(StepItemData stepItemData);

    Step map(StepData stepData);

    DataSourceSelector map(DataSourceSelectorData dataSourceSelectorData);

    Database map(DatabaseData databaseData);

    SFTP map(SFTPData sftpData);

    Local map(LocalData localData);

    Variable map(VariableData variableData);

    DataFile map(DataFileData dataFileData);

    OutputFile map(OutputFileData outputFileData);

    DataTable map(DataTableData dataTableData);

    DataColumn map(DataColumnData dataColumnData);

    TransformTable map(TransformTableData transformTableData);

    TransformColumn map(TransformColumnData transformColumnData);

    ColumnFx map(ColumnFxData columnFxData);

    @Mapping(target = "startPlug", ignore = true)
    ColumnFxPlug map(ColumnFxPlugData columnFxPlugData);

    TableFx map(TableFxData tableFxData);

    Tower map(TowerData towerData);

    Floor map(FloorData floorData);

    Line map(LineData lineData);

    LinePlug map(LinePlugData linePlugData);

    List<Step> toStepList(List<StepItemData> stepItemDataList);

    List<StepItemData> toStepItemDataList(List<Step> stepList);

    Package map(PackageData packageData);

    /*List<PackageFile> map(List<PackageFileData> packageFileData);*/

    /*List<PackageItem> toPackageList(List<PackageItemData> packageItemData);*/

    /*---- ALL ABOUT ID ----*/


    default Integer id(Step step) {
        return step.getId();
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
        return (columnFx == null) ? -1 : columnFx.getId();
    }

    default Integer id(TableFx tableFx) {
        return tableFx.getId();
    }

    default Integer id(Tower tower) {
        return tower.getId();
    }

    default Integer id(Floor floor) {
        return floor.getId();
    }

    default Integer id(Line line) {
        return line.getId();
    }

    default String selectableId(Selectable selectable) {
        return selectable.getSelectableId();
    }


    default DataSource toDataSource(Integer id) {
        return new Local(id);
    }

    default DataFile toDataFile(Integer id) {
        return new DataFile(id);
    }

    default ColumnFx toColumnFx(Integer id) {
        return (id < 0) ? null : new ColumnFx(id);
    }

    default TableFx toTableFx(Integer id) {
        return new TableFx(id);
    }

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


    default List<Integer> fromMap(Map<Integer, ?> map) {
        return new ArrayList<>(map.keySet());
    }

    default List<String> fromVarMap(Map<String, Variable> variableMap) {
        return new ArrayList<>(variableMap.keySet());
    }

    List<StepItemData> fromStepList(List<Step> stepList);

    default List<StepItemData> fromLinkedTreeMap(List<LinkedTreeMap> linkedTreeMapList) {
        List<StepItemData> stepItemDataList = new ArrayList<>();
        for (LinkedTreeMap linkedTreeMap : linkedTreeMapList) {
            StepItemData stepItemData = new StepItemData();
            stepItemData.setId(((Double) linkedTreeMap.get("id")).intValue());
            stepItemData.setName((String) linkedTreeMap.get("name"));
            stepItemDataList.add(stepItemData);
        }
        return stepItemDataList;
    }

    List<Integer> fromDataTableList(List<DataTable> dataList);

    List<Integer> fromDataFileList(List<DataFile> dataFileList);

    List<Integer> fromOutputFileList(List<OutputFile> outputList);

    List<Integer> fromDataColumnList(List<DataColumn> columnList);

    List<Integer> fromTransformTableList(List<TransformTable> transformList);

    List<Integer> fromTableFxList(List<TableFx> tableFxList);

    List<Integer> fromLineList(List<Line> lineList);

    List<Integer> fromFloorList(List<Floor> floorList);

    List<Integer> fromDoubleList(List<Double> doubleList);

    List<Integer> fromDataSourceSelectorList(List<DataSourceSelector> dataSourceSelectorList);

}
