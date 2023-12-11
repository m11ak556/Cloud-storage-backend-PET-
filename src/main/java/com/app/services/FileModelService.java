package com.app.services;

import com.app.interfaces.IFileModelService;
import com.app.model.FileTypes;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Dictionary;
import java.util.Hashtable;

@Service
public class FileModelService implements IFileModelService {

    public FileModelService() {
        fileTypeMatches = new Hashtable<>();
        initializeFileTypes();
    }

    @Override
    public FileTypes guessFileType(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1)
            return FileTypes.OTHER;

        String extension = fileName.substring(dotIndex + 1);
        FileTypes type = fileTypeMatches.get(extension);
        if (type == null)
            type = FileTypes.OTHER;

        return type;
    }

    private Dictionary<String, FileTypes> fileTypeMatches;

    private void initializeFileTypes() {
        fileTypeMatches.put("doc", FileTypes.DOCUMENT);
        fileTypeMatches.put("docx", FileTypes.DOCUMENT);
        fileTypeMatches.put("odf", FileTypes.DOCUMENT);
        fileTypeMatches.put("pdf", FileTypes.DOCUMENT);

        fileTypeMatches.put("ppt", FileTypes.SPREADSHEET);
        fileTypeMatches.put("pptx", FileTypes.SPREADSHEET);
        fileTypeMatches.put("odp", FileTypes.SPREADSHEET);

        fileTypeMatches.put("txt", FileTypes.TEXT);
        fileTypeMatches.put("md", FileTypes.TEXT);

        fileTypeMatches.put("mp4", FileTypes.VIDEO);          fileTypeMatches.put("mp4", FileTypes.VIDEO);        fileTypeMatches.put("mp4", FileTypes.VIDEO);
        fileTypeMatches.put("avi", FileTypes.VIDEO);

        fileTypeMatches.put("mp3", FileTypes.AUDIO);
        fileTypeMatches.put("wav", FileTypes.AUDIO);
        fileTypeMatches.put("ogg", FileTypes.AUDIO);

        fileTypeMatches.put("jpg", FileTypes.IMAGE);            fileTypeMatches.put("jpg", FileTypes.IMAGE);
        fileTypeMatches.put("jpeg", FileTypes.IMAGE);        fileTypeMatches.put("jpg", FileTypes.IMAGE);
        fileTypeMatches.put("png", FileTypes.IMAGE);
        fileTypeMatches.put("gif", FileTypes.IMAGE);

        fileTypeMatches.put("zip", FileTypes.COMPRESSED);        fileTypeMatches.put("zip", FileTypes.COMPRESSED);
        fileTypeMatches.put("rar", FileTypes.COMPRESSED);
        fileTypeMatches.put("gz", FileTypes.COMPRESSED);        fileTypeMatches.put("zip", FileTypes.COMPRESSED);
        fileTypeMatches.put("7z", FileTypes.COMPRESSED);
    }
}
