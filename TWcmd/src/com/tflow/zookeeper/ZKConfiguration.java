package com.tflow.zookeeper;

import com.tflow.util.DateTimeUtil;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.knowm.sundial.SundialJobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Notice: all node values from this Service-Class always in String type only.
 */
public class ZKConfiguration implements Watcher {

    private Logger log = LoggerFactory.getLogger(ZKConfiguration.class);

    private ZooKeeper zooKeeper;
    private String configRoot;

    /**
     * Send heartbeat to zookeeper after milliseconds.
     */
    private boolean heartbeatEnabled;
    private long intervalCheckHeartbeat;
    private long heartbeatDuration;
    private long nextHeartbeat;

    private ScheduleJob scheduleJob;

    private Watcher watcher;

    public ZKConfiguration() {
        /*nothing*/
    }

    public boolean isConnectionExpired() {
        return zooKeeper == null || !zooKeeper.getState().isAlive();
    }

    /**
     * @throws IOException in case of Network failure
     */
    private void reconnect() throws IOException {
        try {
            disconnect();
        } catch (InterruptedException e) {
            /*ignored*/
        }
        connect();
    }

    public void disconnect() throws InterruptedException {
        stopScheduleJob();

        if (zooKeeper == null) return;

        if (zooKeeper.getState().isConnected()) {
            zooKeeper.close();
        }
        zooKeeper = null;
    }

    /**
     * @throws IOException in case of Network failure
     */
    public void connect() throws IOException {
        if (zooKeeper != null) return;

        /*TODO: load all below from zookeeper.properties in same package of this class*/
        String connectString = "localhost:2181";
        heartbeatEnabled = false;
        int sessionTimeout = 18000;
        intervalCheckHeartbeat = 2000;
        heartbeatDuration = sessionTimeout - intervalCheckHeartbeat;
        int maxWait = 15;
        int wait = 0;
        configRoot = "/tflow-configuration";

        zooKeeper = new ZooKeeper(connectString, sessionTimeout, this);
        while (!zooKeeper.getState().isConnected()) {
            if (maxWait < ++wait) break;
            log.info("waiting zooKeeper [" + wait + "/" + maxWait + "]...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }

        heartbeatTick();
        if (scheduleJob == null) createScheduleJob(heartbeatEnabled ? ScheduleJob.ZOOKEEPER_HEARTBEAT : ScheduleJob.ZOOKEEPER_DISCONNECT);
        startScheduleJob();
    }

    public void initial() throws KeeperException, InterruptedException {
        createNodes(configRoot);

        int version;
        for (ZKConfigNode zkConfigNode : ZKConfigNode.values()) {
            String node = getNode(zkConfigNode);
            Stat stat = zooKeeper.exists(node, false);
            if (stat == null) {
                Object initialValue = zkConfigNode.getInitialValue();
                byte[] data;
                if (initialValue instanceof Long) {
                    data = toByteArray((Long) initialValue);
                } else {
                    data = toByteArray((String) initialValue);
                }
                zooKeeper.create(node, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                version = 0;
                log.info("persistent node({} = {}) created on zookeeper", node, initialValue);
            } else {
                version = stat.getVersion();
            }
            zkConfigNode.setVersion(version);
        }

        heartbeatTick();
    }

    private void heartbeatTick() {
        nextHeartbeat = DateTimeUtil.now().getTime() + heartbeatDuration;
    }

    private void createScheduleJob(ScheduleJob scheduleJob) {
        log.trace("createScheduleJob({})", scheduleJob);
        this.scheduleJob = scheduleJob;
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

        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("ZKConfiguration", this);

        String name = scheduleJob.name();
        SundialJobScheduler.addJob(name, scheduleJob.getJobClass(), parameterMap, false);
        log.debug("addJob {} completed.", scheduleJob);
    }

    private void startScheduleJob() {
        String name = scheduleJob.name();
        SundialJobScheduler.addSimpleTrigger(name + "Trigger", name, -1, intervalCheckHeartbeat);
    }

    private void stopScheduleJob() {
        try {
            SundialJobScheduler.removeTrigger(scheduleJob.name() + "Trigger");
        } catch (Exception ex) {
            /*ignored*/
        }
    }

    private void createNodes(String nodePath) throws KeeperException, InterruptedException {
        String[] nodes = nodePath.split("/");
        if (nodes.length <= 1) /*root always exists by zookeeper*/ return;

        nodePath = "";
        for (int index = 1; index < nodes.length; index++) {
            nodePath += "/" + nodes[index];
            if (zooKeeper.exists(nodePath, false) == null) {
                String string = zooKeeper.create(nodePath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.CONTAINER);
                log.debug("createNode: {} completed", nodePath);
            }
        }

        heartbeatTick();
    }

    private byte[] toByteArray(String stringValue) {
        return stringValue.getBytes(StandardCharsets.ISO_8859_1);
    }

    private byte[] toByteArray(Long longValue) {
        return String.valueOf(longValue).getBytes(StandardCharsets.ISO_8859_1);
    }

    /**
     * @throws InterruptedException in case of all unhandled failures.
     */
    public void set(ZKConfigNode configuration, String value) throws InterruptedException {
        log.debug("set(configNode:{}, value:'{}')", configuration, value);
        set(configuration, toByteArray(value));
    }

    /**
     * @throws InterruptedException in case of all unhandled failures.
     */
    public void set(ZKConfigNode configuration, long value) throws InterruptedException {
        log.debug("set(configNode:{}, value:{})", configuration, value);
        set(configuration, toByteArray(value));
    }

    private void set(ZKConfigNode configuration, byte[] data) throws InterruptedException {
        try {
            zooKeeper.setData(getNode(configuration), data, configuration.getVersion());
        } catch (KeeperException ex) {
            if (ex instanceof KeeperException.BadVersionException) {
                updateVersion(configuration);
                set(configuration, data);
            } else if (ex instanceof KeeperException.SessionExpiredException) {
                try {
                    log.info("Reconnect to zookeeper after Session Expired");
                    reconnect();
                    set(configuration, data);
                } catch (IOException e) {
                    String msg = "Reconnect to zookeeper failed by Network failure!";
                    log.error(msg + " set-value(node:{}, data{}) failed", configuration, data);
                    throw new InterruptedException(msg);
                }
            } else throw new InterruptedException("Unexpected error from zookeeper, " + ex.getClass().getSimpleName() + ":" + ex.getMessage());
        }

        heartbeatTick();
    }

    public String getString(ZKConfigNode configuration) throws InterruptedException {
        Stat stat = new Stat();
        byte[] bytes;
        try {
            bytes = zooKeeper.getData(getNode(configuration), false, stat);
        } catch (Exception ex) {
            if (ex instanceof KeeperException.SessionExpiredException || ex instanceof NullPointerException) {
                try {
                    log.info("Reconnect to zookeeper after Session Expired");
                    reconnect();
                    return getString(configuration);
                } catch (IOException e) {
                    String msg = "Reconnect to zookeeper failed by Network failure!";
                    log.error(msg + " get-value(node:{}) failed", configuration);
                    throw new InterruptedException(msg);
                }
            } else throw new InterruptedException("Unexpected error from zookeeper, " + ex.getClass().getSimpleName() + ":" + ex.getMessage());
        }

        configuration.setVersion(stat.getVersion());
        heartbeatTick();
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    public long getLong(ZKConfigNode configuration) throws InterruptedException {
        String stringValue = getString(configuration);
        heartbeatTick();

        try {
            return Long.parseLong(stringValue);
        } catch (NumberFormatException ex) {
            throw new InterruptedException("Invalid value '" + stringValue + "' for Long data-type.");
        }
    }

    public void remove(ZKConfigNode configuration) throws KeeperException, InterruptedException {
        zooKeeper.delete(getNode(configuration), configuration.getVersion());
        heartbeatTick();
    }

    public void setWatcher(Watcher watcher) {
        this.watcher = watcher;
    }

    private void updateVersion(ZKConfigNode node) {
        String nodePath = getNode(node);
        Stat stat = new Stat();
        int version = 0;
        while (version == 0) {
            try {
                zooKeeper.getData(nodePath, false, stat);
                version = stat.getVersion();
            } catch (KeeperException | InterruptedException ex) {
                /*continue to the next loop*/
            }
        }
        node.setVersion(version);

        heartbeatTick();
    }

    private String getNode(ZKConfigNode configuration) {
        return configRoot + "/" + configuration.name().toLowerCase().replaceAll("[_]", ".");
    }

    public long getNextHeartbeat() {
        return nextHeartbeat;
    }

    public String getConfigRoot() {
        return configRoot;
    }

    public void setConfigRoot(String configRoot) {
        this.configRoot = configRoot;
    }

    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }

    @Override
    public void process(WatchedEvent event) {
        if (watcher != null) {
            watcher.process(event);
        }
    }

    public boolean isConnected() {
        return zooKeeper.getState().isConnected();
    }
}
