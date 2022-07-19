package com.tflow.file;

import java.io.IOException;

public interface SerializeReader {

    Object readSerialize() throws IOException, ClassNotFoundException;
}
