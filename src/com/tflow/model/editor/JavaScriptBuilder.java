package com.tflow.model.editor;

import com.tflow.util.DateTimeUtil;
import com.tflow.util.FacesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Automatic remove duplicated javascript statement.<br/>
 * Part to 3 groups (pre, current, post).<br/>
 * One time use guarantee (toString).<br/>
 * jQuery Defer option (toDeferString).<br/>
 * <br/>
 * Client notification messages (Primefaces) that guarantee to show without update attribute required.<br/>
 * Same features as JavaScript, use it the same way of javascript.
 */
public class JavaScriptBuilder {

    private Logger log = LoggerFactory.getLogger(JavaScriptBuilder.class);

    private Map<String, Integer> preMap;
    private Map<String, Integer> jsMap;
    private Map<String, Integer> postMap;

    private ArrayList<String> preList;
    private ArrayList<String> notiList;
    private ArrayList<String> postList;

    private long notiTimeout;

    public JavaScriptBuilder() {
        preMap = new HashMap<>();
        jsMap = new HashMap<>();
        postMap = new HashMap<>();
        preList = new ArrayList<>();
        notiList = new ArrayList<>();
        postList = new ArrayList<>();

        notiTimeout = 6000;
    }

    public JavaScriptBuilder pre(String jsStatement) {
        preMap.put(jsStatement, preMap.size());
        return this;
    }

    public JavaScriptBuilder pre(JavaScript javaScript, Object... params) {
        add(preMap, preList, javaScript, params);
        return this;
    }

    public JavaScriptBuilder append(String jsStatement) {
        jsMap.put(jsStatement, jsMap.size());
        return this;
    }

    public JavaScriptBuilder append(JavaScript javaScript, Object... params) {
        add(jsMap, notiList, javaScript, params);
        return this;
    }

    public JavaScriptBuilder post(String jsStatement) {
        postMap.put(jsStatement, postMap.size());
        return this;
    }

    public JavaScriptBuilder post(JavaScript javaScript, Object... params) {
        add(postMap, postList, javaScript, params);
        return this;
    }

    private void add(Map<String, Integer> jsMap, List<String> notiList, JavaScript javaScript, Object... params) {
        String script = javaScript.getScript();
        if (script == null) {
            /*notification*/
            notiList.add(javaScript.name() + ":" + (String) params[0] + ":" + DateTimeUtil.now().getTime());

            /*Notice: want duplicated filter when the first case occurred*/

            FacesUtil.runClientScript(JavaScript.noti.getScript());
            return;
        }

        if (params != null && params.length > 0) {
            script = String.format(script, params);
        }
        jsMap.put(script, jsMap.size());
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

    private void clearAll() {
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
        if (hasNoti()) javaScript = JavaScript.noti.getScript() + javaScript;
        if (javaScript.isEmpty()) return;
        clearAll();

        if (defer) javaScript = "$(function(){" + javaScript + "});";

        if (log.isDebugEnabled()) {
            String stackTraces = FacesUtil.getFormattedStackTrace(new Exception(""), "com.tflow", "\n");
            log.debug("runOnClient:{}\nstackTrace:{}", javaScript, stackTraces);
        }
        FacesUtil.runClientScript(javaScript);
    }

    /**
     * Called from Controller.showNoti() only, and showNoti() is calling from the remoteCommand(noti) in topMenu.xhtml.
     */
    public void runNoti() {
        if (!hasNoti()) {
            if (log.isDebugEnabled()) {
                String stackTraces = FacesUtil.getFormattedStackTrace(new Exception(""), "com.tflow", "\n");
                log.debug("call runNoti without message in notiMap!\nstackTrace:{}", stackTraces);
            }
            return;
        }

        addMessage(preList);
        addMessage(notiList);
        addMessage(postList);
    }

    private boolean hasNoti() {
        return preList.size() > 0 || notiList.size() > 0 || postList.size() > 0;
    }

    private void addMessage(List<String> notiList) {
        long now = DateTimeUtil.now().getTime();

        for (String noti : new ArrayList<>(notiList)) {
            String[] parts = noti.split("[:]");

            long time = Long.parseLong(parts[2]);
            long diff = now - time;
            if (diff >= notiTimeout) {
                notiList.remove(noti);
                continue;
            }

            switch (JavaScript.valueOf(parts[0])) {
                case notiInfo:
                    FacesUtil.addInfo(parts[1]);
                    break;
                case notiWarn:
                    FacesUtil.addWarn(parts[1]);
                    break;
                case notiError:
                    FacesUtil.addError(parts[1]);
            }
        }
    }

}
