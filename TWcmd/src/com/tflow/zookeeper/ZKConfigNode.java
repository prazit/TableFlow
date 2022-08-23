package com.tflow.zookeeper;

public enum ZKConfigNode {

    LAST_TRANSACTION_ID(1L),
    MAXIMUM_TRANSACTION_ID(999999999L),
    ;

    Object initialValue;
    int version;

    ZKConfigNode(Object initialValue) {
        this.initialValue = initialValue;
        version = 0;
    }

    public static ZKConfigNode parse(String nodePath) {
        try {
            String node = nodePath.substring(nodePath.lastIndexOf("/") + 1);
            node = node.replaceAll("[.]", "_").toUpperCase();
            System.out.println("parse(" + nodePath + ") nodeName = '" + node + "'");
            return valueOf(node);
        } catch (Exception ex) {
            return null;
        }
    }

    public Object getInitialValue() {
        return initialValue;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
        System.out.println("ZK: " + name() + " version updated to " + version);
    }
}
