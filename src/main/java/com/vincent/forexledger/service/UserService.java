package com.vincent.forexledger.service;

import com.vincent.forexledger.model.user.CreateUserRequest;
import com.vincent.forexledger.model.user.AppUserResponse;
import com.vincent.forexledger.repository.AppUserRepository;
import com.vincent.forexledger.util.converter.AppUserConverter;

import java.util.Date;

public class UserService {

    private AppUserRepository appUserRepository;

    public UserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public AppUserResponse createUser(CreateUserRequest request) {
        var user = AppUserConverter.toAppUser(request);
        user.setCreatedTime(new Date());
        appUserRepository.insert(user);

        return AppUserConverter.toAppUserResponse(user);
    }
}
