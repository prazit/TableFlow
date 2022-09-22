package com.tflow.model.editor;

import com.tflow.model.data.FileNameExtension;

public class BinaryFile {

    private int id;
    private String name;
    private FileNameExtension ext;
    private byte[] content;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FileNameExtension getExt() {
        return ext;
    }

    public void setExt(FileNameExtension ext) {
        this.ext = ext;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "{" +
                "id:" + id +
                ", name:'" + name + '\'' +
                ", ext:" + ext +
                '}';
    }
}
