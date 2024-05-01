package com.app.infrastructure.controller;

import com.app.application.dto.user.UpdatePasswordDTO;
import com.app.application.dto.user.UserRequestDTO;
import com.app.application.dto.user.UserResponseDTO;
import com.app.application.exception.ResourceNotFound;
import com.app.application.service.UserService;
import com.app.application.util.http.ErrorResponse;
import com.app.infrastructure.persistence.exceptions.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public ResponseEntity<?> getAll() {
        try {
            List<UserResponseDTO> users = userService.getAll();

            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
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
    public ResponseEntity<?> create(@RequestBody UserRequestDTO userRequestDTO) {
        try {
            UserResponseDTO user = userService.create(userRequestDTO);

            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable int id, @RequestBody UserRequestDTO userRequestDTO) {
        try {
            UserResponseDTO user = userService.update((long) id, userRequestDTO);

            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody UpdatePasswordDTO updatePasswordDTO) {
        try {
            UserResponseDTO user = userService.updatePassword(updatePasswordDTO);

            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/soft/{id}")
    public ResponseEntity<?> softDelete(@PathVariable int id) {
        try {
            boolean deleted = userService.softDelete((long) id);

            return new ResponseEntity<>(null, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/restore/{id}")
    public ResponseEntity<?> restore(@PathVariable int id) {
        try {
            boolean restored = userService.restoreUser((long) id);

            return new ResponseEntity<>(null, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        try {
            boolean deleted = userService.deleteUser((long) id);

            return new ResponseEntity<>(null, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
