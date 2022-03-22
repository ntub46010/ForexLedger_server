package com.vincent.forexledger.util.converter;

import com.vincent.forexledger.model.user.AppUser;
import com.vincent.forexledger.model.user.CreateUserRequest;
import com.vincent.forexledger.model.user.AppUserResponse;

public class AppUserConverter {
    private AppUserConverter() {
    }

    public static AppUser toAppUser(CreateUserRequest request) {
        var user = new AppUser();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setSocialLoginProvider(request.getSocialLoginProvider());
        user.setFirebaseUid(request.getFirebaseUid());

        return user;
    }

    public static AppUserResponse toAppUserResponse(AppUser user) {
        var response = new AppUserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setSocialLoginProvider(user.getSocialLoginProvider());
        response.setCreatedTime(user.getCreatedTime());

        return response;
    }
}
