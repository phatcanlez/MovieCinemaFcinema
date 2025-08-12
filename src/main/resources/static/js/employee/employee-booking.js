// Store API movie data globally
let movieData = []; // Global storage for movie data
let combosData = []; // Global storage for combo data
let showtimeData = []; // Global storage for showtime data
let seats = []; // Global storage for seat data

// Booking data
let bookingData = {
    movieId: "",
    cinemaRoomId: 0,
    showDateId: 0, // Integer to match API expectation
    scheduleId: 0,
    seatsIds: [],
    paymentMethod: "",
    accountId: null,
    promotionCode: "",
    promotionId: null, // Store the promotion ID from API
    combos: [], // Array to store multiple combos { comboId, quantity }
    totalPrice: 0, // Total price calculated from API
    showtimeData: {}, // Store selected showtime details
    bookingId: null, // Store booking ID from API
    usedPoints: 0, // Store the number of points actually used for discount
    memberEmail: "", // Store member email
    memberPoints: 0, // Store member points
    pointsDiscountApplied: false, // Track if points discount is applied
    usedPointsValue: 0, // Store used points value
    originalAmount: 0, // Store original amount before discounts
    discountAmount: 0, // Store discount amount
    finalAmount: 0 // Store final amount after discounts
};

// Cải thiện price state theo mẫu từ payment.js
let priceState = {
    originalPrice: 0,       // Giá gốc khi chưa có khuyến mãi nào
    currentPrice: 0,        // Giá hiện tại sau khi áp dụng các khuyến mãi
    voucherDiscount: 0,     // Số tiền giảm giá từ voucher
    pointsDiscount: 0       // Số tiền giảm giá từ điểm
};

// Current step
let currentStep = 1;

// Initialize
document.addEventListener("DOMContentLoaded", () => {
    populateMovies();
    populateCombos();
    setupEventListeners();
    updateInfoPanel();
});

// Show danh sách phim
function populateMovies() {
    const movieSelect = document.getElementById("movieSelect");
    movieSelect.innerHTML = '<option value="">Select a movie</option>';
    fetch(`/api/employee/movies`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status} - ${response.statusText}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('API response:', data); // Debug log
            movieData = data; // Store the API response
            const uniqueMovies = Array.from(new Set(data.map(m => m.movieId))).map(id => data.find(m => m.movieId === id));
            uniqueMovies.forEach(movie => {
                const option = document.createElement("option");
                option.value = movie.movieId;
                option.textContent = movie.movieNameVn; // Use movieNameVn for display
                movieSelect.appendChild(option);
            });
            if (bookingData.movieId) {
                movieSelect.value = bookingData.movieId;
                populateDates(bookingData.movieId);
            }
        });
}

// Show ngày chiếu
function populateDates(movieId) {
    const dateSelect = document.getElementById("dateSelect");
    dateSelect.disabled = !movieId;
    if (movieId) {
        const movie = movieData.find(m => m.movieId === movieId);
        if (movie) {
            const today = new Date().toISOString().split('T')[0]; // e.g., "2025-07-29"
            dateSelect.min = movie.fromDate < today ? today : movie.fromDate; // Set min to today or fromDate, whichever is later
            dateSelect.max = movie.toDate; // Set max to toDate
            dateSelect.value = bookingData.showDateId || ""; // Set the current value if exists
        } else {
            dateSelect.min = "";
            dateSelect.max = "";
            dateSelect.value = "";
        }
    } else {
        dateSelect.min = "";
        dateSelect.max = "";
        dateSelect.value = "";
    }
    if (bookingData.showDateId && movieId === bookingData.movieId) {
        populateShowtimes(bookingData.movieId, bookingData.showDateId);
    } else {
        document.getElementById("showtimeSelect").innerHTML = '<option value="">Select a showtime</option>';
        document.getElementById("showtimeSelect").disabled = true;
        document.getElementById("nextStep1").disabled = true;
    }
}

// Show lịch chiếu
function populateShowtimes(movieId, dateStr) {
    const showtimeSelect = document.getElementById("showtimeSelect");
    showtimeSelect.innerHTML = '<option value="">Select a showtime</option>';
    showtimeSelect.disabled = !movieId || !dateStr;
    if (movieId && dateStr) {
        fetch(`/api/employee/showtimes?movieId=${movieId}&date=${dateStr}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                // Add authentication header if required by Swagger (e.g., 'Authorization': 'Bearer your-token-here')
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status} - ${response.statusText}`);
                }
                return response.json();
            })
            .then(data => {
                console.log('Showtimes API response:', data); // Debug log
                showtimeData = data; // Store all showtime data globally
                const movieShowtimes = data.filter(showtime => {
                    const showTime = new Date(`${dateStr}T${showtime.time}:00+07:00`);
                    return showTime >= new Date(); // Current time check
                });
                showtimeSelect.innerHTML = '<option value="">Select a showtime</option>'; // Reset options
                movieShowtimes.forEach(showtime => {
                    const option = document.createElement("option");
                    option.value = showtime.scheduleId;
                    option.textContent = showtime.time;
                    option.dataset.cinemaRoomId = showtime.cinemaRoomId;
                    option.dataset.room = showtime.room; // Store room data in dataset
                    showtimeSelect.appendChild(option);
                });
                if (bookingData.scheduleId && movieId === bookingData.movieId && dateStr === bookingData.showDateId) {
                    showtimeSelect.value = bookingData.scheduleId;
                    // Update showtimeData if scheduleId matches
                    const selectedShowtime = showtimeData.find(s => s.scheduleId === bookingData.scheduleId);
                    if (selectedShowtime) {
                        bookingData.showtimeData = {
                            time: selectedShowtime.time,
                            room: selectedShowtime.room
                        };
                        bookingData.showDateId = selectedShowtime.showDateId; // Update to integer ID
                        bookingData.scheduleId = selectedShowtime.scheduleId; // Ensure correct ID
                        bookingData.cinemaRoomId = selectedShowtime.cinemaRoomId;
                    }
                    document.getElementById("nextStep1").disabled = !validateStep(1);
                } else {
                    document.getElementById("nextStep1").disabled = true;
                    bookingData.showtimeData = {}; // Clear showtime data if no match
                }
            });
    }
}

// Show seat map
function populateSeats() {
    const seatMap = document.getElementById("seatMap");
    seatMap.innerHTML = '<div class="screen"></div>'; // Add screen label
    seats = []; // Reset seats array
    if (bookingData.movieId && bookingData.showDateId && bookingData.scheduleId) {
        fetch(`/api/booking/seats?movieId=${bookingData.movieId}&showDateId=${bookingData.showDateId}&scheduleId=${bookingData.scheduleId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status} - ${response.statusText}`);
                }
                return response.json();
            })
            .then(data => {
                console.log('Seats API response:', data); // Debug log
                // Flatten the nested structure into seats array
                for (let row in data) {
                    for (let col in data[row]) {
                        seats.push(data[row][col]);
                    }
                }
                seats.forEach(seat => {
                    const rowDiv = seatMap.querySelector(`.row[row="${seat.seatRow}"]`) || (() => {
                        const newRow = document.createElement("div");
                        newRow.className = "row";
                        newRow.setAttribute("row", seat.seatRow);
                        seatMap.appendChild(newRow);
                        return newRow;
                    })();
                    const seatDiv = document.createElement("div");
                    seatDiv.className = `seat`;
                    if (seat.seatStatus === 1) {
                        seatDiv.classList.add("occupied"); // Gray for occupied
                    } else if (seat.seatType === "VIP") {
                        seatDiv.classList.add("available", "vip"); // Red for VIP
                    } else {
                        seatDiv.classList.add("available", "standard"); // Green for STANDARD
                    }
                    seatDiv.textContent = `${seat.seatColumn}${seat.seatRow}`; // Display seatColumn + seatRow (e.g., "A1")
                    seatDiv.dataset.seatId = seat.seatId;
                    if (seat.seatStatus === 0) {
                        seatDiv.addEventListener("click", () => toggleSeat(seat.seatId, seatDiv));
                    }
                    rowDiv.appendChild(seatDiv);
                    if (bookingData.seatsIds.includes(seat.seatId)) {
                        seatDiv.classList.remove("available", seat.seatType.toLowerCase());
                        seatDiv.classList.add("selected"); // Yellow for selected
                    }
                });
                document.getElementById("nextStep2").disabled = bookingData.seatsIds.length === 0;
                updateInfoPanel(); // Recalculate total price when seats change
            });
    }
}

/**
 * Kiểm tra toàn bộ lựa chọn ghế xem có vi phạm quy tắc ghế trống đơn độc không.
 * @returns {boolean} - Trả về true nếu lựa chọn hợp lệ, ngược lại trả về false.
 */
function validateSeatGaps() {
    // Lấy tất cả các hàng ghế có trong sơ đồ
    const uniqueRows = [...new Set(seats.map(s => s.seatRow))];

    // Hàm nội bộ để kiểm tra một ghế có bị chiếm hay không (chỉ dựa trên ghế đang chọn)
    const isTaken = (seat) => bookingData.seatsIds.includes(seat.seatId);

    // Duyệt qua từng hàng để kiểm tra
    for (const rowLabel of uniqueRows) {
        const rowSeats = seats
            .filter(s => s.seatRow === rowLabel)
            .sort((a, b) => a.seatColumn - b.seatColumn);

        // Duyệt qua từng ghế trong hàng
        for (let i = 0; i < rowSeats.length; i++) {
            const currentSeat = rowSeats[i];

            // Nếu ghế hiện tại là ghế trống (không được chọn)
            if (!isTaken(currentSeat)) {
                // Lấy thông tin ghế bên trái và bên phải
                const leftNeighbor = rowSeats[i - 1];
                const rightNeighbor = rowSeats[i + 1];

                // Coi lối đi/tường (không có ghế bên cạnh) như là một "biên giới đã bị chiếm"
                const isLeftBoundaryTaken = !leftNeighbor || isTaken(leftNeighbor);
                const isRightBoundaryTaken = !rightNeighbor || isTaken(rightNeighbor);

                // Nếu cả hai bên của ghế trống này đều là ghế đang chọn hoặc là biên giới (lối đi)
                // -> Đây chính là một ghế trống đơn độc không hợp lệ giữa các ghế được chọn.
                if (isLeftBoundaryTaken && isRightBoundaryTaken) {
                    console.error('Lỗi ghế trống đơn độc được tìm thấy tại:', currentSeat);
                    return false; // Lựa chọn không hợp lệ
                }
            }
        }
    }

    // Nếu duyệt qua tất cả các ghế mà không tìm thấy lỗi, lựa chọn là hợp lệ
    return true;
}

// Chọn ghế
function toggleSeat(seatId, seatDiv) {
    const index = bookingData.seatsIds.indexOf(seatId);
    seatDiv.classList.add("clicking");
    setTimeout(() => {
        if (index === -1) { // Người dùng đang cố gắng chọn một ghế mới
            if (bookingData.seatsIds.length >= 8) {
                alert("You can only select a maximum of 8 seats!");
                seatDiv.classList.remove("clicking");
                return; // Ngăn không cho chọn ghế này
            }
            bookingData.seatsIds.push(seatId);
            seatDiv.classList.remove("available", seatDiv.classList[1], "clicking");
            seatDiv.classList.add("selected");
        } else { // Người dùng đang bỏ chọn một ghế
            bookingData.seatsIds.splice(index, 1);
            // Xác định lại loại ghế để trả về màu đúng (standard hoặc vip)
            const originalSeatData = seats.find(s => s.seatId === seatId);
            const seatType = originalSeatData ? originalSeatData.seatType.toLowerCase() : "standard";

            seatDiv.classList.remove("selected", "clicking");
            seatDiv.classList.add("available", seatType);
        }
        document.getElementById("nextStep2").disabled = bookingData.seatsIds.length === 0;
        updateInfoPanel(); // Tính toán lại tổng giá tiền khi ghế thay đổi
    }, 200);
}

// Show combo
function populateCombos() {
    const comboList = document.getElementById("comboList");
    comboList.innerHTML = "";
    fetch(`/api/booking/combos`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            // Add authentication header if required by Swagger (e.g., 'Authorization': 'Bearer your-token-here')
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status} - ${response.statusText}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Combos API response:', data); // Debug log
            combosData = data.combos; // Store combo data globally
            combosData.forEach(combo => {
                const div = document.createElement("div");
                div.className = "combo-card";
                div.innerHTML = `
                <img src="${combo.imageUrl}" alt="${combo.comboName}" class="combo-image">
                <div class="combo-details">
                    <h5>${combo.comboName}</h5>
                    <p>${combo.description}</p>
                    <p class="price">${combo.formattedDiscountedPrice} VND</p>
                </div>
                <div class="quantity-controls">
                    <button class="quantity-btn" data-action="decrease" data-combo-id="${combo.comboId}">-</button>
                    <input type="number" class="quantity-input" id="quantity-${combo.comboId}" value="0" min="0" readonly>
                    <button class="quantity-btn" data-action="increase" data-combo-id="${combo.comboId}">+</button>
                </div>
            `;
                comboList.appendChild(div);
                const existingCombo = bookingData.combos.find(c => c.comboId === combo.comboId);
                document.getElementById(`quantity-${combo.comboId}`).value = existingCombo ? existingCombo.quantity : 0;
                document.getElementById(`quantity-${combo.comboId}`).removeAttribute("readonly"); // Allow manual input
            });
            document.getElementById("nextStep3").disabled = false; // Ensure nextStep3 is always enabled
            updateInfoPanel(); // Recalculate total price when combos are populated
        });
}

// Cải thiện updateInfoPanel dựa trên logic từ payment.js
function updateInfoPanel() {
    const dateSelect = document.getElementById("dateSelect").value; // Get the selected date string
    document.getElementById("infoDate").textContent = dateSelect || "None"; // Display the date string
    const movie = bookingData.movieId
        ? movieData.find(m => m.movieId === bookingData.movieId)?.movieNameVn || "None"
        : "None";
    document.getElementById("infoMovie").textContent = movie;
    const showtime = bookingData.showtimeData.time || "None";
    document.getElementById("infoShowtime").textContent = showtime;
    document.getElementById("infoRoom").textContent = bookingData.showtimeData.room || "None";
    const selectedSeats = bookingData.seatsIds.length > 0
        ? seats.filter(s => bookingData.seatsIds.includes(s.seatId)).map(s => `(${s.seatType})${s.seatColumn}${s.seatRow}`).join(", ")
        : "None";
    document.getElementById("infoSeats").textContent = selectedSeats;

    const smallImage = bookingData.movieId
        ? movieData.find(m => m.movieId === bookingData.movieId)?.smallImage || "No Image"
        : "No Image";
    document.getElementById("infoSmallImage").src = smallImage;
    document.getElementById("infoSmallImage").style.display = bookingData.movieId ? "block" : "none";

    // Use globally stored combo data
    const comboText = bookingData.combos.length > 0
        ? bookingData.combos.map(c => {
            const combo = combosData.find(cd => cd.comboId === c.comboId);
            return combo ? `${combo.comboName} (x${c.quantity})` : "Unknown (x" + c.quantity + ")";
        }).join(", ")
        : "No Combo";
    document.getElementById("infoCombo").textContent = comboText;

    document.getElementById("infoPromotionCode").textContent = bookingData.promotionCode || "None";

    // Cải thiện tính toán giá dựa trên logic từ payment.js
    calculateAndDisplayPrices();
}

// Hàm tính toán và hiển thị giá mới (dựa trên payment.js)
function calculateAndDisplayPrices() {
    // Tính giá ghế
    let seatPrice = 0;
    bookingData.seatsIds.forEach(seatId => {
        const seat = seats.find(s => s.seatId === seatId);
        if (seat) seatPrice += seat.seatPrice || 0;
    });

    // Tính giá combo
    let comboPrice = 0;
    bookingData.combos.forEach(combo => {
        const comboData = combosData.find(cd => cd.comboId === combo.comboId);
        if (comboData) {
            comboPrice += comboData.discountedPrice * combo.quantity;
        }
    });

    // Cập nhật giá gốc
    priceState.originalPrice = seatPrice + comboPrice;
    
    // Reset discounts nếu không có khuyến mãi
    if (!bookingData.promotionId) {
        priceState.voucherDiscount = 0;
    }
    if (!bookingData.pointsDiscountApplied) {
        priceState.pointsDiscount = 0;
    }

    // Tính giá hiện tại
    priceState.currentPrice = priceState.originalPrice - priceState.voucherDiscount - priceState.pointsDiscount;
    
    // Đảm bảo giá không âm
    priceState.currentPrice = Math.max(0, priceState.currentPrice);

    // Cập nhật booking data
    bookingData.totalPrice = priceState.currentPrice;
    bookingData.originalAmount = priceState.originalPrice;
    bookingData.discountAmount = priceState.voucherDiscount + priceState.pointsDiscount;
    bookingData.finalAmount = priceState.currentPrice;

    // Update price breakdown in info panel
    document.getElementById("infoSeatPrice").textContent = `${seatPrice.toLocaleString('vi-VI')} đ`;
    document.getElementById("infoComboPrice").textContent = `${comboPrice.toLocaleString('vi-VI')} đ`;
    document.getElementById("infoOriginalAmount").textContent = `${priceState.originalPrice.toLocaleString('vi-VI')} đ`;
    document.getElementById("infoDiscountAmount").textContent = `${(priceState.voucherDiscount + priceState.pointsDiscount).toLocaleString('vi-VI')} đ`;
    document.getElementById("infoFinalAmount").textContent = `${priceState.currentPrice.toLocaleString('vi-VI')} đ`;
}

function calculateTotalPrice() {
    return priceState.currentPrice || priceState.originalPrice;
}

function renderStep(step) {
    document.querySelectorAll(".step").forEach(stepDiv => {
        stepDiv.classList.remove("active");
    });
    document.getElementById(`step${step}`).classList.add("active");

    // Populate dynamic content based on step
    if (step === 1) {
        if (bookingData.movieId) {
            populateDates(bookingData.movieId);
            if (bookingData.showDateId) {
                populateShowtimes(bookingData.movieId, bookingData.showDateId);
            }
        }
    } else if (step === 2) {
        populateSeats();
    } else if (step === 3) {
        populateCombos();
    } else if (step === 4) {
        document.getElementById("promotionCode").value = bookingData.promotionCode || "";
        document.getElementById("paymentMethod").value = bookingData.paymentMethod || "";
        document.getElementById("memberEmail").value = bookingData.memberEmail || "";
        document.getElementById("submitBooking").disabled = !validateStep(4);
        document.getElementById("promoError").innerHTML = ""; // Clear any previous error

        updatePointsDisplay();
        updatePromoCodeDisplay();
    } else if (step === 5) {
        displayTicketDetails();
    }

    document.getElementById("progressBar").style.width = `${step * 20}%`;
    document.getElementById("progressBar").textContent = `Step ${step} of 5`;
}

// Hàm cập nhật hiển thị điểm (cải thiện để hiển thị giới hạn 30%)
function updatePointsDisplay() {
    const memberPointsElement = document.getElementById("memberPoints");
    const usePointsBtnElement = document.getElementById("usePointsBtn");
    const removePointsBtnElement = document.getElementById("removePointsBtn");
    
    if (bookingData.memberPoints > 0) {
        if (bookingData.pointsDiscountApplied) {
            const remainingPoints = bookingData.memberPoints - bookingData.usedPoints;            
            memberPointsElement.innerHTML = `
                <div class="alert alert-success" role="alert">
                    <strong>Points Applied Successfully!</strong><br>
                    Remaining: ${remainingPoints.toLocaleString('vi-VN')} points<br>
                    <small class="text-muted">Used: ${bookingData.usedPoints.toLocaleString('vi-VN')} points (${(bookingData.usedPoints * 10).toLocaleString('vi-VN')}đ discount)</small>
                </div>
            `;
            usePointsBtnElement.style.display = "none";
            removePointsBtnElement.style.display = "inline-block";
        } else {
            // Tính số điểm tối đa có thể sử dụng (30% của tổng tiền)
            const maxDiscountAmount = Math.floor(priceState.originalPrice * 0.3);
            const maxPointsFromDiscount = Math.floor(maxDiscountAmount / 10);
            const maxUsablePoints = Math.min(bookingData.memberPoints, maxPointsFromDiscount);
            const maxDiscountValue = maxUsablePoints * 10;
            
            memberPointsElement.innerHTML = `
                <div class="alert alert-info" role="alert">
                    <strong>Available Points:</strong> ${bookingData.memberPoints.toLocaleString('vi-VN')} points<br>
                    <strong>Max usable:</strong> ${maxUsablePoints.toLocaleString('vi-VN')} points (${maxDiscountValue.toLocaleString('vi-VN')}đ)<br>
                    <small class="text-muted">1 point = 10đ, max 30% of total amount (${maxDiscountAmount.toLocaleString('vi-VN')}đ)</small>
                </div>
            `;
            usePointsBtnElement.style.display = "inline-block";
            removePointsBtnElement.style.display = "none";
        }
    } else if (bookingData.memberEmail && bookingData.accountId) {
        // Member found but no points
        memberPointsElement.innerHTML = `
            <div class="alert alert-warning" role="alert">
                <strong>Member found but no points available.</strong>
            </div>
        `;
        usePointsBtnElement.style.display = "none";
        removePointsBtnElement.style.display = "none";
    } else {
        // No member data
        memberPointsElement.innerHTML = "";
        usePointsBtnElement.style.display = "none";
        removePointsBtnElement.style.display = "none";
    }
}

// Hàm mới để hủy việc sử dụng điểm
function removePointsDiscount() {
    if (!bookingData.pointsDiscountApplied) {
        return;
    }

    // Reset points discount
    priceState.pointsDiscount = 0;
    priceState.currentPrice = priceState.originalPrice - priceState.voucherDiscount;
    
    // Đảm bảo giá không âm
    priceState.currentPrice = Math.max(0, priceState.currentPrice);

    // Reset booking data
    bookingData.pointsDiscountApplied = false;
    bookingData.usedPoints = 0;
    bookingData.totalPrice = priceState.currentPrice;

    // Cập nhật UI
    updateInfoPanel();
    updatePointsDisplay();
    document.getElementById("submitBooking").disabled = !validateStep(4);

    console.log("Points discount removed successfully");
}

function resetMemberData() {
    bookingData.memberPoints = 0;
    bookingData.accountId = null;
    bookingData.pointsDiscountApplied = false;
    bookingData.usedPoints = 0;
    
    // Reset price state nếu đã áp dụng điểm
    if (priceState.pointsDiscount > 0) {
        priceState.pointsDiscount = 0;
        priceState.currentPrice = priceState.originalPrice - priceState.voucherDiscount;
        bookingData.totalPrice = priceState.currentPrice;
        updateInfoPanel();
    }
    
    updatePointsDisplay();
}

// Hàm cập nhật hiển thị mã khuyến mãi
function updatePromoCodeDisplay() {
    const promoCodeInput = document.getElementById("promotionCode");
    const applyPromoBtn = document.getElementById("applyPromotion");
    const promoErrorDiv = document.getElementById("promoError");
    const promoContainer = document.getElementById("promoCodeContainer");
    
    // Xóa nút hủy cũ nếu có
    const oldCancelBtn = document.getElementById("cancelPromoBtn");
    if (oldCancelBtn) {
        oldCancelBtn.remove();
    }
    
    if (bookingData.promotionId && bookingData.promotionCode) {
        // Nếu đã có mã khuyến mãi
        promoCodeInput.value = bookingData.promotionCode;
        promoCodeInput.disabled = true;
        
        if (applyPromoBtn) {
            applyPromoBtn.textContent = "Đã áp dụng";
            applyPromoBtn.disabled = true;
            applyPromoBtn.classList.remove("btn-primary");
            applyPromoBtn.classList.add("btn-success");
            
            // Thêm nút hủy mã khuyến mãi
            const cancelBtn = document.createElement("button");
            cancelBtn.id = "cancelPromoBtn";
            cancelBtn.className = "btn btn-danger ms-2";
            cancelBtn.innerHTML = '<i class="bi bi-x"></i> Hủy';
            cancelBtn.type = "button";
            
            // Thêm sự kiện cho nút hủy
            cancelBtn.addEventListener("click", cancelPromotion);
            
            // Thêm nút hủy vào container
            applyPromoBtn.parentNode.appendChild(cancelBtn);
        }
        
        // Hiển thị thông tin giảm giá
        promoErrorDiv.innerHTML = `<div class="alert alert-success" role="alert">
            Promotion applied successfully! Discount: ${priceState.voucherDiscount.toLocaleString('vi-VN')}đ
        </div>`;
    } else {
        // Nếu chưa có mã khuyến mãi
        promoCodeInput.disabled = false;
        if (applyPromoBtn) {
            applyPromoBtn.textContent = "Áp dụng";
            applyPromoBtn.disabled = false;
            applyPromoBtn.classList.remove("btn-success");
            applyPromoBtn.classList.add("btn-primary");
        }
        promoErrorDiv.innerHTML = "";
    }
}

function validateStep(step) {
    if (step === 1) {
        return bookingData.movieId && bookingData.showDateId && bookingData.scheduleId;
    } else if (step === 2) {
        return bookingData.seatsIds.length > 0;
    } else if (step === 3) {
        return true; // Always allow next step, no combo selection required
    } else if (step === 4) {
        return bookingData.paymentMethod && (!bookingData.memberEmail || bookingData.memberPoints >= 0); // Valid if payment method is set
    }
    return true; // Step 5 has no validation
}

// Hàm hủy mã khuyến mãi
function cancelPromotion() {
    // Lưu lại giá trị cho phép debug
    const oldPromoId = bookingData.promotionId;
    const oldDiscount = priceState.voucherDiscount;
    
    // Reset thông tin promotion trong bookingData
    bookingData.promotionCode = "";
    bookingData.promotionId = null;
    
    // Reset giảm giá voucher
    priceState.voucherDiscount = 0;
    
    // Tính lại giá hiện tại
    priceState.currentPrice = priceState.originalPrice - priceState.pointsDiscount;
    
    // Cập nhật giá trong bookingData
    bookingData.totalPrice = priceState.currentPrice;
    
    // Cập nhật UI
    document.getElementById("promotionCode").value = "";
    document.getElementById("promoError").innerHTML = '<div class="alert alert-info" role="alert">Đã hủy mã khuyến mãi</div>';
    
    console.log(`Canceled promotion ${oldPromoId} with discount ${oldDiscount.toLocaleString('vi-VN')}đ`);
    
    // Cập nhật lại display
    updatePromoCodeDisplay();
    updateInfoPanel();
    
    // Kiểm tra lại trạng thái nút Submit
    document.getElementById("submitBooking").disabled = !validateStep(4);
}

function setupEventListeners() {
    function attachStep1Listeners() {
        const movieSelect = document.getElementById("movieSelect");
        if (movieSelect) {
            movieSelect.addEventListener("change", (e) => {
                bookingData.movieId = e.target.value;
                bookingData.showDateId = 0;
                bookingData.scheduleId = 0;
                bookingData.cinemaRoomId = 0;
                bookingData.showtimeData = {}; // Clear showtime data
                populateDates(bookingData.movieId);
                updateInfoPanel(); // Update info panel on movie selection
            });
        }

        const dateSelect = document.getElementById("dateSelect");
        if (dateSelect) {
            dateSelect.addEventListener("change", (e) => {
                bookingData.showDateId = e.target.value; // Remains a date string for dateSelect initially
                bookingData.scheduleId = 0;
                bookingData.cinemaRoomId = 0;
                bookingData.showtimeData = {}; // Clear showtime data
                if (bookingData.movieId) {
                    populateShowtimes(bookingData.movieId, bookingData.showDateId);
                }
                updateInfoPanel();
            });
        }

        const showtimeSelect = document.getElementById("showtimeSelect");
        if (showtimeSelect) {
            showtimeSelect.addEventListener("change", (e) => {
                bookingData.scheduleId = parseInt(e.target.value);
                const selectedOption = e.target.selectedOptions[0];
                bookingData.cinemaRoomId = parseInt(selectedOption.dataset.cinemaRoomId);
                bookingData.showtimeData = {
                    time: selectedOption.textContent,
                    room: selectedOption.dataset.room
                };
                // Update showDateId and scheduleId from showtimeData
                const selectedShowtime = showtimeData.find(s => s.scheduleId === bookingData.scheduleId);
                if (selectedShowtime) {
                    bookingData.showDateId = selectedShowtime.showDateId;
                }
                document.getElementById("nextStep1").disabled = !validateStep(1);
                updateInfoPanel();
            });
        }

        const nextStep1 = document.getElementById("nextStep1");
        if (nextStep1) {
            nextStep1.addEventListener("click", () => {
                if (validateStep(1)) goToStep(2);
            });
        }
    }

    function attachStep2Listeners() {
        const nextStep2 = document.getElementById("nextStep2");
        if (nextStep2) {
            nextStep2.addEventListener("click", () => {
                // 1. Kiểm tra điều kiện cơ bản: đã chọn ít nhất 1 ghế chưa
                if (!validateStep(2)) {
                    alert("Please select at least one seat.");
                    return;
                }
                // 2. Kiểm tra quy tắc ghế trống đơn độc
                if (!validateSeatGaps()) {
                    alert("Please do not leave a single empty seat. Please check your selection.");
                    return; // Dừng lại, không cho qua bước tiếp theo
                }
                // 3. Nếu hợp lệ, chuyển sang bước 3
                goToStep(3);
            });
        }

        const backStep2 = document.getElementById("backStep2");
        if (backStep2) {
            backStep2.addEventListener("click", () => goToStep(1));
        }
    }

    function attachStep3Listeners() {
        const comboContainer = document.getElementById("comboList");
        if (comboContainer) {
            comboContainer.addEventListener("click", (e) => {
                const btn = e.target.closest(".quantity-btn");
                if (btn) {
                    const comboId = parseInt(btn.dataset.comboId);
                    const action = btn.dataset.action;
                    const quantityInput = document.getElementById(`quantity-${comboId}`);
                    let quantity = parseInt(quantityInput.value) || 0;
                    if (action === "increase") {
                        quantity++;
                    } else if (action === "decrease" && quantity > 0) {
                        quantity--;
                    }
                    quantityInput.value = quantity;
                    updateComboSelection(comboId, quantity);
                    document.getElementById("nextStep3").disabled = false; // Ensure nextStep3 is always enabled
                    updateInfoPanel(); // Recalculate total price when combos change
                }
            });

            comboContainer.addEventListener("input", (e) => {
                const input = e.target.closest(".quantity-input");
                if (input) {
                    const comboId = parseInt(input.id.split("-")[1]);
                    let quantity = parseInt(input.value) || 0;
                    if (quantity < 0) quantity = 0;
                    input.value = quantity;
                    updateComboSelection(comboId, quantity);
                    document.getElementById("nextStep3").disabled = false; // Ensure nextStep3 is always enabled
                    updateInfoPanel(); // Recalculate total price when combos change
                }
            });
        }

        const nextStep3 = document.getElementById("nextStep3");
        if (nextStep3) {
            nextStep3.addEventListener("click", () => {
                if (validateStep(3)) goToStep(4);
            });
        }

        const backStep3 = document.getElementById("backStep3");
        if (backStep3) {
            backStep3.addEventListener("click", () => goToStep(2));
        }
    }

    function attachStep4Listeners() {
    const promotionCode = document.getElementById("promotionCode");
    if (promotionCode) {
        promotionCode.addEventListener("input", (e) => {
            // Convert input to uppercase in real-time
            e.target.value = e.target.value.toUpperCase();
            document.getElementById("promoError").innerHTML = ""; // Clear error on input
        });
    }

    

    const applyEmailBtn = document.getElementById("applyEmail");
    if (applyEmailBtn) {
        applyEmailBtn.addEventListener("click", () => {
            const email = document.getElementById("memberEmail").value.trim();
            const memberError = document.getElementById("memberError");
            
            if (!email) {
                memberError.innerHTML = '<div class="alert alert-warning" role="alert">Please enter an email address!</div>';
                return;
            }
            
            // Gọi hàm checkMemberStatus với email đã nhập
            checkMemberStatusByEmail(email);
        });
    }

   const applyPromotion = document.getElementById("applyPromotion");
    if (applyPromotion) {
        applyPromotion.addEventListener("click", () => {
            const promoCode = document.getElementById("promotionCode").value.trim();
            const promoError = document.getElementById("promoError");

            // Check if the promo code is already applied
            if (bookingData.promotionCode && promoCode === bookingData.promotionCode) {
                promoError.innerHTML = '<div class="alert alert-danger" role="alert">This promotion code is already applied!</div>';
                return; // Exit the function to prevent reapplying
            }

            applyPromotionCode(promoCode, priceState.originalPrice, promoError);
        });
    }

    const paymentMethod = document.getElementById("paymentMethod");
    if (paymentMethod) {
        paymentMethod.addEventListener("change", (e) => {
            bookingData.paymentMethod = e.target.value;
            document.getElementById("submitBooking").disabled = !validateStep(4);
        });
    }

    const memberEmail = document.getElementById("memberEmail");
    if (memberEmail) {
        memberEmail.addEventListener("input", (e) => {
            bookingData.memberEmail = e.target.value.trim();
            // Chỉ clear error và reset points, không gọi checkMemberStatus
            document.getElementById("memberError").innerHTML = "";
            // Reset member data khi email thay đổi
            if (!bookingData.memberEmail) {
                resetMemberData();
            }
            document.getElementById("submitBooking").disabled = !validateStep(4);
        });
    }

    const usePointsBtn = document.getElementById("usePointsBtn");
    if (usePointsBtn) {
        usePointsBtn.addEventListener("click", () => {
            if (bookingData.memberPoints > 0 && !bookingData.pointsDiscountApplied) {
                applyPointsDiscount();
            }
        });
    }

    // Thêm event listener cho nút remove points
    const removePointsBtn = document.getElementById("removePointsBtn");
    if (removePointsBtn) {
        removePointsBtn.addEventListener("click", () => {
            if (bookingData.pointsDiscountApplied) {                
                removePointsDiscount();
            }
        });
    }

    const submitBookingBtn = document.getElementById("submitBooking");
    if (submitBookingBtn) {
        submitBookingBtn.addEventListener("click", () => {
            if (validateStep(4)) submitBooking();
        });
    }

    const backStep4 = document.getElementById("backStep4");
    if (backStep4) {
        backStep4.addEventListener("click", () => goToStep(3));
    }
}

    function attachStep5Listeners() {
        const newBooking = document.getElementById("newBooking");
        if (newBooking) {
            newBooking.addEventListener("click", resetBooking);
        }
    }

    // Attach listeners for all steps
    attachStep1Listeners();
    attachStep2Listeners();
    attachStep3Listeners();
    attachStep4Listeners();
    attachStep5Listeners();

    // Override goToStep to toggle active step
    window.goToStep = function (step) {
        if (!validateStep(currentStep) && step > currentStep) return; // Prevent proceeding if current step is invalid
        currentStep = step;
        renderStep(step);
    };
}

// Update combo
function updateComboSelection(comboId, quantity) {
    let combo = bookingData.combos.find(c => c.comboId === comboId);
    if (quantity > 0) {
        if (combo) {
            combo.quantity = quantity;
        } else {
            bookingData.combos.push({ comboId, quantity });
        }
    } else {
        bookingData.combos = bookingData.combos.filter(c => c.comboId !== comboId);
    }
    updateInfoPanel(); // Recalculate total price when combo selection changes
}

// Submit booking
async function submitBooking() {
    // Set bookingDate to current date and time
    const requestBody = {
        movieId: bookingData.movieId,
        showDateId: bookingData.showDateId,
        scheduleId: bookingData.scheduleId,
        seatsIds: bookingData.seatsIds,
        usedPoints: bookingData.pointsDiscountApplied ? bookingData.usedPoints : 0, // Use actual points used
        combos: bookingData.combos.length > 0 ? bookingData.combos : null,
        paymentMethod: bookingData.paymentMethod.toUpperCase(),
        accountId: bookingData.accountId || null, // Use accountId if available
        totalPrice: priceState.currentPrice, // Send original amount to let backend handle discount
        promotionId: bookingData.promotionId // Use promotionId instead of promotionCode
    };

    console.log('Request Body:', JSON.stringify(requestBody, null, 2)); // Log the request body

    try {
        const response = await fetch(`/api/employee/booking/create`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                // Add authentication header if required by Swagger (e.g., 'Authorization': 'Bearer your-token-here')
            },
            body: JSON.stringify(requestBody)
        });
        const responseData = await response.json(); // Capture the full response
        console.log('Response:', responseData); // Log the response

        if (response.ok) {
            // Update bookingId from API response
            bookingData.bookingId = responseData.bookingId || Math.floor(Math.random() * 1000000) + 1; // Fallback if not provided
            if (bookingData.paymentMethod.toUpperCase() === "MOMO" && responseData.payUrl) {
                window.location.href = responseData.payUrl; // Redirect to MoMo payment page
            } else {
                goToStep(5); // Proceed to ticket details for other payment methods
            }
        } else {
            alert(`Booking failed: ${responseData.message || "Please try again."}`);
        }
    } catch (error) {
        console.error("Error:", error);
        alert("An error occurred. Please try again.");
    }
}

// Show thông tin vé
function displayTicketDetails() {
    const ticketDetails = document.getElementById("ticketDetails");
    const movie = movieData.find(m => m.movieId === bookingData.movieId)?.movieNameVn || "None";
    const showtime = bookingData.showtimeData.time || "None";
    const selectedSeats = bookingData.seatsIds.length > 0
        ? seats.filter(s => bookingData.seatsIds.includes(s.seatId)).map(s => `(${s.seatType})${s.seatColumn}${s.seatRow}`).join(", ")
        : "None";
    const comboText = bookingData.combos.length > 0
        ? bookingData.combos.map(c => {
            const combo = combosData.find(cd => cd.comboId === c.comboId);
            return combo ? `${combo.comboName} (x${c.quantity})` : "Unknown (x" + c.quantity + ")";
        }).join(", ")
        : "No Combo";

    ticketDetails.innerHTML = `
        <p><strong>Booking ID:</strong> ${bookingData.bookingId || "N/A"}</p>
        <p><strong>Movie:</strong> ${movie}</p>
        <p><strong>Date:</strong> ${bookingData.bookingDate || "N/A"}</p>
        <p><strong>Showtime:</strong> ${showtime}</p>
        <p><strong>Room:</strong> ${bookingData.showtimeData.room || "None"}</p>
        <p><strong>Seats:</strong> ${selectedSeats || "None"}</p>
        <p><strong>Combo:</strong> ${comboText}</p>
        <p><strong>Promotion Code:</strong> ${bookingData.promotionCode || "None"}</p>
        <p><strong>Payment Method:</strong> ${bookingData.paymentMethod || "None"}</p>
        <p><strong>Original Amount:</strong> ${priceState.originalPrice.toLocaleString('vi-VN')} VND</p>
        <p><strong>Discount Amount:</strong> ${(priceState.voucherDiscount + priceState.pointsDiscount).toLocaleString('vi-VN')} VND</p>
        <p><strong>Final Amount:</strong> ${priceState.currentPrice.toLocaleString('vi-VN')} VND</p>
        <p class="text-success">Booking confirmed!</p>
    `;
}

// Reset booking data và UI
function resetBooking() {
    bookingData = {
        movieId: "",
        cinemaRoomId: 0,
        showDateId: 0, // Integer
        scheduleId: 0,
        seatsIds: [],
        paymentMethod: "",
        promotionCode: "",
        promotionId: null, // Reset promotion ID
        combos: [],
        showtimeData: {}, // Clear showtime data
        finalAmount: undefined,
        originalAmount: 0,
        discountAmount: 0,
        bookingId: null, // Reset booking ID
        bookingDate: "", // Reset booking date
        memberEmail: "", // Reset member email
        memberPoints: 0, // Reset member points
        pointsDiscountApplied: false, // Reset points discount flag
        usedPointsValue: 0, // Reset used points value
        usedPoints: 0,
        accountId: null
    };

    // Reset price state
    priceState = {
        originalPrice: 0,
        currentPrice: 0,
        voucherDiscount: 0,
        pointsDiscount: 0
    };

    document.getElementById("movieSelect").value = "";
    document.getElementById("dateSelect").value = "";
    document.getElementById("dateSelect").disabled = true;
    document.getElementById("showtimeSelect").value = "";
    document.getElementById("showtimeSelect").disabled = true;
    document.getElementById("promotionCode").value = "";
    document.getElementById("paymentMethod").value = "";
    document.getElementById("memberEmail").value = "";
    document.getElementById("promoError").innerHTML = ""; // Clear error on reset
    document.getElementById("memberError").innerHTML = ""; // Clear member error on reset
    document.getElementById("memberPoints").textContent = ""; // Clear points display
    document.getElementById("usePointsBtn").style.display = "none"; // Hide points button
    populateCombos(); // Reset all quantities to 0
    updateInfoPanel();
    goToStep(1);
}

async function applyPromotionCode(promoCode, originalAmount, promoError) {
    try {
        const response = await fetch(`/api/booking/apply-promotion`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                promotionCode: promoCode,
                originalAmount: originalAmount,
                accountId: bookingData.accountId    
            })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Không thể áp dụng mã khuyến mãi');
        }

        const data = await response.json();
        console.log('Promotion response:', data); // Debug log

        // Cập nhật theo logic từ payment.js
        bookingData.promotionCode = promoCode; // Lưu mã khuyến mãi đã nhập
        bookingData.promotionId = data.promotionId;
        bookingData.originalAmount = data.originalAmount;
        bookingData.discountAmount = data.discountAmount;
        bookingData.finalAmount = data.finalAmount;

        // Cập nhật price state
        priceState.voucherDiscount = data.discountAmount;
        priceState.currentPrice = priceState.originalPrice - priceState.voucherDiscount - priceState.pointsDiscount;
        
        // Đảm bảo giá không âm
        priceState.currentPrice = Math.max(0, priceState.currentPrice);
        
        // Cập nhật booking data
        bookingData.totalPrice = priceState.currentPrice;

        // Clear any previous error and update UI
        promoError.innerHTML = `<div class="alert alert-success" role="alert">
            Promotion applied successfully! Discount: ${data.discountAmount.toLocaleString('vi-VN')}đ
        </div>`;
        
        updateInfoPanel();
        updatePromoCodeDisplay();
        document.getElementById("submitBooking").disabled = !validateStep(4);
    } catch (error) {
        console.error("Error applying promotion:", error);
        promoError.innerHTML = `<div class="alert alert-danger" role="alert">${error.message}</div>`;
        
        // Reset promotion data
        bookingData.promotionCode = "";
        bookingData.promotionId = null;
        priceState.voucherDiscount = 0;
        priceState.currentPrice = priceState.originalPrice - priceState.pointsDiscount;
        bookingData.totalPrice = priceState.currentPrice;
        
        updateInfoPanel();
        document.getElementById("submitBooking").disabled = !validateStep(4);
    }
}

// Check member status and fetch points
async function checkMemberStatusByEmail(email) {
    const memberError = document.getElementById("memberError");
    const applyEmailBtn = document.getElementById("applyEmail");

    // Kiểm tra xem có phải đang hủy email không
    if (applyEmailBtn.classList.contains("btn-danger")) {
        // Nếu đang ở trạng thái "Hủy", reset lại form
        resetMemberData();
        document.getElementById("memberEmail").value = "";
        document.getElementById("memberEmail").disabled = false;
        applyEmailBtn.textContent = "Tìm kiếm";
        applyEmailBtn.disabled = false;
        applyEmailBtn.classList.remove("btn-danger");
        applyEmailBtn.classList.add("btn-primary");
        memberError.innerHTML = "";
        return;
    }

    // Hiển thị trạng thái đang tìm kiếm
    applyEmailBtn.disabled = true;
    applyEmailBtn.textContent = "Đang tìm kiếm...";
    memberError.innerHTML = '<div class="alert alert-info" role="alert">Đang tìm kiếm thành viên...</div>';

    try {
        const response = await fetch(`/api/employee/points?email=${encodeURIComponent(email)}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            }
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Không tìm thấy tài khoản');
        }

        const data = await response.json();
        console.log('Member points response:', data); // Debug log

        if (data.points !== undefined) {
            bookingData.memberPoints = data.points;
            bookingData.accountId = data.accountId; // Store account ID for future reference
            bookingData.memberEmail = email; // Lưu lại email đã tìm
            
            // Hiển thị thông báo thành công
            memberError.innerHTML = `<div class="alert alert-success" role="alert">
                Tìm thấy thành viên! Email: ${email}
            </div>`;
            
            // Đổi nút thành "Hủy" và cập nhật style
            applyEmailBtn.textContent = "Hủy";
            applyEmailBtn.disabled = false;
            applyEmailBtn.classList.remove("btn-primary");
            applyEmailBtn.classList.add("btn-danger");
            
            // Khóa input email để ngăn chỉnh sửa
            document.getElementById("memberEmail").disabled = true;
            
            // Cập nhật hiển thị điểm
            updatePointsDisplay();
            console.log('Booking data after fetching points:', bookingData); // Debug log
        } else {
            throw new Error("Không tìm thấy thông tin điểm");
        }
    } catch (error) {
        console.error("Error checking member status:", error);
        memberError.innerHTML = `<div class="alert alert-danger" role="alert">${error.message || 'Tài khoản không tồn tại!'}</div>`;
        bookingData.memberPoints = 0;
        bookingData.accountId = null;
        
        // Khôi phục nút tìm kiếm
        applyEmailBtn.textContent = "Tìm kiếm";
        applyEmailBtn.disabled = false;
        
        // Cập nhật hiển thị điểm (sẽ hiển thị không có điểm)
        updatePointsDisplay();
    }
}

// Cải thiện Apply points discount dựa trên logic từ payment.js
function applyPointsDiscount() {
    if (bookingData.memberPoints <= 0 || bookingData.pointsDiscountApplied) {
        return;
    }

    // Tính số tiền tối đa có thể giảm (30% của giá gốc)
    const maxDiscountAmount = Math.floor(priceState.originalPrice * 0.3); // 30% của giá gốc
    
    // Tính số tiền tối đa từ điểm có sẵn (1 điểm = 10đ)
    const maxPointsValue = bookingData.memberPoints * 10;
    
    // Lấy giá trị nhỏ hơn giữa maxDiscountAmount và maxPointsValue
    const actualDiscountAmount = Math.min(maxPointsValue, maxDiscountAmount);
    
    // Tính số điểm thực tế sẽ sử dụng
    const usedPoints = Math.floor(actualDiscountAmount / 10);

    if (actualDiscountAmount <= 0 || usedPoints <= 0) {
        alert("Cannot apply points discount. Either insufficient points or discount amount too small.");
        return;
    }

    // Cập nhật price state
    priceState.pointsDiscount = actualDiscountAmount;
    priceState.currentPrice = priceState.originalPrice - priceState.voucherDiscount - priceState.pointsDiscount;
    
    // Đảm bảo giá không âm
    priceState.currentPrice = Math.max(0, priceState.currentPrice);

    // Cập nhật booking data
    bookingData.pointsDiscountApplied = true;
    bookingData.usedPoints = usedPoints;
    bookingData.totalPrice = priceState.currentPrice;

    // Cập nhật UI
    updateInfoPanel();
    updatePointsDisplay();
    document.getElementById("submitBooking").disabled = !validateStep(4);

    console.log(`Applied ${usedPoints.toLocaleString('vi-VN')} points for ${actualDiscountAmount.toLocaleString('vi-VN')}đ`);
  
}