package com.example.projectwebmovie.dto;

import lombok.Data;

@Data
public class SeatForCreateDTO {
    private Integer seatId;
    private String seatColumn;
    private Integer seatRow;
    private boolean isActive;
    private String seatType;
}
