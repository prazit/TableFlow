package com.tflow.controller;

import com.tflow.model.PageParameter;
import com.tflow.model.data.FileNameExtension;
import com.tflow.model.editor.BinaryFile;
import com.tflow.model.editor.DataFileType;
import com.tflow.model.editor.JavaScript;
import com.tflow.system.Environment;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.nio.charset.StandardCharsets;

@ViewScoped
@Named("playCtl")
public class PlayGroundController extends Controller {

    private boolean inDevelopment;
    private int initialSectionIndex;

    private String messageTitle;
    private String message;

    private int messageNumber;

    private UploadedFile uploadedFile;
    private BinaryFile binaryFile;
    private DataFileType dataFileType;


    @Override
    protected Page getPage() {
        return Page.PLAYGROUND;
    }

    public boolean isInDevelopment() {
        return inDevelopment;
    }

    public int getInitialSectionIndex() {
        return initialSectionIndex;
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

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public DataFileType getDataFileType() {
        return dataFileType;
    }

    public BinaryFile getBinaryFile() {
        return binaryFile;
    }

    public void setBinaryFile(BinaryFile binaryFile) {
        this.binaryFile = binaryFile;
    }

    @Override
    void onCreation() {
        Environment currentEnvironment = workspace.getEnvironment();
        inDevelopment = Environment.DEVELOPMENT == currentEnvironment;

        if (!inDevelopment) {
            log.warn("open page PlayGround is not allowed on current environment: '{}'", currentEnvironment);
            workspace.openPage(Page.DEFAULT);
        }

        String sectionIndexString = workspace.getParameterMap().get(PageParameter.SECTION_INDEX);
        if (sectionIndexString == null) {
            initialSectionIndex = 0;
        } else {
            initialSectionIndex = Integer.parseInt(sectionIndexString);
        }

        /*message section*/
        messageNumber = 0;

        /*upload file section*/
        dataFileType = DataFileType.IN_MARKDOWN;
        binaryFile = new BinaryFile();
        binaryFile.setName(dataFileType.getName());
        binaryFile.setContent(new byte[]{});
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

    public void upload(FileUploadEvent event) {
        log.debug("upload(event:{})", event);

        UploadedFile file = event.getFile();
        String fileName = file.getFileName();
        log.debug("file:'{}', content-type:'{}', source:{}:'{}', component:{}:'{}'",
                fileName,
                file.getContentType(),
                event.getSource().getClass().getName(),
                event.getSource(),
                event.getComponent().getClass().getName(),
                event.getComponent().getClientId()
        );

        binaryFile.setName(fileName);
        binaryFile.setContent(file.getContent());

        /*TODO: OWASP: change Ext by real content type from Apache Tika*/
        binaryFile.setExt(FileNameExtension.forName(fileName));

    }

    public String getBinaryFileContent() {
        return new String(binaryFile.getContent(), StandardCharsets.ISO_8859_1).replaceAll("\n", "<br/>");
    }

    public String getUploadedFileContent() {
        if (uploadedFile == null) return "Unavailable";
        return new String(uploadedFile.getContent(), StandardCharsets.ISO_8859_1).replaceAll("\n", "<br/>");
    }

    public void unknownArg(Object arg) {
        if (arg == null) log.debug("unknownArg: arg: null");
        else log.debug("unknownArg: arg:{}:{}", arg.getClass().getName(), arg);
    }

}
