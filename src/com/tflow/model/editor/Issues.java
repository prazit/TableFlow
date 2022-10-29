package com.tflow.model.editor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Issues {
    private List<Issue> issueList;

    private int complete;
    private Boolean finished;

    private Date startDate;
    private Date finishDate;

    public Issues() {
        issueList = new ArrayList<>();
    }

    public List<Issue> getIssueList() {
        return issueList;
    }

    public void setIssueList(List<Issue> issueList) {
        this.issueList = issueList;
    }

    public int getComplete() {
        return complete;
    }

    public void setComplete(int complete) {
        this.complete = complete;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    @Override
    public String toString() {
        return "{" +
                "issueList:" + issueList.size() +
                ", complete:" + complete +
                ", finished:" + finished +
                ", startDate:" + startDate +
                ", finishDate:" + finishDate +
                '}';
    }
}
