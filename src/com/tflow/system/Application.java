package com.tflow.system;

import com.tflow.model.editor.Workspace;
import com.tflow.util.DateTimeUtil;
import com.tflow.zookeeper.*;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;

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
    private AppsHeartbeat appsHeartbeat;

    @Inject
    public Application() {
        LoggerFactory.getLogger(Workspace.class).trace("Application started.");
    }

    @PostConstruct
    public void onCreation() throws IOException {
        loadConfigs();
    }

    private void loadConfigs() {
        Properties configs = null;
        try {
            InputStream configStream = ClassLoader.getSystemResourceAsStream("tflow.properties");
            configs = new Properties();
            configs.load(configStream);
            configStream.close();
        } catch (Exception ex) {
            log.error("Load configurations failed! ", ex);
            System.exit(-1);
        }

        versionString = configs.getProperty("version");
        cssForceReload = configs.getPropertyBoolean("force.reload.resources", true);
        forceReloadResources = "";

        try {
            zkConfiguration = requiresZK(configs);
            String environmentName = zkConfiguration.getString(ZKConfigNode.ENVIRONMENT);
            environment = Environment.valueOf(environmentName);

            if (appsHeartbeat.isNewer(AppName.TABLE_FLOW, versionString)) {
                AppInfo appInfo = appsHeartbeat.getAppInfo(AppName.TABLE_FLOW);
                log.warn("Application({}) found newer version {} is running, this version {} will be terminated.", AppName.TABLE_FLOW, appInfo.getVersion(), versionString);
                System.exit(1);
            }

            appsHeartbeat = new AppsHeartbeat(zkConfiguration, configs);
            appsHeartbeat.setAutoHeartbeat(AppName.TABLE_FLOW);
            appsHeartbeat.setAppVersion(AppName.TABLE_FLOW, versionString);
        } catch (Exception ex) {
            log.error("loadConfigs failed: ", ex);
            System.exit(-1);
            /*TODO: do something to notify admin, requires ZooKeeper */
        }
    }

    private ZKConfiguration requiresZK(Properties configs) throws IOException {
        ZKConfiguration zkConfiguration = new ZKConfiguration(configs);
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
