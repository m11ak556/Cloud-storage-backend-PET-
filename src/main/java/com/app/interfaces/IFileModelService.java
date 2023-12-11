package com.app.interfaces;

import com.app.model.FileModel;
import com.app.model.FileTypes;

public interface IFileModelService {
    FileTypes guessFileType(String fileName);
}
