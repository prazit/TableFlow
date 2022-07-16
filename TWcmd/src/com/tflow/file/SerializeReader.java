package com.tflow.file;

import java.io.IOException;

public interface SerializeReader {

    public Object readSerialize() throws IOException, ClassNotFoundException;
}
