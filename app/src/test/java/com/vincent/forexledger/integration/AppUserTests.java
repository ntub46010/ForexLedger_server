package com.vincent.forexledger.integration;

import com.vincent.forexledger.constants.APIPathConstants;
import com.vincent.forexledger.model.user.CreateUserRequest;
import com.vincent.forexledger.model.user.SocialLoginProvider;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
public class AppUserTests extends BaseTest {

    @Test
    public void testCreateUser() throws Exception {
        var request = new CreateUserRequest();
        request.setEmail("abc@gmail.com");
        request.setName("Andy");
        request.setSocialLoginProvider(SocialLoginProvider.GOOGLE);
        request.setFirebaseUid("ALxA755N");

        var mvcResult = mockMvc.perform(post(APIPathConstants.USERS)
                .headers(httpHeaders)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        var location = mvcResult.getResponse().getHeader(HttpHeaders.LOCATION);
        var userId = StringUtils.substringAfterLast(location, '/');
        var resultUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Can't find created user"));

        Assert.assertEquals(request.getEmail(), resultUser.getEmail());
        Assert.assertEquals(request.getName(), resultUser.getName());
        Assert.assertEquals(request.getSocialLoginProvider(), resultUser.getSocialLoginProvider());
        Assert.assertEquals(request.getFirebaseUid(), resultUser.getFirebaseUid());
        Assert.assertNotNull(resultUser.getCreatedTime());
    }

    @Test
    public void testCreateUserWithExistentEmail() throws Exception {
        var request = new CreateUserRequest();
        request.setEmail("abc@gmail.com");
        request.setName("Andy");
        request.setSocialLoginProvider(SocialLoginProvider.GOOGLE);
        request.setFirebaseUid("ALxA755N");

        mockMvc.perform(post(APIPathConstants.USERS)
                .headers(httpHeaders)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(APIPathConstants.USERS)
                .headers(httpHeaders)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }
}
