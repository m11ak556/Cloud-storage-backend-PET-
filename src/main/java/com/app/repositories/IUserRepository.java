package com.app.repositories;

import com.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Предоставляет метода для обработки запросов к таблице пользователей
 */
@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByLogin(String login);
}
