package com.app.infrastructure.controller;

import com.app.application.dto.RoleDTO;
import com.app.application.exception.ResourceNotFound;
import com.app.application.service.UserRoleService;
import com.app.application.util.ErrorResponse;
import com.app.infrastructure.persistence.exceptions.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class UserRoleController {
    private final UserRoleService userRoleService;

    public UserRoleController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserRoles(@PathVariable int userId) {
        try {
            List<RoleDTO> userRoles = this.userRoleService.getUserRoles((long) userId);
            return new ResponseEntity<>(userRoles, HttpStatus.OK);
        } catch (ResourceNotFound | EntityNotFoundException e) {
            return new ResponseEntity<>(new ErrorResponse("User not found"), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> getUserRoles(@PathVariable int userId, @RequestBody List<RoleDTO> roles) {
        try {
            List<RoleDTO> userRoles = this.userRoleService.syncUserRoles((long) userId, roles);
            return new ResponseEntity<>(userRoles, HttpStatus.OK);
        } catch (ResourceNotFound | EntityNotFoundException e) {
            return new ResponseEntity<>(new ErrorResponse("User not found"), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
