package com.tflow.model.data.verify;

import com.clevel.dconvers.dynvalue.DynamicValueType;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.IssueData;
import com.tflow.model.data.IssueType;
import com.tflow.model.data.TWData;

import java.util.ArrayList;
import java.util.Map;

public abstract class DataVerifier {

    private ArrayList<IssueData> issueList;
    private TWData data;

    private ProjectFileType objectType;
    private int stepId;

    protected boolean hasIssue = false;

    protected void addIssueTooLong(int objectId, String objectName, String propertyVar) {
        addIssue(objectId, propertyVar, IssueType.TOO_LONG);
    }

    protected void addIssueRequired(int objectId, String objectName, String propertyVar) {
        addIssue(objectId, propertyVar, IssueType.REQUIRED);
    }

    protected void addIssueRequired(String objectId, String objectName, String propertyVar) {
        addIssue(objectId, propertyVar, IssueType.REQUIRED);
    }

    protected void addIssueRange(int objectId, String objectName, String propertyVar) {
        addIssue(objectId, propertyVar, IssueType.OUT_OF_RANGE);
    }

    protected void addIssue(int objectId, String propertyVar, IssueType type) {
        addIssue(String.valueOf(objectId), propertyVar, type);
    }

    protected void addIssue(String objectId, String propertyVar, IssueType type) {
        IssueData issueData = new IssueData();

        issueData.setId(lastIssueId() + 1);
        issueData.setType(type.name());

        issueData.setStepId(stepId);
        issueData.setObjectType(objectType.name());
        issueData.setObjectId(objectId);
        issueData.setPropertyVar(propertyVar);

        issueList.add(issueData);
        hasIssue = true;
    }

    private int lastIssueId() {
        int count = issueList.size();
        if (count == 0) return 0;
        return issueList.get(count - 1).getId();
    }

    public ArrayList<IssueData> getIssueList() {
        return issueList;
    }

    public DataVerifier(TWData data) {
        this.data = data;
    }

    protected String verifyName(String name, String objectName, int objectId) {
        return verifyName(name, objectName, String.valueOf(objectId));
    }

    protected String verifyName(String name, String objectName, String objectId) {
        if (name == null || (name != null && name.isEmpty())) {
            name = "[UNNAMED]";
            objectName = objectName.replace("{name}", name);
            addIssueRequired(objectId, name, "name");
        } else {
            objectName = objectName.replace("{name}", name);
        }
        return objectName;
    }

    protected void verifyProperty(String propertyVar, Map<String, Object> propertyMap, int objectId, String objectName) {
        if (isNullOrEmpty(propertyMap.get(propertyVar))) addIssueRequired(objectId, objectName, propertyVar);
    }

    /**
     * TODO: Future Feature: wait until another people come in.
     */
    protected boolean verifyDynamicExpression(Object value) {

        /* Example of value : Full Dynamic Expression
         *
         */

        /*TODO: check match parenthesises $[]*/

        /*TOOD: parse sub expression to check by verifyExpression() */

        return true;
    }

    protected boolean verifyExpression(Object value) {
        if(isNullOrEmpty(value)) return false;

        /* Example of value : Column/Simple Dynamic Expression (without $[])
         * see DynamicValueType class.
         */
        String expression = (String) value;

        /*TODO: check valid ValueType: parse 3CHARSPREFIX to DynamicValueType */
        if(expression.substring(3, 4).equals(":")) {

        }

        DynamicValueType dynamicValueType = DynamicValueType.parse(expression.substring(0,3));

        /*TODO: switch for valueType*/

        /*TODO: SRC,TAR,MAP: valid table name*/

        /*TODO: CAL: valid function name*/

        /*TODO: CAL: FUNCTION: valid parameters (possible values)*/

        /*TODO: */

        /*TODO: check many more*/

        return true;
    }

    protected boolean isNullOrEmpty(Object value) {
        if (value == null) return true;
        return value.toString().isEmpty();
    }

    /**
     * verify data
     *
     * @param stepId found issue only
     * @return true means data is valid, false means data is invalid.
     */
    public boolean verify(int stepId, ProjectFileType objectType, ArrayList<IssueData> issueList) {
        this.stepId = stepId;
        this.objectType = objectType;
        this.issueList = issueList;
        return verifyData(data, issueList);
    }

    protected abstract boolean verifyData(TWData data, ArrayList<IssueData> issueList);

}
