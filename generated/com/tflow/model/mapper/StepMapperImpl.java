package com.tflow.model.mapper;

import com.tflow.model.data.StepData;
import com.tflow.model.editor.Selectable;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.room.Tower;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-07-07T11:49:20+0700",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 11.0.3 (JetBrains s.r.o)"
)
public class StepMapperImpl implements StepMapper {

    @Override
    public Step map(StepData stepData) {
        if ( stepData == null ) {
            return null;
        }

        Step step = new Step();

        step.setId( stepData.getId() );
        step.setName( stepData.getName() );
        step.setIndex( stepData.getIndex() );
        step.setDataTower( toTower( stepData.getDataTower() ) );
        step.setTransformTower( toTower( stepData.getTransformTower() ) );
        step.setOutputTower( toTower( stepData.getOutputTower() ) );
        step.setActiveObject( toSelectable( stepData.getActiveObject() ) );
        step.setZoom( stepData.getZoom() );
        step.setShowStepList( stepData.isShowStepList() );
        step.setShowPropertyList( stepData.isShowPropertyList() );
        step.setShowActionButtons( stepData.isShowActionButtons() );
        step.setStepListActiveTab( stepData.getStepListActiveTab() );

        return step;
    }

    @Override
    public StepData map(Step step) {
        if ( step == null ) {
            return null;
        }

        StepData stepData = new StepData();

        stepData.setDataTower( stepDataTowerId( step ) );
        stepData.setTransformTower( stepTransformTowerId( step ) );
        stepData.setOutputTower( stepOutputTowerId( step ) );
        stepData.setActiveObject( stepActiveObjectSelectableId( step ) );
        stepData.setId( step.getId() );
        stepData.setName( step.getName() );
        stepData.setIndex( step.getIndex() );
        stepData.setZoom( step.getZoom() );
        stepData.setShowStepList( step.isShowStepList() );
        stepData.setShowPropertyList( step.isShowPropertyList() );
        stepData.setShowActionButtons( step.isShowActionButtons() );
        stepData.setStepListActiveTab( step.getStepListActiveTab() );

        return stepData;
    }

    private int stepDataTowerId(Step step) {
        if ( step == null ) {
            return 0;
        }
        Tower dataTower = step.getDataTower();
        if ( dataTower == null ) {
            return 0;
        }
        int id = dataTower.getId();
        return id;
    }

    private int stepTransformTowerId(Step step) {
        if ( step == null ) {
            return 0;
        }
        Tower transformTower = step.getTransformTower();
        if ( transformTower == null ) {
            return 0;
        }
        int id = transformTower.getId();
        return id;
    }

    private int stepOutputTowerId(Step step) {
        if ( step == null ) {
            return 0;
        }
        Tower outputTower = step.getOutputTower();
        if ( outputTower == null ) {
            return 0;
        }
        int id = outputTower.getId();
        return id;
    }

    private String stepActiveObjectSelectableId(Step step) {
        if ( step == null ) {
            return null;
        }
        Selectable activeObject = step.getActiveObject();
        if ( activeObject == null ) {
            return null;
        }
        String selectableId = activeObject.getSelectableId();
        if ( selectableId == null ) {
            return null;
        }
        return selectableId;
    }
}
