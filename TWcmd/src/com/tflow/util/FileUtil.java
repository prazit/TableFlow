package com.tflow.util;

import java.io.File;

public class FileUtil {

    public static void autoCreateParentDir(File file) {
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) parentFile.mkdirs();
    }

}
