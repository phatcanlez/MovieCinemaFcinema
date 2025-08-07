package com.example.projectwebmovie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor // <== Bắt buộc phải có dòng này để dùng được constructor 4 tham số
public class ScheduleDTO {
    private int scheduleId;
    private String scheduleTime;
    private boolean available;
    private boolean selected;
    private int roomId;
    private String startTime;
    private String endTime;
    private String roomName; // ✅ Thêm trường roomName
    private boolean conflicted;

}
