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
import java.io.FileReader;
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

    private Properties configs;
    private ZKConfiguration zkConfiguration;
    private AppsHeartbeat appsHeartbeat;

    @Inject
    public Application() {
        LoggerFactory.getLogger(Workspace.class).trace("Application started.");
    }

    @PostConstruct
    public void onCreation() throws Exception {
        loadConfigs();
    }

    private void loadConfigs() throws Exception {
        configs = null;
        String configFileName = "tflow.properties";
        try {
            configs = new Properties();
            InputStream configStream = getClass().getResourceAsStream(configFileName);
            configs.load(configStream);
            //configs.load(new FileReader(configFileName));
            configStream.close();
        } catch (Exception ex) {
            log.error("Load configurations file:" + configFileName + " failed! ", ex);
            throw ex;
        }
        log.info("{} loaded", configFileName);

        versionString = configs.getProperty("version");
        cssForceReload = configs.getPropertyBoolean("force.reload.resources", true);
        forceReloadResources = "";

        try {
            zkConfiguration = requiresZK(configs);
            log.info("Zookeeper loaded");

            String environmentName = zkConfiguration.getString(ZKConfigNode.ENVIRONMENT);
            environment = Environment.valueOf(environmentName);
            log.info("Active environment: {}", environmentName);

            appsHeartbeat = new AppsHeartbeat(zkConfiguration, configs);
            log.info("AppsHeartbeat loaded");

            if (appsHeartbeat.isNewer(AppName.TABLE_FLOW, versionString)) {
                AppInfo appInfo = appsHeartbeat.getAppInfo(AppName.TABLE_FLOW);
                log.warn("Application({}) found newer version {} is running, this version {} will be terminated.", AppName.TABLE_FLOW, appInfo.getVersion(), versionString);
                System.exit(1);
            }

            appsHeartbeat.startAutoHeartbeat(AppName.TABLE_FLOW);
            appsHeartbeat.setAppVersion(AppName.TABLE_FLOW, versionString);
            log.info("{} heartbeat started", AppName.TABLE_FLOW);

        } catch (Exception ex) {
            log.error("Load configurations failed! ", ex);
            throw ex;
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

    public String getVersionString() {
        return versionString;
    }

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

    public Properties getConfigs() {
        return configs;
    }

    public ZKConfiguration getZkConfiguration() {
        return zkConfiguration;
    }
}
