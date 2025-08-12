package com.example.projectwebmovie.dto.promotion;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PromotionDTO {
    private String promotionId; // Mã khuyến mãi, khóa chính
    private String detail; // Chi tiết khuyến mãi, tối đa 255 ký tự
    private Double discountLevel; // Mức giảm giá (ví dụ: 20 cho 20%)
    private Double maxAmountForPercentDiscount; // Số tiền giảm tối đa đưa trên tổng bill nếu áp dụng giảm giá theo %
    private Double discountAmount; // Mức giảm giá tối đa theo VND
    private Integer usageLimit; // Số lượt được dùng tối đa
    private LocalDateTime endTime; // Thời gian kết thúc khuyến mãi
    private String promotionImage; // Đường dẫn hình ảnh khuyến mãi, tối đa 255 ký tự
    private LocalDateTime startTime; // Thời gian bắt đầu khuyến mãi
    private String title; // Tiêu đề khuyến mãi, tối đa 255 ký tự
    private String promotionCode; // Mã khuyến mãi duy nhất, tối đa 50 ký tự
    private Boolean isActive; // Trạng thái hoạt động của khuyến mãi, mặc định là true

}
