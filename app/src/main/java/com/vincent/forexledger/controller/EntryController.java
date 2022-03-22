package com.vincent.forexledger.controller;

import com.vincent.forexledger.constants.APIPathConstants;
import com.vincent.forexledger.model.entry.CreateEntryRequest;
import com.vincent.forexledger.model.entry.EntryListResponse;
import com.vincent.forexledger.service.EntryService;
import com.vincent.forexledger.util.URIUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = APIPathConstants.ENTRIES, produces = MediaType.APPLICATION_JSON_VALUE)
public class EntryController {

    @Autowired
    private EntryService service;

    // TODO: provide swagger document of API and request body
    @PostMapping
    public ResponseEntity<Void> createEntry(@RequestBody CreateEntryRequest request) {
        var entryId = service.createEntry(request);
        var location = URIUtil.create("/{id}", entryId);

        return ResponseEntity.created(location).build();
    }

    @Operation(
            summary = "Get entries of specific book.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Entries are retrieved successfully."),
                    @ApiResponse(
                            responseCode = "401",
                            description = "The API caller doesn't have effective authorization.",
                            content = @Content),
                    @ApiResponse(
                            responseCode = "404",
                            description = "The book doesn't exist.",
                            content = @Content)
            }
    )
    @GetMapping
    public ResponseEntity<List<EntryListResponse>> loadBookEntries(
            @Parameter(description = "Book id of entries.", required = true)
            @RequestParam("bookId")
                    String bookId) {
        var entries = service.loadBookEntries(bookId);
        return ResponseEntity.ok(entries);
    }
}
