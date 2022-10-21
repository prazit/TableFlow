package com.tflow.model.editor;

/**
 * Java script that called in the flowchart.js<br/>
 * <li>FaceUtil.runClientScript(JavaScript.refreshFlowChart);</li><br/>
 * <br/>
 * if you call these script from editor.js always need to call like this:<br/>
 * <li>FaceUtil.runClientScript(JavaScript.contentWindow + JavaScript.refreshFlowChart);</li><br/>
 */
public enum JavaScript {

    refreshDatabaseList("refreshDatabaseList();"),
    refreshSFTPList("refreshSFTPList();"),
    refreshLocalList("refreshLocalList();"),

    setFlowChart("setFlowchart('%s');"),
    refreshFlowChart("refreshFlowChart();"),
    refreshStepList("refreshStepList();"),

    lineStart("lineStart();"),
    lineEnd("lineEnd();"),

    selectObject("selectObject('%s');"),
    selectAfterUpdateEm("postUpdate(function(){selectObject('%s');});"),
    updateEm("updateEm('%s');"),
    updateEmByClass("updateEmByClass('%s');"),

    preStartup("LeaderLine.positionByWindowResize = false;"),
    postStartup("startup();"),

    contentWindow("contentWindow."),

    notiInfo(null),
    notiError(null),
    notiWarn(null),
    noti("noti();"),

    /**
     * <b>Parameters:</b><br/>
     * <ol>
     * <li>millis - wait before setFocus in milliseconds</li>
     * <li>propertyVar - specified property to got the focus</li>
     * </ol>
     */
    focusProperty("focusProperty(%s,'%s');"),
    updateProperty("updateProperty('%s');"),
    refreshProperties("refreshProperties();"),

    showStepList("showStepList(%s,%s);"),
    showPropertyList("showPropertyList(%s,%s);"),
    showActionButtons("showActionButtons(%s,%s);"),
    showColumnNumbers("showColumnNumbers(%s,%s);"),
    ;

    private String javascript;

    JavaScript(String javascript) {
        this.javascript = javascript;
    }

    public String getScript() {
        return javascript;
    }
}
