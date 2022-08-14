package com.tflow.model.mapper;

import com.google.gson.internal.LinkedTreeMap;
import com.tflow.kafka.KafkaRecordAttributes;
import com.tflow.model.data.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

import java.util.ArrayList;
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
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "fileId", source = "id")
    })
    PackageFileData map(BinaryFileData conversionFileData);

    default List<ItemData> toItemDataList(List<LinkedTreeMap> linkedTreeMapList) {
        List<ItemData> itemDataList = new ArrayList<>();
        for (LinkedTreeMap linkedTreeMap : linkedTreeMapList) {
            ItemData itemData = new ItemData();
            itemData.setId(((Double) linkedTreeMap.get("id")).intValue());
            itemData.setName((String) linkedTreeMap.get("name"));
            itemDataList.add(itemData);
        }
        return itemDataList;
    }

    List<Integer> fromDoubleList(List<Double> doubleList);
}
