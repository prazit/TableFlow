package com.tflow.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.ClientInfo;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

class ZKConfigurationUT {

    ZooKeeper zooKeeper;
    ZKConfiguration zkConfig;
    String rootNode = "/";

    String indent = "";
    String indentChars = "\t";

    void println(String string) {
        System.out.println(indent + string);
    }

    void indent() {
        indent(1);
    }

    void indent(int addIndent) {
        if (addIndent > 0) {
            StringBuilder builder = new StringBuilder(indent);
            for (; addIndent > 0; addIndent--) builder.append(indentChars);
            indent = builder.toString();
            return;
        }
        // addIndex < 0
        int remove = Math.abs(addIndent) * indentChars.length();
        if (remove > indent.length()) {
            indent = "";
        } else {
            indent = indent.substring(0, indent.length() - remove);
        }
    }

    void errorBlocks() throws InterruptedException {

        /* zooKeeper.whoAmI: Unable to read additional data from server sessionid 0x1005b9ca3e60011 */
        for (ClientInfo clientInfo : zooKeeper.whoAmI()) {
            println("ClientInfo: "
                    + "AuthScheme=" + clientInfo.getAuthScheme()
                    + ", User=" + clientInfo.getUser());
        }

    }

    private String getZooKeeperStatus(ZooKeeper zooKeeper) {
        StringBuilder builder = new StringBuilder();

        ZooKeeper.States state = zooKeeper.getState();
        builder
                .append("SessionId: ").append(zooKeeper.getSessionId())
                .append("\nSessionPassword: ").append(zooKeeper.getSessionPasswd())
                .append("\nState.isConnected: ").append(state.isConnected())
                .append("\nState.isAlive: ").append(state.isAlive())
                .append("\nState.name: ").append(state.name())
                .append("\nClientConfig.isSaslClientEnabled: ").append(zooKeeper.getClientConfig().isSaslClientEnabled())
                .append("\nClientConfig.getJaasConfKey: ").append(zooKeeper.getClientConfig().getJaasConfKey())
        ;

        return builder.toString();
    }

    private void listChildOf(String node) throws KeeperException, InterruptedException {
        listChildOf(node, false);
    }

    private void listChildOf(String node, boolean intoSubNode) throws KeeperException, InterruptedException {
        println(node);
        listDataOf(node);
        //listEphemeralOf(node);
        //listACLOf(node);

        List<String> children = zooKeeper.getChildren(node, false);
        String nodeFullPath;
        if (node.compareTo("/") == 0) node = "";
        for (String child : children) {
            nodeFullPath = node + "/" + child;
            if (intoSubNode) listChildOf(nodeFullPath, intoSubNode);
        }
    }

    private void listDataOf(String node) throws KeeperException, InterruptedException {
        Stat stat = new Stat();
        byte[] data = zooKeeper.getData(node, false, stat);
        println("Data-Stat: " + getStatString(stat));
        if (data == null || data.length == 0) return;

        indent();
        println("Data: " + new String(data, StandardCharsets.ISO_8859_1));
        indent(-1);
    }

    private String getStatString(Stat stat) {
        String string = ""
                + "Version:" + stat.getVersion()
                + ",Aversion:" + stat.getAversion()
                + ",Ctime:" + stat.getCtime()
                + ",Cversion:" + stat.getCversion()
                + ",Czxid:" + stat.getCzxid()
                + ",DataLength:" + stat.getDataLength()
                + ",EphemeralOwner:" + stat.getEphemeralOwner()
                + ",Mtime:" + stat.getMtime()
                + ",Mzxid:" + stat.getMzxid()
                + ",NumChildren:" + stat.getNumChildren()
                + ",Pzxid:" + stat.getPzxid();

        return string;
    }

    void listEphemeralOf(String node) throws KeeperException, InterruptedException {
        indent();
        for (String ephemeral : zooKeeper.getEphemerals("/tflow-configuration")) {
            println("ephemeral: " + ephemeral);
        }
        indent(-1);
    }

    void listACLOf(String node) throws KeeperException, InterruptedException {
        Stat stat = new Stat();
        List<ACL> aclList = zooKeeper.getACL(node, stat);
        indent();
        println("ACL-Stat: " + stat.toString().replaceAll("\\n", ""));
        for (ACL acl : aclList) {
            println("ACL: " + acl.toString().replaceAll("\\n", ""));
        }
        indent(-1);
    }

    @BeforeEach
    void setUp() throws InterruptedException, IOException, KeeperException {
        zkConfig = new ZKConfiguration();
        connect();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void connect() throws InterruptedException, IOException, KeeperException {
        System.out.println("---- connect ----");

        zkConfig.connect();
        zkConfig.initial();

        println("ZoomKeeper Information:");
        indent();
        zooKeeper = zkConfig.getZooKeeper();
        println(getZooKeeperStatus(zooKeeper).replaceAll("\\n", "\\\n" + indent));
        indent(-1);

        println("---- connected ----");
    }

    @Test
    void listAllConfigs() throws KeeperException, InterruptedException {
        //String node = rootNode;
        String node = "/tflow-configuration";
        println("Node: " + node);
        indent(1);
        listChildOf(node, true);
        indent(-1);
    }

    @Test
    void set() throws KeeperException, InterruptedException {
        String node = "/tflow-configuration/count-heartbeat";
        byte[] data = "3".getBytes(StandardCharsets.ISO_8859_1);
        int version = 0;
        zooKeeper.create(node, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        zooKeeper.setData(node, data, version);
        println("setData successful");
    }

    @Test
    void process() {
        try {
            ZKConfiguration another = new ZKConfiguration();
            another.connect();
            another.initial();
            another.set(ZKConfigNode.LAST_TRANSACTION_ID, 456L);
            another.set(ZKConfigNode.LAST_TRANSACTION_ID, 789L);
            another.set(ZKConfigNode.LAST_TRANSACTION_ID, 123L);
            another.set(ZKConfigNode.LAST_TRANSACTION_ID, 357L);
            listAllConfigs();

        } catch (KeeperException | InterruptedException | IOException ex) {
            println(ex.getMessage());
        }
    }
}