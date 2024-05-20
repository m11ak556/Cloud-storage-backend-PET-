package com.app.interfaces;

/**
 * Обеспечивает выполнение специальных действий с пользователями
 */
public interface IUserService {
    /**
     * Проверяет логин на существования
     * @param login
     *      Логин для проверки
     */
    boolean isLoginDuplicate(String login);

    /**
     * Проверяет почту на существование
     * @param email
     *      Почта для проверки
     */
    boolean isEmailDuplicate(String email);

    /**
     * Создает рабочую директорию, корзину и папку временных файлов для указанного пользователя
     * @param login
     *      Логин пользователя
     */
    void initializeDirectories(String login);
}
