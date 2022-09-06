package com.tflow.zookeeper;

import lombok.Data;

@Data
public class AppInfo {

    String version;
    long heartbeat;

    public AppInfo(String version, long heartbeat) {
        this.version = version;
        this.heartbeat = heartbeat;
    }
}
