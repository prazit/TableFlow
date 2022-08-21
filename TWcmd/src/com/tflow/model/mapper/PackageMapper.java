package com.tflow.model.mapper;

import com.google.gson.internal.LinkedTreeMap;
import com.tflow.kafka.KafkaRecordAttributes;
import com.tflow.model.data.*;
import com.tflow.model.data.record.RecordAttributesData;
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
            /*@Mapping(target = "id", source = "id"),*/
            @Mapping(target = "fileId", source = "id")
    })
    PackageFileData map(BinaryFileData conversionFileData);

    @Mappings({
            /*@Mapping(target = "id", source = "id"),*/
            @Mapping(target = "fileId", source = "id")
    })
    PackageFileData map(StringItemData binaryFileItemData);


    default List<BinaryFileData> toBinaryFileDataList(List<LinkedTreeMap> linkedTreeMapList) {
        List<BinaryFileData> binaryFileDataList = new ArrayList<>();
        for (LinkedTreeMap linkedTreeMap : linkedTreeMapList) {
            BinaryFileData binaryFileData = new BinaryFileData();
            binaryFileData.setId(((Double) linkedTreeMap.get("id")).intValue());
            binaryFileData.setName((String) linkedTreeMap.get("name"));
            binaryFileData.setExt(FileNameExtension.forName((String) linkedTreeMap.get("ext")));
            binaryFileData.setContent((byte[]) linkedTreeMap.get("content"));
            binaryFileDataList.add(binaryFileData);
        }
        return binaryFileDataList;
    }

    default List<StringItemData> toStringItemData(List<LinkedTreeMap> linkedTreeMapList) {
        List<StringItemData> itemDataList = new ArrayList<>();
        for (LinkedTreeMap linkedTreeMap : linkedTreeMapList) {
            StringItemData itemData = new StringItemData();
            itemData.setId((String) linkedTreeMap.get("id"));
            itemData.setName((String) linkedTreeMap.get("name"));
            itemDataList.add(itemData);
        }
        return itemDataList;
    }

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


    List<ItemData> fromBinaryFileList(List<BinaryFileData> generatedFileList);

    RecordAttributesData clone(RecordAttributesData recordAttributes);
}
