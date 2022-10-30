package com.tflow.model.data.verify;

import com.tflow.model.data.*;

import java.util.ArrayList;
import java.util.Map;

public class DataFileVerifier extends DataVerifier {
    public DataFileVerifier(TWData data) {
        super(data);
    }

    @Override
    protected boolean verifyData(TWData data, ArrayList<IssueData> issueList) {
        DataFileData dataFileData = (DataFileData) data;
        int objectId = dataFileData.getId();

        String objectName = verifyName(dataFileData.getName(), "DataFile({name})", objectId);
        if (isNullOrEmpty(dataFileData.getDataSourceType())) addIssueRequired(objectId, objectName, "dataSourceType");

        String type = dataFileData.getType();
        Map<String, Object> propertyMap = dataFileData.getPropertyMap();
        if (type == null) addIssueRequired(objectId, objectName, "type");
        else switch (DataFileType.valueOf(type)) {
            case IN_SQLDB:
            case IN_SQL:
                verifyProperty("quotesValue", propertyMap, objectId, objectName);
                break;

            case IN_DIR:
                verifyProperty("dir", propertyMap, objectId, objectName);
                break;

            case IN_MARKDOWN:
            case IN_ENVIRONMENT:
                /*nothing*/
                break;
        }

        /*if (dataFileData.getDataSourceId() == 0) addIssueRequired(objectId, objectName, "dataSourceId");*/

        return !hasIssue;
    }

}
