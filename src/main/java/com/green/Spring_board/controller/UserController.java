package com.green.Spring_board.controller;

import com.green.Spring_board.dto.SignupRequest;
import com.green.Spring_board.exceptions.ResourceConflictException;
import com.green.Spring_board.exceptions.UserRequestException;
import com.green.Spring_board.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Void> signup(@RequestBody SignupRequest signupRequest) {
        try {
            userService.signup(signupRequest);
            return ResponseEntity.ok().build();
        } catch (ResourceConflictException e) {
            return ResponseEntity.status(409).build();
        } catch (UserRequestException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }
}
