package com.vincent.forexledger.runner;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class FirebaseInitRunner implements CommandLineRunner {

    private static final String RESOURCE_SERVICE_ACCOUNT = "config/firebase-service-account.json";

    @Override
    public void run(String... args) throws Exception {
        var inputStream = getClass().getClassLoader().getResourceAsStream(RESOURCE_SERVICE_ACCOUNT);
        var options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(inputStream))
                .build();

        FirebaseApp.initializeApp(options);
    }
}
