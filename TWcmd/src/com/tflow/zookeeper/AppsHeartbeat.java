package com.tflow.zookeeper;

import com.tflow.system.Properties;
import com.tflow.util.DateTimeUtil;
import com.tflow.util.SerializeUtil;
import org.knowm.sundial.SundialJobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AppsHeartbeat {

    private Logger log = LoggerFactory.getLogger(AppsHeartbeat.class);

    private ZKConfiguration zk;
    private long heartbeatMs;
    private long appTimeout;
    private AppName currentAppName;
    private Lock lock;
    private Condition condition;

    public AppsHeartbeat(ZKConfiguration zk, Properties configs) throws InterruptedException {
        this.zk = zk;
        heartbeatMs = configs.getPropertyLong("heartbeat.ms", 2000L);
        appTimeout = (long) (heartbeatMs * 2.2);
    }

    public boolean isOnline(AppName appName) {
        AppInfo appInfo = getAppInfo(appName);
        if (appInfo == null) return false;
        if (!isExpired(appInfo)) return true;

        /*block this thread for thread process below until the next heartbeatTick*/
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                unblock();
            }
        }, appTimeout, TimeUnit.MILLISECONDS);
        block(appName);
        scheduler.shutdownNow();

        // checker needed to confirm no change made to this AppName.
        AppInfo checkerInfo = getAppInfo(appName);
        return !(appInfo.getHeartbeat() == checkerInfo.getHeartbeat());
    }

    private void block(AppName appName) {
        if (log.isDebugEnabled()) log.debug("block by thread:{} for confirm offline:{} until the next heartbeatTick", Thread.currentThread().getName(), appName);
        lock = new ReentrantLock();
        condition = lock.newCondition();
        try {
            lock.lock();
            condition.await();
            lock.unlock();
        } catch (InterruptedException e) {
            /*ignored*/
        }

        lock = null;
        condition = null;
        if (log.isDebugEnabled()) log.debug("unblocked by thread:{} for confirm offline:{}", Thread.currentThread().getName(), appName);
    }

    private void unblock() {
        /*unblock checker process*/
        if (condition != null) {
            if (log.isDebugEnabled()) log.debug("unblocking by thread:{}", Thread.currentThread().getName());
            lock.lock();
            condition.signal();
            lock.unlock();
        }
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

    public void startAutoHeartbeat(AppName appName) {
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
                log.error("createScheduleJob: createScheduler error: " + ex.getMessage());
                log.trace("", ex);
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
        log.debug("addJob {} completed.", name);
    }

    private void startScheduleJob() {
        String name = ScheduleJob.APP_HEARTBEAT.name();
        SundialJobScheduler.addSimpleTrigger("EVERY-" + heartbeatMs + "ms", name, -1, heartbeatMs);
    }

    public void stopAutoHeartbeat() {
        SundialJobScheduler.removeTrigger("EVERY-" + heartbeatMs + "ms");
        String name = ScheduleJob.APP_HEARTBEAT.name();
        SundialJobScheduler.stopJob(name);
        SundialJobScheduler.removeJob(name);
        log.debug("removeJob {} completed.", name);
    }
}
