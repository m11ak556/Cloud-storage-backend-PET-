package com.app.interfaces;

import com.app.model.FileModel;
import com.app.model.FileTypes;

/**
 * Обеспечивает работу с моделями файлов
 */
public interface IFileModelService {
    /**
     * Получает тип указанного файла
     * @param fileName
     *      Имя файла, тип которого нужно получить
     */
    FileTypes guessFileType(String fileName);
}
