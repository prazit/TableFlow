package com.tflow.system;

import com.tflow.kafka.EnvironmentConfigs;
import com.tflow.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;

public abstract class CLIbase {

    protected Logger log;

    protected boolean polling;

    protected Environment environment;
    protected EnvironmentConfigs environmentConfigs;

    protected ZKConfiguration zkConfiguration;
    protected AppsHeartbeat appsHeartbeat;
    protected AppName appName;
    protected String appVersion;

    protected Properties configs;

    protected CLIbase(AppName appName) {
        this.appName = appName;
        log = LoggerFactory.getLogger(getClass());
    }

    protected void terminateOnNewerVersion() {
        if (appsHeartbeat.isNewer(appName, appVersion)) {
            AppInfo appInfo = appsHeartbeat.getAppInfo(appName);
            log.warn("Application({}) found newer version {} is running, this version {} will be terminated.", appName, appInfo.getVersion(), appVersion);
            System.exit(0);
        }
    }

    private ZKConfiguration createZK(Properties configs) throws Exception {
        zkConfiguration = new ZKConfiguration(configs);

        zkConfiguration.connect();
        zkConfiguration.initial();

        environment = Environment.valueOf(zkConfiguration.getString(ZKConfigNode.ENVIRONMENT));
        environmentConfigs = EnvironmentConfigs.valueOf(environment.name());

        return zkConfiguration;
    }

    public void stop() {
        polling = false;
    }

    /**
     * TODO: need Ctrl_C_Monitor like Kafka, to shutdown with return-code = 0.
     */
    public void start() {
        try {
            InputStream configStream = ClassLoader.getSystemResourceAsStream(getClass().getSimpleName().toLowerCase() + ".properties");
            configs = new Properties();
            configs.load(configStream);
            configStream.close();
            appVersion = configs.getProperty("version");
        } catch (Exception ex) {
            log.error("Load configurations failed! ", ex);
            System.exit(-1);
        }

        try {
            zkConfiguration = createZK(configs);
        } catch (Exception ex) {
            log.error("Zookeeper is required to run " + getClass().getSimpleName() + ", ", ex);
            System.exit(-1);
        }

        try {
            loadConfigs();
        } catch (Exception ex) {
            log.error("Load configs failed! ", ex);
            System.exit(-1);
        }

        try {
            appsHeartbeat = new AppsHeartbeat(zkConfiguration, configs);
            terminateOnNewerVersion();
            appsHeartbeat.startAutoHeartbeat(appName);
            appsHeartbeat.setAppVersion(appName, appVersion);
        } catch (Exception ex) {
            log.error("Cannot start AppsHeartbeat, it is required to run " + getClass().getSimpleName() + ", ", ex);
            System.exit(-1);
        }

        polling = true;
        run();
    }

    protected Properties getProperties(String prefix, Properties properties) {
        int length = prefix.length();

        Properties subset = new Properties();
        for (Object key : properties.keySet()) {
            String keyString = (String) key;
            if (keyString.startsWith(prefix)) {
                subset.put(keyString.substring(length), properties.get(key));
            }
        }

        return subset;
    }

    protected void printSystemEnv(String label) {
        log.debug("---- {}:system.properties ----", label);
        java.util.Properties systemProps = System.getProperties();
        for (Object key : systemProps.keySet()) {
            log.debug("{} = {}", key, systemProps.get(key));
        }
        log.debug("---- {}:system.environment ----", label);
        Map<String, String> systemEnv = System.getenv();
        for (String key : systemEnv.keySet()) {
            log.debug("{} = {}", key, systemEnv.get(key));
        }
        log.debug("---- {}:end of system ----", label);
    }


    /**
     * Step1. Load program configs and appVersion.
     */
    protected abstract void loadConfigs() throws Exception;

    /**
     * Step2. run program.
     */
    protected abstract void run();

}
