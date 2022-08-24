package com.tflow.zookeeper;

import org.apache.zookeeper.KeeperException;
import org.knowm.sundial.Job;
import org.knowm.sundial.exceptions.JobInterruptException;
import org.slf4j.LoggerFactory;

public class ZKHeartbeatJob extends Job {
    @Override
    public void doRun() throws JobInterruptException {
        ZKConfiguration zkConfiguration = (ZKConfiguration) getJobContext().map.get("ZKConfiguration");
        try {
            zkConfiguration.getString(ZKConfigNode.HEARTBEAT);
        } catch (KeeperException | InterruptedException ex) {
            LoggerFactory.getLogger(ZKHeartbeatJob.class).error("Zookeeper Heartbeat Job: get heartbeat failed! {}:{}", ex.getClass().getSimpleName(), ex.getMessage());
        }
    }
}
