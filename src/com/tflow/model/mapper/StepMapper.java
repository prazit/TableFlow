package com.tflow.model.mapper;

import com.tflow.model.data.StepData;
import com.tflow.model.editor.Selectable;
import com.tflow.model.editor.SelectableIdOnly;
import com.tflow.model.editor.Step;
import com.tflow.model.editor.room.Tower;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "default")
public interface StepMapper {

    Step map(StepData stepData);

    @Mappings({
            @Mapping(target = "dataTower", source = "dataTower.id"),
            @Mapping(target = "transformTower", source = "transformTower.id"),
            @Mapping(target = "outputTower", source = "outputTower.id"),
            @Mapping(target = "activeObject", source = "activeObject.selectableId"),
    })
    StepData map(Step step);

    default Tower toTower(Integer id) {
        return new Tower(id);
    }

    default Selectable toSelectable(String selectableId) {
        return new SelectableIdOnly(selectableId);
    }

}
