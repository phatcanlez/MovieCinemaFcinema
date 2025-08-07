package com.example.projectwebmovie.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeDTO {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 255, message = "Username must be between 3 and 255 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 255, message = "Password must be between 6 and 255 characters")
    private String password;

    @NotBlank(message = "Full Name is required")
    @Size(max = 255, message = "Full Name must not exceed 255 characters")
    private String fullName;

    @NotNull(message = "Date of Birth is required")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "Identity Card is required")
    @Pattern(regexp = "^\\d{9,12}$", message = "Identity Card must be 9 to 12 digits")
    private String identityCard;

    @NotBlank(message = "Phone Number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone Number must be 10 digits")
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    private MultipartFile image; // Avatar upload
    @NotNull(message = "Register Date is required")
    private LocalDate registerDate;
}