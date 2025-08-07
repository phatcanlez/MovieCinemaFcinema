package com.example.projectwebmovie.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class SeatDTO {
    private Integer seatId;
    private String seatColumn;
    private Integer seatRow;
    private Integer seatStatus;
    private boolean isActive;
    private String seatType;
}
