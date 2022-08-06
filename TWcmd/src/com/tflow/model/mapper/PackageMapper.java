package com.tflow.model.mapper;

import com.tflow.kafka.KafkaRecordAttributes;
import com.tflow.model.data.PackageData;
import com.tflow.model.data.PackageItemData;
import com.tflow.model.data.ProjectUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "default",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PackageMapper {

    @Mapping(target = "id", source = "projectId")
    ProjectUser map(KafkaRecordAttributes kafkaRecordAttributes);

    PackageItemData map(PackageData packageData);

}
