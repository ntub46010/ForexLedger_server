package com.vincent.forexledger.service;

import com.vincent.forexledger.model.user.CreateUserRequest;
import com.vincent.forexledger.model.user.UserResponse;

public class UserService {

    public UserResponse createUser(CreateUserRequest request) {
        return new UserResponse();
    }
}
