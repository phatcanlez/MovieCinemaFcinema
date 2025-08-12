package com.example.projectwebmovie.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAccountDTO {

    @NotBlank(message = "Account ID is required")
    private String accountId;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    // Removed @NotNull annotation to make dateOfBirth optional
    private LocalDate dateOfBirth;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name cannot exceed 255 characters")
    private String fullName;

    @Size(max = 255, message = "Gender cannot exceed 255 characters")
    private String gender;

    @Size(max = 255, message = "Identity card cannot exceed 255 characters")
    private String identityCard;

    private MultipartFile image;

    @Size(max = 255, message = "Phone number cannot exceed 255 characters")
    private String phoneNumber;

    private Integer points; // Can be null to keep current points
}