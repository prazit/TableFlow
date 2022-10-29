package com.tflow.model.data.verify;

import com.tflow.model.data.*;

import java.util.ArrayList;
import java.util.Map;

public class OutputFileVerifier extends DataVerifier {
    public OutputFileVerifier(TWData data) {
        super(data);
    }

    @Override
    protected boolean verifyData(TWData data, ArrayList<IssueData> issueList) {
        OutputFileData outputFileData = (OutputFileData) data;
        int objectId = outputFileData.getId();

        String objectName = verifyName(outputFileData.getName(), "OutputFile({name})", objectId);
        if (isNullOrEmpty(outputFileData.getDataSourceType())) addIssueRequired(objectId, objectName, "dataSourceType");
        if (outputFileData.getDataSourceId() == 0) addIssueRequired(objectId, objectName, "dataSourceId");

        String type = outputFileData.getType();
        Map<String, Object> propertyMap = outputFileData.getPropertyMap();
        if (type == null) addIssueRequired(objectId, objectName, "type");
        else switch (DataFileType.valueOf(type)) {
            case OUT_SQL:
                verifyProperty("EOL", propertyMap, objectId, objectName);
                verifyProperty("columns", propertyMap, objectId, objectName);
                verifyProperty("quotesOfName", propertyMap, objectId, objectName);
                verifyProperty("quotesOfValue", propertyMap, objectId, objectName);
                verifyProperty("tableName", propertyMap, objectId, objectName);
                break;

            case OUT_MD:
                verifyProperty("EOL", propertyMap, objectId, objectName);
                break;

            case OUT_CSV:
                verifyProperty("EOL", propertyMap, objectId, objectName);
                verifyProperty("integerFormat", propertyMap, objectId, objectName);
                verifyProperty("decimalFormat", propertyMap, objectId, objectName);
                verifyProperty("dateFormat", propertyMap, objectId, objectName);
                verifyProperty("dateTimeFormat", propertyMap, objectId, objectName);
                break;

            case OUT_TXT:
                verifyProperty("EOL", propertyMap, objectId, objectName);
                verifyProperty("dateFormat", propertyMap, objectId, objectName);
                verifyProperty("dateTimeFormat", propertyMap, objectId, objectName);
                verifyProperty("fillString", propertyMap, objectId, objectName);
                verifyProperty("fillNumber", propertyMap, objectId, objectName);
                verifyProperty("fillDate", propertyMap, objectId, objectName);
                verifyProperty("format", propertyMap, objectId, objectName);
                break;

            case OUT_INS:
            case OUT_UPD:
                verifyProperty("dbTable", propertyMap, objectId, objectName);
                verifyProperty("columns", propertyMap, objectId, objectName);
                verifyProperty("quotesOfName", propertyMap, objectId, objectName);
                verifyProperty("quotesOfValue", propertyMap, objectId, objectName);
                break;
        }

        return !hasIssue;
    }

}
