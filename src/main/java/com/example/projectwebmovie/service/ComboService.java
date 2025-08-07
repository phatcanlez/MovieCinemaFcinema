package com.example.projectwebmovie.service;

import com.example.projectwebmovie.dto.ComboDTO;
import com.example.projectwebmovie.dto.RequestComboDTO;
import com.example.projectwebmovie.mapper.ComboMapper;
import com.example.projectwebmovie.model.Combo;
import com.example.projectwebmovie.repository.ComboRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComboService {

    private static final Logger logger = LoggerFactory.getLogger(ComboService.class);

    @Autowired
    private ComboRepository comboRepository;

    @Autowired
    private ComboMapper comboMapper;

    @Autowired
    private CloudinaryService cloudinaryService;

    public Page<ComboDTO> findAllCombos(Pageable pageable) {
        logger.info("Fetching all combos from repository with pageable: page={}, size={}", pageable.getPageNumber(),
                pageable.getPageSize());
        try {
            Page<Combo> comboPage = comboRepository.findAll(pageable);
            logger.info("Found {} combos in database, total pages={}", comboPage.getTotalElements(),
                    comboPage.getTotalPages());
            for (Combo combo : comboPage.getContent()) {
                logger.debug(
                        "Combo: id={}, name={}, price={}, discountPercentage={}, active={}, imageUrl={}, comboStatus={}",
                        combo.getComboId(), combo.getComboName(), combo.getPrice(), combo.getDiscountPercentage(),
                        combo.isActive(), combo.getImageUrl(), combo.getComboStatus());
            }
            return comboPage.map(comboMapper::toComboDTO);
        } catch (Exception e) {
            logger.error("Error fetching combos: {}", e.getMessage(), e);
            throw e;
        }
    }

    public ComboDTO findComboById(Integer comboId) {
        logger.info("Fetching combo with ID: {}", comboId);
        try {
            Combo combo = comboRepository.findById(comboId)
                    .orElseThrow(() -> new IllegalArgumentException("Combo not found with ID: " + comboId));
            logger.debug(
                    "Combo found: id={}, name={}, price={}, discountPercentage={}, active={}, imageUrl={}, comboStatus={}",
                    combo.getComboId(), combo.getComboName(), combo.getPrice(), combo.getDiscountPercentage(),
                    combo.isActive(), combo.getImageUrl(), combo.getComboStatus());
            return comboMapper.toComboDTO(combo);
        } catch (Exception e) {
            logger.error("Error fetching combo with ID {}: {}", comboId, e.getMessage(), e);
            throw e;
        }
    }

    public void addCombo(RequestComboDTO requestComboDTO, MultipartFile image) {
        logger.info("Adding new combo: {}", requestComboDTO.getComboName());
        logger.debug("RequestComboDTO: imageUrl={}, comboStatus={}", requestComboDTO.getImageUrl(),
                requestComboDTO.getComboStatus());
        try {
            if (requestComboDTO.getDiscountPercentage() == null || requestComboDTO.getDiscountPercentage() < 0
                    || requestComboDTO.getDiscountPercentage() > 100) {
                throw new IllegalArgumentException("Discount percentage must be between 0 and 100");
            }
            if (comboRepository.existsByComboName(requestComboDTO.getComboName())) {
                throw new IllegalArgumentException("Combo name already exists: " + requestComboDTO.getComboName());
            }
            Combo combo = comboMapper.toCombo(requestComboDTO);
            if (image != null && !image.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(image);
                combo.setImageUrl(imageUrl);
            }
            comboRepository.save(combo);
            logger.info("Combo added successfully with ID: {}", combo.getComboId());
        } catch (Exception e) {
            logger.error("Error adding combo: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void updateCombo(Integer comboId, RequestComboDTO requestComboDTO, MultipartFile image) {
        logger.info("Updating combo with ID: {}", comboId);
        logger.debug("RequestComboDTO: imageUrl={}, comboStatus={}", requestComboDTO.getImageUrl(),
                requestComboDTO.getComboStatus());
        try {
            if (requestComboDTO.getDiscountPercentage() == null || requestComboDTO.getDiscountPercentage() < 0
                    || requestComboDTO.getDiscountPercentage() > 100) {
                throw new IllegalArgumentException("Discount percentage must be between 0 and 100");
            }

            // Lấy combo hiện tại từ database
            Combo existingCombo = comboRepository.findById(comboId)
                    .orElseThrow(() -> new IllegalArgumentException("Combo not found with ID: " + comboId));

            // Kiểm tra trùng tên combo
            if (!existingCombo.getComboName().equals(requestComboDTO.getComboName()) &&
                    comboRepository.existsByComboName(requestComboDTO.getComboName())) {
                throw new IllegalArgumentException("Combo name already exists: " + requestComboDTO.getComboName());
            }

            // Chuyển đổi requestComboDTO thành đối tượng Combo mới
            Combo updatedCombo = comboMapper.toCombo(requestComboDTO);

            // Gán ID cho updatedCombo để đảm bảo cập nhật đúng record
            updatedCombo.setComboId(comboId);

            // Xử lý hình ảnh: Chỉ cập nhật nếu có ảnh mới được tải lên
            if (image != null && !image.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(image);
                updatedCombo.setImageUrl(imageUrl);
            } else {
                // Giữ lại URL ảnh cũ nếu không có ảnh mới
                updatedCombo.setImageUrl(existingCombo.getImageUrl());
            }

            // Lưu đối tượng đã cập nhật vào database
            comboRepository.save(updatedCombo);

            logger.info("Combo updated successfully with ID: {}", comboId);
        } catch (Exception e) {
            logger.error("Error updating combo with ID {}: {}", comboId, e.getMessage(), e);
            throw e;
        }
    }

    public void toggleComboStatus(Integer comboId) {
        logger.info("Toggling status for combo with ID: {}", comboId);
        try {
            Combo combo = comboRepository.findById(comboId)
                    .orElseThrow(() -> new IllegalArgumentException("Combo not found with ID: " + comboId));
            combo.setActive(!combo.isActive());
            comboRepository.save(combo);
            logger.info("Combo status toggled to {} for ID: {}", combo.isActive(), comboId);
        } catch (Exception e) {
            logger.error("Error toggling combo status with ID {}: {}", comboId, e.getMessage(), e);
            throw e;
        }
    }
}