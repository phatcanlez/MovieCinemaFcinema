/**
 * Danh sách các loại ghế - chỉ bao gồm STANDARD và VIP
 */
const SEAT_TYPES = ['STANDARD', 'VIP'];

/**
 * Xử lý click trái - đổi loại ghế giữa STANDARD và VIP
 */
function handleLeftClick(button) {
    const row = button.getAttribute('data-row');
    const col = button.getAttribute('data-col');
    const seatTypeInput = document.getElementById(`seatType_${row}_${col}`);
    const isActiveInput = document.getElementById(`isActive_${row}_${col}`);
    const currentSeatType = seatTypeInput.value;
    const currentIsActive = isActiveInput.value === 'true';

    if (currentIsActive) {
        const newSeatType = currentSeatType === 'STANDARD' ? 'VIP' : 'STANDARD';
        seatTypeInput.value = newSeatType;
        updateButtonUI(button, newSeatType, currentIsActive, row, col);
    }
}

/**
 * Xử lý click phải - đổi trạng thái active/inactive
 */
function handleRightClick(button, event) {
    event.preventDefault();
    const row = button.getAttribute('data-row');
    const col = button.getAttribute('data-col');
    const seatTypeInput = document.getElementById(`seatType_${row}_${col}`);
    const isActiveInput = document.getElementById(`isActive_${row}_${col}`);
    const currentSeatType = seatTypeInput.value;
    const currentIsActive = isActiveInput.value === 'true';
    const newIsActive = !currentIsActive;
    isActiveInput.value = newIsActive;
    updateButtonUI(button, currentSeatType, newIsActive, row, col);
}

/**
 * Cập nhật giao diện button
 */
function updateButtonUI(button, seatType, isActive, row, col) {
    // Only display seat label (e.g., "A1") without status text
    button.textContent = col + row;
    button.className = 'btn ';
    if (isActive) {
        if (seatType === 'VIP') {
            button.className += 'vip';
        } else if (seatType === 'STANDARD') {
            button.className += 'normal';
        }
    } else {
        button.className += 'inactive';
    }
}

/**
 * Hàm legacy để tương thích với code cũ (nếu cần)
 */
function toggleSeat(button) {
    handleLeftClick(button);
}

// Thêm event listener cho keyboard shortcuts (tùy chọn)
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        const allButtons = document.querySelectorAll('.btn[data-row]');
        allButtons.forEach(button => {
            const row = button.getAttribute('data-row');
            const col = button.getAttribute('data-col');
            const seatTypeInput = document.getElementById(`seatType_${row}_${col}`);
            const isActiveInput = document.getElementById(`isActive_${row}_${col}`);
            seatTypeInput.value = 'STANDARD';
            isActiveInput.value = 'true';
            updateButtonUI(button, 'STANDARD', true, row, col);
        });
    }
});

// Thêm và điều chỉnh screen area động dựa trên độ rộng của table
document.querySelector('#createRoomForm').addEventListener('submit', function(e) {
    setTimeout(() => {
        const seatMapForm = document.getElementById('seatForm');
        if (seatMapForm) {
            const seatTable = document.getElementById('seatTable');
            const dynamicScreen = document.getElementById('dynamicScreen');
            const vipRowInput = document.getElementById('vipRow').value;
            if (seatTable && !dynamicScreen.textContent) {
                // Lấy độ rộng của table dựa trên số cột
                const columns = seatTable.querySelectorAll('tr:first-child td');
                const totalWidth = columns.length * (80 + 10); // 80px width của btn + 10px border-spacing
                dynamicScreen.style.width = `${totalWidth}px`;
                dynamicScreen.textContent = 'Screen';
                dynamicScreen.style.margin = '0 auto'; // Căn giữa

            }
        }
    }, 100); // Delay để đảm bảo Thymeleaf render xong
});

console.log('Seat Management System loaded successfully!');
console.log('Available seat types:', SEAT_TYPES);

setTimeout(() => {
        const alerts = document.querySelectorAll('.alert');
        if (alerts.length > 0 && typeof bootstrap !== 'undefined') {
            alerts.forEach(alert => {
                if (bootstrap.Alert) {
                    try {
                        const bsAlert = new bootstrap.Alert(alert);
                        bsAlert.close();
                    } catch (error) {
                        console.error("Error closing alert:", error);
                    }
                }
            });
        }
    }, 3000);