package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class IssuesData extends TWData {

    private List<IssueData> issueList;

    private int complete;
    private Boolean finished;

}
