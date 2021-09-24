package com.vincent.forexledger.controller;

import com.vincent.forexledger.constants.APIPathConstants;
import com.vincent.forexledger.model.book.CreateBookRequest;
import com.vincent.forexledger.service.BookService;
import com.vincent.forexledger.util.URIUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(value = APIPathConstants.BOOKS, produces = MediaType.APPLICATION_JSON_VALUE)
public class BookController {

    @Autowired
    private BookService service;

    @Operation(
            summary = "Get exchange rates of specific bank.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Book is created successfully."),
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
    @PostMapping
    public ResponseEntity<Void> createBook(@Valid @RequestBody CreateBookRequest request) {
        var bookId = service.createBook(request);
        var location = URIUtil.create("/{id}", bookId);

        return ResponseEntity.created(location).build();
    }
}
