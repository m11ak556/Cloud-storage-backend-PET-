package com.app.services;

import com.app.model.User;
import com.app.model.UserPrincipal;
import com.app.repositories.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Предоставляет методы для работы с учетными записями пользователей
 */
@Service
public class BasicUserDetailsService implements UserDetailsService {

    @Autowired
    public BasicUserDetailsService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByLogin(username).orElse(null);
        UserPrincipal userPrincipal = new UserPrincipal(user);
        return userPrincipal;
    }

    private final IUserRepository userRepository;
}
