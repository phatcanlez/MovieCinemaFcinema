package com.example.projectwebmovie.service;

import com.example.projectwebmovie.dto.MovieRoomCalenderDTO;
import com.example.projectwebmovie.dto.ScheduleDTO;
import com.example.projectwebmovie.model.*;
import com.example.projectwebmovie.repository.*;

import org.checkerframework.checker.units.qual.N;
import org.checkerframework.checker.units.qual.m;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
public class MovieScheduleService {

    @Autowired
    private MovieScheduleRepository movieScheduleRepo;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MovieScheduleRepository movieScheduleRepository;

    @Autowired
    private ShowDatesRepository showDatesRepository;

    @Autowired
    private MovieDateRepository movieDateRepository;
    @Autowired
    private SeatService seatService;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CinemaRoomRepository cinemaRoomRepository;
    private static final Logger logger = LoggerFactory.getLogger(MovieScheduleService.class);
    private static final int MINIMUM_GAP_BETWEEN_SCHEDULES = 20;
    private static final int BREAK_TIME = 10;

    /**
     * Lấy danh sách các khung giờ chiếu hợp lệ cho một phim tại một phòng chiếu cụ
     * thể vào một ngày nhất định.
     * Bao gồm đánh dấu các khung giờ:
     * - selected: đã chọn trước đó
     * - conflicted: đã bị chọn bởi phòng khác (cùng phim cùng ngày)
     * - available: có thể chọn (không bị chiếm bởi phim khác và không trùng giờ)
     */
    public List<ScheduleDTO> getScheduleOptionsForMovieAndRoom(Movie movie, int roomId, LocalDate showDate) {
        List<ScheduleDTO> result = new ArrayList<>();
        List<Schedule> allSchedules = scheduleRepository.findAllByOrderByScheduleTimeAsc();

        // ✅ Danh sách suất chiếu của phim hiện tại ở phòng hiện tại (dùng để xác định
        // selected)
        List<MovieSchedule> currentMovieSchedules = movieScheduleRepository
                .findByMovie_MovieIdAndCinemaRoom_CinemaRoomIdAndShowDates_ShowDate(
                        movie.getMovieId(), roomId, showDate);

        CinemaRoom room = cinemaRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Phòng chiếu không tồn tại với ID: " + roomId));

        Set<Integer> selectedIds = currentMovieSchedules.stream()
                .map(m -> m.getId().getScheduleId())
                .collect(Collectors.toSet());

        // ✅ Lấy tất cả thời điểm cần cách ra ít nhất MINIMUM_GAP_BETWEEN_SCHEDULES
        List<LocalTime> tempUsedTimes = new ArrayList<>();

        List<TimeRange> conflictedTimeRanges = getConflictedTimeRanges(movie.getMovieId(), roomId, showDate);
        List<TimeRange> occupiedRanges = getOccupiedTimeRanges(movie.getMovieId(), roomId, showDate);

        int minGap = movie.getDuration() + BREAK_TIME; // Thời gian tối thiểu giữa các suất chiếu

        for (Schedule s : allSchedules) {
            LocalTime start = LocalTime.parse(s.getScheduleTime());
            LocalTime end = start.plusMinutes(minGap);

            boolean selected = selectedIds.contains(s.getScheduleId());
            boolean overlapsWithSelected = currentMovieSchedules.stream()
                    .filter(ms -> selectedIds.contains(ms.getId().getScheduleId()))
                    .anyMatch(ms -> {
                        LocalTime selectedStart = LocalTime.parse(ms.getSchedule().getScheduleTime());
                        LocalTime selectedEnd = selectedStart.plusMinutes(movie.getDuration());
                        return start.isBefore(selectedEnd) && end.isAfter(selectedStart);
                    });
            boolean isOccupied = occupiedRanges.stream()
                    .anyMatch(range -> start.isBefore(range.getEnd().plusMinutes(MINIMUM_GAP_BETWEEN_SCHEDULES))
                            && end.isAfter(range.getStart()));

            boolean conflicted = conflictedTimeRanges.stream()
                    .anyMatch(range -> (!start.isBefore(range.getStart()) &&
                            start.isBefore(range.getStart().plusMinutes(MINIMUM_GAP_BETWEEN_SCHEDULES))));

            if ((isOccupied || conflicted || overlapsWithSelected) && !selected)
                continue;

            // ✅ Check nếu giờ bắt đầu này cách xa tất cả các giờ đã chiếu ít nhất
            // MINIMUM_GAP
            if (selected) {
                result.add(new ScheduleDTO(
                        s.getScheduleId(),
                        s.getScheduleTime(),
                        true,
                        true,
                        roomId,
                        start.toString(),
                        end.toString(),
                        room.getCinemaRoomName(),
                        conflicted));
                tempUsedTimes.add(end);
            } else {
                boolean available = isTimeAfterAll(start, tempUsedTimes,
                        MINIMUM_GAP_BETWEEN_SCHEDULES);
                if (available) {
                    result.add(new ScheduleDTO(
                            s.getScheduleId(),
                            s.getScheduleTime(),
                            available,
                            false,
                            roomId,
                            start.toString(),
                            end.toString(),
                            room.getCinemaRoomName(),
                            conflicted));
                    tempUsedTimes.add(start);
                }
            }
        }

        return result;
    }

    /**
     * Lấy danh sách MovieRoomCalenderDTO theo ngày, tính endTime dựa vào duration
     * phim
     */
    public List<MovieRoomCalenderDTO> getMovieRoomCalenderByShowDate(LocalDate showDate) {
        List<MovieRoomCalenderDTO> dtos = movieScheduleRepository.findMovieRoomCalenderByShowDate(showDate);
        for (MovieRoomCalenderDTO dto : dtos) {
            String movieId = dto.getMovieId();
            String startTime = dto.getStartTime();
            if (movieId != null && startTime != null) {
                Optional<Movie> movieOpt = movieRepository.findById(movieId);
                if (movieOpt.isPresent()) {
                    Integer duration = movieOpt.get().getDuration();
                    if (duration != null) {
                        try {
                            java.time.LocalTime start = java.time.LocalTime.parse(startTime);
                            java.time.LocalTime end = start.plusMinutes(duration).plusMinutes(BREAK_TIME);
                            dto.setEndTime(end.toString());
                        } catch (Exception e) {
                            dto.setEndTime(null);
                        }
                    }
                }
            }
        }
        return dtos;
    }

    /**
     * Lấy danh sách các khoảng thời gian bị xung đột – tức là phim này đã được
     * chiếu ở các phòng khác
     * trong cùng một ngày, và do đó không nên cho phép chọn trùng giờ ở các phòng
     * khác.
     *
     * @param movieId       ID phim đang xét
     * @param currentRoomId ID phòng hiện tại (phòng đang chọn lịch)
     * @param showDate      Ngày chiếu đang xét
     * @return Danh sách các khoảng thời gian (start → end) mà phim này đã chiếu ở
     *         các phòng khác
     */
    private List<TimeRange> getConflictedTimeRanges(String movieId, int currentRoomId, LocalDate showDate) {
        // 1. Lấy tất cả các suất chiếu của phim này trong ngày, ở tất cả các phòng
        List<MovieSchedule> allMovieSchedules = movieScheduleRepository
                .findByMovie_MovieIdAndShowDates_ShowDate(movieId, showDate);

        // 2. Lọc ra các suất chiếu không phải ở phòng hiện tại
        List<MovieSchedule> otherRoomSchedules = allMovieSchedules.stream()
                .filter(ms -> ms.getCinemaRoom().getCinemaRoomId() != currentRoomId)
                .toList();

        // 3. Tạo danh sách các khoảng thời gian bị chiếm bởi các suất chiếu ở phòng
        // khác
        return otherRoomSchedules.stream()
                .map(ms -> {
                    LocalTime start = LocalTime.parse(ms.getSchedule().getScheduleTime());
                    LocalTime end = start.plusMinutes(ms.getMovie().getDuration()); // thời lượng + nghỉ
                    return new TimeRange(start.toString(), end.toString());
                })
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách các khoảng thời gian đã bị chiếm bởi các suất chiếu khác trong
     * phòng này.
     * Sử dụng để kiểm tra xem giờ mới có bị trùng với phim khác hay không.
     *
     * @param movieId  ID phim hiện tại
     * @param roomId   ID phòng đang xét
     * @param showDate Ngày chiếu đang xét
     * @return Danh sách các khoảng thời gian đã bị chiếm bởi các suất chiếu khác
     */
    public List<TimeRange> getOccupiedTimeRanges(String movieId, int roomId, LocalDate showDate) {
        // Lấy tất cả suất chiếu trong phòng
        List<MovieSchedule> allSchedules = movieScheduleRepository
                .findByCinemaRoom_CinemaRoomIdAndShowDates_ShowDate(roomId, showDate);

        // Lọc các suất KHÁC phim hiện tại → tính occupied ranges
        return allSchedules.stream()
                .filter(ms -> !ms.getMovie().getMovieId().equals(movieId))
                .map(ms -> {
                    LocalTime start = LocalTime.parse(ms.getSchedule().getScheduleTime());
                    LocalTime end = start.plusMinutes(ms.getMovie().getDuration() + BREAK_TIME); // fixed
                    return new TimeRange(start.toString(), end.toString());
                })
                .collect(Collectors.toList());
    }

    /**
     * Kiểm tra giờ này có vi phạm khoảng cách tối thiểu giữa các suất chiếu không
     */

    private boolean isTimeAfterAll(LocalTime candidate, List<LocalTime> selectedList, int middleTime) {
        for (LocalTime selected : selectedList) {
            if (candidate.isBefore(selected.plusMinutes(middleTime))
                    || candidate.equals(selected.minusMinutes(middleTime))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Lưu danh sách giờ chiếu mới cho phim tại một phòng cụ thể.
     * Có kiểm tra trùng giờ với phim khác.
     */
    @Transactional
    public void saveSchedulesForMovie(Movie movie, List<Integer> newScheduleIds, Integer roomId, LocalDate date) {
        if (date.isBefore(movie.getFromDate()) || date.isAfter(movie.getToDate())) {
            throw new IllegalArgumentException("Ngày chiếu không được sau ngày khởi chiếu !");
        }
        if (date.isEqual(LocalDate.now()) || date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Không thể tạo lịch chiếu ở hiện tại hoặc quá khứ !");
        }
        // Step 1: Find or create ShowDate
        ShowDates showDate = showDatesRepository.findByShowDate(date)
                .orElseGet(() -> {
                    ShowDates sd = new ShowDates();
                    sd.setShowDate(date);
                    return showDatesRepository.save(sd);
                });

        // Step 2: Find or create MovieDate
        movieDateRepository.findByMovieAndShowDates(movie, showDate)
                .orElseGet(() -> {
                    MovieDate md = new MovieDate();
                    md.setMovie(movie);
                    md.setShowDates(showDate);
                    md.setId(new MovieDateId(movie.getMovieId(), showDate.getShowDateId()));
                    return movieDateRepository.save(md);
                });

        // Step 3: Get existing schedules for the movie in the room and date
        List<MovieSchedule> existing = movieScheduleRepo
                .findByMovie_MovieIdAndCinemaRoom_CinemaRoomIdAndShowDates_ShowDateId(
                        movie.getMovieId(), roomId, showDate.getShowDateId());

        Set<Integer> existingIds = existing.stream()
                .map(ms -> ms.getId().getScheduleId())
                .collect(Collectors.toSet());

        Set<Integer> newIds = new HashSet<>(newScheduleIds);

        // Step 4: Check for schedule conflicts
        List<Integer> conflictedIds = movieScheduleRepo.findConflictedScheduleIds(
                roomId, movie.getMovieId(), showDate.getShowDateId(), newIds);
        if (!conflictedIds.isEmpty()) {
            throw new IllegalArgumentException("Một hoặc nhiều giờ chiếu bị trùng");
        }

        // Step 5: Exit if no changes are needed
        if (existingIds.equals(newIds)) {
            return;
        }

        // Step 6: Delete schedules that are no longer needed
        List<MovieSchedule> toDelete = existing.stream()
                .filter(ms -> !newIds.contains(ms.getId().getScheduleId()))
                .collect(Collectors.toList());
        if (!toDelete.isEmpty()) {
            movieScheduleRepo.deleteAll(toDelete);
        }

        // Step 7: Add new schedules
        Set<Integer> toAdd = new HashSet<>(newIds);
        toAdd.removeAll(existingIds);

        if (!toAdd.isEmpty()) {
            List<Schedule> schedules = scheduleRepository.findAllById(toAdd);
            CinemaRoom room = cinemaRoomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found"));

            List<MovieSchedule> newEntries = schedules.stream()
                    .map(schedule -> {
                        MovieSchedule ms = new MovieSchedule();
                        ms.setId(new MovieScheduleId(
                                movie.getMovieId(),
                                schedule.getScheduleId(),
                                room.getCinemaRoomId(),
                                showDate.getShowDateId()));
                        ms.setMovie(movie);
                        ms.setSchedule(schedule);
                        ms.setCinemaRoom(room);
                        ms.setShowDates(showDate);
                        return ms;
                    })
                    .collect(Collectors.toList());

            movieScheduleRepo.saveAll(newEntries);

            // Step 8: Create ScheduleSeat for each new schedule
            // 🚀 Gọi xử lý sinh ghế bất đồng bộ
            seatService.createScheduleSeatsAsync(newEntries);
        }
    }

    /**
     * Lấy danh sách tất cả các phim có lịch chiếu trong ngày cụ thể.
     *
     * @param showDateId ID của ngày chiếu
     * @return Danh sách các phim có lịch chiếu trong ngày đó
     */
    public List<Movie> getAllMoviesByDay(Integer showDateId) {
        /*
         * List<MovieSchedule> movieSchedules =
         * movieScheduleRepository.findByCinemaRoom_ShowDates_ShowDateId(showDateId);
         */
        /*
         * if (movieSchedules.isEmpty()) {
         * throw new RuntimeException("No movies found for the given show date ID: " +
         * showDateId);
         * }
         * System.out.println("List of movie schedules: " + movieSchedules);
         * return movieSchedules.stream()
         * .map(MovieSchedule::getMovie)
         * .distinct()
         * .toList();
         */
        return null;
        // public List<Movie> getAllMoviesByDay(Integer showDateId) {
        // List<MovieSchedule> movieSchedules =
        // movieScheduleRepository.findByCinemaRoom_ShowDates_ShowDateId(showDateId);
        // if (movieSchedules.isEmpty()) {
        // throw new RuntimeException("No movies found for the given show date ID: " +
        // showDateId);
        // }
        // System.out.println("List of movie schedules: " + movieSchedules);
        // return movieSchedules.stream()
        // .map(MovieSchedule::getMovie)
        // .distinct()
        // .toList();
    }

    // public List<MovieSchedule> getAllMovieSchedules() {
    // return movieScheduleRepository.findAll();
    // }

    public MovieSchedule getMovieScheduleById(MovieScheduleId id) {
        return movieScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie schedule not found with ID: " + id));
    }

    public List<MovieSchedule> getSchedulesByMovieAndDate(String movieId, LocalDate date) {
        return movieScheduleRepository.findByMovieMovieIdAndShowDatesShowDate(movieId, date);
    }

    public List<MovieSchedule> getAllMovieSchedules() {
        return movieScheduleRepository.findAll();
    }

    public MovieSchedule getScheduleByDetails(String movieId, Integer cinemaRoomId, Integer showDateId,
            Integer scheduleId) {
        return movieScheduleRepository
                .findByMovie_MovieIdAndCinemaRoom_CinemaRoomIdAndShowDates_ShowDateIdAndSchedule_ScheduleId(
                        movieId, cinemaRoomId, showDateId, scheduleId);
    }

}
