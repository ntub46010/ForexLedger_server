package com.vincent.forexledger.model.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "app_user")
public class AppUser {
    @Id
    private String id;
    private String email;
    private String name;
    private SocialLoginProvider socialLoginProvider;
    private String firebaseUid;
    private Date createdTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
}
