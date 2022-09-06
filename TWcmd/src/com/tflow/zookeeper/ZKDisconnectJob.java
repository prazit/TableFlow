package com.tflow.zookeeper;

import com.tflow.util.DateTimeUtil;
import org.knowm.sundial.Job;
import org.knowm.sundial.exceptions.JobInterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZKDisconnectJob extends Job {
    private Logger log = LoggerFactory.getLogger(ZKDisconnectJob.class);

    @Override
    public void doRun() throws JobInterruptException {
        long now = DateTimeUtil.now().getTime();

        ZKConfiguration zkConfiguration = (ZKConfiguration) getJobContext().map.get("ZKConfiguration");
        long nextHeartbeat = zkConfiguration.getNextHeartbeat();
        if (nextHeartbeat > now) return;

        try {
            log.debug("ZKDisconnectJob: disconnect from zookeeper, time({})", now);
            zkConfiguration.disconnect();
        } catch (InterruptedException ex) {
            Logger log = LoggerFactory.getLogger(ZKDisconnectJob.class);
            log.error("ZKDisconnectJob: disconnect from zookeeper failed by {}:{}", ex.getClass().getSimpleName(), ex.getMessage());
        }
    }
}
