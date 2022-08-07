package com.tflow.model.mapper;

import com.tflow.kafka.KafkaRecordAttributes;
import com.tflow.model.data.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "default",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PackageMapper {

    @Mapping(target = "id", source = "projectId")
    ProjectUser map(KafkaRecordAttributes kafkaRecordAttributes);

    PackageItemData map(PackageData packageData);

    @Mapping(target = "fileId", source = "id")
    PackageFileData map(BinaryFileItemData binaryFileItemData);

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "fileId", source = "id")
    })
    PackageFileData map(BinaryFileData conversionFileData);
}
