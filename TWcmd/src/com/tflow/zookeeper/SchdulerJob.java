package com.tflow.zookeeper;

import org.knowm.sundial.Job;

public enum SchdulerJob {

    ZOOKEEPER_HEARTBEAT(ZKHeartbeatJob.class)
    ;

    private Class<? extends Job> jobClass;

    SchdulerJob(Class<? extends Job> jobClass) {
        this.jobClass = jobClass;
    }

    public Class<? extends Job> getJobClass() {
        return jobClass;
    }
}
