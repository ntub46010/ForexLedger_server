package com.vincent.forexledger.unit;

import com.vincent.forexledger.exception.DuplicatedKeyException;
import com.vincent.forexledger.model.user.AppUser;
import com.vincent.forexledger.model.user.CreateUserRequest;
import com.vincent.forexledger.model.user.SocialLoginProvider;
import com.vincent.forexledger.repository.AppUserRepository;
import com.vincent.forexledger.service.AppUserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AppUserServiceTests {

    private AppUserService appUserService;
    private AppUserRepository appUserRepository;

    @Before
    public void init() {
        appUserRepository = mock(AppUserRepository.class);
        appUserService = new AppUserService(appUserRepository);
    }

    @Test
    public void testCreateUser() {
        when(appUserRepository.existsByEmail(anyString())).thenReturn(false);

        var createUserReq = new CreateUserRequest();
        createUserReq.setEmail("andy@gmail.com");
        createUserReq.setName("Andy");
        createUserReq.setSocialLoginProvider(SocialLoginProvider.GOOGLE);
        createUserReq.setFirebaseUid("ALxA755N");
        var userRes = appUserService.createUser(createUserReq);

        Assert.assertEquals(createUserReq.getEmail(), userRes.getEmail());
        Assert.assertEquals(createUserReq.getName(), userRes.getName());
        Assert.assertEquals(createUserReq.getSocialLoginProvider(), userRes.getSocialLoginProvider());
        Assert.assertNotNull(userRes.getCreatedTime());

        verify(appUserRepository).insert(any(AppUser.class));
    }

    @Test(expected = DuplicatedKeyException.class)
    public void testCreateUserWithExistentEmail() {
        when(appUserRepository.existsByEmail(anyString())).thenReturn(true);

        var createUserReq = new CreateUserRequest();
        createUserReq.setEmail("andy@gmail.com");
        appUserService.createUser(createUserReq);

        verify(appUserRepository).existsByEmail(createUserReq.getEmail());
    }
}
