package com.tflow.util;

import org.primefaces.PrimeFaces;
import org.primefaces.context.PrimeRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.Map;

public class FacesUtil implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(FacesUtil.class);

    public static void addInfo(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "[INFO]", message));
    }

    public static void addInfo(String clientId, String message) {
        FacesContext.getCurrentInstance().addMessage(clientId, new FacesMessage(FacesMessage.SEVERITY_INFO, "[INFO]", message));
    }

    public static void addError(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "[ERROR]", message));
    }

    public static void addError(String clientId, String message) {
        FacesContext.getCurrentInstance().addMessage(clientId, new FacesMessage(FacesMessage.SEVERITY_ERROR, "[ERROR]", message));
    }

    public static void actionSuccess() {
        PrimeRequestContext requestContext = PrimeRequestContext.getCurrentInstance();
        requestContext.getCallbackParams().put("isActionSuccess", true);
    }

    public static void actionSuccess(String infoMessage) {
        actionSuccess();
        addInfo(infoMessage);
    }

    public static void actionFailed() {
        PrimeRequestContext requestContext = PrimeRequestContext.getCurrentInstance();
        requestContext.getCallbackParams().put("isActionSuccess", false);
    }

    public static void actionFailed(String errorMessage) {
        actionFailed();
        addError(errorMessage);
    }

    public static HttpServletRequest getRequest() {
        return (HttpServletRequest) getExternalContext().getRequest();
    }

    public static HttpServletResponse getResponse() {
        return (HttpServletResponse) getExternalContext().getResponse();
    }

    public static HttpSession getSession() {
        return getRequest().getSession();
    }

    public static ExternalContext getExternalContext() {
        return FacesContext.getCurrentInstance().getExternalContext();
    }

    public static FacesContext getFactContext() {
        return FacesContext.getCurrentInstance();
    }

    /**
     * @return null when parameter not found.
     */
    public static String getRequestParam(String param) {
        Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        return requestParameterMap.get(param);
    }

    public static void runClientScript(String javaScript) {
        PrimeFaces.current().executeScript(javaScript);
    }

    public static void redirect(String uriPath) {
        String contextPath = "";
        try {
            ExternalContext ec = getExternalContext();
            contextPath = ec.getRequestContextPath();
            String url = contextPath.concat(uriPath);
            log.debug("redirect to url: {}", url);
            ec.redirect(url);
        } catch (Exception e) {
            log.error("Exception while redirection! (contextPath: {}, uriPath: {})", contextPath, uriPath);
            log.error("", e);
        }
    }
}
