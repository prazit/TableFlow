package com.tflow.model.mapper;

import com.tflow.model.data.*;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.Variable;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.datasource.SFTP;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.stream.Collectors;

@Mapper(componentModel = "cdi",
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

    @Mappings({
            @Mapping(target = "stepList", expression = "java(project.getStepList().stream().map(Step::getId).collect(Collectors.toList()))"),
            @Mapping(target = "databaseList", expression = "java(project.getDatabaseMap().values().stream().map(Database::getId).collect(Collectors.toList()))"),
            @Mapping(target = "sftpList", expression = "java(project.getSftpMap().values().stream().map(SFTP::getId).collect(Collectors.toList()))"),
            @Mapping(target = "localList", expression = "java(project.getLocalMap().values().stream().map(Local::getId).collect(Collectors.toList()))"),
            @Mapping(target = "variableList", expression = "java(project.getVariableMap().values().stream().map(Variable::getName).collect(Collectors.toList()))"),
    })
    ProjectData map(Project project);

    @Mappings({
            @Mapping(target = "stepList", expression = "java(projectData.getStepList().stream().map(Step::new).collect(Collectors.toList()))"),
            @Mapping(target = "databaseMap", expression = "java(projectData.getDatabaseList().stream().map(Database::new).collect(Collectors.toMap(Database::getId,item->{return item;})))"),
            @Mapping(target = "sftpMap", expression = "java(projectData.getSftpList().stream().map(SFTP::new).collect(Collectors.toMap(SFTP::getId,item->{return item;})))"),
            @Mapping(target = "localMap", expression = "java(projectData.getLocalList().stream().map(Local::new).collect(Collectors.toMap(Local::getId,item->{return item;})))"),
            @Mapping(target = "variableMap", expression = "java(projectData.getVariableList().stream().map(Variable::new).collect(Collectors.toMap(Variable::getName,item->{return item;})))"),
    })
    Project map(ProjectData projectData);

}
