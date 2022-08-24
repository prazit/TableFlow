package com.tflow.zookeeper;

import org.knowm.sundial.Job;

public enum SundialJob {

    ZOOKEEPER_HEARTBEAT(ZKHeartbeatJob.class)
    ;

    private Class<? extends Job> jobClass;

    SundialJob(Class<? extends Job> jobClass) {
        this.jobClass = jobClass;
    }

    public Class<? extends Job> getJobClass() {
        return jobClass;
    }
}
