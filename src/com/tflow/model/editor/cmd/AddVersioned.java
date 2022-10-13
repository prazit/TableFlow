package com.tflow.model.editor.cmd;

import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.DataManager;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.data.StringItemData;
import com.tflow.model.data.VersionedFileData;
import com.tflow.model.editor.*;
import com.tflow.model.editor.view.PropertyView;
import com.tflow.model.editor.view.VersionedFile;
import com.tflow.model.mapper.ProjectMapper;
import com.tflow.util.ProjectUtil;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class AddVersioned extends Command {

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Logger log = LoggerFactory.getLogger(getClass());

        Workspace workspace = (Workspace) paramMap.get(CommandParamKey.WORKSPACE);
        BinaryFile binaryFile = (BinaryFile) paramMap.get(CommandParamKey.BINARY_FILE);
        PropertyView property = (PropertyView) paramMap.get(CommandParamKey.PROPERTY);
        VersionedFile versionedFile = (VersionedFile) paramMap.get(CommandParamKey.SELECTABLE);

        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        DataManager dataManager = workspace.getDataManager();
        ProjectUser projectUser = workspace.getProjectUser();

        /*need to replace older uploaded file if exists*/
        PropertyView newProperty = new PropertyView();
        newProperty.setVar(property.getParams()[1]);
        newProperty.setType(PropertyType.INT);
        int fileId = versionedFile.getId().getFileId();
        binaryFile.setId(fileId);

        // save file content
        dataManager.addData(ProjectFileType.VERSIONED, mapper.map(binaryFile), projectUser, fileId);

        // set name to versionedFile by next command
        property.setNewValue(binaryFile.getName());

        // for next command (ChangePropertyValue)
        paramMap.put(CommandParamKey.PROJECT_FILE_TYPE, versionedFile.getProjectFileType());
        paramMap.put(CommandParamKey.DATA, versionedFile);
        paramMap.put(CommandParamKey.SWITCH_ON, true);

    }

}
