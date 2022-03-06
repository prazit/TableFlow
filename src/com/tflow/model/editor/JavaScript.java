package com.tflow.model.editor;

/**
 * Java script that called in the flowchart.js<br/>
 * <li>FaceUtil.runClientScript(JavaScript.refreshFlowChart);</li><br/>
 * <br/>
 * if you call these script from editor.js always need to call like this:<br/>
 * <li>FaceUtil.runClientScript(JavaScript.contentWindow + JavaScript.refreshFlowChart);</li><br/>
 */
public enum JavaScript {

    refreshFlowChart("refreshFlowChart();"),
    refreshStepList("refreshStepList();"),

    lineStart("lineStart();"),
    lineEnd("lineEnd();"),

    selectAfterUpdateEm("postUpdate(function(){selectObject('%s');});"),
    updateEm("updateEm('%s');"),

    preStartup("LeaderLine.positionByWindowResize = false;"),
    postStartup("startup();"),

    contentWindow("contentWindow."),
    ;

    private String javascript;

    JavaScript(String javascript) {
        this.javascript = javascript;
    }

    public String getScript() {
        return javascript;
    }
}