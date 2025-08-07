package com.example.projectwebmovie.service;

import com.example.projectwebmovie.dto.CinemaRoomDTO;
import com.example.projectwebmovie.dto.SeatForCreateDTO;

import com.example.projectwebmovie.model.CinemaRoom;

import com.example.projectwebmovie.model.Seat;
import com.example.projectwebmovie.model.SeatType;
import com.example.projectwebmovie.repository.CinemaRoomRepository;
import com.example.projectwebmovie.repository.SeatJdbcRepository;
import com.example.projectwebmovie.repository.SeatRepository;
import com.example.projectwebmovie.repository.SeatTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CinemaRoomService {
    @Autowired
    private CinemaRoomRepository cinemaRoomRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private SeatTypeRepository seatTypeRepository;
    @Autowired
    private SeatJdbcRepository seatJdbcRepository;


    public Page<CinemaRoomDTO> getAll(Pageable pageable) {
        return cinemaRoomRepository.getCinemaRoomsWithActiveSeatCount(pageable);
    }

    /**
     * Lấy danh sách phòng chiếu có phân trang và tìm kiếm theo tên hoặc id.
     * @param pageable phân trang
     * @param search chuỗi tìm kiếm (có thể null hoặc rỗng)
     * @return Page<CinemaRoomDTO>
     */
    public Page<CinemaRoomDTO> getAll(Pageable pageable, String search) {
        if (search == null || search.trim().isEmpty()) {
            return getAll(pageable);
        }
        // Giả sử repository đã có hàm tìm kiếm, nếu chưa thì cần bổ sung ở repository
        return cinemaRoomRepository.findByCinemaRoomNameContainingOrCinemaRoomId(search.trim(), pageable);
    }

    public List<CinemaRoomDTO> getAll() {
        return cinemaRoomRepository.getCinemaRoomsWithActiveSeatCount();
    }


    public boolean isExistNameRoom(String nameRoom) {
        return cinemaRoomRepository.findByCinemaRoomName(nameRoom) != null;
    }

    @Transactional
    public void addNewRoom(String nameRoom, List<SeatForCreateDTO> seatList) {
        // 1. Tạo phòng mới
        CinemaRoom room = new CinemaRoom();
        room.setCinemaRoomName(nameRoom);
        cinemaRoomRepository.save(room); // Lưu trước để có ID dùng set vào ghế

        List<SeatType> seatTypeList = seatTypeRepository.findAll();

        Map<String, SeatType> seatTypeMap = seatTypeList.stream()
                .collect(Collectors.toMap(SeatType::getName, Function.identity()));

        Integer roomId = cinemaRoomRepository.findByCinemaRoomName(nameRoom).getCinemaRoomId();
        // 2. Lọc các ghế active, ánh xạ thành entity, set seatType + room
        List<Seat> entityList = seatList.stream()
                .filter(SeatForCreateDTO::isActive) // bỏ các ghế không active
                .map(dto -> {
                    Seat seat = new Seat();
                    seat.setSeatRow(dto.getSeatRow());
                    seat.setSeatColumn(dto.getSeatColumn());
                    seat.setActive(dto.isActive()); // chỉ map ghế active thôi

                    // Lấy loại ghế từ repo theo tên (name)
                    seat.setSeatTypeId(
                            seatTypeMap.get(dto.getSeatType()).getId());

                    // Set phòng cho ghế
                    seat.setCinemaRoomId(roomId);
                    return seat;
                })
                .toList();
        // 3. Lưu tất cả ghế vào DB
        seatJdbcRepository.insertSeatsBatch(entityList);
        // seatRepository.saveAll(entityList);
    }

    public CinemaRoomDTO getById(Integer id) {
        CinemaRoom room = cinemaRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phòng chiếu không tồn tại"));

        return CinemaRoomDTO.builder()
                .cinemaRoomId(room.getCinemaRoomId())
                .cinemaRoomName(room.getCinemaRoomName())
                .build();
    }

}
