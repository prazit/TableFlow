package com.tflow.model.editor;

import com.tflow.kafka.KafkaErrorCode;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.ClientData;
import com.tflow.model.data.ProjectDataException;
import com.tflow.model.data.ProjectDataManager;
import com.tflow.model.data.ProjectUser;
import com.tflow.system.Application;
import com.tflow.system.Environment;
import com.tflow.system.constant.Theme;
import com.tflow.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@SessionScoped
@Named("workspace")
public class Workspace implements Serializable {
    private static final long serialVersionUID = 2021121709996660006L;

    @Inject
    private Application app;

    @Inject
    private HttpSession httpSession;

    @Inject
    private HttpServletRequest httpRequest;

    private Environment environment;

    /*Project Group Id is needed when create new project */
    private int projectGroupId;
    private Project project;
    private User user;
    private Client client;
    private ProjectDataManager projectDataManager;

    @PostConstruct
    public void onCreation() {
        Logger log = LoggerFactory.getLogger(Workspace.class);
        log.trace("Session started.");

        environment = app.getEnvironment();
        projectDataManager = new ProjectDataManager(environment, httpRequest.getRequestedSessionId(), app.getZkConfiguration());

        // dummy user before Authentication, Notice: after authenticated need to setUser to this workspace.
        user = new User();
        user.setTheme(Theme.DARK);

        // load client information into Client instance.
        client = loadClientInfo(httpRequest, projectDataManager);

        // TODO: temporary used to reset-to-empty-project/create-new-project.
        resetProject();

        printHttpSession(httpSession, log);
        printHttpRequest(httpRequest, log);
    }

    private Client loadClientInfo(HttpServletRequest httpRequest, ProjectDataManager projectDataManager) {
        Client client = new Client();
        client.setComputerName(getComputerName(httpRequest));
        client.setIp(httpRequest.getRemoteHost());
        client.setId(registerClient(client, httpRequest, projectDataManager));
        return client;
    }

    private long registerClient(Client client, HttpServletRequest httpRequest, ProjectDataManager projectDataManager) {
        ClientData clientData;
        ProjectUser projectUser = new ProjectUser();
        try {
            /*found existing then return existing ID*/
            clientData = (ClientData) throwExceptionOnError(projectDataManager.getData(ProjectFileType.CLIENT, projectUser));
        } catch (ProjectDataException ex) {
            /*not found, then create and return next to the last ID*/
            /*TODO: where is lastClientId?
             * last-package-id is in ?
             */
            clientData = getClientData(client);
            projectDataManager.addData(ProjectFileType.CLIENT, clientData, projectUser);
        }
        return clientData.getUniqueNumber();
    }

    private ClientData getClientData(Client client) {
        ClientData clientData = new ClientData();
        String computerName = client.getComputerName();
        String ip = client.getIp();
        clientData.setId((computerName == null ? "" : computerName) + ":" + (ip == null ? "" : ip));
        clientData.setUniqueNumber(client.getId());
        return clientData;
    }

    private Object throwExceptionOnError(Object data) throws ProjectDataException {
        if (data instanceof Long) {
            throw new ProjectDataException(KafkaErrorCode.parse((Long) data).name());
        }
        return data;
    }


    private String getAgent(HttpServletRequest httpRequest) {
        String computerName = getComputerName(httpRequest);
        return (computerName == null ? "" : computerName + " ") + httpRequest.getHeader("User-Agent");
    }

    private String getComputerName(HttpServletRequest httpRequest) {
        for (Cookie cookie : httpRequest.getCookies()) {
            if (cookie.getName().compareTo("JSESSIONID") == 0) {
                String[] values = cookie.getValue().split("[.]");
                if (values.length > 1) {
                    return values[1];
                }
                return null;
            }
        }
        return null;
    }

    private void printHttpRequest(HttpServletRequest httpRequest, Logger log) {
        log.debug("printHttpRequest: {}", DateTimeUtil.now().getTime());

        Enumeration<String> attributeNames = httpRequest.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            Object attributeValue = httpRequest.getAttribute(attributeName);
            log.debug("attribute.{}: {}:{}", attributeName, attributeValue.getClass().getName(), attributeValue);
        }

        log.debug("AuthType: String:{}", httpRequest.getAuthType());
        log.debug("ContextPath: String:{}", httpRequest.getContextPath());

        String msg;
        Cookie[] cookies = httpRequest.getCookies();
        for (int index = 0; index < cookies.length; index++) {
            Cookie cookie = cookies[index];
            msg = "Name:" + cookie.getName()
                    + ", Value:" + cookie.getValue()
                    + ", Secure:" + cookie.getSecure()
                    + ", Domain:" + cookie.getDomain()
                    + ", Path:" + cookie.getPath()
                    + ", Version:" + cookie.getVersion()
                    + ", MaxAge:" + cookie.getMaxAge()
                    + ", Comment:" + cookie.getComment()
            ;
            log.debug("Cookies[{}]: {}", index, msg);
        }

        log.debug("ContextPath: String:{}", httpRequest.getCharacterEncoding());
        log.debug("ContentLength: int:{}", httpRequest.getContentLength());
        log.debug("ContentLengthLong: Long:{}", httpRequest.getContentLengthLong());
        log.debug("ContentType: String:{}", httpRequest.getContentType());
        log.debug("DispatcherType: DispatcherType:{}", httpRequest.getDispatcherType());

        Enumeration<String> headerNames = httpRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Object headerValue = httpRequest.getHeader(headerName);
            log.debug("header.{} = {}:{}", headerName, headerValue.getClass().getName(), headerValue);
        }

        log.debug("HttpServletMapping: HttpServletMapping:{}", httpRequest.getHttpServletMapping());
        log.debug("LocalAddr: String:{}", httpRequest.getLocalAddr());
        log.debug("LocalName: String:{}", httpRequest.getLocalName());
        log.debug("LocalPort: int:{}", httpRequest.getLocalPort());
        log.debug("Locale: Locale:{}", httpRequest.getLocale());
        log.debug("Method: String:{}", httpRequest.getMethod());

        Collection<Part> parts = null;
        try {
            parts = httpRequest.getParts();
            for (Part part : parts) {
                String partName = part.getName();
                List<String> headerNamesList = new ArrayList<>(part.getHeaderNames());
                for (String headerName : headerNamesList) {
                    Object headerValue = part.getHeader(headerName);
                    log.debug("part.{}.header.{} = {}:{}", partName, headerName, headerValue.getClass().getName(), headerValue);
                }

                log.debug("part.{}.ContentType: String:{}", partName, part.getContentType());
                log.debug("part.{}.Size: long:{}", partName, part.getSize());
                log.debug("part.{}.SubmittedFileName: String:{}", partName, part.getSubmittedFileName());
            } // end of for (Part)
        } catch (IOException | ServletException ex) {
            log.debug("Parts: error={}", ex.getMessage());
        }

        log.debug("PathInfo: String:{}", httpRequest.getPathInfo());
        log.debug("PathTranslated: String:{}", httpRequest.getPathTranslated());

        Enumeration<String> parameterNames = httpRequest.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = httpRequest.getParameter(paramName);
            log.debug("Parameter.{} = String:{}", paramName, paramValue);
        }

        log.debug("Protocol: String:{}", httpRequest.getProtocol());
        log.debug("QueryString: String:{}", httpRequest.getQueryString());
        log.debug("RemoteHost: String:{}", httpRequest.getRemoteHost());
        log.debug("RemotePort: int:{}", httpRequest.getRemotePort());
        log.debug("RemoteUser: String:{}", httpRequest.getRemoteUser());
        log.debug("RequestedSessionId: String:{}", httpRequest.getRequestedSessionId());
        log.debug("RequestURI: String:{}", httpRequest.getRequestURI());
        log.debug("RequestURL: StringBuffer:{}", httpRequest.getRequestURL());
        log.debug("RemoteAddr: String:{}", httpRequest.getRemoteAddr());
        log.debug("ServerPort: String:{}", httpRequest.getServerPort());
        log.debug("Scheme: String:{}", httpRequest.getScheme());
        log.debug("ServerName: String:{}", httpRequest.getServerName());

        Map<String, String> trailerFieldMap = httpRequest.getTrailerFields();
        for (String fieldName : trailerFieldMap.keySet()) {
            log.debug("TrailerField.{}: String:{}", fieldName, trailerFieldMap.get(fieldName));
        }
    }

    private void printHttpSession(HttpSession httpSession, Logger log) {
        log.debug("printHttpSession: {}", DateTimeUtil.now().getTime());

        Enumeration<String> attributeNames = httpSession.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            Object attributeValue = httpSession.getAttribute(attributeName);
            log.debug("attribute.{} = {}:{}", attributeName, attributeValue.getClass().getName(), attributeValue);
        }

        log.debug("Id = String:'{}'", httpSession.getId());
        log.debug("CreationTime = Date:'{}'", httpSession.getCreationTime());
    }

    public void resetProject() {
        project = null;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public ProjectDataManager getProjectDataManager() {
        return projectDataManager;
    }
}
