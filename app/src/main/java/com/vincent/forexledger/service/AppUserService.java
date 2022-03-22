package com.vincent.forexledger.service;

import com.vincent.forexledger.exception.DuplicatedKeyException;
import com.vincent.forexledger.model.user.CreateUserRequest;
import com.vincent.forexledger.model.user.AppUserResponse;
import com.vincent.forexledger.repository.AppUserRepository;
import com.vincent.forexledger.util.converter.AppUserConverter;

import java.util.Date;

public class AppUserService {

    private AppUserRepository appUserRepository;

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public AppUserResponse createUser(CreateUserRequest request) {
        if (appUserRepository.existsByEmail(request.getEmail())) {
            throw new DuplicatedKeyException("This email has been registered.");
        }

        var user = AppUserConverter.toAppUser(request);
        user.setCreatedTime(new Date());
        appUserRepository.insert(user);

        return AppUserConverter.toAppUserResponse(user);
    }
}
