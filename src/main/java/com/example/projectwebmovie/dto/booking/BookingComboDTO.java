package com.example.projectwebmovie.dto.booking;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class BookingComboDTO {
    private Integer comboId; // ID của combo
    private Integer quantity; // Số lượng của combo

    public BookingComboDTO(Integer comboId, Integer quantity) {
        this.comboId = comboId;
        this.quantity = quantity;
    }

}
