package com.example.projectwebmovie.model;

import com.example.projectwebmovie.enums.ComboStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "MOVIETHEATER_COMBO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Combo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMBO_ID")
    private Integer comboId;

    @Column(name = "COMBO_NAME", length = 255, nullable = false)
    private String comboName;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "PRICE", nullable = false)
    private Integer price;

    @Column(name = "DISCOUNT_PERCENTAGE", nullable = false)
    private Integer discountPercentage = 0;

    @Column(name = "IS_ACTIVE", nullable = false)
    private boolean active;

    @Column(name = "IMAGE_URL", length = 1000)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "COMBO_STATUS", length = 20)
    private ComboStatus comboStatus = ComboStatus.NORMAL; // Sử dụng enum mới

    @OneToMany(mappedBy = "combo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingCombo> bookingCombos;

    @Override
    public String toString() {
        return "Combo{comboId=" + comboId + ", comboName='" + comboName + "', price=" + price +
                ", discountPercentage=" + discountPercentage + ", active=" + active +
                ", imageUrl='" + imageUrl + "', comboStatus=" + comboStatus + "}";
    }

    public Integer getDiscountedPrice() {
        return price - (price * discountPercentage / 100);
    }
}