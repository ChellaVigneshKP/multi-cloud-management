package com.multicloud.auth.service;

import com.multicloud.auth.model.User;  // User model representing user data
import com.multicloud.auth.repository.UserRepository;  // Repository for accessing user data
import org.springframework.stereotype.Service;  // Indicates that this class is a service component

import java.util.ArrayList;  // For creating a dynamic list of users
import java.util.List;  // General list interface

@Service  // Marks this class as a Spring service
public class UserService {
    private final UserRepository userRepository;  // Repository for performing CRUD operations on User entities

    // Constructor to initialize UserRepository
    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;  // Assigning the UserRepository instance
    }

    // Retrieves all users from the database
    public List<User> allUsers() {
        List<User> users = new ArrayList<>();  // List to hold all retrieved users
        userRepository.findAll().forEach(users::add);  // Fetching all users and adding them to the list
        return users;  // Returning the list of users
    }
}
