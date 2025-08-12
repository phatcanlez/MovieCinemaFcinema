package com.example.projectwebmovie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatForCreateRequestDTO {
    private Integer seatId;
    private String seatColumn;
    private Integer seatRow;
    private boolean active;
    private String seatType;

}
