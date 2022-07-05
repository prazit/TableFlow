package com.tflow.model.mapper;

import com.tflow.model.data.DatabaseData;
import com.tflow.model.data.LocalData;
import com.tflow.model.data.SFTPData;
import com.tflow.model.editor.Line;
import com.tflow.model.editor.datasource.Database;
import com.tflow.model.editor.datasource.Local;
import com.tflow.model.editor.datasource.SFTP;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "default")
public interface DataSourceMapper {

    default Integer map(Line line) {
        return line.getId();
    }

    default Line map(Integer id) {
        return new Line(id);
    }

    List<Integer> toLineList(List<Line> lineList);

    List<Line> toIntegerList(List<Integer> lineList);

    Database map(DatabaseData databaseData);

    DatabaseData map(Database database);

    SFTP map(SFTPData sftpData);

    SFTPData map(SFTP sftp);

    Local map(LocalData localData);

    LocalData map(Local local);

}
