package com.tflow.zookeeper;

import com.tflow.util.DateTimeUtil;
import org.knowm.sundial.Job;
import org.knowm.sundial.JobContext;
import org.knowm.sundial.exceptions.JobInterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppHeartbeatJob extends Job {
    private Logger log = LoggerFactory.getLogger(AppHeartbeatJob.class);

    @Override
    public void doRun() throws JobInterruptException {
        long now = DateTimeUtil.now().getTime();

        AppName appName = null;
        try {
            JobContext jobContext = getJobContext();
            appName = (AppName) jobContext.map.get("AppName");
            AppsHeartbeat appsHeartbeat = (AppsHeartbeat) jobContext.map.get("AppsHeartbeat");

            log.debug("AppHeartbeatJob: sent heartbeat(app:{}) to zookeeper, time({})", appName, now);
            appsHeartbeat.sendHeartbeat(appName);

        } catch (Exception ex) {
            Logger log = LoggerFactory.getLogger(AppHeartbeatJob.class);
            log.error("AppHeartbeatJob: send heartbeat(app:{}) to zookeeper failed by {}:{}", appName, ex.getClass().getSimpleName(), ex.getMessage());
            log.trace("", ex);
        }
    }
}
