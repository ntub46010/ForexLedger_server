package com.vincent.forexledger.unit;

import com.vincent.forexledger.model.user.AppUser;
import com.vincent.forexledger.model.user.CreateUserRequest;
import com.vincent.forexledger.model.user.SocialLoginProvider;
import com.vincent.forexledger.util.converter.AppUserConverter;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class AppUserConverterTests {

    @Test
    public void testConvertToAppUser() {
        var createUserReq = new CreateUserRequest();
        createUserReq.setEmail("andy@gmail.com");
        createUserReq.setName("Andy");
        createUserReq.setSocialLoginProvider(SocialLoginProvider.GOOGLE);
        createUserReq.setFirebaseUid("ALxA755N");

        var appUser = AppUserConverter.toAppUser(createUserReq);

        Assert.assertEquals(createUserReq.getEmail(), appUser.getEmail());
        Assert.assertEquals(createUserReq.getName(), appUser.getName());
        Assert.assertEquals(createUserReq.getSocialLoginProvider(), appUser.getSocialLoginProvider());
        Assert.assertEquals(createUserReq.getFirebaseUid(), appUser.getFirebaseUid());
    }

    @Test
    public void testConvertToAppUserResponse() {
        var appUser = new AppUser();
        appUser.setId(ObjectId.get().toHexString());
        appUser.setEmail("andy@gmail.com");
        appUser.setName("Andy");
        appUser.setSocialLoginProvider(SocialLoginProvider.GOOGLE);
        appUser.setFirebaseUid("ALxA755N");
        appUser.setCreatedTime(new Date());

        var appUserRes = AppUserConverter.toAppUserResponse(appUser);

        Assert.assertEquals(appUserRes.getId(), appUser.getId());
        Assert.assertEquals(appUserRes.getEmail(), appUser.getEmail());
        Assert.assertEquals(appUserRes.getName(), appUser.getName());
        Assert.assertEquals(appUserRes.getSocialLoginProvider(), appUser.getSocialLoginProvider());
        Assert.assertEquals(appUserRes.getCreatedTime(), appUser.getCreatedTime());
    }
}
