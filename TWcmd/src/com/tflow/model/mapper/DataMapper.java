package com.tflow.model.mapper;

import com.tflow.model.data.GroupData;
import com.tflow.model.data.GroupItemData;
import com.tflow.model.data.ProjectData;
import com.tflow.model.data.ProjectItemData;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "default",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface DataMapper {

    ProjectItemData map(ProjectData emptyProject);

    GroupItemData map(GroupData groupData);

}
