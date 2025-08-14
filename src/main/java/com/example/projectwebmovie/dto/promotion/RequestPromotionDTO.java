package com.example.projectwebmovie.dto.promotion;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class RequestPromotionDTO {
    @NotBlank(message = "Promotion Detail cannot be blank")
    @Size(max = 255, message = "Promotion Detail cannot exceed 255 characters")
    private String detail; // Chi tiết khuyến mãi, tối đa 255 ký tự

    @Max(value = 20, message = "Discount level cannot exceed 20%")
    @Min(value = 0, message = "Discount level cannot be negative")
    private Double discountLevel; // Mức giảm giá (ví dụ: 20 cho 20%)

    @Max(value = 100000, message = "Max amount for percent discount cannot exceed 100.000đ")
    @Min(value = 1000, message = "Max amount for percent discount must be at least 1.000đ")
    private Double maxAmountForPercentDiscount;

    @Max(value = 100000, message = "Discount level cannot exceed 100.000đ")
    @Min(value = 1000, message = "Discount level must be at least 1.000đ")
    private Double discountAmount;

    private Integer usageLimit;

    @NotNull(message = "End Time cannot be blank")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime endTime; // Thời gian kết thúc khuyến mãi

    @NotNull(message = "Start time cannot be blank")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime startTime; // Thời gian bắt đầu khuyến mãi

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255, message = "Promotion Title cannot exceed 255 characters")
    private String title; // Tiêu đề khuyến mãi, tối đa 255 ký tự

    @NotBlank(message = "Promotion code cannot be blank")
    @Size(max = 30, message = "Promotion Code cannot exceed 30 characters")
    private String promotionCode; // Mã khuyến mãi duy nhất, tối đa 50 ký tự

    private String promotionImage;

    private Boolean isActive; // Trạng thái hoạt động của khuyến mãi, mặc định là true
}
