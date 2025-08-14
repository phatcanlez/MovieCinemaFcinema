package com.example.projectwebmovie.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.projectwebmovie.dto.promotion.RequestPromotionDTO;
import com.example.projectwebmovie.dto.promotion.PromotionDTO;
import com.example.projectwebmovie.mapper.PromotionMapper;
import com.example.projectwebmovie.model.Booking;
import com.example.projectwebmovie.model.Promotion;
import com.example.projectwebmovie.repository.BookingRepository;
import com.example.projectwebmovie.repository.PromotionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class PromotionService {

    private static final Logger logger = LoggerFactory.getLogger(PromotionService.class);

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private IdGenerateService idGenerateService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AccountService accountService;

    public List<PromotionDTO> findActivePromotions() {
        List<Promotion> promotions = promotionRepository
                .findCurrentActivePromotions();
        if (promotions == null || promotions.isEmpty()) {
            return List.of();
        }
        return promotions.stream()
                .map(PromotionMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Page<PromotionDTO> findAllPromotions(Pageable pageable) {
        Page<Promotion> promotionPage = promotionRepository.findAll(pageable);
        return promotionPage.map(PromotionMapper::toDTO);
    }

    public PromotionDTO findPromotionById(String promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId).orElse(null);
        return PromotionMapper.toDTO(promotion);
    }

    @Transactional
    public void addPromotion(RequestPromotionDTO createPromotionDTO, MultipartFile image) {
        Promotion promotion = PromotionMapper.toPromotion(createPromotionDTO);

        if (promotion == null) {
            throw new IllegalArgumentException("Promotion data is invalid");
        }

        Promotion existingPromotionCode = promotionRepository
                .findByPromotionCode(promotion.getPromotionCode());
        if (existingPromotionCode != null) {
            throw new IllegalArgumentException("Promotion code is existing");
        }

        if (promotion.getDiscountLevel() != null) {
            if (promotion.getDiscountAmount() != null) {
                throw new IllegalArgumentException("Only one type of promotion can be selected: % or fixed amount");
            } else {
                if (promotion.getDiscountLevel() <= 0 || promotion.getDiscountLevel() > 20) {
                    throw new IllegalArgumentException("Discount level must be between 1 and 20");
                } else {
                    promotion.setDiscountLevel(promotion.getDiscountLevel());
                    promotion.setMaxAmountForPercentDiscount(promotion.getMaxAmountForPercentDiscount());
                }
            }
        }

        if (promotion.getDiscountAmount() != null) {
            if (promotion.getDiscountLevel() != null || promotion.getMaxAmountForPercentDiscount() != null) {
                throw new IllegalArgumentException("Only one type of promotion can be selected: % or fixed amount");
            } else {
                promotion.setDiscountAmount(promotion.getDiscountAmount());
            }
        }

        if (promotion.getStartTime().isAfter(promotion.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        // Chờ ảnh upload hoàn tất
        String imageUrl = cloudinaryService.uploadImage(image);

        // Kiểm tra nếu imageUrl là null hoặc rỗng
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new IllegalArgumentException("Image upload failed or image is empty");
        }

        promotion.setImage(imageUrl);

        try {
            // Generate ID cho promotion
            String newPromotionId = idGenerateService.generateStringId("PROMO", Promotion.class, "promotionId");
            promotion.setPromotionId(newPromotionId);
            logger.info("Generated new promotion ID: {}", newPromotionId);
        } catch (Exception e) {
            throw new RuntimeException("Error when generate Id: " + e.getMessage(), e);
        }

        promotion.setIsActive(true);
        promotionRepository.save(promotion);
    }

    @Transactional
    public void updatePromotion(String promotionId, RequestPromotionDTO updatePromotionDTO, MultipartFile image) {
        Promotion existingPromotion = promotionRepository
                .findById(promotionId)
                .orElseThrow(() -> new IllegalArgumentException("Can not find promotion have ID: " + promotionId));

        Promotion updatedPromotion = PromotionMapper.toPromotion(updatePromotionDTO);

        logger.info("Updating promotion with ID: {}, updatedPromotion: {}", promotionId, updatedPromotion);
        if (updatedPromotion == null) {
            throw new IllegalArgumentException("Data invalid");
        }

        Promotion existingPromotionCode = promotionRepository
                .findByPromotionCode(updatedPromotion.getPromotionCode());

        if (existingPromotionCode != null && !existingPromotionCode.getPromotionId().equals(promotionId)) {
            throw new IllegalArgumentException("Promotion code is existing");
        }

        if (updatedPromotion.getStartTime() == null || updatedPromotion.getEndTime() == null) {
            throw new IllegalArgumentException("Start time and end time cannot be null");
        }

        if (updatedPromotion.getStartTime().isAfter(updatedPromotion.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        if (updatedPromotion.getDiscountLevel() != null) {
            if (updatedPromotion.getDiscountAmount() != null) {
                throw new IllegalArgumentException("Only one type of promotion can be selected: % or fixed amount");
            } else {
                if (updatedPromotion.getDiscountLevel() <= 0 || updatedPromotion.getDiscountLevel() > 20) {
                    throw new IllegalArgumentException("Discount level must be between 1 and 20");
                } else {
                    existingPromotion.setDiscountLevel(updatedPromotion.getDiscountLevel());
                    existingPromotion.setMaxAmountForPercentDiscount(updatedPromotion.getMaxAmountForPercentDiscount());
                    existingPromotion.setDiscountAmount(null);
                }

            }
        }

        if (updatedPromotion.getDiscountAmount() != null) {
            if (updatedPromotion.getDiscountLevel() != null
                    || updatedPromotion.getMaxAmountForPercentDiscount() != null) {
                throw new IllegalArgumentException("Only one type of promotion can be selected: % or fixed amount");
            } else {
                existingPromotion.setDiscountAmount(updatedPromotion.getDiscountAmount());
                existingPromotion.setDiscountLevel(null);
                existingPromotion.setMaxAmountForPercentDiscount(null);
            }
        }

        if (image != null && !image.isEmpty()) {
            updatedPromotion.setImage(cloudinaryService.uploadImage(image));
        } else {
            updatedPromotion.setImage(existingPromotion.getImage());
        }

        if (updatePromotionDTO.getIsActive() == null) {
            updatedPromotion.setIsActive(existingPromotion.getIsActive());
        }

        updatedPromotion.setPromotionId(existingPromotion.getPromotionId());

        promotionRepository.save(updatedPromotion);
    }

    public PromotionDTO getPromotionById(String promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy promotion với ID: " + promotionId));
        return PromotionMapper.toDTO(promotion);
    }

    @Transactional
    public void togglePromotionStatus(String promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khuyến mãi với ID: " + promotionId));
        logger.info("Toggling status for promotion with ID: {}, Status before toggle: {}", promotionId,
                promotion.getIsActive());
        // Toggle trạng thái
        promotion.setIsActive(!promotion.getIsActive());

        logger.info(" Status after toggle: {}", promotion.getIsActive());
        promotionRepository.save(promotion);
    }

    @Transactional
    public Map<String, Object> applyPromotionByCode(String promotionCode, Double originalAmount, String accountId) {
        if (promotionCode == null || originalAmount == null) {
            throw new IllegalArgumentException("Promotion code hoặc số tiền không được để trống");
        }

        if (originalAmount <= 0) {
            throw new IllegalArgumentException("Không thể áp dụng khuyến mãi");
        }

        // Tìm promotion dựa trên mã code
        Promotion promotion = promotionRepository.findByPromotionCode(promotionCode);
        if (promotion == null) {
            throw new IllegalArgumentException("Mã khuyến mãi không hợp lệ");
        }

        // Kiểm tra người dùng đã từng sử dụng mã khuyến mãi này chưa
        if (accountId == null || accountId.isEmpty()) {
            // Nếu không có accountId, lấy từ dịch vụ account
            if (accountService.getCurrentAccount().getRole().getRoleName().equals("USER")) {
                accountId = accountService.getCurrentAccount().getAccountId();
                logger.info("Lấy accountId từ dịch vụ account: {}", accountId);
            } else {
                throw new IllegalArgumentException("Bạn cần cung cấp tài khoản để áp dụng khuyến mãi");
            }
        }
        Booking latestBooking = bookingRepository.findLatestBookingWithPromotion(accountId, promotion.getPromotionId());
        if (latestBooking != null) {
            throw new IllegalArgumentException("Bạn đã sử dụng mã khuyến mãi này rồi. Không thể áp dụng lại");
        }

        // Kiểm tra promotion có hiệu lực không
        LocalDateTime now = LocalDateTime.now();
        if (!promotion.getIsActive() || now.isBefore(promotion.getStartTime()) || now.isAfter(promotion.getEndTime())) {
            throw new IllegalArgumentException("Mã khuyến mãi đã hết hạn hoặc chưa có hiệu lực");
        }

        if (promotion.getUsageLimit() != null && promotion.getUsageLimit() <= 0) {
            throw new IllegalArgumentException("Mã khuyến mãi đã hết lượt sử dụng");
        }

        // Tính giá sau khi áp dụng khuyến mãi
        double discountedAmount = originalAmount;
        double discountAmount = 0;

        if (promotion.getDiscountAmount() != null) {
            // Giảm giá trực tiếp theo số tiền
            discountAmount = promotion.getDiscountAmount();
        } else if (promotion.getDiscountLevel() != null) {
            // Giảm giá theo phần trăm
            discountAmount = originalAmount * (promotion.getDiscountLevel() / 100.0);

            // Áp dụng giới hạn tối đa cho giảm giá phần trăm nếu có
            if (promotion.getMaxAmountForPercentDiscount() != null) {
                discountAmount = Math.min(discountAmount, promotion.getMaxAmountForPercentDiscount());
            }
        }

        // Đảm bảo số tiền giảm không lớn hơn số tiền gốc
        discountAmount = Math.min(discountAmount, originalAmount);

        // Tính giá cuối cùng
        discountedAmount = Math.max(0, originalAmount - discountAmount);

        // Tạo kết quả trả về
        Map<String, Object> result = new HashMap<>();
        result.put("promotionId", promotion.getPromotionId());
        result.put("promotionCode", promotion.getPromotionCode());
        result.put("title", promotion.getTitle());
        result.put("originalAmount", originalAmount);
        result.put("discountAmount", discountAmount);
        result.put("finalAmount", discountedAmount);

        return result;
    }
}
