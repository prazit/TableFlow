package com.tflow.controller;

import com.tflow.model.editor.JavaScript;
import com.tflow.system.Environment;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

@ViewScoped
@Named("playCtl")
public class PlayGroundController extends Controller {

    private String messageTitle;
    private String message;

    private int messageNumber;

    @Override
    protected Page getPage() {
        return Page.PLAYGROUND;
    }

    public String getMessageTitle() {
        return messageTitle;
    }

    public void setMessageTitle(String messageTitle) {
        this.messageTitle = messageTitle;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    void onCreation() {
        if (Environment.DEVELOPMENT != workspace.getEnvironment()) {
            workspace.openPage(Page.DEFAULT);
        }

        messageNumber = 0;
    }

    public void notiInfo() {
        //addMessage(FacesMessage.SEVERITY_INFO, "Info Message #" + (++message), "Message Content #" + (++message));
        messageTitle = ":: Information ::";
        message = "Information Message #" + (++messageNumber);

        jsBuilder.post(JavaScript.notiInfo, message);
        jsBuilder.runOnClient(true);
    }

    public void notiWarn() {
        jsBuilder.append(JavaScript.notiWarn, "Warning Message #" + (++messageNumber) + " Here");
        jsBuilder.runOnClient(true);
    }

    public void notiError() {
        jsBuilder.pre(JavaScript.notiError, "Error Message #" + (++messageNumber) + " Here");
        jsBuilder.runOnClient(true);
    }

    public void multiple() {
        jsBuilder.post(JavaScript.notiInfo, "Info Message #" + (++messageNumber) + " Here");
        jsBuilder.post(JavaScript.notiInfo, "Info Message #" + (++messageNumber) + " Here");

        jsBuilder.append(JavaScript.notiWarn, "Warning Message #" + (++messageNumber) + " Here");

        jsBuilder.pre(JavaScript.notiError, "Error Message #" + (++messageNumber) + " Here");
        jsBuilder.runOnClient(true);
    }

    public void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

}
