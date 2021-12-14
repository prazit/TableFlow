package com.tflow.controller;

import org.slf4j.Logger;
import com.tflow.system.Application;

import javax.inject.Inject;
import java.io.Serializable;

public class BaseController implements Serializable {

    @Inject
    Logger log;

    @Inject
    Application application;

    private boolean init = true;

    public String getForceReloadResources() {
        log.trace("BaseController.getForceReloadResources()");
        if (init) {
            init = false;
            application.refreshForceReloadResources();
        }
        return application.getForceReloadResources();
    }

}
