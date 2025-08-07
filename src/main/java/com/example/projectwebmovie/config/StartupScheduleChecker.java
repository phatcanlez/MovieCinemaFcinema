package com.example.projectwebmovie.config;

import com.example.projectwebmovie.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class StartupScheduleChecker {

    @Autowired
    private MovieService movieService;

    // Giờ mà bạn muốn chạy nếu app mở sau thời điểm này
    private static final LocalTime SCHEDULE_TIME = LocalTime.of(1, 0);

    @EventListener(ContextRefreshedEvent.class)
    public void checkAndRunScheduledJobIfMissed() {
        LocalTime now = LocalTime.now();

        if (now.isAfter(SCHEDULE_TIME)) {
            System.out.println("⏰ Sau 1h sáng — Chạy cập nhật trạng thái phim (vì app mở trễ)");
            movieService.updateMovieStatusesBasedOnDate();
        } else {
            System.out.println("✅ Trước 1h sáng — Không cần chạy cập nhật trạng thái");
        }
    }
}
