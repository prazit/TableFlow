package com.tflow.controller;

import com.tflow.model.PageParameter;
import com.tflow.model.data.FileNameExtension;
import com.tflow.model.editor.BinaryFile;
import com.tflow.model.editor.DataFileType;
import com.tflow.model.editor.JavaScript;
import com.tflow.system.Environment;
import com.tflow.util.MetaDiffUtil;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.line.LineChartOptions;
import org.primefaces.model.charts.optionconfig.title.Title;
import org.primefaces.model.file.UploadedFile;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.math.BigDecimal;
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
    private List<List<MetaDiffUtil.MetaDiff>> operandCollection;
    private List<List<MetaDiffUtil.MetaDiff>> operatorCollection;

    private LineChartModel cartesianLinearModel;
    private int periodicity;

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

    public LineChartModel getCartesianLinearModel() {
        return cartesianLinearModel;
    }

    public void setCartesianLinearModel(LineChartModel cartesianLinearModel) {
        this.cartesianLinearModel = cartesianLinearModel;
    }

    public int getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(int periodicity) {
        this.periodicity = periodicity;
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
        operandCollection = new ArrayList<>();
        operatorCollection = new ArrayList<>();
        transactionData = defaultTransactionData();
        collectionData = new ArrayList<>(Arrays.asList("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));
        metaDiffData = "";

        /*Apache Math*/
        initCartesianLinearModel();
        periodicity = 4;
    }

    public void throwException() throws Exception {
        throw new Exception("Testing Throw Exception from Action Listener");
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

    public void prototypeCreateMetaDiffData() {
        createMetaDiffData();
        /*auto push next button*/
        prototypeCollectSameOperand();
    }

    private void createMetaDiffData() {
        log.debug("createMetaDiffData:fromClient");
        log.debug("transactionData='{}'", transactionData);
        MetaDiffUtil util = new MetaDiffUtil();

        /*parse transactionData to List of MetaDiff, line by line*/
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
                intList.add(Double.valueOf(digit).intValue());
            }

            try {
                metaDiffList = util.createMetaDiff(intList);
                metaDiffDataBuilder
                        .append("---- Line #").append(++count).append(" ----\n")
                        .append(util.toString(metaDiffList));
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

    public void prototypeCollectSameOperand() {
        log.debug("collectOperands:fromClient");
        MetaDiffUtil util = new MetaDiffUtil();

        collectionData.remove(0);
        collectionData.remove(0);
        /*collectionData.remove(0);*/

        collectionData.add(0, collectSameOperandString(true, true, operandCollection, util));
        collectionData.add(1, collectSameOperandString(true, false, operatorCollection, util));
        /*collectionData.add(2, collectSameOperandString(false, true));*/

        /*auto push next button*/
        //estimateNextValue();
    }

    private String collectSameOperandString(boolean compareOperator, boolean compareOperand, List<List<MetaDiffUtil.MetaDiff>> operandCollection, MetaDiffUtil util) {
        if (metaDiffCollection.size() == 0) {
            jsBuilder.pre(JavaScript.notiWarn, "Create Meta Diff First!");
            return "";
        }
        operandCollection.clear();

        StringBuilder collectionDataBuilder = new StringBuilder();
        List<MetaDiffUtil.MetaDiff> newMetaDiffList;
        int count = 0;
        for (List<MetaDiffUtil.MetaDiff> metaDiffList : metaDiffCollection) {
            newMetaDiffList = prototypeCollectSameOperand(metaDiffList, compareOperator, compareOperand);
            collectionDataBuilder
                    .append("---- Line #").append(++count).append(" ----\n")
                    .append(util.toString(newMetaDiffList));
            operandCollection.add(newMetaDiffList);
        }

        return collectionDataBuilder.toString();
    }

    private List<MetaDiffUtil.MetaDiff> prototypeCollectSameOperand(List<MetaDiffUtil.MetaDiff> metaDiffList, boolean compareOperator, boolean compareOperand) {
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
                prototypeCollectSameOperand(collect.operandList, previous, current, compareOperator, compareOperand);
                if (previous.operandList.size() == 0) {
                    previous.operandList.addAll(collect.operandList);
                }
            }

            previous = current;
        }

        return newMetaDiffList;
    }

    private void prototypeCollectSameOperand(List<MetaDiffUtil.Operand> operandList, MetaDiffUtil.MetaDiff previous, MetaDiffUtil.MetaDiff current, boolean compareOperator, boolean compareOperand) {
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

    public void prototypeEstimateNextValue() {
        log.debug("estimateNextValue:fromClient");
        MetaDiffUtil util = new MetaDiffUtil();
        List<MetaDiffUtil.MetaDiff> metaDiffList;

        /*parse transactionData to TransactionObject*/
        StringBuilder metaDiffDataBuilder = new StringBuilder();
        String[] lines = transactionData.split("\\n");
        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            String[] cols = lines[lineIndex].split("[,]");
            metaDiffList = getMaxWeightList(operandCollection.get(lineIndex), operatorCollection.get(lineIndex));
            double value = util.estimate(Double.valueOf(cols[cols.length - 1]).intValue(), metaDiffList.get(metaDiffList.size() - 1));
            lines[lineIndex] += "," + Double.toString(value);
        }

        transactionData = String.join("\n", lines);
    }

    private List<MetaDiffUtil.MetaDiff> getMaxWeightList(List<MetaDiffUtil.MetaDiff> operandList, List<MetaDiffUtil.MetaDiff> operatorList) {
        List<MetaDiffUtil.MetaDiff> maxList;

        /*First Step: count not empty for weight*/
        int operandWeight = countNotEmpty(operandList);
        int operatorWeight = countNotEmpty(operatorList);
        maxList = (operandWeight == Math.max(operandWeight, operatorWeight)) ? operandList : operatorList;
        return maxList;

        /*Second Step: remove operands and keep max-weight only*/
        //return maxWeightOnly(maxList);
    }

    private List<MetaDiffUtil.MetaDiff> maxWeightOnly(List<MetaDiffUtil.MetaDiff> metaDiffList) {
        /*TODO: remove operands and keep max-weight only*/

        for (MetaDiffUtil.MetaDiff metaDiff : metaDiffList) {

        }

        return null;
    }

    private int countNotEmpty(List<MetaDiffUtil.MetaDiff> operandList) {
        int weight = 0;
        for (MetaDiffUtil.MetaDiff metaDiff : operandList) {
            if (metaDiff.operandList.size() > 0) weight++;
        }
        return weight;
    }

    public void patternCreateMetaDiffData() {
        createMetaDiffData();

        /*auto push next button*/
        patternCollectSameOperand();
    }

    public void patternCollectSameOperand() {
        log.debug("patternCollectSameOperand:fromClient");
        MetaDiffUtil util = new MetaDiffUtil();

        collectionData.remove(0);
        collectionData.remove(0);
        collectionData.remove(0);
        collectionData.remove(0);

        List<List<MetaDiffUtil.MetaDiffPattern>> sameOperandFullPatternCollection = new ArrayList<>();
        List<List<MetaDiffUtil.MetaDiffPattern>> sameOperatorFullPatternCollection = new ArrayList<>();

        List<List<MetaDiffUtil.MetaDiffPattern>> sameOperandHalfPatternCollection = new ArrayList<>();
        List<List<MetaDiffUtil.MetaDiffPattern>> sameOperatorHalfPatternCollection = new ArrayList<>();

        collectionData.add(0, collectPatternTo(sameOperandFullPatternCollection, true, true, util));
        collectionData.add(1, collectPatternTo(sameOperatorFullPatternCollection, true, false, util));
        collectionData.add(2, collectPatternTo(sameOperandHalfPatternCollection, false, true, util));
        collectionData.add(3, collectPatternTo(sameOperatorHalfPatternCollection, false, false, util));

        /*auto push next button*/
        //patternEstimateNextValue();
    }

    private String collectPatternTo(List<List<MetaDiffUtil.MetaDiffPattern>> patternCollection, boolean fullPattern, boolean needSameOperand, MetaDiffUtil util) {
        StringBuilder stringBuilder = new StringBuilder();
        int lineIndex = 0;

        List<List<MetaDiffUtil.MetaDiff>> sourceList = this.metaDiffCollection;
        patternCollection.clear();

        List<MetaDiffUtil.MetaDiffPattern> patternList;
        for (List<MetaDiffUtil.MetaDiff> source : sourceList) {
            patternList = new ArrayList<>();
            patternCollection.add(patternList);

            for (int patternLength = source.size() / 2; patternLength > 1; patternLength--) {
                //if (fullPattern) {
                patternList.addAll(collectPossiblePattern(source, patternLength, needSameOperand, util));
                //} else {
                /*TODO: halfPattern is in Advanced Feature*/
                //}
            }

            stringBuilder
                    .append("---- Line #").append(++lineIndex).append(" ----\n")
                    .append(util.toString(patternList));
        }

        return stringBuilder.toString();
    }

    /**
     * @return all possible patterns at index, all patterns always have the same patternLength
     */
    private List<MetaDiffUtil.MetaDiffPattern> collectPossiblePattern(List<MetaDiffUtil.MetaDiff> source, int patternLength, boolean needSameOperand, MetaDiffUtil util) {
        if (log.isDebugEnabled()) log.debug("collectPossiblePattern: source[{}], patternLength:{}, needSameOperand:{}", source.size(), patternLength, needSameOperand);
        List<MetaDiffUtil.MetaDiffPattern> possiblePatternList = new ArrayList<>();
        List<MetaDiffUtil.MetaDiffPattern> workingPatternList;

        /*------- collect source of pattern -------*/
        List<MetaDiffUtil.MetaDiff> truncatedSource = new ArrayList<>();
        int sourceSize = source.size();
        int index = sourceSize - patternLength;
        for (int srcIndex = index; srcIndex < sourceSize; srcIndex++) {
            truncatedSource.add(source.get(srcIndex));
        }

        MetaDiffUtil.MetaDiffPattern pattern;
        workingPatternList = new ArrayList<>();

        /*INCORRECT: collect pattern by same operator only*/
        /*{
            boolean foundSame;
            for (MetaDiffUtil.Operator operator : MetaDiffUtil.Operator.values()) {
                pattern = util.newMetaDiffPattern(source);
                foundSame = false;
                for (MetaDiffUtil.MetaDiff metaDiff : truncatedSource) {
                    foundSame = false;
                    for (MetaDiffUtil.Operand operand : metaDiff.operandList) {
                        if (operand.operator == operator) {
                            pattern.pattern.add(operand);
                            foundSame = true;
                            break;
                        }
                    }
                    if (!foundSame) break;
                }
                if (foundSame) workingPatternList.add(pattern);
            }
        }*/

        /*------- create possible patterns from truncatedSource -------*/
        workingPatternList.add(util.newMetaDiffPattern(source));
        collectOperand(workingPatternList, truncatedSource, patternLength, 0, 0, util);
        log.debug("patterns from collectOperand: {}", Arrays.toString(workingPatternList.toArray()));

        /*------- find pattern appearance -------*/
        for (MetaDiffUtil.MetaDiffPattern workingPattern : workingPatternList) {
            int exclusiveIndex = index - 1;
            int foundIndex = 0;
            if (needSameOperand) {
                while ((foundIndex = findPatternSameOperand(workingPattern, source, foundIndex, exclusiveIndex)) >= 0) {
                    workingPattern.appearance.add(foundIndex++);
                }
            } else {
                while ((foundIndex = findPatternSameOperator(workingPattern, source, foundIndex, exclusiveIndex)) >= 0) {
                    workingPattern.appearance.add(foundIndex++);
                }
            }
            workingPattern.appearance.add(index);

            if (workingPattern.appearance.size() > 1) {
                possiblePatternList.add(workingPattern);
            }
        }

        return possiblePatternList;
    }

    /**
     * @param fromIndex inclusive
     * @param toIndex   exclusive
     * @return index of first appearance of pattern, otherwise -1
     */
    private int findPatternSameOperand(MetaDiffUtil.MetaDiffPattern pattern, List<MetaDiffUtil.MetaDiff> source, int fromIndex, int toIndex) {
        MetaDiffUtil.MetaDiff sourceItem;
        int relativeIndex;
        int foundCount = 0;
        boolean found;
        for (int sourceIndex = fromIndex; sourceIndex < toIndex; sourceIndex++) {
            relativeIndex = 0;
            for (MetaDiffUtil.Operand patternItem : pattern.pattern) {
                sourceItem = source.get(sourceIndex + relativeIndex);
                relativeIndex++;
                found = false;
                for (MetaDiffUtil.Operand sourceOperand : sourceItem.operandList) {
                    if (patternItem.operator == sourceOperand.operator && patternItem.operand == sourceOperand.operand) {
                        found = true;
                        foundCount++;
                        break;
                    }
                }
                if (!found) break;
            }
            if (foundCount == pattern.pattern.size()) {
                log.debug("findPatternSameOperand: found index = {}", sourceIndex);
                return sourceIndex;
            }
        }
        return -1;
    }

    /**
     * @param fromIndex inclusive
     * @param toIndex   exclusive
     * @return index of first appearance of pattern, otherwise -1
     */
    private int findPatternSameOperator(MetaDiffUtil.MetaDiffPattern pattern, List<MetaDiffUtil.MetaDiff> source, int fromIndex, int toIndex) {
        MetaDiffUtil.MetaDiff sourceItem;
        int relativeIndex;
        int foundCount = 0;
        boolean found;
        for (int sourceIndex = fromIndex; sourceIndex < toIndex; sourceIndex++) {
            relativeIndex = 0;
            for (MetaDiffUtil.Operand patternItem : pattern.pattern) {
                sourceItem = source.get(sourceIndex + relativeIndex);
                relativeIndex++;
                found = false;
                for (MetaDiffUtil.Operand sourceOperand : sourceItem.operandList) {
                    if (patternItem.operator == sourceOperand.operator) {
                        found = true;
                        foundCount++;
                        break;
                    }
                }
                if (!found) break;
            }
            if (foundCount == pattern.pattern.size()) {
                log.debug("findPatternSameOperator: found index = {}", sourceIndex);
                return sourceIndex;
            }
        }
        return -1;
    }

    private int collectOperandCount;

    /**
     * (root)
     * target: pattern[0]
     * target: pattern[0,0]
     * target: pattern[0,0,0]       // new, copy, remove last operand
     * // increase operandIndex
     * target: new pattern[0,0,1]   // new, copy, remove last operand
     * <p>
     * target: new pattern[0,1]
     * target: pattern[0,1,0]
     * <p>
     * target: new pattern[0,1,1]
     * <p>
     * (root)
     * target: new pattern[1]
     * target: pattern[1,0]
     * target: pattern[1,0,0]
     * <p>
     * target: new pattern[1,0,1]
     * <p>
     * target: new pattern[1,1]
     * target: pattern[1,1,0]
     * <p>
     * target: new pattern[1,1,1]
     */
    private void collectOperand(List<MetaDiffUtil.MetaDiffPattern> patternList, List<MetaDiffUtil.MetaDiff> trunk, int patternLength, int patternPosition, int operandIndex, MetaDiffUtil util) {
        if (patternPosition == 0 && operandIndex == 0) {
            collectOperandCount = 0;
            if (log.isDebugEnabled()) log.debug("start collectOperand: truck:{}", Arrays.toString(trunk.toArray()));
        } else {
            collectOperandCount++;
        }
        if (collectOperandCount >= 1000) {
            /*dead loop and stack overflow protection*/
            return;
        }

        List<MetaDiffUtil.Operand> operandList = trunk.get(patternPosition).operandList;
        MetaDiffUtil.MetaDiffPattern pattern = patternList.get(patternList.size() - 1);

        while (pattern.pattern.size() > patternPosition) {
            pattern.pattern.remove(pattern.pattern.size() - 1);
        }

        pattern.pattern.add(operandList.get(operandIndex));
        log.debug("collectOperand: patternPosition:{}, operandIndex:{}, pattern:{}", patternPosition, operandIndex, pattern);

        if (patternPosition == patternLength - 1) {
            // new, copy, remove last operand
            MetaDiffUtil.MetaDiffPattern newPattern = util.newMetaDiffPattern(pattern.source);
            for (int i = 0; i < pattern.pattern.size() - 1; i++) {
                newPattern.pattern.add(pattern.pattern.get(i));
            }
            patternList.add(newPattern);

            if (operandIndex == operandList.size() - 1) {
                return;
            }

        } else {
            collectOperand(patternList, trunk, patternLength, patternPosition + 1, 0, util);
        }

        if (operandIndex + 1 < operandList.size()) {
            collectOperand(patternList, trunk, patternLength, patternPosition, operandIndex + 1, util);
        }

    }

    /**
     * Prediction
     */
    public void patternEstimateNextValue() {
        log.debug("patternEstimateNextValue:fromClient");

        /* TODO: Choose Pattern to use for estimate
         * case1: if. fond pattern with sameOperand : use sameOperandFullPatternCollection/sameOperandHalfPatternCollection (weight by ?)
         * case2: if. found pattern with sameOperator : use sameOperatorFullPatternCollection/sameOperatorHalfPatternCollection (weight by ?)
         * case3: else. : use operand from first MetaDiff (weight by Operator.weight)
         */

    }


    public List<List<BigDecimal>> extractTransactionData() {
        /*parse transactionData to List of MetaDiff, line by line*/
        String[] lines = transactionData.split("\\n");
        List<List<BigDecimal>> transactionSetList = new ArrayList<>();
        List<BigDecimal> transactionDataSet;

        int count = 0;
        for (String line : lines) {
            if (line.isEmpty()) continue;

            String[] trans = line.split("[,]");
            transactionDataSet = new ArrayList<>();
            for (String digit : trans) {
                transactionDataSet.add(BigDecimal.valueOf(Double.parseDouble(digit)));
            }

            transactionSetList.add(transactionDataSet);
        }

        return transactionSetList;
    }

    /**
     * Predict using Apache Commons Math
     */
    public void predictNextValue() {
        linearRegression(periodicity);
        interpolation(periodicity);
    }

    /**
     * TODO: about Interpolation see https://commons.apache.org/proper/commons-math/userguide/analysis.html
     * @param periodicity
     */
    private void interpolation(int periodicity) {

        // Notice: change Interpolator here
        UnivariateInterpolator interpolator = new SplineInterpolator();

        // Add your data to the regression model
        List<List<BigDecimal>> transactionDataList = extractTransactionData();
        List<BigDecimal> decimalXList = new ArrayList<>();
        List<BigDecimal> interpolateXList;
        List<BigDecimal> interpolateYList;
        BigDecimal simX;
        StringBuilder transactionBuilder;
        StringBuilder lines = new StringBuilder();
        LineChartDataSet dataSet;
        ChartData chartData = new ChartData();
        BigDecimal data;
        int startPoint;
        int dataSetCount = 0;
        for (List<BigDecimal> decimalYList : transactionDataList) {
            transactionBuilder = new StringBuilder();

            // Use periodicity to make vary of result
            startPoint = Math.max(0, decimalYList.size() - periodicity);
            if (startPoint > 0) {
                for (int i = 0; i < startPoint; i++) {
                    transactionBuilder.append(",").append(decimalYList.get(i).toString());
                }
            }

            // Simulate X from 1
            interpolateYList = new ArrayList<>();
            interpolateXList = new ArrayList<>();
            simX = BigDecimal.ONE;
            for (int i = startPoint; i < decimalYList.size(); i++) {
                data = decimalYList.get(i);
                interpolateXList.add(simX);
                interpolateYList.add(data);
                transactionBuilder.append(",").append(data.toString());
                if (!decimalXList.contains(simX)) decimalXList.add(simX);
                simX = simX.add(BigDecimal.ONE);
            }

            // Predict Next of Y by Next of X
            double[] x = toArrayOfDouble(interpolateXList);
            double[] y = toArrayOfDouble(interpolateYList);
            log.debug("x = {}", x);
            log.debug("y = {}", y);
            BigDecimal next = BigDecimal.valueOf(interpolator.interpolate(x, y).value(simX.doubleValue()));
            decimalYList.add(next);
            if (!decimalXList.contains(simX)) decimalXList.add(simX);

            // update chart
            dataSetCount++;
            dataSet = new LineChartDataSet();
            dataSet.setLabel("Dataset #" + dataSetCount);
            dataSet.setYaxisID("left-y-axis");
            dataSet.setFill(true);
            dataSet.setTension(0.5);
            dataSet.setData(Arrays.asList(decimalYList.toArray()));
            chartData.addChartDataSet(dataSet);

            // update transactionData
            transactionBuilder.append(",").append(next.toString());
            lines.append(transactionBuilder.toString().substring(1)).append("\n");
        }

        this.transactionData = lines.toString();
        setChartData(chartData, decimalXList);
    }

    private double[] toArrayOfDouble(List<BigDecimal> decimalList) {
        double[] db = new double[decimalList.size()];
        for (int i = 0; i < db.length; i++)
            db[i] = decimalList.get(i).doubleValue();
        return db;
    }

    private void linearRegression(int periodicity) {

        // Create a new SimpleRegression instance
        SimpleRegression regression;

        // Add your data to the regression model
        List<List<BigDecimal>> transactionDataList = extractTransactionData();
        List<BigDecimal> decimalXList = new ArrayList<>();
        BigDecimal simX, maxX = BigDecimal.ZERO;
        StringBuilder transactionBuilder;
        StringBuilder lines = new StringBuilder();
        LineChartDataSet dataSet;
        ChartData chartData = new ChartData();
        BigDecimal data;
        int startPoint;
        int dataSetCount = 0;
        for (List<BigDecimal> decimalYList : transactionDataList) {
            transactionBuilder = new StringBuilder();
            regression = new SimpleRegression();

            // Use periodicity to make result of Moving Average
            startPoint = Math.max(0, decimalYList.size() - periodicity);
            if (startPoint > 0) {
                for (int i = 0; i < startPoint; i++) {
                    transactionBuilder.append(",").append(decimalYList.get(i).toString());
                }
            }

            // Simulate X from 1
            simX = BigDecimal.ONE;
            for (int i = startPoint; i < decimalYList.size(); i++) {
                data = decimalYList.get(i);

                regression.addData(simX.doubleValue(), data.doubleValue());
                transactionBuilder.append(",").append(data.toString());

                if (!decimalXList.contains(simX)) decimalXList.add(simX);
                simX = simX.add(BigDecimal.ONE);
            }

            // Predict Next of Y by Next of X
            BigDecimal next = BigDecimal.valueOf(regression.predict(simX.doubleValue()));
            decimalYList.add(next);
            if (!decimalXList.contains(simX)) decimalXList.add(simX);

            // update chart
            dataSetCount++;
            dataSet = new LineChartDataSet();
            dataSet.setLabel("Dataset #" + dataSetCount);
            dataSet.setYaxisID("left-y-axis");
            dataSet.setFill(true);
            dataSet.setTension(0.5);
            dataSet.setData(Arrays.asList(decimalYList.toArray()));
            chartData.addChartDataSet(dataSet);

            // update transactionData
            transactionBuilder.append(",").append(next.toString());
            lines.append(transactionBuilder.toString().substring(1)).append("\n");
        }

        this.transactionData = lines.toString();
        setChartData(chartData, decimalXList);
    }

    private void initCartesianLinearModel() {
        ChartData chartData = new ChartData();
        LineChartDataSet dataSet = new LineChartDataSet();
        dataSet.setLabel("Dataset");
        dataSet.setYaxisID("left-y-axis");
        dataSet.setFill(true);
        dataSet.setTension(0.5);
        dataSet.setData(new ArrayList<>());
        chartData.addChartDataSet(dataSet);
        setChartData(chartData, new ArrayList<>());
    }

    private void setChartData(ChartData chartData, List<BigDecimal> decimalXList) {
        cartesianLinearModel = new LineChartModel();

        List<String> labels = new ArrayList<>();
        for (BigDecimal decimal : decimalXList) {
            labels.add(decimal.toString());
        }
        chartData.setLabels(labels);
        cartesianLinearModel.setData(chartData);

        //Options
        LineChartOptions options = new LineChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setId("left-y-axis");
        linearAxes.setPosition("left");
        CartesianLinearAxes linearAxes2 = new CartesianLinearAxes();
        linearAxes2.setId("right-y-axis");
        linearAxes2.setPosition("right");

        cScales.addYAxesData(linearAxes);
        cScales.addYAxesData(linearAxes2);
        options.setScales(cScales);

        Title title = new Title();
        title.setDisplay(true);
        title.setText("Cartesian Linear Chart");
        options.setTitle(title);

        cartesianLinearModel.setOptions(options);
    }

}