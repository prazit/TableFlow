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

public class ZKConfiguration implements Watcher {

    private Logger log = LoggerFactory.getLogger(ZKConfiguration.class);

    private ZooKeeper zooKeeper;
    private String configRoot;

    /**
     * Send heartbeat to zookeeper after milliseconds.
     */
    private long intervalCheckHeartbeat;
    private long heartbeatDuration;
    private long nextHeartbeat;

    private Watcher watcher;

    public ZKConfiguration() {
        /*nothing*/
    }

    public void connect() throws IOException, KeeperException, InterruptedException {
        if (zooKeeper != null) return;

        /*TODO: load all below from zookeeper.properties in same package of this class*/
        String connectString = "localhost:2181";
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
            Thread.sleep(1000);
        }

        heartbeatTick();
    }

    public void initial() throws KeeperException, InterruptedException {
        createHeartbeatJob();
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

    private void createHeartbeatJob() {
        log.trace("createHeartbeatJob.");
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
            log.error("createHeartbeatJob: createScheduler error: ", ex);
            return;
        }

        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("ZKConfiguration", this);

        String name = SchdulerJob.ZOOKEEPER_HEARTBEAT.name();
        SundialJobScheduler.addJob(name, SchdulerJob.ZOOKEEPER_HEARTBEAT.getJobClass(), parameterMap, false);
        log.debug("addJob {} completed.", SchdulerJob.ZOOKEEPER_HEARTBEAT);
        SundialJobScheduler.addSimpleTrigger(name + "Trigger", name, -1, intervalCheckHeartbeat);
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

    public void set(ZKConfigNode configuration, String value) throws KeeperException, InterruptedException {
        log.debug("set(configNode:{}, value:'{}')", configuration, value);
        try {
            zooKeeper.setData(getNode(configuration), toByteArray(value), configuration.getVersion());
        } catch (KeeperException ex) {
            if (ex.getMessage().contains("BadVersion")) {
                updateVersion(configuration);
                set(configuration, value);
            }
        }

        heartbeatTick();
    }

    public void set(ZKConfigNode configuration, long value) throws InterruptedException {
        log.debug("set(configNode:{}, value:{})", configuration, value);
        /* unreadable value in zookeeper is not recommended.
        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
        byteBuffer.putLong(value);
        byte[] bytes = byteBuffer.array();*/

        try {
            zooKeeper.setData(getNode(configuration), toByteArray(value), configuration.getVersion());
        } catch (KeeperException ex) {
            if (ex.getMessage().contains("BadVersion")) {
                updateVersion(configuration);
                set(configuration, value);
            } else {
                log.error("", ex);
            }
        }

        heartbeatTick();
    }

    public String getString(ZKConfigNode configuration) throws KeeperException, InterruptedException {
        Stat stat = new Stat();
        byte[] bytes = zooKeeper.getData(getNode(configuration), false, stat);
        configuration.setVersion(stat.getVersion());
        heartbeatTick();
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    public long getLong(ZKConfigNode configuration) throws KeeperException, InterruptedException {
        /* unreadable data in zookeeper is not recommended.
        byte[] bytes = zooKeeper.getData(getNode(configuration), false, null);
        return ByteBuffer.wrap(bytes).getLong();*/

        String stringValue = getString(configuration);
        heartbeatTick();
        return Long.parseLong(stringValue);
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
