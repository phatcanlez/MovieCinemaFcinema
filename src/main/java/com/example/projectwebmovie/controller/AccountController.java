package com.example.projectwebmovie.controller;

import com.example.projectwebmovie.service.AccountService;
import io.swagger.v3.oas.annotations.Operation; // Import Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse; // Import ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses; // Import ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag; // Import Tag
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Account Management", description = "APIs for user account operations") // Thêm Tag cho Controller
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AccountService accountService;

    @GetMapping("/delete-account/{accountEmail}")
    @Operation(summary = "Delete an account by email", description = "Deletes a user account based on the provided email address.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account deleted successfully"),
            @ApiResponse(responseCode = "500", description = "Error deleting account")
    })
    public ResponseEntity<String> deleteAccount(@PathVariable("accountEmail") String accountEmail) {
        logger.info("Received request to delete account with email: {}", accountEmail);
        try {
            accountService.deleteAccount(accountEmail);
            logger.info("Account with email {} deleted successfully", accountEmail);
            return ResponseEntity.ok("xoa thanh cong");
        } catch (Exception e) {
            logger.error("Error deleting account with email {}: {}", accountEmail, e.getMessage(), e);
            return ResponseEntity.status(500).body("Error deleting account: " + e.getMessage());
        }
    }

    // THÊM API TEST "HELLO" NÀY
    @GetMapping("/test/hello")
    @Operation(summary = "Test API Endpoint", description = "Returns a simple 'Hello, World!' message to test API connectivity.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation")
    })
    public ResponseEntity<String> testHello() {
        logger.info("Hello endpoint accessed.");
        return ResponseEntity.ok("Hello, World!");
    }
    // KẾT THÚC API TEST "HELLO"

}