
// API data và functions
let combosData = [];
let selectedCombos = {};
// let timeLeft = 10 * 60; // 10 minutes

// Lấy thông tin đặt vé từ sessionStorage
const bookingDataFromSession = JSON.parse(sessionStorage.getItem('bookingData') || '{}');
const ticketPrice = bookingDataFromSession.ticketPrice || 0;
const movieId = bookingDataFromSession.movieId;
const showDateId = bookingDataFromSession.showDateId;
const scheduleId = bookingDataFromSession.scheduleId;
console.log('Booking Data:', bookingDataFromSession);
// Kiểm tra nếu không có dữ liệu ghế, chuyển về trang chọn ghế
if (!bookingDataFromSession.seats || bookingDataFromSession.seats.length === 0) {
    alert('Không tìm thấy thông tin ghế đã chọn. Vui lòng chọn ghế trước!');
    window.location.href = '/movie-list';
}

// Alert System Class
class AlertSystem {
    constructor() {
        this.container = document.getElementById('alertContainer');
        this.alertCount = 0;
    }

    show(message, type = 'info', duration = 5000) {
        const alertId = `alert-${++this.alertCount}`;
        const icons = {
            success: '✅',
            danger: '❌',
            warning: '⚠️',
            info: 'ℹ️'
        };

        const alertElement = document.createElement('div');
        alertElement.id = alertId;
        alertElement.className = `alert alert-${type}`;
        alertElement.innerHTML = `
            <span class="alert-icon">${icons[type] || icons.info}</span>
            <span class="alert-message">${message}</span>
            <button class="alert-close" onclick="alertSystem.close('${alertId}')">&times;</button>
        `;

        this.container.appendChild(alertElement);

        setTimeout(() => {
            alertElement.classList.add('show');
        }, 100);

        if (duration > 0) {
            setTimeout(() => {
                this.close(alertId);
            }, duration);
        }

        return alertId;
    }

    close(alertId) {
        const alertElement = document.getElementById(alertId);
        if (alertElement) {
            alertElement.classList.remove('show');
            setTimeout(() => {
                if (alertElement.parentNode) {
                    alertElement.parentNode.removeChild(alertElement);
                }
            }, 300);
        }
    }

    success(message, duration = 5000) { return this.show(message, 'success', duration); }
    danger(message, duration = 5000) { return this.show(message, 'danger', duration); }
    warning(message, duration = 5000) { return this.show(message, 'warning', duration); }
    info(message, duration = 5000) { return this.show(message, 'info', duration); }
}

// Initialize alert system
const alertSystem = new AlertSystem();

// Fetch combos from API
async function fetchCombos() {
    try {
        const response = await fetch('http://localhost:8081/api/booking/combos');
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        const data = await response.json();
        combosData = data.combos || [];
        renderCombos();
        // Khôi phục combo đã chọn sau khi render xong
        restoreCombosFromSession();
    } catch (error) {
        console.error('Error fetching combos:', error);
        document.getElementById('combo-grid').innerHTML = `
            <div class="error-message">
                <i class="bi bi-exclamation-triangle" style="font-size: 24px; margin-bottom: 10px;"></i>
                <div>Không thể tải danh sách combo. Vui lòng thử lại sau.</div>
                <button onclick="fetchCombos()" class="btn btn-outline-danger btn-sm mt-2">
                    <i class="bi bi-arrow-clockwise"></i> Thử lại
                </button>
            </div>
        `;
        alertSystem.danger('Không thể tải danh sách combo!', 5000);
    }
}

// Render combos
function renderCombos() {
    const comboGrid = document.getElementById('combo-grid');

    if (combosData.length === 0) {
        comboGrid.innerHTML = `
            <div class="error-message">
                <i class="bi bi-box" style="font-size: 24px; margin-bottom: 10px;"></i>
                <div>Hiện tại chưa có combo nào!</div>
            </div>
        `;
        return;
    }

    comboGrid.innerHTML = combosData.map(combo => `
        <div class="combo-card">
            <div class="combo-image">
                <img src="${combo.imageUrl || 'https://via.placeholder.com/300x200/333/fff?text=No+Image'}" 
                        alt="${combo.comboName}"
                        onerror="this.src='https://via.placeholder.com/300x200/333/fff?text=No+Image'">
                ${combo.comboStatus === 'HOT' ? '<div class="combo-popular">Hot</div>' : ''}
                ${combo.comboStatus === 'POPULAR' ? '<div class="combo-popular">Phổ biến</div>' : ''}
            </div>
            <div class="combo-content">
                <h3 class="combo-name">${combo.comboName}</h3>
                <p class="combo-description">${combo.description || 'Combo ngon miệng'}</p>
                <div class="combo-price">
                    <span class="price-current">${combo.formattedDiscountedPrice}đ</span>
                    ${combo.discountPercentage > 0 ?
            `<span class="price-original">${combo.formattedPrice}đ</span>` : ''
        }
                </div>
                <div class="combo-actions">
                    <div class="quantity-control">
                        <button class="quantity-btn" onclick="changeQuantity(${combo.comboId}, -1)">-</button>
                        <div class="quantity-display" id="combo-${combo.comboId}-quantity">0</div>
                        <button class="quantity-btn" onclick="changeQuantity(${combo.comboId}, 1)">+</button>
                    </div>
                    <button class="add-to-cart-btn" onclick="addToCart(${combo.comboId})">Thêm</button>
                </div>
            </div>
        </div>
    `).join('');
}

// Thêm hàm mới để khôi phục combo đã chọn từ sessionStorage
function restoreCombosFromSession() {
    if (!bookingDataFromSession || !bookingDataFromSession.combos) return;

    const savedCombos = bookingDataFromSession.combos;
    if (!savedCombos || savedCombos.length === 0) return;

    // Đợi cho combosData được tải xong
    const waitForCombos = setInterval(() => {
        if (combosData && combosData.length > 0) {
            clearInterval(waitForCombos);

            // Khôi phục các combo đã chọn
            savedCombos.forEach(savedCombo => {
                const combo = combosData.find(c => c.comboId === savedCombo.comboId);
                if (combo) {
                    // Lưu vào đối tượng selectedCombos
                    selectedCombos[combo.comboId] = {
                        quantity: savedCombo.quantity,
                        combo: combo
                    };

                    // Cập nhật số lượng hiển thị trên UI
                    const quantityDisplay = document.getElementById(`combo-${combo.comboId}-quantity`);
                    if (quantityDisplay) {
                        quantityDisplay.textContent = savedCombo.quantity;
                    }
                }
            });

            // Cập nhật panel đặt vé
            updateBookingPanel();

            // Hiển thị thông báo cho người dùng
            if (Object.keys(selectedCombos).length > 0) {
                alertSystem.success(`Đã khôi phục ${Object.keys(selectedCombos).length} combo đã chọn trước đó`, 3000);
            }
        }
    }, 100);

    // Đặt timeout để tránh vòng lặp vô hạn
    setTimeout(() => {
        clearInterval(waitForCombos);
    }, 10000);
}

// Change quantity function
function changeQuantity(comboId, delta) {
    const combo = combosData.find(c => c.comboId === comboId);
    if (!combo) return;

    const currentQty = parseInt(document.getElementById(`combo-${comboId}-quantity`).textContent);
    const newQty = Math.max(0, Math.min(5, currentQty + delta)); // Max 5 items

    document.getElementById(`combo-${comboId}-quantity`).textContent = newQty;

    // Update selected combos
    if (newQty > 0) {
        selectedCombos[comboId] = {
            quantity: newQty,
            combo: combo
        };
    } else {
        delete selectedCombos[comboId];
    }

    updateBookingPanel();

    // Show feedback
    if (delta > 0 && newQty > currentQty) {
        alertSystem.success(`Đã thêm ${combo.comboName}`, 2000);
    } else if (delta < 0 && newQty < currentQty) {
        alertSystem.info(`Đã giảm ${combo.comboName}`, 2000);
    }
}

// Add to cart function
function addToCart(comboId) {
    const combo = combosData.find(c => c.comboId === comboId);
    if (!combo) return;

    const currentQty = parseInt(document.getElementById(`combo-${comboId}-quantity`).textContent);

    if (currentQty < 5) {
        changeQuantity(comboId, 1);
        alertSystem.success(`Đã thêm ${combo.comboName} vào giỏ`, 2000);
    } else {
        alertSystem.warning('Tối đa 5 combo cùng loại!', 3000);
    }
}

// Update booking panel
function updateBookingPanel() {
    const comboList = document.getElementById('combo-items-list');
    const comboTotal = document.getElementById('combo-total');

    // Clear current combo list
    comboList.innerHTML = '';

    let totalComboPrice = 0;
    let hasCombo = false;

    // Add selected combos to the list
    for (const [comboId, data] of Object.entries(selectedCombos)) {
        if (data.quantity > 0) {
            hasCombo = true;
            const combo = data.combo;
            const itemTotal = combo.discountedPrice * data.quantity;
            totalComboPrice += itemTotal;

            const comboItem = document.createElement('div');
            comboItem.className = 'combo-item';
            comboItem.innerHTML = `
                <div class="combo-item-info">
                    <div class="combo-item-name">${combo.comboName}</div>
                    <div class="combo-item-quantity">Số lượng: ${data.quantity}</div>
                </div>
                <div class="combo-item-price">${itemTotal.toLocaleString('vi-VN')}đ</div>
                <button onclick="removeCombo(${comboId})" 
                        style="background: rgba(239, 68, 68, 0.2); border: 1px solid #ef4444; color: #ef4444; 
                               border-radius: 6px; width: 24px; height: 24px; display: flex; align-items: center; 
                               justify-content: center; cursor: pointer; margin-left: 10px; font-size: 14px;"
                        title="Xóa combo">×</button>
            `;
            comboList.appendChild(comboItem);
        }
    }

    // Show empty message if no combos selected
    if (!hasCombo) {
        comboList.innerHTML = `
            <div style="text-align: center; color: #666; font-style: italic; padding: 20px;" id="empty-combo-list">
                Chưa chọn combo nào
            </div>
        `;
    }
    const grandTotal = Number(totalComboPrice) + Number(ticketPrice);

    // Update prices
    document.getElementById('ticket-total').textContent = `${Number(ticketPrice).toLocaleString('vi-VN')}đ`;
    comboTotal.textContent = `${totalComboPrice.toLocaleString('vi-VN')}đ`;
    document.getElementById('total-price').textContent = `${grandTotal.toLocaleString('vi-VN')}đ`;
}

// Hiển thị thông tin ghế đã chọn
function displaySelectedSeats() {
    const selectedSeatsElement = document.getElementById('selected-seats-list');
    if (!selectedSeatsElement || !bookingDataFromSession.seats) return;

    // Xóa nội dung hiện tại
    selectedSeatsElement.innerHTML = '';

    // Ẩn thông báo "Chưa chọn ghế" nếu có
    const emptyMessage = document.getElementById('empty-seat-list');
    if (emptyMessage) {
        emptyMessage.style.display = 'none';
    }

    // Hiển thị từng ghế đã chọn
    bookingDataFromSession.seats.forEach(seat => {
        const seatItem = document.createElement('div');
        seatItem.className = 'seat-item';

        const seatInfo = document.createElement('div');
        seatInfo.className = 'seat-info';

        const indicator = document.createElement('div');
        indicator.className = 'seat-type-indicator';
        indicator.style.background = seat.type === 'VIP' ? '#f59e0b' : '#6b7280';

        const seatLabel = document.createElement('span');
        seatLabel.textContent = `Ghế ${seat.seatColumn}${seat.seatRow} (${seat.type})`;

        seatInfo.appendChild(indicator);
        seatInfo.appendChild(seatLabel);

        const priceSpan = document.createElement('span');
        priceSpan.textContent = parseInt(seat.price).toLocaleString('vi-VN') + 'đ';

        seatItem.appendChild(seatInfo);
        seatItem.appendChild(priceSpan);
        selectedSeatsElement.appendChild(seatItem);
    });

    // Cập nhật tổng giá vé
    document.getElementById('ticket-total').textContent = ticketPrice.toLocaleString('vi-VN') + 'đ';
    updateBookingPanel();

}

// Remove combo function
function removeCombo(comboId) {
    const combo = combosData.find(c => c.comboId === comboId);
    if (!combo) return;

    delete selectedCombos[comboId];
    document.getElementById(`combo-${comboId}-quantity`).textContent = '0';
    updateBookingPanel();
    alertSystem.info(`Đã xóa ${combo.comboName}`, 2000);
}

// Skip combo function
function skipCombo() {
    if (Object.keys(selectedCombos).length > 0) {
        if (confirm('Bạn có chắc muốn bỏ qua tất cả combo đã chọn?')) {
            // Clear all combos
            selectedCombos = {};

            // Reset all quantity displays
            combosData.forEach(combo => {
                document.getElementById(`combo-${combo.comboId}-quantity`).textContent = '0';
            });

            updateBookingPanel();
            alertSystem.info('Đã bỏ qua tất cả combo', 2000);

            // Continue to payment
            setTimeout(() => {
                continueToPayment();
            }, 1000);
        }
    } else {
        alertSystem.info('Đang chuyển đến trang thanh toán...', 2000);
        setTimeout(() => {
            continueToPayment();
        }, 1000);
    }
}

// Continue to payment function
function continueToPayment() {
    // Calculate total combo price
    const totalComboPrice = Object.values(selectedCombos).reduce((total, data) => {
        return total + (data.combo.discountedPrice * data.quantity);
    }, 0);

    // Lấy thông tin từ bookingDataFromSession và kết hợp với combo
    const finalBookingData = {
        ...bookingDataFromSession,
        combos: Object.values(selectedCombos)
            .filter(item => item.quantity > 0)
            .map(item => ({
                comboId: item.combo.comboId,
                comboName: item.combo.comboName,
                quantity: item.quantity,
                price: item.combo.discountedPrice,
                totalPrice: item.combo.discountedPrice * item.quantity
            })),
        totalComboPrice: totalComboPrice,
        totalPrice: parseInt(ticketPrice) + totalComboPrice

    };

    // Lưu dữ liệu vào sessionStorage
    sessionStorage.setItem('bookingData', JSON.stringify(finalBookingData));

    alertSystem.success('Đang chuyển đến trang thanh toán...', 1000);

    // Redirect to payment page
    setTimeout(() => {
        window.location.href = `/payment`;
    }, 1000);
}
const TOTAL_TIME = 10 * 60; // 10 phút
// ⚠️ XỬ LÝ QUAY LẠI BẰNG BACK — PHẢI XÓA VÀ RELOAD
const navEntry = performance.getEntriesByType('navigation')[0];
const isReload = navEntry ? navEntry.type === 'reload' : performance.navigation.type === 1;

if (!isReload) {
    // 👉 Nếu là truy cập mới hoặc back → reset timer
    localStorage.setItem('StartTime', Date.now());
}
let startTime = localStorage.getItem('StartTime');
if (!startTime) {
    startTime = Date.now();
    localStorage.setItem('StartTime', startTime);
} else {
    startTime = parseInt(startTime);
}
// Timer countdown
function updateTimer() {
    const now = Date.now();
    const elapsedTime = Math.floor((now - startTime) / 1000); // số giây đã trôi qua
    const timeLeft = TOTAL_TIME - elapsedTime;

    const timer = document.getElementById('timer');
    if (timeLeft <= 0) {
        alertSystem.danger('Hết thời gian giữ ghế! Vui lòng chọn lại.', 10000);
        setTimeout(() => {
            localStorage.removeItem('StartTime');
            window.location.href = '/';
        }, 2000);
        return
    }
    const minutes = Math.floor(timeLeft / 60);
    const seconds = timeLeft % 60;
    if (timer) {
        timer.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    }


    // Thông báo cảnh báo
    if (timeLeft === 60) {
        alertSystem.danger('Chỉ còn 1 phút! Vui lòng hoàn tất ngay!', 10000);
    }
}

// Initialize page
document.addEventListener('DOMContentLoaded', function () {
    // Hiển thị thông tin ghế đã chọn
    displaySelectedSeats();

    // Fetch combos from API
    fetchCombos();
    // window.addEventListener('pageshow', function (event) {
    //     // Kiểm tra nếu là navigation type "back_forward" (quay lại trang)
    //     const navType = performance.getEntriesByType("navigation")[0]?.type;

    //     if (navType === "back_forward") {
    //         // Xóa localStorage để reset đếm thời gian
    //         localStorage.removeItem('StartTime');
    //         // Reload lại trang cho sạch state
    //         location.reload();
    //     }
    // });

    // Start timer

    updateTimer();
    setInterval(updateTimer, 1000);

    // Initialize booking panel
    updateBookingPanel();

    // Add event listener to continue button
    document.getElementById('continue-btn').addEventListener('click', continueToPayment);

    // Add event listener to skip combo button
    document.getElementById('skip-btn')?.addEventListener('click', skipCombo);
});