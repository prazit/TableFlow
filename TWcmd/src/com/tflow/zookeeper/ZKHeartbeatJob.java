package com.tflow.zookeeper;

import com.tflow.util.DateTimeUtil;
import org.apache.zookeeper.KeeperException;
import org.knowm.sundial.Job;
import org.knowm.sundial.exceptions.JobInterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ZKHeartbeatJob extends Job {
    private Logger log = LoggerFactory.getLogger(ZKHeartbeatJob.class);

    @Override
    public void doRun() throws JobInterruptException {
        long now = DateTimeUtil.now().getTime();

        ZKConfiguration zkConfiguration = (ZKConfiguration) getJobContext().map.get("ZKConfiguration");
        long nextHeartbeat = zkConfiguration.getNextHeartbeat();
        if (nextHeartbeat > now) return;

        try {
            log.debug("ZKHeartbeatJob: sent heartbeat to zookeeper, time({})", now);
            zkConfiguration.getString(ZKConfigNode.HEARTBEAT);
        } catch (InterruptedException ex) {
            Logger log = LoggerFactory.getLogger(ZKHeartbeatJob.class);
            log.error("ZKHeartbeatJob: send heartbeat to zookeeper failed by {}:{}", ex.getClass().getSimpleName(), ex.getMessage());
        }
    }
}
