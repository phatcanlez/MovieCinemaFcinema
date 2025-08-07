package com.example.projectwebmovie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MovieRoomCalenderDTO {
    private String movieName;
    private String roomName;
    private String showDate;
    private String startTime;
    private String endTime;
    private String movieId;
    private String scheduleId;
    private String showDateId;
}
