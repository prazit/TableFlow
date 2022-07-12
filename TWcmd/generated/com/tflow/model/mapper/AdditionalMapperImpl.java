package com.tflow.model.mapper;

import com.tflow.kafka.KafkaTWAdditional;
import com.tflow.model.data.AdditionalData;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-07-12T12:53:09+0700",
    comments = "version: 1.5.2.Final, compiler: javac, environment: Java 11.0.3 (JetBrains s.r.o)"
)
public class AdditionalMapperImpl implements AdditionalMapper {

    @Override
    public AdditionalData map(KafkaTWAdditional additional) {
        if ( additional == null ) {
            return null;
        }

        AdditionalData additionalData = new AdditionalData();

        additionalData.setModifiedClientId( additional.getClientId() );
        additionalData.setModifiedUserId( additional.getUserId() );
        additionalData.setRecordId( additional.getRecordId() );
        additionalData.setProjectId( additional.getProjectId() );
        additionalData.setStepId( additional.getStepId() );
        additionalData.setDataTableId( additional.getDataTableId() );
        additionalData.setTransformTableId( additional.getTransformTableId() );

        return additionalData;
    }
}
