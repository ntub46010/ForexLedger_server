package com.vincent.forexledger.controller;

import com.vincent.forexledger.constants.APIPathConstants;
import com.vincent.forexledger.model.backup.BookAndEntryBackup;
import com.vincent.forexledger.service.BackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = APIPathConstants.BACKUP, produces = MediaType.APPLICATION_JSON_VALUE)
public class BackupController {

    @Autowired
    private BackupService backupService;

    @Operation(
            summary = "Backup book and related entries as JSON format.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Book and related entries are backup successfully."),
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
        var backup = backupService.backupBookAndEntry(bookId);
        return ResponseEntity.ok(backup);
    }

    @Operation(
            summary = "Use JSON format data to restore book and related entries.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Book and related entries are restored successfully."),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Data format in request body isn't expected.",
                            content = @Content),
                    @ApiResponse(
                            responseCode = "401",
                            description = "The API caller doesn't have effective authorization.",
                            content = @Content)
            }
    )
    @PostMapping(APIPathConstants.BOOK + APIPathConstants.RESTORE)
    public ResponseEntity<Void> restoreBookAndEntry(@RequestBody BookAndEntryBackup backup) {
        backupService.restoreBookAndEntry(backup);
        return ResponseEntity.noContent().build();
    }
}
