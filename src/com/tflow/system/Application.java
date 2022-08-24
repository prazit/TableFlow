package com.tflow.system;

import com.tflow.model.data.ProjectDataManager;
import com.tflow.model.editor.Workspace;
import com.tflow.util.DateTimeUtil;
import com.tflow.zookeeper.ZKConfigNode;
import com.tflow.zookeeper.ZKConfiguration;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.common.ZKConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

@ApplicationScoped
@Named("app")
public class Application {
    @Inject
    private Logger log;

    //    private VersionConfigFile version;
    private String versionString;

    private boolean cssForceReload;
    private String forceReloadResources;

    private String appPath;
    private Environment environment;

    private ZKConfiguration zkConfiguration;

    @Inject
    public Application() {
        LoggerFactory.getLogger(Workspace.class).trace("Application started.");
    }

    @PostConstruct
    public void onCreation() throws IOException {
        loadConfigs();
    }

    private void loadConfigs() {
        // TODO: do this after Configuration File Module is completed, load configuration first then remove initialize below
        cssForceReload = true;
        forceReloadResources = "";

        try {
            zkConfiguration = requiresZK();
            String environmentName = zkConfiguration.getString(ZKConfigNode.ENVIRONMENT);
            environment = Environment.valueOf(environmentName);
        } catch (Exception ex) {
            /*TODO: do something to notify admin, requires ZooKeeper */
        }
    }

    private ZKConfiguration requiresZK() throws IOException {
        ZKConfiguration zkConfiguration = new ZKConfiguration();
        try {
            zkConfiguration.connect();
            zkConfiguration.initial();
        } catch (IOException | KeeperException | InterruptedException ex) {
            throw new IOException("Zookeeper is required for shared configuration!!! ", ex);
        }
        return zkConfiguration;
    }

    /*private void loadApplicationVersion() {
        log.trace("loadApplicationVersion.");
        VersionFormatter formatter = new VersionFormatter();
        version = formatter.versionConfigFile("version.property");
        versionString = formatter.versionString(version);
        log.debug("loadApplicationVersion.version = {}", versionString);

        if (cssForceReload) {
            cssFileName = "kudu." + version.getVersionNumber() + "." + version.getRevisionNumber() + "." + version.getBuildNumber() + ".css";
        } else {
            cssFileName = "kudu.css";
        }
        log.debug("cssFileName = {}", cssFileName);
    }*/

    public String getVersionString() {
        return versionString;
    }

    /*public VersionConfigFile getVersion() {
        return version;
    }*/

    public String getAppPath() {
        return appPath;
    }

    public void setAppPath(String appPath) {
        this.appPath = appPath;
    }

    public void refreshForceReloadResources() {
        if (cssForceReload) {
            forceReloadResources = "?v=" + DateTimeUtil.getDateStr(DateTimeUtil.now(), "yyMMddHHmmss");
        } else {
            forceReloadResources = "";
        }
    }

    public String getForceReloadResources() {
        return forceReloadResources;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public ZKConfiguration getZkConfiguration() {
        return zkConfiguration;
    }
}
