package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BinaryFileData extends TWData {

    int id;
    String name;
    FileNameExtension ext;
    byte[] content;

    @Override
    public String toString() {
        return "BinaryFileData(" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", ext=" + ext +
                ", content[" + content.length + "]" +
                ')';
    }
}
