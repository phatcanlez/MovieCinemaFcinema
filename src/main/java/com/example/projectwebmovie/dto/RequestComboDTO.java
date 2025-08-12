package com.example.projectwebmovie.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestComboDTO {
    @NotBlank(message = "Combo name is required")
    private String comboName;

    private String description;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be non-negative")
    private Integer price;

    @NotNull(message = "Discount percentage is required")
    @Min(value = 0, message = "Discount percentage must be non-negative")
    @Max(value = 100, message = "Discount percentage must not exceed 100")
    private Integer discountPercentage;

    @NotNull(message = "Active status is required")
    private Boolean active;

    private String imageUrl; // Đã có, đảm bảo getter/setter từ @Data

    @NotNull(message = "Combo status is required")
    private String comboStatus; // Đã có, đảm bảo getter/setter từ @Data
}