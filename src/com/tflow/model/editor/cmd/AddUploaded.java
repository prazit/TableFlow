package com.tflow.model.editor.cmd;

import com.tflow.kafka.KafkaErrorCode;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.BinaryFileItemData;
import com.tflow.model.data.DataManager;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.data.TWData;
import com.tflow.model.editor.*;
import com.tflow.model.editor.view.PropertyView;
import com.tflow.model.mapper.ProjectMapper;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class AddUploaded extends Command {

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Logger log = LoggerFactory.getLogger(getClass());

        Workspace workspace = (Workspace) paramMap.get(CommandParamKey.WORKSPACE);
        BinaryFile binaryFile = (BinaryFile) paramMap.get(CommandParamKey.BINARY_FILE);
        PropertyView property = (PropertyView) paramMap.get(CommandParamKey.PROPERTY);
        Selectable selectable = (Selectable) paramMap.get(CommandParamKey.SELECTABLE);

        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        DataManager dataManager = workspace.getDataManager();
        ProjectUser projectUser = workspace.getProjectUser();
        Properties properties = selectable.getProperties();

        Object data = dataManager.getData(ProjectFileType.UPLOADED_LIST, projectUser);
        List<BinaryFileItemData> binaryFileItemDataList = (List<BinaryFileItemData>) throwExceptionOnError(data);
        int fileId = binaryFileItemDataList.size() + 1;

        /*need to replace older uploaded file if exists*/
        PropertyView newProperty = new PropertyView();
        newProperty.setVar(property.getParams()[1]);
        newProperty.setType(PropertyType.INT);
        Object uploadedFileId = properties.getPropertyValue(selectable, newProperty, log);
        boolean append = true;
        if (uploadedFileId instanceof Integer) {
            Integer olderFileId = (Integer) uploadedFileId;
            if (olderFileId > 0) {
                fileId = olderFileId;
                append = false;
            }
        }

        binaryFile.setId(fileId);
        if (append) {
            // add Uploaded file to current project
            binaryFileItemDataList.add(mapper.toBinaryFileItemData(binaryFile));
            dataManager.addData(ProjectFileType.UPLOADED_LIST, binaryFileItemDataList, projectUser);
        }

        // save file content
        dataManager.addData(ProjectFileType.UPLOADED, mapper.map(binaryFile), projectUser, fileId);

        // set file-id to selectable[property-param[1]]
        newProperty.setNewValue(fileId);
        try {
            properties.setPropertyValue(selectable, newProperty, log);
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Cannot set property(" + newProperty + ") to selectable(" + selectable.getSelectableId() + ")", ex);
        }

        // set name to selectable by next command
        property.setNewValue(binaryFile.getName());

        // for next command
        paramMap.put(CommandParamKey.PROJECT_FILE_TYPE, selectable.getProjectFileType());
        paramMap.put(CommandParamKey.DATA, selectable);

    }

    private Object throwExceptionOnError(Object data) throws UnsupportedOperationException {
        if (data instanceof Long) {
            throw new UnsupportedOperationException(KafkaErrorCode.parse((Long) data).name());
        }
        return data;
    }

}
