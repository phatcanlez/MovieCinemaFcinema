
        // Hide success animation after 3 seconds
        setTimeout(() => {
            document.getElementById('successAnimation').style.display = 'none';
        }, 3000);

        // Load combo data from session storage and update display
        document.addEventListener('DOMContentLoaded', function () {
            // Get booking data from session storage
            const bookingData = JSON.parse(sessionStorage.getItem('bookingData') || '{}');
            const selectedCombos = bookingData.combos || {};

            let totalCombo = 0;
            let hasCombo = false;

            // Calculate total combo price
            for (const [comboType, quantity] of Object.entries(selectedCombos)) {
                if (quantity > 0) {
                    hasCombo = true;
                    // Combo prices (matching combo_selection.html)
                    const comboPrices = {
                        solo: 85000,
                        couple: 149000,
                        family: 199000
                    };

                    if (comboPrices[comboType]) {
                        totalCombo += comboPrices[comboType] * quantity;
                    }
                }
            }

            // Get the combo price item elements
            const comboStaticItem = document.querySelector('.price-item:has([text="Combo"])');
            const comboDynamicItem = document.getElementById('combo-price-item');

            if (hasCombo) {
                // Hide the static "Combo: 0đ" item
                if (comboStaticItem) {
                    comboStaticItem.style.display = 'none';
                }

                // Show and update the dynamic combo item
                if (comboDynamicItem) {
                    comboDynamicItem.style.display = 'flex';
                    document.getElementById('combo-total').textContent = totalCombo.toLocaleString('vi-VN') + 'đ';
                }

                // Update total price
                const seatTotal = 85000 + 120000; // C3 (normal) + C4 (VIP)
                const finalTotal = seatTotal + totalCombo;
                document.getElementById('finalTotal').textContent = finalTotal.toLocaleString('vi-VN') + 'đ';
            } else {
                // Hide both combo items when no combo is selected
                if (comboStaticItem) {
                    comboStaticItem.style.display = 'none';
                }
                if (comboDynamicItem) {
                    comboDynamicItem.style.display = 'none';
                }

                // Keep total as seat price only
                const seatTotal = 85000 + 120000;
                document.getElementById('finalTotal').textContent = seatTotal.toLocaleString('vi-VN') + 'đ';
            }
        });

        // Show success message
        setTimeout(() => {
            console.log('Vé đã được đặt thành công!');
        }, 1000);

        // Clear session storage after successful booking
        setTimeout(() => {
            sessionStorage.removeItem('selectedCombos');
        }, 5000);