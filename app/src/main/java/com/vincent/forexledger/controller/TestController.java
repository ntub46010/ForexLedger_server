package com.vincent.forexledger.controller;

import com.vincent.forexledger.security.UserIdentity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
public class TestController {

    @Autowired
    private UserIdentity userIdentity;

    @GetMapping("/identity")
    public ResponseEntity<Map<String, String>> getUserIdentity() {
        Map<String, String> response = new HashMap<>();
        response.put("id", userIdentity.getId());
        response.put("name", userIdentity.getName());
        response.put("email", userIdentity.getEmail());

        return ResponseEntity.ok(response);
    }
}
