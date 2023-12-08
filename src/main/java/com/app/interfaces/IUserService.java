package com.app.interfaces;

public interface IUserService {
    boolean isLoginDuplicate(String login);
    boolean isEmailDuplicate(String email);
    void initializeDirectories(String login);
}
