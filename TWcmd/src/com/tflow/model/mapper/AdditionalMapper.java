package com.tflow.model.mapper;

import com.tflow.kafka.KafkaTWAdditional;
import com.tflow.model.data.AdditionalData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "default",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AdditionalMapper {

    @Mappings({
            @Mapping(target = "modifiedClientId", source = "clientId"),
            @Mapping(target = "modifiedUserId", source = "userId")
    })
    AdditionalData map(KafkaTWAdditional additional);

}
