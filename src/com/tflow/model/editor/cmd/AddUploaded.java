package com.tflow.model.editor.cmd;

import com.tflow.kafka.KafkaErrorCode;
import com.tflow.kafka.ProjectFileType;
import com.tflow.model.data.BinaryFileItemData;
import com.tflow.model.data.DataManager;
import com.tflow.model.data.ProjectUser;
import com.tflow.model.editor.BinaryFile;
import com.tflow.model.editor.PropertyType;
import com.tflow.model.editor.Selectable;
import com.tflow.model.editor.Workspace;
import com.tflow.model.editor.view.PropertyView;
import com.tflow.model.mapper.ProjectMapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

public class AddUploaded extends Command {

    @Override
    public void execute(Map<CommandParamKey, Object> paramMap) throws UnsupportedOperationException {
        Workspace workspace = (Workspace) paramMap.get(CommandParamKey.WORKSPACE);
        BinaryFile binaryFile = (BinaryFile) paramMap.get(CommandParamKey.BINARY_FILE);
        PropertyView property = (PropertyView) paramMap.get(CommandParamKey.PROPERTY);
        Selectable selectable = (Selectable) paramMap.get(CommandParamKey.SELECTABLE);

        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        DataManager dataManager = workspace.getDataManager();
        ProjectUser projectUser = workspace.getProjectUser();

        // add Uploaded file to current project
        Object data = dataManager.getData(ProjectFileType.UPLOADED_LIST, projectUser);
        List<BinaryFileItemData> binaryFileItemDataList = (List<BinaryFileItemData>) throwExceptionOnError(data);
        int fileId = binaryFileItemDataList.size() + 1;
        binaryFile.setId(fileId);
        binaryFileItemDataList.add(mapper.toBinaryFileItemData(binaryFile));
        dataManager.addData(ProjectFileType.UPLOADED_LIST, binaryFileItemDataList, projectUser);
        dataManager.addData(ProjectFileType.UPLOADED, mapper.map(binaryFile), projectUser, fileId);

        // set file-id to selectable[property-param[1]]
        PropertyView newProperty = new PropertyView();
        try {
            newProperty.setVar(property.getParams()[1]);
            newProperty.setType(PropertyType.INT);
            newProperty.setNewValue(fileId);
            setPropertyValue(selectable, newProperty);
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
