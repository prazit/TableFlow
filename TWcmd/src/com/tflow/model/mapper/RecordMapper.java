package com.tflow.model.mapper;

import com.tflow.kafka.KafkaRecord;
import com.tflow.kafka.KafkaRecordAttributes;
import com.tflow.model.data.record.ClientRecordData;
import com.tflow.model.data.record.RecordAttributesData;
import com.tflow.model.data.record.RecordData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "default",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RecordMapper {

    @Mappings({
            @Mapping(target = "modifiedClientId", source = "clientId"),
            @Mapping(target = "modifiedUserId", source = "userId")
    })
    RecordAttributesData map(KafkaRecordAttributes additional);

    @Mappings({
            @Mapping(target = "clientId", source = "modifiedClientId"),
            @Mapping(target = "userId", source = "modifiedUserId")
    })
    KafkaRecordAttributes map(RecordAttributesData additional);

    RecordData map(KafkaRecord kafkaRecord);



    @Mappings({
            @Mapping(target = "clientId", source = "modifiedClientId"),
            @Mapping(target = "userId", source = "modifiedUserId")
    })
    ClientRecordData toClientRecordData(RecordAttributesData additional);
}
