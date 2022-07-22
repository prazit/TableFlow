package com.tflow.model.editor;

import com.tflow.util.FacesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Automatic remove duplicated javascript statement.<br/>
 * Part to 3 groups (pre, current, post).<br/>
 * One time use guarantee (toString).<br/>
 * jQuery Defer option (toDeferString).
 */
public class JavaScriptBuilder {

    private Logger log = LoggerFactory.getLogger(JavaScriptBuilder.class);

    private Map<String, Integer> preMap;
    private Map<String, Integer> jsMap;
    private Map<String, Integer> postMap;

    public JavaScriptBuilder() {
        preMap = new HashMap<>();
        jsMap = new HashMap<>();
        postMap = new HashMap<>();
    }

    public JavaScriptBuilder pre(String jsStatement) {
        preMap.put(jsStatement, preMap.size());
        return this;
    }

    public JavaScriptBuilder pre(JavaScript javaScript, Object... params) {
        String script = javaScript.getScript();
        if (params != null && params.length > 0) {
            script = String.format(script, params);
        }
        preMap.put(script, preMap.size());
        return this;
    }

    public JavaScriptBuilder append(String jsStatement) {
        jsMap.put(jsStatement, jsMap.size());
        return this;
    }

    public JavaScriptBuilder append(JavaScript javaScript) {
        jsMap.put(javaScript.getScript(), jsMap.size());
        return this;
    }

    public JavaScriptBuilder post(String jsStatement) {
        postMap.put(jsStatement, postMap.size());
        return this;
    }

    public JavaScriptBuilder post(JavaScript javaScript, Object... params) {
        String script = javaScript.getScript();
        if (params != null && params.length > 0) {
            script = String.format(script, params);
        }
        postMap.put(script, postMap.size());
        return this;
    }

    private String getJavaScript(Map<String, Integer> jsMap) {
        StringBuilder stringBuilder = new StringBuilder();

        Map<Integer, String> sortMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : jsMap.entrySet()) {
            sortMap.put(entry.getValue(), entry.getKey());
        }
        Object[] sorted = sortMap.keySet().toArray();
        Arrays.sort(sorted);

        for (Object key : sorted) {
            stringBuilder.append(sortMap.get(key));
        }

        return stringBuilder.toString();
    }

    public boolean postContains(String jsStatement) {
        return postMap.containsKey(jsStatement);
    }

    public boolean postContains(JavaScript javaScript) {
        return postMap.containsKey(javaScript.getScript());
    }

    public JavaScriptBuilder preClear() {
        preMap.clear();
        return this;
    }

    public JavaScriptBuilder clear() {
        jsMap.clear();
        return this;
    }

    public JavaScriptBuilder postClear() {
        postMap.clear();
        return this;
    }

    private void clearAll(){
        preMap.clear();
        jsMap.clear();
        postMap.clear();
    }

    @Override
    public String toString() {
        if (postMap.containsKey(JavaScript.refreshFlowChart.getScript())) {
            jsMap.clear();
            postMap.clear();
            post(JavaScript.refreshFlowChart);
        }

        return getJavaScript(preMap) + getJavaScript(jsMap) + getJavaScript(postMap);
    }

    public boolean isEmpty() {
        return preMap.isEmpty() && jsMap.isEmpty() && postMap.isEmpty();
    }

    public void runOnClient() {
        runOnClient(false);
    }


    public void runOnClient(boolean defer) {
        String javaScript = toString();
        if (javaScript.isEmpty()) return;
        clearAll();

        if (defer) javaScript = "$(function(){" + javaScript + "});";
        log.warn("runOnClient:{}", javaScript);

        FacesUtil.runClientScript(javaScript);
    }
}
