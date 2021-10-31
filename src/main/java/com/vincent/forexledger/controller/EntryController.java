package com.vincent.forexledger.controller;

import com.vincent.forexledger.constants.APIPathConstants;
import com.vincent.forexledger.model.entry.CreateEntryRequest;
import com.vincent.forexledger.service.EntryService;
import com.vincent.forexledger.util.URIUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = APIPathConstants.ENTRIES, produces = MediaType.APPLICATION_JSON_VALUE)
public class EntryController {

    @Autowired
    private EntryService service;

    @PostMapping
    public ResponseEntity<Void> createEntry(@RequestBody CreateEntryRequest request) {
        var entryId = service.createEntry(request);
        var location = URIUtil.create("/{id}", entryId);

        return ResponseEntity.created(location).build();
    }
}
