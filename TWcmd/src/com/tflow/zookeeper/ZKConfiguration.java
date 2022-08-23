package com.tflow.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * TODO: IMPORTANT: need unit test for
 * 1. print all in configRoot.
 * 2. read/write on the same node.
 */
public class ZKConfiguration implements Watcher {

    private Logger log = LoggerFactory.getLogger(ZKConfiguration.class);
    private ZooKeeper zooKeeper;
    private String configRoot;

    private Watcher watcher;

    public ZKConfiguration() {
        /*nothing*/
    }

    public void connect() throws IOException, KeeperException, InterruptedException {
        if (zooKeeper != null) return;

        /*TODO: load all below from zookeeper.properties in same package of this class*/
        String connectString = "localhost:2181";
        int sessionTimeout = 18000;
        int maxWait = 15;
        int wait = 0;
        configRoot = "/tflow-configuration";

        zooKeeper = new ZooKeeper(connectString, sessionTimeout, this);
        while (!zooKeeper.getState().isConnected()) {
            if (maxWait < ++wait) break;
            log.info("waiting zooKeeper [" + wait + "/" + maxWait + "]...");
            Thread.sleep(1000);
        }
    }

    public void initial() throws KeeperException, InterruptedException {
        createNodes(configRoot);

        int version;
        for (ZKConfigNode zkConfigNode : ZKConfigNode.values()) {
            Stat stat = zooKeeper.exists(getNode(zkConfigNode), false);
            if (stat == null) {
                Object initialValue = zkConfigNode.getInitialValue();
                if (initialValue instanceof Long) {
                    set(zkConfigNode, (Long) initialValue);
                } else {
                    set(zkConfigNode, (String) initialValue);
                }
                version = 0;
            } else {
                version = stat.getVersion();
            }
            zkConfigNode.setVersion(version);
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
    }

    public void set(ZKConfigNode configuration, String value) throws KeeperException, InterruptedException {
        try {
            zooKeeper.setData(getNode(configuration), value.getBytes(StandardCharsets.ISO_8859_1), configuration.getVersion());
        } catch (KeeperException ex) {
            if (ex.getMessage().contains("BadVersion")) {
                updateVersion(configuration);
                set(configuration, value);
            }
        }
    }

    public void set(ZKConfigNode configuration, long value) throws InterruptedException {
        /* unreadable value in zookeeper is not recommended.
        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
        byteBuffer.putLong(value);
        byte[] bytes = byteBuffer.array();*/

        try {
            zooKeeper.setData(getNode(configuration), String.valueOf(value).getBytes(StandardCharsets.ISO_8859_1), configuration.getVersion());
        } catch (KeeperException ex) {
            if (ex.getMessage().contains("BadVersion")) {
                updateVersion(configuration);
                set(configuration, value);
            }
        }
    }

    private String getNode(ZKConfigNode configuration) {
        return configRoot + "/" + configuration.name().toLowerCase().replaceAll("[_]", ".");
    }

    public String getString(ZKConfigNode configuration) throws KeeperException, InterruptedException {
        Stat stat = new Stat();
        byte[] bytes = zooKeeper.getData(getNode(configuration), false, stat);
        configuration.setVersion(stat.getVersion());
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    public long getLong(ZKConfigNode configuration) throws KeeperException, InterruptedException {
        /* unreadable data in zookeeper is not recommended.
        byte[] bytes = zooKeeper.getData(getNode(configuration), false, null);
        return ByteBuffer.wrap(bytes).getLong();*/

        String stringValue = getString(configuration);
        return Long.parseLong(stringValue);
    }

    public void remove(ZKConfigNode configuration) throws KeeperException, InterruptedException {
        zooKeeper.delete(getNode(configuration), configuration.getVersion());
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
}
