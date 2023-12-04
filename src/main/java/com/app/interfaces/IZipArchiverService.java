package com.app.interfaces;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface IZipArchiverService {
    File zip(String[] entries) throws IOException;
}
