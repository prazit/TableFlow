package com.tflow.model.editor.cmd;

import com.tflow.model.editor.*;
import com.tflow.model.editor.datasource.DataSource;
import com.tflow.model.editor.room.Floor;
import com.tflow.model.editor.room.Tower;

import java.util.List;
import java.util.Map;

/**
 * <b>Required parameters:</b><br/>
 * DATA_SOURCE<br/>
 * PROJECT<br/>
 */
public class AddDataSource extends Command {

    @SuppressWarnings("unchecked")
    public void execute(Map<CommandParamKey, Object> paramMap) {
        DataSource dataSource = (DataSource) paramMap.get(CommandParamKey.DATA_SOURCE);
        Project project = (Project) paramMap.get(CommandParamKey.PROJECT);
        project.getDataSourceList().put(dataSource.getName(), dataSource);
    }

}
