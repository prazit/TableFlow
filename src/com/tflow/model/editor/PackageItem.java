package com.tflow.model.editor;

public class PackageItem {

    private int packageId;
    private String name;

    public int getPackageId() {
        return packageId;
    }

    public void setPackageId(int packageId) {
        this.packageId = packageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "{" +
                "packageId:" + packageId +
                ", name:'" + name + '\'' +
                '}';
    }
}
