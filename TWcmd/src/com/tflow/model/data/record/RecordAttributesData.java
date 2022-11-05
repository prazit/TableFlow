package com.tflow.model.data.record;

import com.tflow.kafka.ProjectFileType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class RecordAttributesData implements Serializable {
    private static final transient long serialVersionUID = 2022061609996660001L;

    /* Parent Field Group: all fields are optional */
    private String recordId;
    private String projectId;
    private String stepId;
    private String dataTableId;
    private String transformTableId;
    private String childId;

    /* Transaction Field Group: all fields are required */
    private ProjectFileType fileType;
    private long modifiedClientId;
    private long modifiedUserId;

    /* Generated Field Group: generate by WriteCommand */
    private long transactionId;
    private long createdClientId;
    private long createdUserId;
    private Date createdDate;
    private Date modifiedDate;

}
