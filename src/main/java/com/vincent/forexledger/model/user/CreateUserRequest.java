package com.vincent.forexledger.model.user;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Deprecated
public class CreateUserRequest {
    @Schema(description = "Email address associating with social network.")
    @Email
    private String email;

    @Schema(description = "User full name displaying in social network.")
    @NotBlank
    private String name;

    @Schema(description = "Social network provider using in registration.", example = "FACEBOOK")
    @NotNull
    private SocialLoginProvider socialLoginProvider;

    @Schema(description = "User id stored in Firebase Authentication service.")
    @NotBlank
    private String firebaseUid;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SocialLoginProvider getSocialLoginProvider() {
        return socialLoginProvider;
    }

    public void setSocialLoginProvider(SocialLoginProvider socialLoginProvider) {
        this.socialLoginProvider = socialLoginProvider;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }
}
