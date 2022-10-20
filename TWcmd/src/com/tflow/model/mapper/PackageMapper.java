package com.tflow.model.mapper;

import com.tflow.kafka.KafkaRecordAttributes;
import com.tflow.model.data.*;
import com.tflow.model.data.record.RecordAttributesData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "default",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PackageMapper {

    @Mapping(target = "id", source = "projectId")
    ProjectUser map(KafkaRecordAttributes kafkaRecordAttributes);

    ItemData map(PackageData packageData);

    @Mapping(target = "fileId", source = "id")
    PackageFileData map(BinaryFileItemData binaryFileItemData);

    @Mappings({
            @Mapping(target = "fileId", source = "id")
    })
    PackageFileData map(BinaryFileData conversionFileData);

    @Mappings({
            /*@Mapping(target = "id", source = "id"),*/
            @Mapping(target = "fileId", source = "id")
    })
    PackageFileData map(StringItemData binaryFileItemData);

    ItemData toItemData(BinaryFileData conversionFileData);

    List<ItemData> fromBinaryFileList(List<BinaryFileData> generatedFileList);

    RecordAttributesData clone(RecordAttributesData recordAttributes);
}
