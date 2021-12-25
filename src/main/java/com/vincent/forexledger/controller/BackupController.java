package com.vincent.forexledger.controller;

import com.vincent.forexledger.constants.APIPathConstants;
import com.vincent.forexledger.model.BookAndEntryBackup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = APIPathConstants.BACKUP, produces = MediaType.APPLICATION_JSON_VALUE)
public class BackupController {

    @Operation(
            summary = "Create book for login user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Book and related entries are exported successfully."),
                    @ApiResponse(
                            responseCode = "401",
                            description = "The API caller doesn't have effective authorization.",
                            content = @Content),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Book doesn't exist.",
                            content = @Content)
            }
    )
    @PostMapping(APIPathConstants.BOOK + "/{bookId}")
    public ResponseEntity<BookAndEntryBackup> backupBookAndEntry(@PathVariable("bookId") String bookId) {
        var backup = new BookAndEntryBackup();
        return ResponseEntity.ok(backup);
    }

    @Operation(
            summary = "Create book for login user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Book and related entries are imported successfully."),
                    @ApiResponse(
                            responseCode = "401",
                            description = "The API caller doesn't have effective authorization.",
                            content = @Content)
            }
    )
    @PostMapping
    public ResponseEntity<Void> importBookAndEntry(@RequestBody BookAndEntryBackup backup) {
        return ResponseEntity.noContent().build();
    }
}
