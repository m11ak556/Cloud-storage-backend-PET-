package com.app.services;

import com.app.interfaces.IUserService;
import com.app.model.User;
import com.app.repositories.IUserRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.caseSensitive;

@Service
public class UserService implements IUserService {
    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isLoginDuplicate(String login) {
        return userRepository.findUserByLogin(login).isPresent();
    }

    public boolean isEmailDuplicate(String email) {
        User probe = new User();
        probe.setEmail(email);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("id")
                .withMatcher("email", caseSensitive());

        return userRepository.exists(Example.of(probe, matcher));
    }

    private final IUserRepository userRepository;
}
