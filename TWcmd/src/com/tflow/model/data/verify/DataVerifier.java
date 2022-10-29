package com.tflow.model.data.verify;

import com.tflow.model.data.IssueData;
import com.tflow.model.data.TWData;

import java.util.ArrayList;
import java.util.Map;

public abstract class DataVerifier {

    private ArrayList<IssueData> issueList;
    private TWData data;
    private int stepId;

    protected boolean hasIssue = false;

    protected void addIssueRequired(int objectId, String objectName, String propertyVar) {
        addIssue(objectId, propertyVar, objectName + " requires " + propertyVar, null);
    }

    protected void addIssueRequired(String objectId, String objectName, String propertyVar) {
        addIssue(objectId, propertyVar, objectName + " requires " + propertyVar, null);
    }

    protected void addIssueRange(int objectId, String objectName, String propertyVar, String range) {
        addIssue(objectId, propertyVar, objectName + " " + propertyVar + " out of range " + range, null);
    }

    protected void addIssue(int objectId, String propertyVar, String displayName, String description) {
        addIssue(String.valueOf(objectId), propertyVar, displayName, description);
    }

    protected void addIssue(String objectId, String propertyVar, String displayName, String description) {
        IssueData issueData = new IssueData();

        issueData.setStepId(stepId);
        issueData.setObjectId(objectId);
        issueData.setPropertyVar(propertyVar);

        issueData.setId(lastIssueId() + 1);
        issueData.setName(displayName);
        issueData.setDescription(description);

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
    public boolean verify(int stepId, ArrayList<IssueData> issueList) {
        this.stepId = stepId;
        this.issueList = issueList;
        return verifyData(data, issueList);
    }

    protected abstract boolean verifyData(TWData data, ArrayList<IssueData> issueList);

}
