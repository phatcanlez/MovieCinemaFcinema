package com.example.projectwebmovie.service;

import com.example.projectwebmovie.model.SeatType;
import com.example.projectwebmovie.repository.SeatTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeatTypeService {
    @Autowired
    private SeatTypeRepository seatTypeRepository;
    public SeatType getTypeByName(String typeName){
        return seatTypeRepository.findByName(typeName).orElse(null);
    }
}
