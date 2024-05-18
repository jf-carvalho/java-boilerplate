package com.app.infrastructure.storage;

import java.io.File;

public interface StorageInterface {
    String put(File file);
    boolean delete(String fileUrl);
}
