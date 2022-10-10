package com.tflow.controller;

import com.tflow.model.PageParameter;
import com.tflow.model.data.FileNameExtension;
import com.tflow.model.editor.BinaryFile;
import com.tflow.model.editor.DataFileType;
import com.tflow.model.editor.JavaScript;
import com.tflow.system.Environment;
import com.tflow.util.MetaDiffUtil;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private ArrayList<String> inputChips;

    private String transactionData;
    private ArrayList<String> collectionData;
    private String metaDiffData;
    private List<List<MetaDiffUtil.MetaDiff>> metaDiffCollection;

    @Override
    public Page getPage() {
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

    public ArrayList<String> getInputChips() {
        return inputChips;
    }

    public void setInputChips(ArrayList<String> inputChips) {
        this.inputChips = inputChips;
    }

    public String getTransactionData() {
        return transactionData;
    }

    public void setTransactionData(String transactionData) {
        this.transactionData = transactionData;
    }

    public String getMetaDiffData() {
        return metaDiffData;
    }

    public void setMetaDiffData(String metaDiffData) {
        this.metaDiffData = metaDiffData;
    }

    public ArrayList getCollectionData() {
        return collectionData;
    }

    public void setCollectionData(ArrayList collectionData) {
        this.collectionData = collectionData;
    }

    public List<List<MetaDiffUtil.MetaDiff>> getMetaDiffCollection() {
        return metaDiffCollection;
    }

    public void setMetaDiffCollection(List<List<MetaDiffUtil.MetaDiff>> metaDiffCollection) {
        this.metaDiffCollection = metaDiffCollection;
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

        /*inputs*/
        initInputChips();

        /*Meta Diff Data*/
        metaDiffCollection = new ArrayList<>();
        transactionData = defaultTransactionData();
        collectionData = new ArrayList<>(Arrays.asList("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));
        metaDiffData = "";
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

    public void submitInputs() {
        jsBuilder.pre(JavaScript.notiInfo, "Inputs Submitted");
        jsBuilder.pre(JavaScript.notiInfo, "DEBUG: Chips Value = '{}'", inputChips);
        log.debug("DEBUG: Chips Values = '{}'", inputChips);
    }

    public void initInputChips() {
        inputChips = new ArrayList<String>(Arrays.asList("One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten"));
    }

    private String defaultTransactionData() {
        return "1,2,4,5,7,8,10\n" +
                "10,20,30,40,50\n" +
                "5,50,500,5000,50000";
    }

    public void createMetaDiffData() {
        log.debug("createMetaDiffData:fromClient");
        log.debug("transactionData='{}'", transactionData);
        MetaDiffUtil util = new MetaDiffUtil();

        /*parse transactionData to TransactionObject*/
        StringBuilder metaDiffDataBuilder = new StringBuilder();
        String[] lines = transactionData.split("\\n");
        metaDiffCollection = new ArrayList<>();
        List<MetaDiffUtil.MetaDiff> metaDiffList;
        int count = 0;
        for (String line : lines) {
            if (line.isEmpty()) continue;

            String[] trans = line.split("[,]");
            List<Integer> intList = new ArrayList<>();
            for (String digit : trans) {
                intList.add(Integer.valueOf(digit));
            }

            try {
                metaDiffList = util.createMetaDiff(intList);
                metaDiffDataBuilder
                        .append("---- Line #").append(++count).append(" ----\n")
                        .append(toString(metaDiffList));
                metaDiffCollection.add(metaDiffList);
            } catch (Exception ex) {
                String msg = "createMetaDiff failed!\n{}:{}";
                Object[] objects = new Object[]{ex.getClass().getSimpleName(), ex.getMessage()};
                jsBuilder.pre(JavaScript.notiError, msg, objects);
                log.error(msg, objects);
                log.trace("", ex);
            }
        }

        metaDiffData = metaDiffDataBuilder.toString();
    }

    private String toString(List<MetaDiffUtil.MetaDiff> metaDiffList) {
        StringBuilder builder = new StringBuilder();
        for (MetaDiffUtil.MetaDiff diff : metaDiffList) {
            builder.append(diff).append("\n");
        }
        return builder.toString();
    }

    public void collectSameOperand() {
        log.debug("collectOperands:fromClient");

        collectionData.remove(0);
        collectionData.remove(0);
        collectionData.remove(0);
        collectionData.add(0, collectSameOperandString(true, true));
        collectionData.add(1, collectSameOperandString(true, false));
        collectionData.add(2, collectSameOperandString(false, true));
    }

    private String collectSameOperandString(boolean compareOperator, boolean compareOperand) {
        if (metaDiffCollection.size() == 0) {
            jsBuilder.pre(JavaScript.notiWarn, "Create Meta Diff First!");
            return "";
        }

        StringBuilder collectionDataBuilder = new StringBuilder();
        List<MetaDiffUtil.MetaDiff> newMetaDiffList;
        int count = 0;
        for (List<MetaDiffUtil.MetaDiff> metaDiffList : metaDiffCollection) {
            newMetaDiffList = collectSameOperand(metaDiffList, compareOperator, compareOperand);
            collectionDataBuilder
                    .append("---- Line #").append(++count).append(" ----\n")
                    .append(toString(newMetaDiffList));
        }

        return collectionDataBuilder.toString();
    }

    private List<MetaDiffUtil.MetaDiff> collectSameOperand(List<MetaDiffUtil.MetaDiff> metaDiffList, boolean compareOperator, boolean compareOperand) {
        MetaDiffUtil util = new MetaDiffUtil();
        List<MetaDiffUtil.MetaDiff> newMetaDiffList = new ArrayList<>();

        MetaDiffUtil.MetaDiff previous = null;
        MetaDiffUtil.MetaDiff collect = null;
        for (MetaDiffUtil.MetaDiff current : metaDiffList) {
            collect = util.newMetaDiff();
            collect.current = current.current;
            collect.next = current.next;
            newMetaDiffList.add(collect);

            if (previous != null) {
                collectSameOperand(collect.operandList, previous, current, compareOperator, compareOperand);
                if (previous.operandList.size() == 0) {
                    previous.operandList.addAll(collect.operandList);
                }
            }

            previous = current;
        }

        return newMetaDiffList;
    }

    private void collectSameOperand(List<MetaDiffUtil.Operand> operandList, MetaDiffUtil.MetaDiff previous, MetaDiffUtil.MetaDiff current, boolean compareOperator, boolean compareOperand) {
        List<MetaDiffUtil.Operand> currentOperandList = current.operandList;
        for (MetaDiffUtil.Operand operand : previous.operandList) {
            if (containsOperand(operand, currentOperandList, compareOperator, compareOperand)) {
                operandList.add(operand);
            }
        }
    }

    private boolean containsOperand(MetaDiffUtil.Operand operand, List<MetaDiffUtil.Operand> operandList, boolean compareOperator, boolean compareOperand) {
        if (compareOperator && compareOperand) for (MetaDiffUtil.Operand compare : operandList) {
            if (compare.isSame(operand)) return true;
        }
        else if (compareOperator) for (MetaDiffUtil.Operand compare : operandList) {
            if (compare.isSameOperator(operand)) return true;
        }
        else for (MetaDiffUtil.Operand compare : operandList) {
                if (compare.isSameOperand(operand)) return true;
            }
        return false;
    }
}