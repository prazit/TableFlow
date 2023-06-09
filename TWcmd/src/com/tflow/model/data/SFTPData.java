package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SFTPData extends DataSourceData {
    private static final transient long serialVersionUID = 2021121709996660012L;

    private List<String> pathHistory;
    private String rootPath;

    private String host;
    private int port;
    private String user;
    private String password;
    private int retry;

    private boolean userEncrypted;
    private boolean passwordEncrypted;

    /*tmp=/ftp/<id>/<root-path>/*/
    private String tmp;
}
