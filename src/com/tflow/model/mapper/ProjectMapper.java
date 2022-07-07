package com.tflow.model.mapper;

import com.tflow.model.data.*;
import com.tflow.model.editor.*;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.datasource.SFTP;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Tower;
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
            @Mapping(target = "databaseMap", expression = "java(projectData.getDatabaseList().stream().map(Database::new).collect(Collectors.toMap(Database::getId,item->{return item;})))"),
            @Mapping(target = "sftpMap", expression = "java(projectData.getSftpList().stream().map(SFTP::new).collect(Collectors.toMap(SFTP::getId,item->{return item;})))"),
            @Mapping(target = "localMap", expression = "java(projectData.getLocalList().stream().map(Local::new).collect(Collectors.toMap(Local::getId,item->{return item;})))"),
            @Mapping(target = "variableMap", expression = "java(projectData.getVariableList().stream().map(Variable::new).collect(Collectors.toMap(Variable::getName,item->{return item;})))"),
    })
    Project map(ProjectData projectData);

    Database map(DatabaseData databaseData);

    DatabaseData map(Database database);

    SFTP map(SFTPData sftpData);

    SFTPData map(SFTP sftp);

    Local map(LocalData localData);

    LocalData map(Local local);

    Variable map(VariableData variableData);
    VariableData map(Variable variableData);

    @Mappings({
            @Mapping(target = "name", ignore = true),
            @Mapping(target = "index", expression = "java(-1)")
    })
    Step map(StepItemData stepItemData);

    Step map(StepData stepData);

    Tower map(TowerData towerData);



    @Mappings({
            @Mapping(target = "dataTower", source = "dataTower.id"),
            @Mapping(target = "transformTower", source = "transformTower.id"),
            @Mapping(target = "outputTower", source = "outputTower.id"),
            @Mapping(target = "activeObject", source = "activeObject.selectableId"),
    })
    StepData map(Step step);

    DataTable map(DataTableData dataTableData);

    DataTableData map(DataTable dataTable);

    LineData map(Line newLine);



    List<Step> toStepList(List<StepItemData> stepItemDataList);

    List<StepItemData> toStepItemDataList(List<Step> stepList);



    /*---- ALL ABOUT ID ----*/

    default Integer id(Line line) {
        return line.getId();
    }

    default Integer id(DataFile dataFile) {
        return dataFile.getId();
    }

    default Integer id(DataTable dataTable) {
        return dataTable.getId();
    }

    /*TODO: where to use this function*/
    String id(LinePlugData linePlugData);




    default Line toLine(Integer id) {
        return new Line(id);
    }

    default DataFile toDataFile(Integer id) {
        return new DataFile(id);
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


    List<Integer> fromDataTableList(List<DataTable> dataList);

    default List<Integer> fromMap(Map<Integer, ? extends Object> map) {
        return new ArrayList<>(map.keySet());
    }

    /*----*/
}
