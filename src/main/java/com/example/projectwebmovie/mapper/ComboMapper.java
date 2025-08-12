package com.example.projectwebmovie.mapper;

import com.example.projectwebmovie.dto.ComboDTO;
import com.example.projectwebmovie.dto.RequestComboDTO;
import com.example.projectwebmovie.enums.ComboStatus;
import com.example.projectwebmovie.model.Combo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ComboMapper {
    @Mapping(target = "comboId", source = "comboId")
    @Mapping(target = "comboName", source = "comboName")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "discountPercentage", source = "discountPercentage")
    @Mapping(target = "discountedPrice", expression = "java(combo.getDiscountedPrice())")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "imageUrl", source = "imageUrl") // Đảm bảo ánh xạ imageUrl
    @Mapping(target = "comboStatus", expression = "java(combo.getComboStatus().name())")
    ComboDTO toComboDTO(Combo combo);

    @Mapping(target = "comboId", ignore = true)
    @Mapping(target = "bookingCombos", ignore = true)
    @Mapping(target = "price", source = "price")
    @Mapping(target = "discountPercentage", source = "discountPercentage")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "imageUrl", source = "imageUrl") // Ánh xạ từ RequestComboDTO.imageUrl
    @Mapping(target = "comboStatus", source = "comboStatus", qualifiedByName = "mapComboStatus")
    Combo toCombo(RequestComboDTO requestComboDTO);

    @Named("mapComboStatus")
    default ComboStatus mapComboStatus(String status) {
        if (status == null)
            return ComboStatus.NORMAL;
        try {
            return ComboStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ComboStatus.NORMAL; // Giá trị mặc định nếu không hợp lệ
        }
    }
}