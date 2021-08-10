package com.vincent.forexledger.controller;

import com.vincent.forexledger.model.user.CreateUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    @Operation(
            summary = "Create user authenticated by Firebase Auth service.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Create user successfully."),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Data format in request body don't meet requirement.",
                            content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<Void> createUser(@Valid @RequestBody CreateUserRequest request) {
        // TODO: Provide location in header.
        return ResponseEntity.created(null).build();
    }
}
