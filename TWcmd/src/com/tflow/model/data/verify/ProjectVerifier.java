package com.tflow.model.data.verify;

import com.tflow.model.data.IssueData;
import com.tflow.model.data.ProjectData;
import com.tflow.model.data.TWData;

import java.util.ArrayList;

public class ProjectVerifier extends DataVerifier {
    public ProjectVerifier(TWData data) {
        super(data);
    }

    @Override
    protected boolean verifyData(TWData data, ArrayList<IssueData> issueList) {
        ProjectData projectData = (ProjectData) data;
        String objectId = projectData.getId();

        String objectName = verifyName(projectData.getName(), "Project({name})", objectId);
        if (isNullOrEmpty(projectData.getVersion())) addIssueRequired(objectId, objectName, "version");

        return !hasIssue;
    }
}
