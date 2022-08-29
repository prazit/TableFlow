package com.tflow.controller;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.Date;

@ViewScoped
@Named("unexpectedCtl")
public class UnexpectedExceptionController extends Controller {

    private Date lastExceptionTimestamp;

    @Override
    void onCreation() {
        /*nothing*/
    }

    @Override
    protected Page getPage() {
        return workspace.getCurrentPage();
    }

    public void noException() {
        log.warn("Open Unexpected Exception Page without Exception.");
        workspace.openPage(Page.GROUP);
    }

    public boolean isNewException(Date timestamp) {
        return (lastExceptionTimestamp == null) ||
                (timestamp != null && timestamp.getTime() != lastExceptionTimestamp.getTime());
    }

    public void handledException(Date timestamp) {
        lastExceptionTimestamp = timestamp;
    }
}
