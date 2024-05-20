package com.app.interfaces;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Обеспечивает архивацию файлов
 */
public interface IZipArchiverService {
    /**
     * Создает архив .zip с указанными файлами.
     * Сжатие производится при помощи алгоритма DEFLATE
     * @param entries
     *      Пути к файлам, добавляемым в архив
     * @param saveTo
     *      Путь сохранения архива
     */
    File zip(String[] entries, String saveTo) throws IOException;
}
