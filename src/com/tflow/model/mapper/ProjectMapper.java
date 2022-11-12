package com.tflow.model.mapper;

import com.tflow.model.data.*;
import com.tflow.model.data.query.*;
import com.tflow.model.editor.Package;
import com.tflow.model.editor.*;
import com.tflow.model.editor.datasource.*;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Room;
import com.tflow.model.editor.room.Tower;
import com.tflow.model.editor.sql.*;
import com.tflow.model.editor.view.VersionedFile;
import org.mapstruct.*;

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

    GroupListData map(ProjectGroupList projectGroupList);

    GroupData map(ProjectGroup dataObject);

    VersionedFileData map(VersionedFile versionedFile);

    ProjectData map(Project project);

    PackageData map(Package aPackage);

    BinaryFileData map(BinaryFile binaryFile);

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

    TowerData map(Tower tower);

    FloorData map(Floor floor);

    RoomData map(Room room);

    QueryData map(Query query);

    QueryTableData map(QueryTable queryTable);

    QueryColumnData map(QueryColumn queryColumn);

    QueryFilterData map(QueryFilter queryFilter);

    QuerySortData map(QuerySort querySort);


    ProjectGroupList map(GroupListData groupListData);

    ProjectGroup map(GroupData groupData);

    Project map(ProjectData projectData);

    @Mapping(target = "index", expression = "java(-1)" /* Notice: index >= 0 = Working Data, index < 0 = Label Data */)
    Step map(ItemData itemData);

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

    @Mapping(target = "startPlug", ignore = true)
    ColumnFxPlug map(ColumnFxPlugData columnFxPlugData);

    TableFx map(TableFxData tableFxData);

    Tower map(TowerData towerData);

    Floor map(FloorData floorData);

    Line map(LineData lineData);

    LinePlug map(LinePlugData linePlugData);

    Package map(PackageData packageData);

    PackageFile map(PackageFileData packageFileData);

    BinaryFile map(BinaryFileData binaryFileData);

    BinaryFileItem map(BinaryFileItemData binaryFileItemData);

    VersionedFile map(VersionedFileData versionedFileData);

    Issues map(IssuesData issuesData);

    Query map(QueryData queryData);

    QueryTable map(QueryTableData queryTableData);

    QueryColumn map(QueryColumnData queryColumnData);

    QueryFilter map(QueryFilterData queryFilterData);

    QueryFilter map(QuerySortData querySortData);

    /*---- ALL COPY ----*/

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "selected", ignore = true)
    void copy(QueryColumn sourceColumn, @MappingTarget QueryColumn targetColumn);

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

    default Integer id(Variable variable) {
        return variable.getId();
    }

    default Integer id(QueryTable queryTable) {
        return queryTable.getId();
    }

    default Integer id(QueryColumn queryColumn) {
        return queryColumn.getId();
    }

    default Integer id(QueryFilter queryFilter) {
        return queryFilter.getId();
    }

    default Integer id(QuerySort querySort) {
        return querySort.getId();
    }

    default String selectableId(Selectable selectable) {
        return selectable.getSelectableId();
    }


    @Mappings({
            @Mapping(target = "userId", source = "owner.user.id"),
            @Mapping(target = "clientId", source = "owner.client.id")
    })
    ProjectUser toProjectUser(Project project);

    List<ItemData> toItemDataList(List<Step> stepList);

    BinaryFileItemData toBinaryFileItemData(BinaryFile binaryFile);

    StepList<Step> toStepList(List<ItemData> itemDataList);

    List<Item> toItemList(List<ItemData> itemDataList);

    List<BinaryFileItem> toBinaryFileItemList(List<BinaryFileItemData> binaryFileItemDataList);

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

    List<ItemData> fromPackageList(List<Item> packageList);

    List<ItemData> fromStepList(List<Step> stepList);

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

    List<VersionedFileData> fromVersionedFileList(List<VersionedFile> versionedFileList);

    List<Integer> fromVariableList(List<Variable> variableList);

    List<Integer> fromQueryTableList(List<QueryTable> tableList);

    List<Integer> fromQueryColumnList(List<QueryColumn> columnList);

    List<Integer> fromQueryFilterList(List<QueryFilter> filterList);

    List<Integer> fromQuerySortList(List<QuerySort> sortList);

}
