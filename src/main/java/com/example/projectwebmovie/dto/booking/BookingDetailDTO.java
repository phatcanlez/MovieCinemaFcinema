package com.example.projectwebmovie.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailDTO {
    // Booking Information
    private String bookingId;
    private LocalDateTime bookingDate;
    private String status;
    private String paymentMethod;
    private Double totalAmount;

    // Customer Information
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Integer memberPoints;

    // Ticket Information
    private String movieName;
    private LocalDate showDate;
    private LocalTime showTime;
    private String roomName;
    private String seats;
    private Double ticketPrice;
    private Double foodBeveragePrice;
    private Double discountAmount;
    private Integer usedPoints;

    // Food & Beverage Details
    private List<ComboDetailDTO> combos;

    // Promotion Details
    private String promotionCode;
    private String promotionName;
    private Double promotionDiscount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComboDetailDTO {
        private String comboName;
        private Integer quantity;
        private Double unitPrice;
        private Double totalPrice;
    }
}
