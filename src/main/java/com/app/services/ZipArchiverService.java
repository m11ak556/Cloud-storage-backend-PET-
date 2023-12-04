package com.app.services;

import com.app.configuration.FileSystemConfiguration;
import com.app.interfaces.IZipArchiverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.AccessType;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ZipArchiverService implements IZipArchiverService {

    @Autowired
    public ZipArchiverService(FileSystemConfiguration fileSystemConfiguration) {
        this.fileSystemConfiguration = fileSystemConfiguration;
    }

    @Override
    public File zip(String[] entries) throws IOException {
        String zipFileName = generateZipName();
        Path pathToZipFile = fileSystemConfiguration
                .getTmpPath()
                .resolve(zipFileName)
                .normalize();

        File zipFile = pathToZipFile.toFile();

        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (String entry : entries) {
                ZipEntry e = new ZipEntry(entry);
                out.putNextEntry(e);

                try (FileInputStream fis = new FileInputStream(entry)) {
                    byte[] buffer = new byte[fis.available()];
                    fis.read(buffer);
                    out.write(buffer);
                    out.closeEntry();
                }
            }
        }

        return zipFile;
    }

    private String generateZipName() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("_yyyy-mm-dd_hh:mm:ss:SSS");
        StringBuilder name = new StringBuilder();
        Random random = new Random();

        name.append("files_");
        name.append(random.nextInt(100000));
        name.append(dateFormat.format(date));
        name.append(".zip");

        return name.toString();
    }

    private final FileSystemConfiguration fileSystemConfiguration;
}
