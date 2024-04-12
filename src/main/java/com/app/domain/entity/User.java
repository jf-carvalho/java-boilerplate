package com.app.domain.entity;

import com.app.domain.exception.UserException;
import com.app.domain.valueobjects.Email;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class User {
    private Long id;
    private final String name;
    private String email;
    private String password;

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public boolean validateCreate() {
        this.validateName(this.name);
        this.validateEmail(this.email);
        this.validatePassword(this.password);

        return true;
    }

    public boolean validateUpdate() {
        this.validateName(name);
        this.validateEmail(this.email);

        return true;
    }

    private void validateName(String name) {
        if (name.isEmpty() || name.isBlank()) {
            throw new UserException("User name must not be empty or blank.");
        }

        if (name.length() < 3 || name.length() > 30) {
            throw new UserException("User name length must be between 3 and 30 characters and it is actually " + name.length() + ".");
        }
    }

    private void validateEmail(String email) {
        Email validEmail = new Email(email);

        this.email = validEmail.toString();
    }

    private void validatePassword(String password) {
        String regexPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(password);
        boolean passwordMatchesRequiredPattern = matcher.matches();

        if (!passwordMatchesRequiredPattern) {
            throw new UserException("User password should contain at least: 8 characters, 1 uppercase character, 1 lowercase character and 1 digit.");
        }
    }
}
