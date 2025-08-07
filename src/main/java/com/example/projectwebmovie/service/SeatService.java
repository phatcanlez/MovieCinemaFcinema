package com.example.projectwebmovie.service;

import com.example.projectwebmovie.dto.SeatForCreateDTO;
import com.example.projectwebmovie.dto.SeatForCreateRequestDTO;
import com.example.projectwebmovie.mapper.SeatMapper;
import com.example.projectwebmovie.model.*;
import com.example.projectwebmovie.repository.*;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatService {
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private SeatTypeRepository seatTypeRepository;
    @Autowired
    private ScheduleSeatRepository scheduleSeatRepository;
    @Autowired
    private MovieScheduleRepository movieScheduleRepository;
    @Autowired
    private SeatJdbcRepository seatJdbcRepository;
    @Autowired
    private MovieRepository movieRepository;

    /**
     * Lấy danh sách ghế theo ID phòng chiếu
     * 
     * @param id ID của phòng chiếu
     * @return Danh sách ghế dưới dạng SeatForCreateDTO
     */
    public List<SeatForCreateDTO> getListByRoomId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Cinema room ID cannot be null");
        }
        return seatRepository.findByCinemaRoomId(id).stream()
                .map(SeatMapper::toDTO)
                .toList();
    }





    /**
     * Lấy bản đồ ghế theo ID phòng chiếu
     * 
     * @param id ID của phòng chiếu
     * @return Bản đồ ghế dưới dạng Map<row, Map<column, SeatForCreateDTO>>
     */
    public Map<Integer, Map<String, SeatForCreateDTO>> getSeatMapByRoomId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Cinema room ID cannot be null");
        }
        Map<Integer, Map<String, SeatForCreateDTO>> seatMap = new HashMap<>();
        List<SeatForCreateDTO> seats = getListByRoomId(id);
        for (SeatForCreateDTO seat : seats) {
            seatMap.computeIfAbsent(seat.getSeatRow(), r -> new HashMap<>())
                    .put(seat.getSeatColumn(), seat);
        }
        return seatMap;
    }





    /**
     * Tạo danh sách cột ghế từ số lượng tối đa cột
     * 
     * @param maxColumn Số lượng cột tối đa
     * @return Danh sách cột ghế
     */
    public List<String> generateColumnList(int maxColumn) {
        if (maxColumn <= 0) {
            throw new IllegalArgumentException("Max column must be positive");
        }
        List<String> columnList = new ArrayList<>();
        for (int j = 0; j < maxColumn; j++) {
            columnList.add(String.valueOf((char) ('A' + j)));
        }
        return columnList;
    }



    /**
     * Tạo danh sách ghế mới với số hàng và cột
     * 
     * @param maxCol Số cột tối đa
     * @param maxRow Số hàng tối đa
     * @return Danh sách ghế mới
     */
    public List<SeatForCreateDTO> createNewSeatMatrixList(int maxCol, int maxRow) {
        if (maxCol <= 0 || maxRow <= 0) {
            throw new IllegalArgumentException("Max columns and rows must be positive");
        }
        List<SeatForCreateDTO> seatList = new ArrayList<>();
        for (int row = 1; row <= maxRow; row++) {
            for (int col = 1; col <= maxCol; col++) {
                SeatForCreateDTO seat = new SeatForCreateDTO();
                seat.setSeatRow(row);
                seat.setSeatColumn(String.valueOf((char) ('A' + col - 1)));
                seat.setActive(true);
                seat.setSeatType("STANDARD");
                seatList.add(seat);
            }
        }
        return seatList;
    }

    /**
     * Lưu danh sách ghế vào cơ sở dữ liệu
     * 
     * @param seats Danh sách ghế cần lưu
     */
    public void saveAll(List<Seat> seats) {
        if (seats == null || seats.isEmpty()) {
            throw new IllegalArgumentException("Seat list cannot be null or empty");
        }
        seatRepository.saveAll(seats);
    }

    /**
     * Cập nhật bản đồ ghế theo ID phòng chiếu
     * 
     * @param cinemaRoomId ID của phòng chiếu
     * @param seatList      Bản đồ ghế cần cập nhật
     */
    public void updateSeatMap(Integer cinemaRoomId, List<SeatForCreateRequestDTO> seatList) {
        if (cinemaRoomId == null || seatList == null) {
            throw new IllegalArgumentException("Cinema room ID and seat map cannot be null");
        }
        List<Seat> seats = new ArrayList<>();

        // Tải tất cả seatType vào map
        Map<String, SeatType> seatTypeMap = seatTypeRepository.findAll().stream()
                .collect(Collectors.toMap(SeatType::getName, st -> st));

        List<Seat> entityList = seatList.stream()
                .filter(SeatForCreateRequestDTO::isActive) // bỏ các ghế không active
                .map(dto -> {
                    Seat seat = new Seat();
                    seat.setSeatId(dto.getSeatId());
                    seat.setSeatRow(dto.getSeatRow());
                    seat.setSeatColumn(dto.getSeatColumn());
                    seat.setActive(dto.isActive()); // chỉ map ghế active thôi

                    // Lấy loại ghế từ repo theo tên (name)
                    seat.setSeatTypeId(
                            seatTypeMap.get(dto.getSeatType()).getId());

                    // Set phòng cho ghế
                    seat.setCinemaRoomId(cinemaRoomId);
                    return seat;
                })
                .toList();

        seatJdbcRepository.insertSeatsBatch(entityList);
    }



    @Transactional
    public void createScheduleSeatsForSchedules(List<MovieSchedule> schedules) {
        if (schedules == null || schedules.isEmpty())
            return;

        String movieId = schedules.get(0).getMovie().getMovieId();
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found: " + movieId));

        if (movie.getPrice() == null) {
            throw new IllegalArgumentException("Movie price cannot be null");
        }

        Double basePrice = movie.getPrice();

        // Gom nhóm theo (roomId + showDateId)
        Map<String, List<MovieSchedule>> grouped = schedules.stream()
                .collect(Collectors.groupingBy(
                        ms -> ms.getCinemaRoom().getCinemaRoomId() + "_" + ms.getShowDates().getShowDateId()));

        for (Map.Entry<String, List<MovieSchedule>> entry : grouped.entrySet()) {
            String[] keyParts = entry.getKey().split("_");
            Integer roomId = Integer.parseInt(keyParts[0]);
            Integer showDateId = Integer.parseInt(keyParts[1]);

            // 1. Load danh sách ghế đang có trong phòng
            List<Seat> seats = seatRepository.findByCinemaRoomIdAndIsActiveTrue(roomId);
            if (seats.isEmpty())
                continue;

            // 2. Lấy danh sách ScheduleSeat đang có cho ngày và phòng này của phim này
            List<ScheduleSeat> existingSeats = scheduleSeatRepository
                    .findByMovieIdAndCinemaRoomIdAndShowDateId(movieId, roomId, showDateId);

            // Tập các key hiện đang tồn tại trong DB
            Set<String> existingKeys = existingSeats.stream()
                    .map(ss -> ss.getScheduleId() + "_" + ss.getSeatId())
                    .collect(Collectors.toSet());

            // Tập các scheduleId đang được giữ lại
            Set<Integer> incomingScheduleIds = entry.getValue().stream()
                    .map(ms -> ms.getSchedule().getScheduleId())
                    .collect(Collectors.toSet());

            // 3. Xoá các ghế thuộc suất chiếu đã bị huỷ
            List<ScheduleSeat> toDelete = existingSeats.stream()
                    .filter(ss -> !incomingScheduleIds.contains(ss.getScheduleId()))
                    .collect(Collectors.toList());

            if (!toDelete.isEmpty()) {
                scheduleSeatRepository.deleteAll(toDelete);
            }

            // 4. Chuẩn bị bảng giá
            Map<Integer, Double> seatPriceMap = seats.stream()
                    .collect(Collectors.toMap(
                            Seat::getSeatId,
                            seat -> seat.getSeatType().getId() == 1
                                    ? basePrice
                                    : basePrice + basePrice * 0.3));

            List<ScheduleSeat> toInsert = new ArrayList<>();

            // 5. Duyệt từng suất để thêm nếu chưa có
            for (MovieSchedule ms : entry.getValue()) {
                Integer scheduleId = ms.getSchedule().getScheduleId();

                for (Seat seat : seats) {
                    String key = scheduleId + "_" + seat.getSeatId();
                    if (existingKeys.contains(key))
                        continue; // ghế đã có → bỏ qua

                    ScheduleSeat ss = new ScheduleSeat();
                    ss.setScheduleSeatId(UUID.randomUUID().toString().substring(0, 8));
                    ss.setMovieId(movieId);
                    ss.setScheduleId(scheduleId);
                    ss.setShowDateId(showDateId);
                    ss.setCinemaRoomId(roomId);
                    ss.setSeatId(seat.getSeatId());
                    ss.setSeatRow(seat.getSeatRow());
                    ss.setSeatColumn(seat.getSeatColumn());
                    ss.setSeatType(seat.getSeatType());
                    ss.setSeatStatus(0); // trạng thái mặc định
                    ss.setSeatPrice(seatPriceMap.get(seat.getSeatId()));

                    toInsert.add(ss);
                }
            }

            // 6. Ghi theo batch
            int batchSize = 100;
            for (int i = 0; i < toInsert.size(); i += batchSize) {
                int end = Math.min(i + batchSize, toInsert.size());
                scheduleSeatRepository.saveAll(toInsert.subList(i, end));
            }
        }
    }

    @Async
    public void createScheduleSeatsAsync(List<MovieSchedule> schedules) {
        createScheduleSeatsForSchedules(schedules);
    }


}