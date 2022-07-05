package com.tflow.model.mapper;

import com.tflow.model.data.DatabaseData;
import com.tflow.model.data.LocalData;
import com.tflow.model.data.ProjectData;
import com.tflow.model.data.SFTPData;
import com.tflow.model.data.StepData;
import com.tflow.model.data.StepItemData;
import com.tflow.model.data.VariableData;
import com.tflow.model.editor.Project;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.Variable;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.datasource.SFTP;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-07-05T21:09:05+0700",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 11.0.3 (JetBrains s.r.o)"
)
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
        projectData.setStepList( stepListToStepItemDataList( project.getStepList() ) );

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
        project.setStepList( stepItemDataListToStepList( projectData.getStepList() ) );

        project.setDatabaseMap( projectData.getDatabaseList().stream().map(Database::new).collect(Collectors.toMap(Database::getId,item->{return item;})) );
        project.setSftpMap( projectData.getSftpList().stream().map(SFTP::new).collect(Collectors.toMap(SFTP::getId,item->{return item;})) );
        project.setLocalMap( projectData.getLocalList().stream().map(Local::new).collect(Collectors.toMap(Local::getId,item->{return item;})) );
        project.setVariableMap( projectData.getVariableList().stream().map(Variable::new).collect(Collectors.toMap(Variable::getName,item->{return item;})) );

        return project;
    }

    protected StepItemData stepToStepItemData(Step step) {
        if ( step == null ) {
            return null;
        }

        StepItemData stepItemData = new StepItemData();

        stepItemData.setId( step.getId() );
        stepItemData.setName( step.getName() );

        return stepItemData;
    }

    protected List<StepItemData> stepListToStepItemDataList(List<Step> list) {
        if ( list == null ) {
            return null;
        }

        List<StepItemData> list1 = new ArrayList<StepItemData>( list.size() );
        for ( Step step : list ) {
            list1.add( stepToStepItemData( step ) );
        }

        return list1;
    }

    protected Step stepItemDataToStep(StepItemData stepItemData) {
        if ( stepItemData == null ) {
            return null;
        }

        Step step = new Step();

        step.setId( stepItemData.getId() );
        step.setName( stepItemData.getName() );

        return step;
    }

    protected List<Step> stepItemDataListToStepList(List<StepItemData> list) {
        if ( list == null ) {
            return null;
        }

        List<Step> list1 = new ArrayList<Step>( list.size() );
        for ( StepItemData stepItemData : list ) {
            list1.add( stepItemDataToStep( stepItemData ) );
        }

        return list1;
    }
}
