package com.expense.service;

import com.expense.dao.UserDao;
import com.expense.model.User;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {
    private final UserDao userDao;
    private User currentUser;

    public AuthService() {
        this.userDao = new UserDao();
    }

    public boolean register(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return false;
        }
        
        User existing = userDao.getUserByUsername(username);
        if (existing != null) {
            return false; // User exists
        }

        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        return userDao.createUser(username, hashed);
    }

    public boolean login(String username, String password) {
        if (username == null || password == null) return false;

        User user = userDao.getUserByUsername(username);
        if (user != null && BCrypt.checkpw(password, user.getPasswordHash())) {
            currentUser = user;
            return true;
        }
        return false;
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
