package com.tflow.zookeeper;

import org.knowm.sundial.Job;

public enum ScheduleJob {

    APP_HEARTBEAT(AppHeartbeatJob.class),
    ZOOKEEPER_HEARTBEAT(ZKHeartbeatJob.class),
    ZOOKEEPER_DISCONNECT(ZKDisconnectJob.class),
    ;

    private Class<? extends Job> jobClass;

    ScheduleJob(Class<? extends Job> jobClass) {
        this.jobClass = jobClass;
    }

    public Class<? extends Job> getJobClass() {
        return jobClass;
    }
}
