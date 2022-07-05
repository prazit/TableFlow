package com.tflow.model.mapper;

import com.tflow.model.data.StepData;
import com.tflow.model.data.StepItemData;
import com.tflow.model.editor.Step;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-07-05T19:33:20+0700",
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

    @Override
    public List<StepItemData> toStepItemDataList(List<Step> stepList) {
        if ( stepList == null ) {
            return null;
        }

        List<StepItemData> list = new ArrayList<StepItemData>( stepList.size() );
        for ( Step step : stepList ) {
            list.add( stepToStepItemData( step ) );
        }

        return list;
    }

    @Override
    public List<Step> toStepList(List<StepItemData> stepItemDataList) {
        if ( stepItemDataList == null ) {
            return null;
        }

        List<Step> list = new ArrayList<Step>( stepItemDataList.size() );
        for ( StepItemData stepItemData : stepItemDataList ) {
            list.add( stepItemDataToStep( stepItemData ) );
        }

        return list;
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

    protected Step stepItemDataToStep(StepItemData stepItemData) {
        if ( stepItemData == null ) {
            return null;
        }

        Step step = new Step();

        step.setId( stepItemData.getId() );
        step.setName( stepItemData.getName() );

        return step;
    }
}
