package com.tflow.system;

import com.tflow.model.editor.User;
import com.tflow.model.editor.Workspace;
import com.tflow.system.constant.Theme;
import com.tflow.util.DateTimeUtil;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

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

    @Inject
    public Application() {
    }

    @PostConstruct
    public void onCreation() {
        /*log.trace("onCreation.");*/

        // TODO: do this after Configuration File Module is completed, load configuration first then remove initialize below
        cssForceReload = true;
        forceReloadResources = "";

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
}
