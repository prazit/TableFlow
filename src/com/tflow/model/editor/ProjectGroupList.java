package com.tflow.model.editor;

import java.util.Arrays;
import java.util.List;

public class ProjectGroupList {

    private int lastProjectId;
    private List<Item> groupList;

    public int getLastProjectId() {
        return lastProjectId;
    }

    public void setLastProjectId(int lastProjectId) {
        this.lastProjectId = lastProjectId;
    }

    public List<Item> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<Item> groupList) {
        this.groupList = groupList;
    }

    @Override
    public String toString() {
        return "{" +
                "lastProjectId:" + lastProjectId +
                ", groupList:" + Arrays.toString(groupList.toArray()) +
                '}';
    }
}
