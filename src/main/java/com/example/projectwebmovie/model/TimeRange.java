package com.example.projectwebmovie.model;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeRange {
    private LocalTime start;
    private LocalTime end;
    
    // Optional: nếu cần thêm constructor với String
    public TimeRange(String startStr, String endStr) {
        this.start = LocalTime.parse(startStr);
        this.end = LocalTime.parse(endStr);
    }

    public boolean contains(LocalTime time) {
        return !time.isBefore(start) && time.isBefore(end);
    }
    public boolean overlaps(LocalTime start, LocalTime end) {
        return !(this.end.compareTo(start) <= 0 || this.start.compareTo(end) >= 0);
    }

}
