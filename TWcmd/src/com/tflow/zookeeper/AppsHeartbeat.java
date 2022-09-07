package com.tflow.zookeeper;

import com.tflow.system.Properties;
import com.tflow.util.DateTimeUtil;
import com.tflow.util.SerializeUtil;
import org.knowm.sundial.SundialJobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class AppsHeartbeat {

    private Logger log = LoggerFactory.getLogger(AppsHeartbeat.class);

    private ZKConfiguration zk;
    private long appTimeout;
    private AppName currentAppName;

    public AppsHeartbeat(ZKConfiguration zk, Properties configs) throws InterruptedException {
        this.zk = zk;
        appTimeout = configs.getPropertyLong("heartbeat.ms", 2000L);
    }

    public boolean isOnline(AppName appName) {
        AppInfo appInfo = getAppInfo(appName);
        if (appInfo == null) return false;
        return !isExpired(appInfo);
    }

    public boolean isNewer(AppName appName, String version) {
        AppInfo appInfo = getAppInfo(appName);
        if (appInfo == null || appInfo.getVersion() == null) return false;
        return appInfo.getVersion().compareTo(version) > 0;
    }

    private boolean isExpired(AppInfo appInfo) {
        return DateTimeUtil.now().getTime() - appInfo.getHeartbeat() > appTimeout;
    }

    public AppInfo getAppInfo(AppName appName) {
        ZKConfigNode zkNode = ZKConfigNode.valueOf(appName.name());
        try {
            String json = zk.getString(zkNode);
            AppInfo appInfo = SerializeUtil.getGson().fromJson(json, AppInfo.class);
            return appInfo;
        } catch (Exception ex) {
            log.warn("getAppInfo(" + appName + ") failed, ", ex);
            return null;
        }
    }

    public String getAppVersion(AppName appName) {
        AppInfo appInfo = getAppInfo(appName);
        if (appInfo == null) return null;
        return appInfo.getVersion();
    }

    public void setAppVersion(AppName appName, String version) {
        try {
            ZKConfigNode zkNode = ZKConfigNode.valueOf(appName.name());
            AppInfo appInfo = new AppInfo(version, DateTimeUtil.now().getTime());
            String json = SerializeUtil.getGson().toJson(appInfo);
            zk.set(zkNode, json);
        } catch (Exception ex) {
            log.warn("setAppVersion(appName:" + appName + ", version:" + version + ") failed, ", ex);
        }
    }

    public void setAutoHeartbeat(AppName appName) {
        currentAppName = appName;
        createScheduleJob();
        startScheduleJob();
    }

    public void sendHeartbeat(AppName appName) throws Exception {
        AppInfo appInfo = getAppInfo(appName);
        if (appInfo == null) throw new Exception("");

        setAppVersion(appName, appInfo.getVersion());
    }

    /**
     * copied from ZKConfiguration.createScheduleJob
     **/
    private void createScheduleJob() {
        if (SundialJobScheduler.getScheduler() == null) {
            SundialJobScheduler.startScheduler();
            try {
                int count = 0;
                int maxCount = 10;
                while (SundialJobScheduler.getScheduler() == null) {
                    log.debug("waiting scheduler to be created [{}}/{}]...", ++count, maxCount);
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        log.error("Thread.sleep error: ", e);
                    }
                    if (count >= maxCount) break;
                }
            } catch (Exception ex) {
                log.error("createScheduleJob: createScheduler error: ", ex);
                return;
            }
        }

        ScheduleJob scheduleJob = ScheduleJob.APP_HEARTBEAT;
        String name = scheduleJob.name();
        SundialJobScheduler.removeJob(name);

        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("AppName", currentAppName);
        parameterMap.put("AppsHeartbeat", this);

        SundialJobScheduler.addJob(name, scheduleJob.getJobClass(), parameterMap, false);
        log.debug("addJob {} completed.", scheduleJob);
    }

    private void startScheduleJob() {
        String name = ScheduleJob.APP_HEARTBEAT.name();
        SundialJobScheduler.addSimpleTrigger("EVERY-" + appTimeout + "ms", name, -1, appTimeout);
    }

}
