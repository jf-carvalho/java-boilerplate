package com.app.domain.valueobjects;

import com.app.domain.exception.UserException;
import org.apache.commons.validator.routines.EmailValidator;

public class Email {
    private final String email;

    public Email(String email) {
        this.email = email;

        this.validate();
    }

    private void validate() {
        boolean emailIsValid = EmailValidator.getInstance().isValid(this.email);

        if (!emailIsValid) {
            throw new UserException("User email is not valid.");
        }
    }

    @Override
    public String toString() {
        return this.email;
    }
}
