// Lấy dữ liệu từ sessionStorage
const bookingDataFromSession = JSON.parse(sessionStorage.getItem('bookingData') || '{}');
console.log("SessionStorage:", bookingDataFromSession);

// Trích xuất dữ liệu cần thiết từ bookingData
const movieId = bookingDataFromSession.movieId || '';
const showDateId = bookingDataFromSession.showDateId || '';
const scheduleId = bookingDataFromSession.scheduleId || '';

let priceState = {
    originalPrice: 0,       // Giá gốc khi chưa có khuyến mãi nào
    currentPrice: 0,        // Giá hiện tại sau khi áp dụng các khuyến mãi
    voucherDiscount: 0,     // Số tiền giảm giá từ voucher
    pointsDiscount: 0       // Số tiền giảm giá từ điểm
};

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

// Khởi tạo hệ thống thông báo
const alertSystem = new AlertSystem();

// Biến đếm thời gian thanh toán
const TOTAL_PAYMENT_TIME = 10 * 60; // 10 phút
const navEntry = performance.getEntriesByType('navigation')[0];
const isReload = navEntry ? navEntry.type === 'reload' : performance.navigation.type === 1;

if (!isReload) {
    // 👉 Nếu là truy cập mới hoặc back → reset timer
    localStorage.setItem('paymentStartTime', Date.now());
}

let startTime = localStorage.getItem('paymentStartTime');
if (!startTime) {
    startTime = Date.now();
    localStorage.setItem('paymentStartTime', startTime);
} else {
    startTime = parseInt(startTime);
}
// Hàm cập nhật bộ đếm thời gian
function updatePaymentTimer() {
    const now = Date.now();
    const elapsedTime = Math.floor((now - startTime) / 1000); // số giây đã trôi qua
    const timeLeft = TOTAL_PAYMENT_TIME - elapsedTime;

    const paymentTimer = document.getElementById('paymentTimer');
    if (timeLeft <= 0) {
        if (paymentTimer) {
            paymentTimer.textContent = "00:00";
        }
        alertSystem.danger('Hết thời gian thanh toán! Vui lòng đặt vé lại.', 10000);
        setTimeout(() => {
            localStorage.removeItem('paymentStartTime');
            window.location.href = '/';
        }, 2000);
        return
    }
    const minutes = Math.floor(timeLeft / 60);
    const seconds = timeLeft % 60;
    if (paymentTimer) {
        paymentTimer.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    }


    // Thông báo cảnh báo
    if (timeLeft === 300) {
        alertSystem.warning('Còn 5 phút để hoàn tất thanh toán!', 5000);
    }
    if (timeLeft === 60) {
        alertSystem.danger('Chỉ còn 1 phút! Vui lòng hoàn tất ngay!', 10000);
    }
}

// Hàm lấy thông tin phim từ API
function fetchMovieDetails() {
    fetch(`/api/booking/details?movieId=${movieId}&showDateId=${showDateId}&scheduleId=${scheduleId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            console.log('Movie details:', data);
            updateMovieInfo(data);
        })
        .catch(error => {
            console.error('Error fetching movie details:', error);
            alertSystem.danger('Không thể tải thông tin phim. Vui lòng thử lại sau.', 5000);
        });
}

// Hàm cập nhật thông tin phim
function updateMovieInfo(data) {
    // Cập nhật tiêu đề phim
    const movieTitle = document.getElementById('movie-title');
    if (movieTitle && data.movieName) {
        movieTitle.textContent = data.movieName;
        // Cập nhật tiêu đề trang
        document.title = `Thanh toán: ${data.movieName} - FCine`;
    }

    // Cập nhật poster phim
    const moviePoster = document.getElementById('movie-poster-img');
    if (moviePoster && data.posterUrl) {
        moviePoster.src = data.posterUrl;
        moviePoster.alt = data.movieName || 'Movie Poster';
    }

    // Cập nhật thông tin meta với icon
    const movieShowtime = document.querySelector('#movie-showtime span');
    const movieRoom = document.querySelector('#movie-room span');
    const movieDuration = document.querySelector('#movie-duration span');

    if (movieShowtime && data.showDate && data.startTime) {
        movieShowtime.textContent = `${formatDate(data.showDate)} - ${data.startTime}`;
    }

    if (movieRoom && data.roomName) {
        movieRoom.textContent = `${data.roomName}`;
    }

    if (movieDuration && data.duration) {
        movieDuration.textContent = `${data.duration} phút - ${data.movieFormat || '2D'}`;
    }

    // Cập nhật lại booking details với dữ liệu mới
    updateBookingInfoFromAPI(data);
}

// Hàm cập nhật điểm người dùng
function fetchUserScore() {
    // Giả sử có một API để lấy điểm người dùng
    fetch('/api/booking/points')
        .then(response => {
            if (!response.ok) {
                throw new Error('Không thể lấy điểm người dùng');
            }
            return response.json();
        })
        .then(data => {
            console.log('User score:', data);
            
            // Lưu tổng điểm từ server vào sessionStorage
            bookingDataFromSession.userScore = data.point || 0;
            sessionStorage.setItem('bookingData', JSON.stringify(bookingDataFromSession));

            // Hiển thị điểm người dùng CHỈ KHI chưa sử dụng điểm nào
            if (!bookingDataFromSession.usedPoints || bookingDataFromSession.usedPoints <= 0) {
                updateUserPointsDisplay(data.point || 0);
            }

            // Tính và hiển thị điểm tối đa có thể sử dụng
            updateMaxUsablePoints(data.point || 0);
        })
        .catch(error => {
            console.error('Error fetching user score:', error);
            alertSystem.danger(`Lỗi lấy điểm người dùng: ${error.message}`, 5000);
        });
}


// Hàm cập nhật booking details từ API
function updateBookingInfoFromAPI(data) {
    // Cập nhật ngày & giờ chiếu
    const showtimeInfo = document.getElementById('showtime-info');
    if (showtimeInfo && data.showDate && data.startTime) {
        showtimeInfo.textContent = `${formatDate(data.showDate)} - ${data.startTime}`;
    }

    // Cập nhật phòng chiếu
    const roomInfo = document.getElementById('room-info');
    if (roomInfo && data.roomName) {
        roomInfo.textContent = `${data.roomName} - ${data.movieFormat || '2D'}`;
    }
}

// Hàm hiển thị chi tiết vé đã đặt
function displayBookingDetails() {
    // Cập nhật thông tin chi tiết đặt vé
    updateBookingInfo();

    // Hiển thị thông tin ghế
    if (bookingDataFromSession.seats && bookingDataFromSession.seats.length > 0) {
        const seatsDisplay = document.getElementById('selected-seats');
        if (seatsDisplay) {
            seatsDisplay.innerHTML = '';

            bookingDataFromSession.seats.forEach(seat => {
                const seatBadge = document.createElement('div');
                seatBadge.className = seat.type === 'VIP' ? 'seat-badge vip' : 'seat-badge';
                seatBadge.textContent = seat.seatName;
                seatsDisplay.appendChild(seatBadge);
            });
        }
    }

    // Hiển thị thông tin combo
    if (bookingDataFromSession.combos && bookingDataFromSession.combos.length > 0) {
        const comboSection = document.getElementById('combo-info-section');
        const comboDisplay = document.getElementById('selected-combos');

        if (comboSection && comboDisplay) {
            comboSection.style.display = '';
            comboDisplay.innerHTML = '';

            bookingDataFromSession.combos.forEach(combo => {
                const comboBadge = document.createElement('div');
                comboBadge.className = 'combo-badge';
                comboBadge.textContent = `${combo.comboName} x${combo.quantity}`;
                comboDisplay.appendChild(comboBadge);
            });
        }
    }

    updatePriceSummary();
}

// Hàm cập nhật thông tin booking details
function updateBookingInfo() {
    // Cập nhật ngày & giờ chiếu
    const showtimeInfo = document.getElementById('showtime-info');
    if (showtimeInfo && bookingDataFromSession.showDate && bookingDataFromSession.startTime) {
        showtimeInfo.textContent = `${formatDate(bookingDataFromSession.showDate)} - ${bookingDataFromSession.startTime}`;
    }

    // Cập nhật phòng chiếu
    const roomInfo = document.getElementById('room-info');
    if (roomInfo && bookingDataFromSession.roomName) {
        roomInfo.textContent = `${bookingDataFromSession.roomName} - ${bookingDataFromSession.movieFormat || '2D'}`;
    }

    // Cập nhật số lượng ghế
    const seatCount = document.getElementById('seat-count');
    if (seatCount && bookingDataFromSession.seats) {
        seatCount.textContent = `${bookingDataFromSession.seats.length} ghế`;
    }

    // Fallback: Nếu không có thông tin từ API, sử dụng dữ liệu từ session
    if (bookingDataFromSession.movieName) {
        const movieTitle = document.getElementById('movie-title');
        if (movieTitle && !movieTitle.textContent) {
            movieTitle.textContent = bookingDataFromSession.movieName;
        }
    }

    if (bookingDataFromSession.posterUrl) {
        const moviePoster = document.getElementById('movie-poster-img');
        if (moviePoster && !moviePoster.src) {
            moviePoster.src = bookingDataFromSession.posterUrl;
            moviePoster.alt = bookingDataFromSession.movieName || 'Movie Poster';
        }
    }
}

// Hàm cập nhật tổng giá
function updatePriceSummary() {
    // Cập nhật giá vé
    const seatPrice = document.getElementById('seat-price');
    if (seatPrice) {
        const totalSeatPrice = bookingDataFromSession.ticketPrice || 0;
        seatPrice.textContent = `${totalSeatPrice.toLocaleString('vi-VN')}đ`;
    }

    // Cập nhật giá combo
    const comboPrice = document.getElementById('combo-price');
    if (comboPrice) {
        const totalComboPrice = bookingDataFromSession.totalComboPrice || 0;
        comboPrice.textContent = `${totalComboPrice.toLocaleString('vi-VN')}đ`;
        // Ẩn dòng combo nếu không có combo
        if (totalComboPrice === 0 && (!bookingDataFromSession.combos || bookingDataFromSession.combos.length === 0)) {
            comboPrice.closest('.price-item').style.display = 'none';
        }
    }

    // Cập nhật tổng tiền
    const totalAmount = document.getElementById('totalAmount');
    if (totalAmount) {
        const grandTotal = priceState.currentPrice || bookingDataFromSession.totalPrice || 0;
        totalAmount.textContent = `${grandTotal.toLocaleString('vi-VN')}đ`;
    }
}

// Định dạng ngày tháng
function formatDate(dateString) {
    if (!dateString) return '';

    const date = new Date(dateString);
    const dayOfWeek = ['Chủ nhật', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7'];
    const day = date.getDate();
    const month = date.getMonth() + 1;
    const year = date.getFullYear();

    return `${dayOfWeek[date.getDay()]}, ${day}/${month}/${year}`;
}

// Xử lý thanh toán
function processPayment() {
    // Kiểm tra phương thức thanh toán được chọn
    const selectedMethod = document.querySelector('.payment-option.active');
    const paymentMethod = selectedMethod.getAttribute('data-method');

    const loadingOverlay = document.getElementById('loadingOverlay');
    const loadingText = document.getElementById('loadingText');
    const loadingSubtext = document.getElementById('loadingSubtext');

    // Hiển thị trạng thái đang tải
    if (loadingOverlay) loadingOverlay.style.display = 'flex';

    if (paymentMethod === 'MOMO') {
        if (loadingText) loadingText.textContent = 'Đang kết nối với MoMo...';
        if (loadingSubtext) loadingSubtext.textContent = 'Vui lòng đợi trong giây lát...';
    } else if (paymentMethod === 'CASH') {
        if (loadingText) loadingText.textContent = 'Đang xác nhận đặt vé...';
        if (loadingSubtext) loadingSubtext.textContent = 'Vui lòng thanh toán tại quầy vé';
    }

    // Chuyển đổi định dạng seats để phù hợp với API
    const seatsIds = bookingDataFromSession.seats.map(seat => {
        // Kiểm tra xem seat.seatId có tồn tại không
        if (!seat.seatId && seat.seatId !== 0) {
            // Nếu không có seatId, thử lấy từ định dạng khác (có thể là từ cấu trúc cũ)
            return null;
        }

        // Nếu seat.seatId là chuỗi chứa số, chuyển về số
        if (typeof seat.seatId === 'string' && !isNaN(seat.seatId)) {
            return parseInt(seat.seatId);
        }
        // Nếu seat.seatId đã là số, giữ nguyên
        else if (typeof seat.seatId === 'number') {
            return seat.seatId;
        }
        // Trường hợp khác, trả về null
        return null;
    }).filter(id => id !== null); // Lọc bỏ các giá trị null

    // Chuyển đổi định dạng combos
    const combos = [];
    if (bookingDataFromSession.combos && Array.isArray(bookingDataFromSession.combos)) {
        bookingDataFromSession.combos.forEach(combo => {
            if (combo && combo.quantity > 0 && combo.comboId) {
                combos.push({
                    comboId: typeof combo.comboId === 'string' && !isNaN(combo.comboId)
                        ? parseInt(combo.comboId)
                        : combo.comboId,
                    quantity: combo.quantity
                });
            }
        });
    }

    // Lấy mã khuyến mãi (nếu có)
    const promoCodeInput = document.getElementById('promoCode');
    let promotionId = null;

    if (bookingDataFromSession.promotionId) {
        promotionId = bookingDataFromSession.promotionId;
    }// Nếu không có trong session, kiểm tra nếu input đã disabled (đã áp dụng mã)
    else if (promoCodeInput && promoCodeInput.disabled && promoCodeInput.value) {
        // Trong trường hợp này, chúng ta chỉ có mã code, không có ID
        // Cần phải lấy ID bằng cách gọi API validate
        // Đây là một cách khác, có thể bạn muốn bỏ qua phần này nếu đã xử lý ở trên
        promotionId = null;
    }

    // Lấy điểm đã sử dụng (nếu tính năng này được hỗ trợ)
    const usedPoints = bookingDataFromSession.usedPoints || 0;

    // Tổng giá cuối cùng
    let totalPrice = 0;
    try {
        // Thử lấy từ phần tử DOM
        const totalPriceText = document.getElementById('totalAmount')?.textContent || '';
        totalPrice = parseInt(totalPriceText.replace(/[^\d]/g, '') || '0');
    } catch (error) {
        console.error('Error parsing total price from DOM:', error);
    }

    // Nếu không lấy được từ DOM, thử lấy từ bookingData
    if (!totalPrice) {
        totalPrice = parseInt(bookingDataFromSession.totalPrice || 0);
    }

    // Đảm bảo movieId, showDateId, scheduleId là số nếu có thể
    const movieIdValue = typeof movieId === 'string' && !isNaN(movieId) ? parseInt(movieId) : movieId;
    const showDateIdValue = typeof showDateId === 'string' && !isNaN(showDateId) ? parseInt(showDateId) : showDateId;
    const scheduleIdValue = typeof scheduleId === 'string' && !isNaN(scheduleId) ? parseInt(scheduleId) : scheduleId;
    const roomInfo = document.getElementById('room-info');
    console.log('🚀 ~ processPayment ~ roomInfo:', roomInfo)

    // Chuẩn bị dữ liệu gửi lên API
    const requestData = {
        movieId: movieIdValue,
        showDateId: showDateIdValue,
        scheduleId: scheduleIdValue,
        seatsIds: seatsIds,
        usedPoints: usedPoints,
        combos: combos,
        paymentMethod: paymentMethod,
        totalPrice: totalPrice,
        promotionId: promotionId
    };

    console.log("Sending booking data:", requestData);

    // Kiểm tra dữ liệu JSON trước khi gửi đi
    try {
        // Kiểm tra xem dữ liệu có thể serialize thành JSON hợp lệ không
        const testJson = JSON.stringify(requestData);
        console.log("Validated JSON data:", testJson);
    } catch (error) {
        console.error("Invalid JSON data:", error);
        alertSystem.danger(`Lỗi dữ liệu: ${error.message}`, 5000);
        if (loadingOverlay) loadingOverlay.style.display = 'none';
        return; // Dừng hàm nếu dữ liệu không hợp lệ
    }

    // Gửi request đặt vé đến server
    fetch('/api/booking/create', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestData)
    })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(`Server responded with status ${response.status}: ${text}`);
                });
            }
            return response.json();
        })
        .then(data => {
            console.log("API response:", data);

            // API trả về {payUrl: "..."} khi là thanh toán MoMo
            if (data.payUrl) {
                // Hiển thị thông báo thành công
                alertSystem.success('Đặt vé thành công! Đang chuyển hướng đến trang thanh toán...', 2000);

                // Chuyển hướng đến URL thanh toán sau 2 giây
                setTimeout(() => {
                    window.location.href = data.payUrl;
                }, 2000);
            } else {
                // Hiển thị thông báo thành công
                alertSystem.success('Đặt vé thành công! Bạn sẽ được chuyển đến trang xác nhận.', 2000);

                // Lưu mã đặt vé từ response nếu có
                const bookingId = data.bookingId || data.id || '';
                console.log("Booking ID:", bookingId);
                // Xóa dữ liệu booking trong sessionStorage
                setTimeout(() => {
                    sessionStorage.removeItem('bookingData');
                    localStorage.removeItem('paymentStartTime');
                    localStorage.removeItem('StartTime');

                    window.location.href = `/booking/confirmation/${bookingId}`;
                }, 2000);
            }
        })
        .catch(error => {
            console.error('Payment error:', error);
            alertSystem.danger(`Lỗi thanh toán: ${error.message}`, 5000);
        })
        .finally(() => {
            // Luôn ẩn overlay loading khi hoàn tất
            if (loadingOverlay) loadingOverlay.style.display = 'none';
        });
}

// Hiệu ứng đổi màu khi click nút MoMo
function initMomoButtonEffect() {
    const momoBtn = document.getElementById('confirmPaymentBtn');
    if (momoBtn) {
        momoBtn.addEventListener('mousedown', function () {
            momoBtn.classList.add('active');
        });

        momoBtn.addEventListener('mouseup', function () {
            momoBtn.classList.remove('active');
        });

        momoBtn.addEventListener('mouseleave', function () {
            momoBtn.classList.remove('active');
        });
    }
}

// Xử lý khuyến mãi
function initPromoCode() {
    // Thêm khai báo biến ở đây
    const promoCodeInput = document.getElementById('promoCode');
    const applyPromoBtn = document.getElementById('applyPromoBtn');
    
    if (applyPromoBtn) {
        applyPromoBtn.addEventListener('click', function() {
            const promoCode = document.getElementById('promoCode').value;
            
            if (!promoCode) {
                alertSystem.warning('Vui lòng nhập mã khuyến mãi!', 3000);
                return;
            }
            
            // Lấy tổng giá hiện tại
            let currentTotal = priceState.originalPrice || (bookingDataFromSession.ticketPrice + bookingDataFromSession.totalComboPrice) || 0;           
            
            // Hiển thị loading
            applyPromoBtn.disabled = true;
            applyPromoBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Đang kiểm tra...';
            
            // Gọi API để áp dụng mã khuyến mãi
            fetch(`/api/booking/apply-promotion`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    promotionCode: promoCode,
                    originalAmount: currentTotal
                })
            })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(data => {
                        throw new Error(data.error || 'Không thể áp dụng mã khuyến mãi');
                    });
                }
                return response.json();
            })
            .then(data => {
                console.log('Promotion applied:', data);
                
                // Lưu thông tin giảm giá từ voucher
                priceState.voucherDiscount = data.discountAmount;

                // Tính toán lại giá cuối cùng = giá gốc - giảm giá voucher - giảm giá điểm 
                priceState.currentPrice = priceState.originalPrice - priceState.voucherDiscount - priceState.pointsDiscount;

                // Cập nhật giá hiển thị
                document.getElementById('totalAmount').textContent = `${priceState.currentPrice.toLocaleString('vi-VN')}đ`;

                // Thêm dòng giảm giá
                const priceBreakdown = document.querySelector('.price-breakdown');
                
                // Kiểm tra xem đã có dòng giảm giá chưa
                let discountItem = document.querySelector('.discount-item');
                
                if (!discountItem) {
                    discountItem = document.createElement('div');
                    discountItem.className = 'price-item discount-item';
                    
                    // FIX: Cách an toàn để chèn phần tử vào đúng vị trí
                    const totalSection = document.querySelector('.total-section') || document.querySelector('.total-amount');
                    
                    // Chỉ chèn trước totalSection nếu nó là con của priceBreakdown
                    if (totalSection && priceBreakdown && priceBreakdown.contains(totalSection)) {
                        priceBreakdown.insertBefore(discountItem, totalSection);
                    } else {
                        // Nếu không tìm thấy totalSection hoặc nó không phải con của priceBreakdown,
                        // thêm vào cuối của priceBreakdown
                        priceBreakdown.appendChild(discountItem);
                    }
                }
                
                discountItem.innerHTML = `
                    <span style="color: #22c55e;">
                        <i class="bi bi-tag-fill"></i> 
                        Giảm giá (${data.title || promoCode}: <strong>${promoCode}</strong>)
                    </span>
                    <span style="color: #22c55e;">-${data.discountAmount.toLocaleString('vi-VN')}đ</span>
                `;
                // Thêm style cho phần tử hiển thị
                discountItem.style.cssText = `
                    background: rgba(34, 197, 94, 0.1); 
                    border-radius: 8px; 
                    padding: 8px 12px; 
                    margin: 10px;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    animation: fadeIn 0.3s;
                `;

                // Lưu promotionId và promotionCode vào bookingDataFromSession
                bookingDataFromSession.promotionId = data.promotionId;
                bookingDataFromSession.promotionCode = promoCode;
                bookingDataFromSession.voucherDiscount = data.discountAmount;
                sessionStorage.setItem('bookingData', JSON.stringify(bookingDataFromSession));
                
                console.log('Updated booking data with promotion:', bookingDataFromSession);
                // Lưu thông tin giá gốc để có thể khôi phục khi hủy mã
                bookingDataFromSession.originalPrice = priceState.originalPrice;
                
                // Cập nhật tổng tiền
                bookingDataFromSession.totalPrice = priceState.currentPrice;

                alertSystem.success(`Áp dụng mã giảm giá thành công! Giảm ${data.discountAmount.toLocaleString('vi-VN')}đ`, 3000);
                
                // Vô hiệu hóa input và nút áp dụng
                promoCodeInput.disabled = true;
                
                // THAY ĐỔI: Đổi style nút áp dụng và vô hiệu hóa
                applyPromoBtn.disabled = true;
                applyPromoBtn.textContent = 'Đã áp dụng';
                applyPromoBtn.classList.remove('btn-warning');
                applyPromoBtn.classList.add('btn-success');
                
                // Xóa nút hủy cũ nếu có
                const oldCancelBtn = document.getElementById('cancelPromoBtn');
                if (oldCancelBtn) {
                    oldCancelBtn.remove();
                }
                
                // Thêm nút hủy mã giảm giá
                const cancelBtn = document.createElement('button');
                cancelBtn.className = 'btn btn-outline-danger ms-2';
                cancelBtn.id = 'cancelPromoBtn';
                cancelBtn.innerHTML = '<i class="bi bi-x-lg"></i>';
                cancelBtn.title = 'Hủy mã giảm giá';
                applyPromoBtn.parentNode.appendChild(cancelBtn);
                
                // Xử lý sự kiện hủy mã giảm giá
                cancelBtn.addEventListener('click', function() {
                    // Xóa thông tin khuyến mãi
                    delete bookingDataFromSession.promotionId;
                    delete bookingDataFromSession.promotionCode;
                    sessionStorage.setItem('bookingData', JSON.stringify(bookingDataFromSession));
                    
                    // Khôi phục giá ban đầu
                    priceState.voucherDiscount = 0;
                    priceState.currentPrice = priceState.originalPrice - priceState.voucherDiscount - priceState.pointsDiscount;
                    document.getElementById('totalAmount').textContent = `${priceState.currentPrice.toLocaleString('vi-VN')}đ`;
                    bookingDataFromSession.totalPrice = priceState.currentPrice;

                    console.log('Updated TotalPrice data after cancelling promotion:', bookingDataFromSession);
                    // Xóa dòng giảm giá
                    if (discountItem) {
                        discountItem.remove();
                    }
                    
                    // Khôi phục trạng thái ban đầu
                    promoCodeInput.disabled = false;
                    promoCodeInput.value = '';
                    
                    // THAY ĐỔI: Khôi phục lại nút áp dụng
                    applyPromoBtn.disabled = false;
                    applyPromoBtn.textContent = 'Áp dụng';
                    applyPromoBtn.classList.remove('btn-success');
                    applyPromoBtn.classList.add('btn-warning');
                    
                    // Xóa nút hủy
                    cancelBtn.remove();
                    
                    alertSystem.info('Đã hủy mã giảm giá', 2000);
                });
            })
            .catch(error => {
                console.error('Error applying promotion:', error);
                alertSystem.danger(`Lỗi: ${error.message}`, 3000);
            })
            .finally(() => {
                // Khôi phục nút nếu có lỗi
                if (!document.querySelector('.discount-item')) {
                    applyPromoBtn.disabled = false;
                    applyPromoBtn.textContent = 'Áp dụng';
                }
            });
        });
    }
}

// Khởi tạo xử lý phương thức thanh toán
function initPaymentMethods() {
    const paymentOptions = document.querySelectorAll('.payment-option');
    const confirmBtn = document.getElementById('confirmPaymentBtn');
    const paymentIcon = document.getElementById('payment-icon');
    const paymentText = document.getElementById('payment-text');

    paymentOptions.forEach(option => {
        option.addEventListener('click', function () {
            // Bỏ chọn tất cả các option khác
            paymentOptions.forEach(opt => opt.classList.remove('active'));

            // Chọn option hiện tại
            this.classList.add('active');

            const method = this.getAttribute('data-method');

            // Cập nhật nút thanh toán theo phương thức
            if (method === 'MOMO') {
                confirmBtn.className = 'confirm-btn momo-btn';
                paymentIcon.className = 'bi bi-wallet2';
                paymentText.textContent = 'Thanh toán bằng MoMo';
            } else if (method === 'CASH') {
                confirmBtn.className = 'confirm-btn cash-btn';
                paymentIcon.className = 'bi bi-cash-coin';
                paymentText.textContent = 'Thanh toán tiền mặt';
            }
        });
    });
}

// Hàm hiển thị điểm người dùng
function updateUserPointsDisplay(points) {
    const userPointsElement = document.getElementById('userPoints');
    if (userPointsElement) {
        userPointsElement.textContent = `${points.toLocaleString('vi-VN')} điểm`;
    }
}

// Hàm tính và cập nhật điểm tối đa có thể sử dụng
function updateMaxUsablePoints(points) {
    // Lấy tổng tiền hiện tại
    let currentTotal = 0;
    try {
        // const totalPriceText = document.getElementById('totalAmount')?.textContent || '';
        // currentTotal = parseFloat(totalPriceText.replace(/[^\d]/g, '') || '0');
        currentTotal = priceState.currentPrice || (bookingDataFromSession.ticketPrice + bookingDataFromSession.totalComboPrice) || 0;
    } catch (error) {
        console.error('Error parsing current total:', error);
    }

    // Tính số tiền tối đa có thể giảm (30% của tổng tiền)
    const maxDiscountAmount = Math.floor(currentTotal * 0.3);

    // Tính số điểm tối đa từ maxDiscountAmount (1 điểm = 10đ)
    const maxPointsFromDiscount = Math.floor(maxDiscountAmount / 10);

    // Số điểm tối đa có thể sử dụng = min(điểm có sẵn, điểm từ 30% giá trị đơn hàng)
    const maxPoints = Math.min(points, maxPointsFromDiscount);

    // Hiển thị số điểm tối đa
    const maxUsablePointsElement = document.getElementById('maxUsablePoints');
    if (maxUsablePointsElement) {
        maxUsablePointsElement.textContent = maxPoints.toLocaleString('vi-VN');
    }

    // Cập nhật text hiển thị chi tiết
    const pointsDetailsElement = document.getElementById('pointsDetails');
    if (pointsDetailsElement && points > 0) {
        pointsDetailsElement.innerHTML = `
            <div style="margin-bottom: 5px">• 1 điểm = 10đ</div>
            <div style="margin-bottom: 5px">• Tối đa giảm 30% giá trị đơn hàng (${maxDiscountAmount.toLocaleString('vi-VN')}đ)</div>
            <div>• Tối đa sử dụng <span style="color: #fbbf24; font-weight: bold">${maxPoints.toLocaleString('vi-VN')}</span> điểm cho đơn hàng này</div>
        `;
        pointsDetailsElement.style.display = 'block';
    } else if (pointsDetailsElement) {
        pointsDetailsElement.style.display = 'none';
    }

    // Kích hoạt checkbox nếu có điểm
    const usePointsCheckbox = document.getElementById('usePointsCheckbox');
    if (usePointsCheckbox) {
        usePointsCheckbox.disabled = points <= 0 || maxPoints <= 0;
    }
}


// Áp dụng điểm người dùng vào hóa đơn
function applyUserPoints() {
    const userScore = bookingDataFromSession.userScore || 0;
    if (userScore <= 0) return;

    // Sử dụng giá gốc để tính điểm tối đa có thể sử dụng
    let currentTotal = priceState.originalPrice || (bookingDataFromSession.ticketPrice + bookingDataFromSession.totalComboPrice) || 0;

    // Tính số tiền tối đa có thể giảm (30% của tổng tiền hiện tại)
    const maxDiscountAmount = Math.floor(currentTotal * 0.3);

    // Tính số điểm tối đa từ maxDiscountAmount (1 điểm = 10đ)
    const maxPointsFromDiscount = Math.floor(maxDiscountAmount / 10);

    // Số điểm thực tế sử dụng = min(điểm có sẵn, điểm từ 30% giá trị)
    const maxPoints = Math.min(userScore, maxPointsFromDiscount);
    const discountAmount = maxPoints * 10;

    if (maxPoints <= 0 || discountAmount <= 0) {
        alertSystem.warning('Không thể sử dụng điểm. Số điểm không đủ hoặc đã đạt giới hạn giảm giá.', 3000);
        return;
    }

    // Lưu lại giảm giá từ điểm
    priceState.pointsDiscount = discountAmount;

    // Tính lại giá cuối cùng
    priceState.currentPrice = priceState.originalPrice - priceState.pointsDiscount - priceState.voucherDiscount;

    // Cập nhật tổng tiền vào session
    bookingDataFromSession.usedPoints = maxPoints;
    // bookingDataFromSession.totalPrice = priceState.currentPrice;
    sessionStorage.setItem('bookingData', JSON.stringify(bookingDataFromSession));

    // Cập nhật hiển thị
    document.getElementById('totalAmount').textContent = `${priceState.currentPrice.toLocaleString('vi-VN')}đ`;

    // Thêm dòng giảm giá từ điểm
    addPointsDiscountRow(discountAmount, maxPoints);

    // Hiển thị thông báo thành công với thông tin chi tiết
    alertSystem.success(`Đã sử dụng ${maxPoints.toLocaleString('vi-VN')} điểm để giảm ${discountAmount.toLocaleString('vi-VN')}đ`, 5000);
}

// Thêm dòng giảm giá từ điểm
function addPointsDiscountRow(discountAmount, usedPoints) {
    const priceBreakdown = document.querySelector('.price-breakdown');

    // Kiểm tra xem đã có dòng giảm giá từ điểm chưa
    let pointsDiscountItem = document.querySelector('.points-discount-item');

    if (!pointsDiscountItem) {
        pointsDiscountItem = document.createElement('div');
        pointsDiscountItem.className = 'price-item points-discount-item';

        // Chèn vào trước total-section
        const totalSection = document.querySelector('.total-section');

        // Kiểm tra xem totalSection có tồn tại và có phải là con của priceBreakdown không
        if (totalSection && priceBreakdown && priceBreakdown.contains(totalSection)) {
            priceBreakdown.insertBefore(pointsDiscountItem, totalSection);
        } else {
            // Nếu không, thêm vào cuối của priceBreakdown
            if (priceBreakdown) {
                priceBreakdown.appendChild(pointsDiscountItem);
            }
        }
    }

    pointsDiscountItem.innerHTML = `
        <span style="color: #22c55e;">
            <i class="bi bi-star-fill"></i> 
            Sử dụng <span id="usedPoints" class="used-points-value">${usedPoints.toLocaleString('vi-VN')}</span> điểm tích lũy
        </span>
        <span style="color: #22c55e;" class="used-points-discount">-${discountAmount.toLocaleString('vi-VN')}đ</span>
    `;

    // Thêm style cho phần tử hiển thị
    pointsDiscountItem.style.cssText = `
        background: rgba(34, 197, 94, 0.1); 
        border-radius: 8px; 
        padding: 8px 12px; 
        margin: 10px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        animation: fadeIn 0.3s;
    `;

    // Cập nhật hiển thị chi tiết điểm
    updatePointsDetailDisplay(usedPoints);
}

// Hàm cập nhật hiển thị chi tiết điểm sau khi sử dụng
function updatePointsDetailDisplay(usedPoints) {
    const userScore = bookingDataFromSession.userScore || 0;
    const remainingPoints = userScore - usedPoints;

    // Cập nhật hiển thị điểm còn lại
    const userPointsElement = document.getElementById('userPoints');
    if (userPointsElement) {
        // Hiển thị điểm còn lại sau khi đã sử dụng
        userPointsElement.innerHTML = `<span>${remainingPoints.toLocaleString('vi-VN')}</span> <span style="color: #999; font-size: 0.8em;">(Đã dùng: ${usedPoints.toLocaleString('vi-VN')})</span>`;
    }
}

// Hủy sử dụng điểm
function removeUserPoints() {
    if (!bookingDataFromSession.usedPoints) return;

    // Lấy số điểm đã sử dụng để thông báo
    const usedPoints = bookingDataFromSession.usedPoints;

    // Đặt lại giảm giá điểm về 0
    priceState.pointsDiscount = 0;

    // Tính lại giá cuối cùng
    priceState.currentPrice = priceState.originalPrice - priceState.voucherDiscount - priceState.pointsDiscount;

    // Cập nhật hiển thị
    document.getElementById('totalAmount').textContent = `${priceState.currentPrice.toLocaleString('vi-VN')}đ`;

    // Xóa dòng giảm giá từ điểm
    const pointsDiscountItem = document.querySelector('.points-discount-item');
    if (pointsDiscountItem) {
        pointsDiscountItem.remove();
    }

    // Xóa thông tin điểm đã sử dụng
    delete bookingDataFromSession.usedPoints;
    sessionStorage.setItem('bookingData', JSON.stringify(bookingDataFromSession));

    // Khôi phục hiển thị điểm ban đầu
    const userScore = bookingDataFromSession.userScore || 0;
    const userPointsElement = document.getElementById('userPoints');
    if (userPointsElement) {
        userPointsElement.textContent = `${userScore.toLocaleString('vi-VN')} điểm`;
    }

    alertSystem.info(`Đã hủy sử dụng ${usedPoints.toLocaleString('vi-VN')} điểm tích lũy`, 2000);
}

// Khởi tạo xử lý sử dụng điểm
function initPointsUsage() {
    const usePointsCheckbox = document.getElementById('usePointsCheckbox');

    if (usePointsCheckbox) {
        usePointsCheckbox.addEventListener('change', function () {
            if (this.checked) {
                applyUserPoints();
            } else {
                removeUserPoints();
            }
        });
    }
}

// Hàm khôi phục trạng thái khi reload trang
function restoreStateFromSession() {
    // 1. Khôi phục giá ban đầu
    if (bookingDataFromSession.originalPrice) {
        priceState.originalPrice = bookingDataFromSession.originalPrice;
    } else {
        // Nếu không có originalPrice, lấy từ tổng giá vé và combo
        priceState.originalPrice = (bookingDataFromSession.ticketPrice || 0) + 
                                  (bookingDataFromSession.totalComboPrice || 0);
    }
    
    // 2. Khôi phục trạng thái khuyến mãi
    restorePromotionState();
    
    // 3. Khôi phục trạng thái điểm tích lũy
    restorePointsState();
    
    // 4. Tính lại giá cuối cùng
    priceState.currentPrice = priceState.originalPrice - priceState.voucherDiscount - priceState.pointsDiscount;
    
    // 5. Cập nhật hiển thị giá cuối cùng
    const totalAmount = document.getElementById('totalAmount');
    if (totalAmount) {
        totalAmount.textContent = `${priceState.currentPrice.toLocaleString('vi-VN')}đ`;
    }
    
    console.log("Restored price state:", priceState);
}


// Khôi phục trạng thái khuyến mãi
function restorePromotionState() {
    const promoCodeInput = document.getElementById('promoCode');
    const applyPromoBtn = document.getElementById('applyPromoBtn');
    
    // Kiểm tra nếu đã có promotion áp dụng trước đó
    if (bookingDataFromSession.promotionId) {
        console.log("Restoring promotion state:", bookingDataFromSession.promotionId);
        
        // Thêm code để kiểm tra và xóa các phần tử cũ nếu có
        const oldCancelBtn = document.getElementById('cancelPromoBtn');
        if (oldCancelBtn) {
            oldCancelBtn.remove();
        }
        
        // Xóa dòng giảm giá cũ nếu có
        const oldDiscountItem = document.querySelector('.discount-item');
        if (oldDiscountItem) {
            oldDiscountItem.remove();
        }

        // Hiển thị mã khuyến mãi đã được lưu trong input
        if (bookingDataFromSession.promotionCode) {
            promoCodeInput.value = bookingDataFromSession.promotionCode;
        } else if (bookingDataFromSession.promotionId) {
            // Nếu không có mã nhưng có ID, hiển thị ID làm mã
            promoCodeInput.value = bookingDataFromSession.promotionId;
        }
        
        // Vô hiệu hóa input và nút áp dụng
        promoCodeInput.disabled = true;
        applyPromoBtn.disabled = true;
        applyPromoBtn.textContent = 'Đã áp dụng';
        applyPromoBtn.classList.remove('btn-warning');
        applyPromoBtn.classList.add('btn-success');
        
        // Lấy giá trị giảm giá từ session
        const discountAmount = bookingDataFromSession.voucherDiscount || 0;
        console.log("After reload restored discount amount:", discountAmount);
        priceState.voucherDiscount = discountAmount;
        
        // Lấy tên mã khuyến mãi từ session nếu có
        let promotionTitle = bookingDataFromSession.promotionTitle || 'Mã khuyến mãi';
        
        if (discountAmount > 0) {
            // Tạo lại dòng giảm giá
            const priceBreakdown = document.querySelector('.price-breakdown');
            if (priceBreakdown) {
                const discountItem = document.createElement('div');
                discountItem.className = 'price-item discount-item';
                
                // Chèn vào trước total-amount
                const totalSection = document.querySelector('.total-section') || document.querySelector('.total-amount');
                
                // Chỉ chèn trước totalSection nếu nó là con của priceBreakdown
                if (totalSection && priceBreakdown.contains(totalSection)) {
                    priceBreakdown.insertBefore(discountItem, totalSection);
                } else {
                    priceBreakdown.appendChild(discountItem);
                }
                
                discountItem.innerHTML = `
                    <span style="color: #22c55e;">
                        <i class="bi bi-tag-fill"></i> 
                        Giảm giá (${promotionTitle}: <strong>${bookingDataFromSession.promotionCode || bookingDataFromSession.promotionId}</strong>)
                    </span>
                    <span style="color: #22c55e;">-${discountAmount.toLocaleString('vi-VN')}đ</span>
                `;

                // Thêm style cho phần tử
                discountItem.style.cssText = `
                    background: rgba(34, 197, 94, 0.1); 
                    border-radius: 8px; 
                    padding: 8px 12px; 
                    margin: 10px;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    animation: fadeIn 0.3s;
                `;
            }
        }
        
        // Thêm nút hủy
        const cancelBtn = document.createElement('button');
        cancelBtn.className = 'btn btn-outline-danger ms-2';
        cancelBtn.id = 'cancelPromoBtn';
        cancelBtn.innerHTML = '<i class="bi bi-x-lg"></i>';
        cancelBtn.title = 'Hủy mã giảm giá';
        applyPromoBtn.parentNode.appendChild(cancelBtn);
        
        // Xử lý sự kiện hủy
        cancelBtn.addEventListener('click', function() {
            // Logic hủy mã khuyến mãi
            delete bookingDataFromSession.promotionId;
            delete bookingDataFromSession.promotionCode;
            delete bookingDataFromSession.promotionTitle;
            delete bookingDataFromSession.voucherDiscount;
            sessionStorage.setItem('bookingData', JSON.stringify(bookingDataFromSession));
            
            // Đặt lại giá trị giảm giá trong priceState
            priceState.voucherDiscount = 0;
            
            // Tính lại giá cuối cùng
            priceState.currentPrice = priceState.originalPrice - priceState.pointsDiscount;
            document.getElementById('totalAmount').textContent = `${priceState.currentPrice.toLocaleString('vi-VN')}đ`;
            bookingDataFromSession.totalPrice = priceState.currentPrice;
            
            // Xóa dòng giảm giá
            const discountItem = document.querySelector('.discount-item');
            if (discountItem) {
                discountItem.remove();
            }
            
            // Khôi phục trạng thái ban đầu cho input và nút
            promoCodeInput.disabled = false;
            promoCodeInput.value = '';
            applyPromoBtn.disabled = false;
            applyPromoBtn.textContent = 'Áp dụng';
            applyPromoBtn.classList.remove('btn-success');
            applyPromoBtn.classList.add('btn-warning');
            
            // Xóa nút hủy
            cancelBtn.remove();
            
            alertSystem.info('Đã hủy mã giảm giá', 2000);
        });
    }
}

// Khôi phục trạng thái điểm tích lũy
function restorePointsState() {
    const usePointsCheckbox = document.getElementById('usePointsCheckbox');
    
    // Khôi phục trạng thái sử dụng điểm
    if (bookingDataFromSession.usedPoints && bookingDataFromSession.usedPoints > 0) {
        console.log("Restoring points state:", bookingDataFromSession.usedPoints);
        
        // Lấy số điểm đã sử dụng
        const usedPoints = bookingDataFromSession.usedPoints;
        
        // Tính giá trị giảm giá từ điểm (1 điểm = 10đ)
        const discountAmount = usedPoints * 10;
        priceState.pointsDiscount = discountAmount;
        
        // Đánh dấu checkbox là đã chọn
        if (usePointsCheckbox) {
            usePointsCheckbox.checked = true;
        }
        
        // Thêm dòng giảm giá từ điểm
        const pointsDiscountItem = document.querySelector('.points-discount-item');
        if (!pointsDiscountItem) {
            addPointsDiscountRow(discountAmount, usedPoints);
        }
        
        // CẬP NHẬT THÊM: Đảm bảo cập nhật hiển thị điểm còn lại
        const userScore = bookingDataFromSession.userScore || 0;
        const remainingPoints = userScore - usedPoints;
        
        const userPointsElement = document.getElementById('userPoints');
        if (userPointsElement) {
            userPointsElement.innerHTML = `<span>${remainingPoints.toLocaleString('vi-VN')}</span> <span style="color: #999; font-size: 0.8em;">(Đã dùng: ${usedPoints.toLocaleString('vi-VN')})</span>`;
        }
    }
}

// Hàm khởi tạo trạng thái giá
function initializePriceState() {
    priceState = {
        originalPrice: bookingDataFromSession.totalPrice || 0,
        currentPrice: bookingDataFromSession.totalPrice || 0,
        voucherDiscount: 0,
        pointsDiscount: 0
    };
    
    // Nếu đã có thông tin từ session, khôi phục lại trạng thái
    restoreStateFromSession();
}

// Khởi tạo trang
document.addEventListener('DOMContentLoaded', function () {
    console.log('Payment page loaded, booking data:', bookingDataFromSession);

    // Kiểm tra dữ liệu đặt vé
    if (!bookingDataFromSession.seats || bookingDataFromSession.seats.length === 0) {
        alertSystem.danger('Không tìm thấy thông tin ghế đã chọn. Vui lòng đặt vé lại!', 3000);
        setTimeout(() => {
            window.location.href = `/booking/seats?movieId=${movieId}&showDateId=${showDateId}&scheduleId=${scheduleId}`;
        }, 3000);
        return;
    }
    // Khởi tạo priceState và khôi phục trạng thái từ session
    initializePriceState();

    // Lấy thông tin phim
    fetchMovieDetails();

    // Hiển thị chi tiết đặt vé ngay lập tức với dữ liệu có sẵn
    displayBookingDetails();

    // Cập nhật timer
    updatePaymentTimer();
    // Bắt đầu đếm ngược thời gian
    setInterval(updatePaymentTimer, 1000);

    // Khởi tạo xử lý phương thức thanh toán
    initPaymentMethods();

    // Khởi tạo hiệu ứng cho nút thanh toán
    initMomoButtonEffect();

    // Khởi tạo xử lý mã khuyến mãi
    initPromoCode();

    // Lấy và hiển thị điểm người dùng
    fetchUserScore();

    // Khởi tạo xử lý sử dụng điểm
    initPointsUsage();

    // Gắn sự kiện cho nút thanh toán
    const confirmPaymentBtn = document.getElementById('confirmPaymentBtn');
    if (confirmPaymentBtn) {
        confirmPaymentBtn.addEventListener('click', processPayment);
    }

    // Thông báo chào mừng
    setTimeout(() => {
        alertSystem.info('Vui lòng xác nhận thông tin và hoàn tất thanh toán', 3000);
    }, 1000);
});