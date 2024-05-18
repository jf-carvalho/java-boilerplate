package com.app.infrastructure.controller;

import com.app.application.dto.user.UpdatePasswordDTO;
import com.app.application.dto.user.UserRequestDTO;
import com.app.application.dto.user.UserResponseDTO;
import com.app.application.exception.ForbiddenException;
import com.app.application.exception.ResourceNotFound;
import com.app.application.service.UserService;
import com.app.application.util.authorization.RequiresAuthorization;
import com.app.application.util.http.ErrorResponse;
import com.app.infrastructure.persistence.exceptions.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    @RequiresAuthorization("retrieve users")
    public ResponseEntity<?> getAll() {
        try {
            List<UserResponseDTO> users = userService.getAll();

            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    @RequiresAuthorization("retrieve users")
    public ResponseEntity<?> get(@PathVariable int id) {
        try {
            UserResponseDTO user = userService.get((long) id);

            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch(ResourceNotFound | EntityNotFoundException e) {
            return new ResponseEntity<>(new ErrorResponse("User not found"), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping()
    @RequiresAuthorization("create users")
    public ResponseEntity<?> create(@RequestBody UserRequestDTO userRequestDTO) {
        try {
            UserResponseDTO user = userService.create(userRequestDTO);

            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    @RequiresAuthorization("update users")
    public ResponseEntity<?> update(@PathVariable int id, @RequestBody UserRequestDTO userRequestDTO) {
        try {
            UserResponseDTO user = userService.update((long) id, userRequestDTO);

            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/password")
    @RequiresAuthorization("update users")
    public ResponseEntity<?> updatePassword(@RequestBody UpdatePasswordDTO updatePasswordDTO) {
        try {
            UserResponseDTO user = userService.updatePassword(updatePasswordDTO);

            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (ForbiddenException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/soft/{id}")
    @RequiresAuthorization("delete users")
    public ResponseEntity<?> softDelete(@PathVariable int id) {
        try {
            boolean deleted = userService.softDelete((long) id);

            return new ResponseEntity<>(null, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/restore/{id}")
    @RequiresAuthorization("update users")
    public ResponseEntity<?> restore(@PathVariable int id) {
        try {
            boolean restored = userService.restoreUser((long) id);

            return new ResponseEntity<>(null, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    @RequiresAuthorization("delete users")
    public ResponseEntity<?> delete(@PathVariable int id) {
        try {
            boolean deleted = userService.deleteUser((long) id);

            return new ResponseEntity<>(null, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/picture/{id}")
    @RequiresAuthorization("update users")
    public ResponseEntity<?> updatePicture(@PathVariable int id, @RequestParam("file") MultipartFile file) {
        try {
            Files.createDirectories(Paths.get("src/main/resources/tmp/"));

            File tempFile = new File(
                    "src/main/resources/tmp/" +
                    UUID.randomUUID() +
                    "-" +
                    URLEncoder.encode(file.getOriginalFilename())
            );

            try (OutputStream os = new FileOutputStream(tempFile)) {
                os.write(file.getBytes());
            }

            UserResponseDTO user = userService.updateUserPicture((long) id, tempFile);

            tempFile.delete();

            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (ForbiddenException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
