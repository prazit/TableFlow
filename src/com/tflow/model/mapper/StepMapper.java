package com.tflow.model.mapper;

import com.tflow.model.data.StepData;
import com.tflow.model.data.StepItemData;
import com.tflow.model.editor.Step;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "default")
public interface StepMapper {

    Step map(StepData stepData);

    StepData map(Step step);

    List<StepItemData> toStepItemDataList(List<Step> stepList);

    List<Step> toStepList(List<StepItemData> stepItemDataList);

}
