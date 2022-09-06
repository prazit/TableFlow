package com.tflow.zookeeper;

/**
 * Notice: for Shared Configuration Only.
 */
public enum ZKConfigNode {

    /**
     * HEARTBEAT: internal use only to check version of heartbeat
     */
    HEARTBEAT(8L),

    /**
     * LAST_TRANSACTION_ID will be reset when the value reach the MAXIMUM_TRANSACTION_ID.
     */
    LAST_TRANSACTION_ID(1L),
    MAXIMUM_TRANSACTION_ID(999999999L),

    /**
     * Global Environment.
     */
    ENVIRONMENT("DEVELOPMENT"),

    /**
     * AppInfo for AppsHeartbeat.
     */
    APP_TIMEOUT(2000L),
    TABLE_FLOW("{}"),
    DATA_WRITER("{}"),
    DATA_READER("{}"),
    PACKAGE_BUILDER("{}"),

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
    }
}
