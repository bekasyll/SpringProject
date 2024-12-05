package com.example.project.services;

import com.example.project.model.User;
import com.example.project.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Сохранить пользователя (регистрация)
    public User saveUser(User user) {
        // Хэшируем пароль перед сохранением
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // Найти пользователя по имени
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Найти пользователя по email
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Найти пользователя по ID
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    // Удалить пользователя по ID
    public void deleteUserById(Long userId) {
        userRepository.deleteById(userId);
    }

    // Получить список всех пользователей
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
}

