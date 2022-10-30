package com.tflow.model.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class BinaryFileData extends TWData implements Serializable {
    private static final transient long serialVersionUID = 2021121709996660022L;

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
