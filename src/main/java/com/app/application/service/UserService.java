package com.app.application.service;

import com.app.application.dto.user.UpdatePasswordDTO;
import com.app.application.dto.user.UserRequestDTO;
import com.app.application.dto.user.UserResponseDTO;
import com.app.application.exception.IncorrectPasswordException;
import com.app.application.exception.ResourceNotFound;
import com.app.infrastructure.persistence.criteria.Criteria;
import com.app.infrastructure.persistence.entity.User;
import com.app.infrastructure.persistence.exceptions.EntityNotFoundException;
import com.app.infrastructure.persistence.repository.RepositoryInterface;
import com.app.infrastructure.security.hasher.HasherInterface;

import javax.swing.text.html.parser.Entity;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserService {
    private final RepositoryInterface<User> userRepository;
    private final HasherInterface hasherInterface;

    public UserService(RepositoryInterface<User> userRepository, HasherInterface hasherInterface) {
        this.userRepository = userRepository;
        this.hasherInterface = hasherInterface;
    }

    public UserResponseDTO get(Long id) {
        User user = this.userRepository.getById(id);

        if (user == null) {
            throw new ResourceNotFound("User with id " + id + "not found.");
        }

        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getDeletedAt()
        );
    }

    public List<UserResponseDTO> getAll() {
        List<User> users = this.userRepository.getAll();

        List<UserResponseDTO> usersDTOs = new ArrayList<>();

        users.forEach(user -> {
            UserResponseDTO dto = new UserResponseDTO(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getCreatedAt(),
                    user.getUpdatedAt(),
                    user.getDeletedAt()
            );
            usersDTOs.add(dto);
        });

        return usersDTOs;
    }

    public UserResponseDTO create(UserRequestDTO userRequestDTO) {
        com.app.domain.entity.User user = new com.app.domain.entity.User(
                userRequestDTO.name(),
                userRequestDTO.email(),
                userRequestDTO.password()
        );

        user.validateCreate();

        String passwordSalt = hasherInterface.getSalt();
        String hashedPassword = hasherInterface.getHash(user.getPassword(), passwordSalt);

        User userPersistenceEntity = new User(
                user.getName(),
                user.getEmail(),
                hashedPassword
        );

        User savedUser = userRepository.create(userPersistenceEntity);

        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt(),
                savedUser.getDeletedAt()
        );
    }

    public UserResponseDTO update(Long userId, UserRequestDTO userRequestDTO) {
        com.app.domain.entity.User user = new com.app.domain.entity.User(
                userId,
                userRequestDTO.name(),
                userRequestDTO.email()
        );

        user.validateUpdate();

        User userPersistenceEntity = new User(
                userId,
                user.getName(),
                user.getEmail()
        );

        User savedUser = userRepository.update(userId, userPersistenceEntity);

        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt(),
                savedUser.getDeletedAt()
        );
    }

    public UserResponseDTO updatePassword(UpdatePasswordDTO updatePasswordDTO) {
        Criteria criteria = new Criteria();
        criteria.equals("email", updatePasswordDTO.email());

        List<User> matchingUsers = userRepository.getByFilter(criteria);

        if (matchingUsers.isEmpty()) {
            throw new ResourceNotFound("User with provided email not found.");
        }

        User user = matchingUsers.getFirst();

        boolean isOldPasswordCorrect = hasherInterface.checkHash(user.getPassword(), updatePasswordDTO.oldPassword());

        if (!isOldPasswordCorrect) {
            throw new IncorrectPasswordException("The provided password is incorrect.");
        }

        com.app.domain.entity.User userToUpdateDomain = new com.app.domain.entity.User(
                user.getId(), updatePasswordDTO.newPassword()
        );

        userToUpdateDomain.validateNewPassword();

        String passwordSalt = hasherInterface.getSalt();
        String hashedPassword = hasherInterface.getHash(updatePasswordDTO.newPassword(), passwordSalt);

        User userToSave = new User(user.getId(), user.getName(), user.getEmail(), hashedPassword);
        User savedUser = userRepository.update(user.getId(), userToSave);

        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt(),
                savedUser.getDeletedAt()
        );
    }

    public boolean softDelete(Long userId) {
        User userToSave = new User(userId, new Date().toString());

        try {
            userRepository.update(userId, userToSave);
        } catch (EntityNotFoundException e) {
            return false;
        }

        return true;
    }

    public boolean restoreUser(Long userId) {
        User userToSave = new User(userId, null);

        try {
            userRepository.update(userId, userToSave);
        } catch (EntityNotFoundException e) {
            return false;
        }

        return true;
    }

    public boolean deleteUser(Long userId) {
        try {
            userRepository.delete(userId);
        } catch (EntityNotFoundException e) {
            return false;
        }

        return true;
    }
}
