package com.tflow.model.mapper;

import com.tflow.model.data.record.RecordAttributes;
import com.tflow.model.data.record.RecordAttributesData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "default",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RecordAttributesMapper {

    @Mappings({
            @Mapping(target = "modifiedClientId", source = "clientId"),
            @Mapping(target = "modifiedUserId", source = "userId")
    })
    RecordAttributesData map(RecordAttributes additional);

}
