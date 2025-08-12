package com.example.projectwebmovie.wapper;

import com.example.projectwebmovie.dto.SeatForCreateDTO;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SeatForCreateDTOListWrapper {
    private List<SeatForCreateDTO> seatList;
}
