package com.tflow.file;

import com.tflow.UTBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;

public class FileOperationUT extends UTBase {

    private Logger log;

    @BeforeEach
    public void begin() {
        log = LoggerFactory.getLogger(getClass());
    }

    @Test
    public void move() {
        FileSystem fileSystem = FileSystems.getDefault();
        Path source = fileSystem.getPath("C:\\Apps\\kafka\\logs\\project-data-0");
        Path target = fileSystem.getPath("C:\\Apps\\kafka\\logs\\project-data-0.181e0322d8bf4d4285e2e8b87ca64e64-delete");
        /*C:\Apps\kafka\logs\project-data-0 -> C:\Apps\kafka\logs\project-data-0.181e0322d8bf4d4285e2e8b87ca64e64-delete*/

        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException outer) {
            try {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                println("Non-atomic move of {} to {} succeeded after atomic move failed due to {}", source, target,
                        outer.getMessage());
            } catch (IOException ex) {
                log.error("{}", ex.getMessage());
                log.trace("", ex);
            }
        }

    }

}
