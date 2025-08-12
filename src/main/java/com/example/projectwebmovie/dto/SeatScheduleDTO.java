package com.example.projectwebmovie.dto;

import lombok.Data;

@Data
public class SeatScheduleDTO {
    private Integer seatId;
    private Integer seatRow; // Đổi từ String sang Integer
    private String seatColumn; // Đổi từ Integer sang String
    private Integer seatStatus; // 0: trống, 1: đã book
    private Double seatPrice; // Giá ghế, có thể null nếu không áp dụng
    private String seatType; // Loại ghế, có thể là "normal", "vip", "couple", v.v.

    public SeatScheduleDTO() {
        // Default constructor
    }

    public SeatScheduleDTO(Integer seatId, Integer seatRow, String seatColumn, Integer seatStatus, Double seatPrice,
            String seatType) {
        this.seatId = seatId;
        this.seatRow = seatRow;
        this.seatColumn = seatColumn;
        this.seatStatus = seatStatus;
        this.seatPrice = seatPrice;
        this.seatType = seatType;
    }
}