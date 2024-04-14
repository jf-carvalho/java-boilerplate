package com.app.domain.entity;

import static org.junit.jupiter.api.Assertions.*;

import com.app.domain.exception.UserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class UserTest {

    @Test
    public void shouldBeValidForCreate_withValidFields() {
        User user = new User(
                "John Doe",
                "jdoe@domain.com",
                "Password1"
        );

        assertTrue(user.validateCreate());
    }

    @ParameterizedTest
    @MethodSource("testCreateData")
    void shouldNotBeValidForCreate_withInvalidFields(User user, String exceptionMessage) {
        assertThrows(UserException.class, user::validateCreate, exceptionMessage);
    }

    static Stream<Arguments> testCreateData() {
        return Stream.of(
                Arguments.of(
                        new User("", "", ""),
                        "User name must not be empty or blank."
                ),
                Arguments.of(
                        new User("      ", "", ""),
                        "User name must not be empty or blank."
                ),
                Arguments.of(
                        new User("Yi", "", ""),
                        "User name length must be between 3 and 30 characters and it is actually 2."
                ),
                Arguments.of(
                        new User("Johnathan Christopher Petterson", "", ""),
                        "User name length must be between 3 and 30 characters and it is actually 2."
                ),
                Arguments.of(
                        new User("Johnathan", "", ""),
                        "User email is not valid."
                ),
                Arguments.of(
                        new User("Johnathan", "jdoe@domain.com", ""),
                        "User password should contain at least: 8 characters, 1 uppercase character, 1 lowercase character and 1 digit."
                ),
                Arguments.of(
                        new User("Johnathan", "jdoe@domain.com", "password1"),
                        "User password should contain at least: 8 characters, 1 uppercase character, 1 lowercase character and 1 digit."
                ),
                Arguments.of(
                        new User("Johnathan", "jdoe@domain.com", "Password"),
                        "User password should contain at least: 8 characters, 1 uppercase character, 1 lowercase character and 1 digit."
                ),
                Arguments.of(
                        new User("Johnathan", "jdoe@domain.com", "Psswrd1"),
                        "User password should contain at least: 8 characters, 1 uppercase character, 1 lowercase character and 1 digit."
                )
        );
    }

    @Test
    public void shouldBeValidForUpdate_withValidFields() {
        User user = new User(
                1L,
                "John Doe",
                "jdoe@domain.com"
        );

        assertTrue(user.validateUpdate());
    }

    @ParameterizedTest
    @MethodSource("testUpdateData")
    void shouldNotBeValidForUpdate_withInvalidFields(User user, String exceptionMessage) {
        assertThrows(UserException.class, user::validateUpdate, exceptionMessage);
    }

    static Stream<Arguments> testUpdateData() {
        return Stream.of(
                Arguments.of(
                        new User("", "", ""),
                        "User id is required."
                ),
                Arguments.of(
                        new User(1L, "", ""),
                        "User name must not be empty or blank."
                ),
                Arguments.of(
                        new User(1L, "      ", ""),
                        "User name must not be empty or blank."
                ),
                Arguments.of(
                        new User(1L, "Yi", ""),
                        "User name length must be between 3 and 30 characters and it is actually 2."
                ),
                Arguments.of(
                        new User(1L, "Johnathan Christopher Petterson", ""),
                        "User name length must be between 3 and 30 characters and it is actually 2."
                ),
                Arguments.of(
                        new User(1L, "Johnathan", ""),
                        "User email is not valid."
                )
        );
    }

    @Test
    public void shouldBeValidForPasswordUpdate_withValidPassword() {
        User user = new User(
                1L,
                "Password1"
        );

        assertTrue(user.validateNewPassword());
    }

    @Test
    public void shouldNotBeValidForPasswordUpdate_withInvalidPassword() {
        User user = new User(
                1L,
                "asd"
        );

        assertThrows(UserException.class, user::validateNewPassword, "User password should contain at least: 8 characters, 1 uppercase character, 1 lowercase character and 1 digit.");
    }
}
