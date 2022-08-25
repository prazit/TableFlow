package com.tflow.zookeeper;

import com.tflow.util.DateTimeUtil;
import org.apache.zookeeper.KeeperException;
import org.knowm.sundial.Job;
import org.knowm.sundial.exceptions.JobInterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZKHeartbeatJob extends Job {
    private Logger log = LoggerFactory.getLogger(ZKHeartbeatJob.class);

    @Override
    public void doRun() throws JobInterruptException {
        long now = DateTimeUtil.now().getTime();

        ZKConfiguration zkConfiguration = (ZKConfiguration) getJobContext().map.get("ZKConfiguration");
        long nextHeartbeat = zkConfiguration.getNextHeartbeat();
        if(nextHeartbeat > now) {
            //log.debug("skip sent heartbeat, nextheartbeat({}) > now({})", nextHeartbeat, now);
            return;
        }

        try {
            log.debug("sent heartbeat to zookeeper, nextheartbeat({}) <= now({})", nextHeartbeat, now);
            zkConfiguration.getString(ZKConfigNode.HEARTBEAT);
        } catch (KeeperException | InterruptedException ex) {
            LoggerFactory.getLogger(ZKHeartbeatJob.class).error("Zookeeper Heartbeat Job: get heartbeat failed! {}:{}", ex.getClass().getSimpleName(), ex.getMessage());
        }
    }
}
