package com.tflow.model.mapper;

import com.tflow.model.data.DatabaseData;
import com.tflow.model.data.LocalData;
import com.tflow.model.data.ProjectData;
import com.tflow.model.data.SFTPData;
import com.tflow.model.data.StepData;
import com.tflow.model.data.VariableData;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.Variable;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.datasource.SFTP;
import java.util.stream.Collectors;
import javax.annotation.processing.Generated;
import javax.enterprise.context.ApplicationScoped;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-07-05T11:46:32+0700",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 11.0.3 (JetBrains s.r.o)"
)
@ApplicationScoped
public class ProjectMapperImpl implements ProjectMapper {

    @Override
    public ProjectData map(Project project) {
        if ( project == null ) {
            return null;
        }

        ProjectData projectData = new ProjectData();

        projectData.setId( project.getId() );
        projectData.setName( project.getName() );
        projectData.setActiveStepIndex( project.getActiveStepIndex() );

        projectData.setStepList( project.getStepList().stream().map(Step::getId).collect(Collectors.toList()) );
        projectData.setDatabaseList( project.getDatabaseMap().values().stream().map(Database::getId).collect(Collectors.toList()) );
        projectData.setSftpList( project.getSftpMap().values().stream().map(SFTP::getId).collect(Collectors.toList()) );
        projectData.setLocalList( project.getLocalMap().values().stream().map(Local::getId).collect(Collectors.toList()) );
        projectData.setVariableList( project.getVariableMap().values().stream().map(Variable::getName).collect(Collectors.toList()) );

        return projectData;
    }

    @Override
    public Project map(ProjectData projectData) {
        if ( projectData == null ) {
            return null;
        }

        String name = null;

        name = projectData.getName();

        Project project = new Project( name );

        project.setId( projectData.getId() );
        project.setActiveStepIndex( projectData.getActiveStepIndex() );

        project.setStepList( projectData.getStepList().stream().map(Step::new).collect(Collectors.toList()) );
        project.setDatabaseMap( projectData.getDatabaseList().stream().map(Database::new).collect(Collectors.toMap(Database::getId,item->{return item;})) );
        project.setSftpMap( projectData.getSftpList().stream().map(SFTP::new).collect(Collectors.toMap(SFTP::getId,item->{return item;})) );
        project.setLocalMap( projectData.getLocalList().stream().map(Local::new).collect(Collectors.toMap(Local::getId,item->{return item;})) );
        project.setVariableMap( projectData.getVariableList().stream().map(Variable::new).collect(Collectors.toMap(Variable::getName,item->{return item;})) );

        return project;
    }
}
