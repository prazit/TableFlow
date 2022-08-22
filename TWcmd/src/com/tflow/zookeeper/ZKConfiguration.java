package com.tflow.zookeeper;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
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

    public ZKConfiguration() {
        /*nothing*/
    }

    public void connect() throws IOException, KeeperException, InterruptedException {
        if (zooKeeper != null) return;

        /*TODO: load all below from zookeeper.properties in same package of this class*/
        String connectString = "localhost:2181";
        int sessionTimeout = 18000;
        configRoot = "/tflow-configuration";

        zooKeeper = new ZooKeeper(connectString, sessionTimeout, this);
        createConfigurationRoot(configRoot);
    }

    private void createConfigurationRoot(String configRoot) throws KeeperException, InterruptedException {
        if (zooKeeper.exists(configRoot, false) == null) {
            String string = zooKeeper.create(configRoot, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.CONTAINER);
            log.debug("createConfigurationRoot(configRoot:{}) = {}", configRoot, string);
        }
    }

    public void set(ZKConfigNode configuration, String value) throws KeeperException, InterruptedException {
        replace(configuration, value.getBytes(StandardCharsets.ISO_8859_1));
    }

    public void set(ZKConfigNode configuration, long value) throws KeeperException, InterruptedException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
        byteBuffer.putLong(value);
        byte[] bytes = byteBuffer.array();
        replace(configuration, bytes);
    }

    private void replace(ZKConfigNode configuration, byte[] bytes) throws KeeperException, InterruptedException {
        String node = getNode(configuration);
        try {
            zooKeeper.delete(node, 0);
        } catch (InterruptedException | KeeperException ex) {
            /*nothing*/
        }
        zooKeeper.create(node, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
    }

    private String getNode(ZKConfigNode configuration) {
        return configRoot + "/" + configuration.name().toLowerCase().replaceAll("[_]", ".");
    }

    public String getString(ZKConfigNode configuration) throws KeeperException, InterruptedException {
        byte[] bytes = zooKeeper.getData(getNode(configuration), false, null);
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    public String getString(ZKConfigNode configuration, String defaultValue) {
        log.trace("getString(config:{},default:{})", configuration, defaultValue);
        try {
            return getString(configuration);
        } catch (KeeperException | InterruptedException ex) {
            log.error("getString failed, defaultValue is returned! {}", ex.getMessage());
            try {
                set(configuration, defaultValue);
            } catch (KeeperException | InterruptedException e) {
                /*nothing*/
            }
            return defaultValue;
        }
    }

    public long getLong(ZKConfigNode configuration) throws KeeperException, InterruptedException {
        byte[] bytes = zooKeeper.getData(getNode(configuration), false, null);
        return ByteBuffer.wrap(bytes).getLong();
    }

    public long getLong(ZKConfigNode configuration, long defaultValue) {
        log.trace("getLong(config:{},default:{})", configuration, defaultValue);
        try {
            return getLong(configuration);
        } catch (KeeperException | InterruptedException ex) {
            log.error("getLong failed, defaultValue is returned! {}", ex.getMessage());
            try {
                set(configuration, defaultValue);
            } catch (KeeperException | InterruptedException e) {
                /*nothing*/
            }
            return defaultValue;
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        log.debug("ConfigurationManager.process(watchedEvent:{})", watchedEvent);
    }
}
