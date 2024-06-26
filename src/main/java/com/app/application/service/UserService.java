package com.app.application.service;

import com.app.application.dto.user.UpdatePasswordDTO;
import com.app.application.dto.user.UserRequestDTO;
import com.app.application.dto.user.UserResponseDTO;
import com.app.application.dto.user.UserResponseWithPasswordDTO;
import com.app.application.exception.ForbiddenException;
import com.app.application.exception.IncorrectPasswordException;
import com.app.application.exception.ResourceNotFound;
import com.app.domain.exception.UserException;
import com.app.infrastructure.persistence.criteria.Criteria;
import com.app.infrastructure.persistence.entity.User;
import com.app.infrastructure.persistence.exceptions.EntityNotFoundException;
import com.app.infrastructure.persistence.repository.RepositoryInterface;
import com.app.infrastructure.security.auth.AuthHolderInterface;
import com.app.infrastructure.security.hasher.HasherInterface;
import com.app.infrastructure.storage.StorageInterface;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class UserService {
    private final RepositoryInterface<User> userRepository;
    private final HasherInterface hasherInterface;
    private final AuthHolderInterface authHolder;
    private final StorageInterface storage;

    public UserService(
            RepositoryInterface<User> userRepository,
            HasherInterface hasherInterface,
            AuthHolderInterface authHolder,
            StorageInterface storage
    ) {
        this.userRepository = userRepository;
        this.hasherInterface = hasherInterface;
        this.authHolder = authHolder;
        this.storage = storage;
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
                user.getPicture(),
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
                    user.getPicture(),
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

        this.validateUniqueEmail(user.getEmail(), null);

        User savedUser = userRepository.create(userPersistenceEntity);

        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getPicture(),
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

        this.validateUniqueEmail(user.getEmail(), userId);

        User userPersistenceEntity = userRepository.getById(userId);

        if (user.getName() != null) {
            userPersistenceEntity.setName(user.getName());
        }

        if (user.getEmail() != null) {
            userPersistenceEntity.setEmail(user.getEmail());
        }

        User savedUser = userRepository.update(userId, userPersistenceEntity);

        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getPicture(),
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt(),
                savedUser.getDeletedAt()
        );
    }

    public UserResponseDTO updatePassword(UpdatePasswordDTO updatePasswordDTO) {
        User user = this.getUserByEmail(updatePasswordDTO.email());

        this.validateAccountOwner(user);
        this.checkPassword(user.getPassword(), updatePasswordDTO.oldPassword());

        com.app.domain.entity.User userToUpdateDomain = new com.app.domain.entity.User(
                user.getId(), updatePasswordDTO.newPassword()
        );

        userToUpdateDomain.validateNewPassword();

        String passwordSalt = hasherInterface.getSalt();
        String hashedPassword = hasherInterface.getHash(updatePasswordDTO.newPassword(), passwordSalt);

        User userToSave = userRepository.getById(user.getId());

        userToSave.setPassword(hashedPassword);

        User savedUser = userRepository.update(user.getId(), userToSave);

        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getPicture(),
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt(),
                savedUser.getDeletedAt()
        );
    }

    private User getUserByEmail(String email) {
        Criteria criteria = new Criteria();
        criteria.equals("email", email);

        List<User> matchingUsers = userRepository.getByFilter(criteria);

        if (matchingUsers.isEmpty()) {
            throw new ResourceNotFound("User with provided email not found.");
        }

        return matchingUsers.getFirst();
    }

    private void checkPassword(String userRealPassword, String oldPasswordInput) {
        boolean isOldPasswordCorrect = hasherInterface.checkHash(userRealPassword, oldPasswordInput);

        if (!isOldPasswordCorrect) {
            throw new IncorrectPasswordException("The provided password is incorrect.");
        }
    }

    private void validateAccountOwner(User user) {
        com.app.domain.entity.User loggedUser = authHolder.getUser();

        if (!Objects.equals(loggedUser.getId(), user.getId())) {
            throw new ForbiddenException("Forbidden.");
        }
    }

    public boolean softDelete(Long userId) {
        try {
            User userToSave = userRepository.getById(userId);

            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
            String formattedDate = dateFormat.format(currentDate);

            userToSave.setDeletedAt(formattedDate);

            userRepository.update(userId, userToSave);
        } catch (EntityNotFoundException e) {
            return false;
        }

        return true;
    }

    public boolean restoreUser(Long userId) {
       try {
           User userToSave = userRepository.getById(userId);

           userToSave.setDeletedAt(null);

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

    public UserResponseWithPasswordDTO getUserForLogin(String email) {
        Criteria criteria = new Criteria();
        criteria.equals("email", email);

        List<User> matchingUsers = userRepository.getByFilter(criteria);

        if (matchingUsers.isEmpty()) {
            throw new ResourceNotFound("User with provided email not found.");
        }

        User user = matchingUsers.getFirst();

        return new UserResponseWithPasswordDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getDeletedAt());
    }

    private void validateUniqueEmail(String email, Long userId) {
        Criteria criteria = new Criteria();
        criteria.equals("email", email);

        if (userId != null) {
            criteria.notEquals("id", userId);
        }

        List<User> matchingUsers = userRepository.getByFilter(criteria);

        if (!matchingUsers.isEmpty()) {
            throw new UserException("There is already an user registered with the provided email.");
        }
    }

    public UserResponseDTO updateUserPicture(Long userId, File userPicture) {
        User userPersistenceEntity = userRepository.getById(userId);

        if (userPersistenceEntity.getPicture() != null) {
            storage.delete(userPersistenceEntity.getPicture());
        }

        String userPictureString = storage.put(userPicture);

        userPersistenceEntity.setPicture(userPictureString);

        User savedUser = userRepository.update(userId, userPersistenceEntity);

        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getPicture(),
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt(),
                savedUser.getDeletedAt()
        );
    }
}
