
const selectedSeats = [];

let timeLeft = 10 * 60; // 10 phút
let nestedMap = new Map();

let bookedSeatList = [];
let bookedSeatNameList = [];
let totalSeats = 0;

// Giá ghế mặc định (dùng khi không lấy được từ API)
const normalSeatPrice = 50000;
const vipSeatPrice = 65000;


const movieId = new URLSearchParams(window.location.search).get(
    "movieId")
const showDateId = new URLSearchParams(window.location.search).get(
    "showDateId")
const scheduleId = new URLSearchParams(window.location.search).get(
    "scheduleId")

document.addEventListener('DOMContentLoaded', function () {
    // Khởi tạo bộ đếm thời gian
    initTimer();

    // Lấy thông tin phim từ API
    fetchMovieDetails();

    // Lấy thông tin ghế từ API
    fetchSeatData();
});

function checkSavedSeats() {
    // Lấy dữ liệu booking từ sessionStorage
    const savedBookingData = sessionStorage.getItem('bookingData');

    if (savedBookingData) {
        const bookingData = JSON.parse(savedBookingData);

        // Kiểm tra xem bookingData có cùng phim/lịch chiếu không
        if (bookingData.movieId == movieId &&
            bookingData.showDateId == showDateId &&
            bookingData.scheduleId == scheduleId &&
            bookingData.seats &&
            bookingData.seats.length > 0) {

            console.log('Đã tìm thấy ghế đã chọn trước đó:', bookingData.seats);

            // Chờ cho ghế được tạo xong rồi mới khôi phục
            const waitForSeatsInterval = setInterval(() => {
                if (document.querySelectorAll('button.seat').length > 0) {
                    clearInterval(waitForSeatsInterval);
                    // Đợi thêm một chút để đảm bảo tất cả ghế đã được tạo
                    setTimeout(() => {
                        restoreSavedSeats(bookingData.seats);
                    }, 500);
                }
            }, 100);
        }
    }
}


function restoreSavedSeats(savedSeats) {
    if (!savedSeats || savedSeats.length === 0) return;

    console.log('Restoring saved seats:', savedSeats);

    // Xóa dữ liệu ghế đã chọn hiện tại
    bookedSeatList = [];
    bookedSeatNameList = [];
    totalSeats = 0;

    // Xóa các ghế đã hiển thị trong danh sách (nếu có)
    const selectedSeatListElement = document.getElementById('selected-seats-list');
    const emptyMessage = document.getElementById('empty-seat-list');

    // Xóa tất cả các phần tử con trừ thông báo "chưa chọn ghế nào"
    Array.from(selectedSeatListElement.children).forEach(child => {
        if (child.id !== 'empty-seat-list') {
            selectedSeatListElement.removeChild(child);
        }
    });

    // Đảm bảo không có ghế nào có class 'selected'
    document.querySelectorAll('.seat.selected').forEach(seat => {
        seat.classList.remove('selected');
    });

    // Đối với mỗi ghế đã lưu, gọi lại hàm addSeat để thêm ghế vào danh sách đã chọn
    let restoredCount = 0;

    savedSeats.forEach(savedSeat => {
        // Cần đợi cho sơ đồ ghế được tạo trước khi khôi phục
        const seatButton = document.querySelector(`button[value="${savedSeat.seatId}"]`);

        if (seatButton && !seatButton.classList.contains('occupied')) {
            console.log('Found seat to restore:', seatButton.value, seatButton.textContent);
            // Sử dụng hàm addSeat để thêm ghế này vào danh sách đã chọn
            addSeat(seatButton);
            restoredCount++;
        } else {
            console.log('Seat not found or occupied:', savedSeat.seatId);
        }
    });
    // Cập nhật trạng thái nút tiếp tục
    updateContinueButton();
}

function initTimer() {
    const timerElement = document.getElementById('timer');
    if (!timerElement) return;

    // Cập nhật đếm ngược mỗi giây
    const countdown = setInterval(() => {
        timeLeft--;

        if (timeLeft <= 0) {
            clearInterval(countdown);
            alert('Hết thời gian giữ ghế! Vui lòng chọn lại.');
            setTimeout(() => {
                location.reload();
            }, 3000);
        }

        const minutes = Math.floor(timeLeft / 60);
        const seconds = timeLeft % 60;
        timerElement.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    }, 1000);
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
            // Nếu có lỗi, hiển thị thông báo cho người dùng
            showAlert('Không thể tải thông tin phim. Vui lòng thử lại sau.', 'danger');
        });
}

function updateMovieInfo(data) {
    // Cập nhật tiêu đề phim
    const movieTitle = document.querySelector('.movie-details h1');
    if (movieTitle && data.movieName) {
        movieTitle.textContent = data.movieName;
        // Cập nhật tiêu đề trang
        document.title = `Đặt vé: ${data.movieName} - FCine`;
    }

    // Cập nhật poster phim
    const moviePoster = document.querySelector('.movie-poster img');
    if (moviePoster && data.posterUrl) {
        moviePoster.src = data.posterUrl;
        moviePoster.alt = data.movieName || 'Movie Poster';
    }

    // Cập nhật ngày chiếu
    const showDate = document.querySelector('.meta-item:nth-child(1) .meta-value');
    if (showDate && data.showDate) {
        showDate.textContent = "Ngày: " + formatDate(data.showDate);
    }

    // Cập nhật suất chiếu
    const showtime = document.querySelector('.meta-item:nth-child(2) .meta-value');
    if (showtime && data.startTime) {
        showtime.textContent = "Suất chiếu: " + data.startTime;
    }

    // Cập nhật phòng chiếu
    const room = document.querySelector('.meta-item:nth-child(3) .meta-value');
    if (room && data.roomName) {
        room.textContent = "Rạp: " + `${data.roomName} - ${data.movieFormat || '2D'}`;
    }
}

// Hàm định dạng ngày tháng
function formatDate(dateString) {
    const date = new Date(dateString);
    const dayOfWeek = ['Chủ nhật', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7'];
    const day = date.getDate();
    const month = date.getMonth() + 1;
    const year = date.getFullYear();

    return `${dayOfWeek[date.getDay()]}, ${day}/${month}/${year}`;
}

function addSeat(button) {
    const selectedSeatListElement = document.getElementById('selected-seats-list');
    let seatRow, seatColumn;

    // Lấy thông tin hàng và cột từ data attributes thay vì từ value
    seatRow = button.dataset.row;
    seatColumn = button.dataset.col;

    if (!button.classList.contains('selected')) {
        if (bookedSeatList.length >= 8) {
            alert('Bạn chỉ có thể chọn tối đa 8 ghế!', 3000);
            return;
        }

        button.classList.toggle('selected');
        bookedSeatList.push(button.value);
        bookedSeatNameList.push(button.textContent);

        const seatItem = document.createElement('div');
        seatItem.className = 'seat-item';

        const seatInform = document.createElement('div');
        seatInform.className = 'seat-info';

        const divSeatTypeIndicator = document.createElement('div');
        divSeatTypeIndicator.className = 'seat-type-indicator';
        divSeatTypeIndicator.style.background = button.classList.contains('vip') ? '#f59e0b' : '#6b7280';

        const spanSeatType = document.createElement('span');
        spanSeatType.textContent = `Ghế ${seatColumn}${seatRow} (${button.classList.contains('vip') ? 'VIP' : 'STANDARD'})`;
        seatInform.appendChild(divSeatTypeIndicator);
        seatInform.appendChild(spanSeatType);
        seatItem.appendChild(seatInform);

        const spanSeatPrice = document.createElement('span');
        const singlePrice = nestedMap.get(Number(seatRow)).get(seatColumn).seatPrice;
        //singlePrice = 100000;
        spanSeatPrice.textContent = Number(singlePrice).toLocaleString('vi-VN') + 'đ';
        totalSeats += Number(singlePrice);

        seatItem.id = button.value;
        seatItem.appendChild(spanSeatPrice);
        selectedSeatListElement.appendChild(seatItem);

        console.log("Booked Seat List:" + bookedSeatList);
        console.log("Ghế đã chọn:", bookedSeatNameList);

        if (bookedSeatList.length === 1) {
            document.getElementById('empty-seat-list').style.display = 'none';
            //return;
        }
    } else {
        button.classList.remove('selected');
        const index = bookedSeatList.indexOf(button.value);

        if (index !== -1) {
            bookedSeatList.splice(index, 1);
            bookedSeatNameList.splice(index, 1);
            console.log("Ghế sau khi bỏ chọn:", bookedSeatList);
            console.log("Tên ghế sau khi bỏ chọn:", bookedSeatNameList);
        }

        selectedSeatListElement.removeChild(document.getElementById(button.value));
        if (bookedSeatList.length === 0) {
            document.getElementById('empty-seat-list').style.display = 'block';
        }

        const singlePrice = nestedMap.get(Number(seatRow)).get(seatColumn).seatPrice;
        totalSeats -= Number(singlePrice);
    }
    const totalPriceElement = document.getElementById('total-price');
    if (totalPriceElement) {
        totalPriceElement.textContent = totalSeats.toLocaleString('vi-VN') + 'đ';
    }
    // Cập nhật trạng thái nút tiếp tục sau mỗi lần chọn/bỏ chọn ghế
    updateContinueButton();
}

function updateBookingPanel() {
    const seatsList = document.getElementById('selected-seats-list');
    const continueBtn = document.getElementById('continue-btn');

    if (selectedSeats.length === 0) {
        seatsList.innerHTML = '<div style="text-align: center; color: #666; font-style: italic; padding: 20px;">Chưa chọn ghế nào</div>';
        continueBtn.disabled = true;
    } else {
        // Check if user has selected the required number of seats
        const isCompleteSelection = selectedSeats.length === requiredSeats;
        continueBtn.disabled = !isCompleteSelection;

        if (!isCompleteSelection) {
            continueBtn.innerHTML = `Vui lòng chọn ${requiredSeats - selectedSeats.length} ghế nữa`;
        } else {
            continueBtn.innerHTML = 'Tiếp tục thanh toán';
        }
        let seatsHTML = '';
        let normalCount = 0;
        let vipCount = 0;

        selectedSeats.forEach(seatId => {
            const seatElement = document.querySelector(`[data-seat="${seatId}"]`);
            const isVip = seatElement.classList.contains('vip');
            const seatType = isVip ? 'VIP' : 'STANDARD';
            const seatPrice = seatElement.dataset.price;
            if (isVip) vipCount++;
            else normalCount++;

            seatsHTML += `
                <div class="seat-item">
                    <div class="seat-info">
                        <div class="seat-type-indicator" style="background: ${isVip ? '#f59e0b' : '#6b7280'}"></div>
                        <span>Ghế ${seatId} (${seatType})</span>
                    </div>
                    <span>${seatPrice.toLocaleString('vi-VN')}đ</span>
                </div>
            `;
        });

        seatsList.innerHTML = seatsHTML;

        // Update price summary
        const normalTotal = normalCount * normalSeatPrice;
        const vipTotal = vipCount * vipSeatPrice;
        const grandTotal = normalTotal + vipTotal;

        document.getElementById('total-price').textContent = `${grandTotal.toLocaleString('vi-VN')}đ`;

        // Update price row labels
        document.querySelector('.price-row:nth-child(1) span:first-child').textContent = `Ghế STANDARD (${normalCount}x)`;
        document.querySelector('.price-row:nth-child(2) span:first-child').textContent = `Ghế VIP (${vipCount}x)`;
    }
}

// Cập nhật trạng thái nút tiếp tục
function updateContinueButton() {
    const continueBtn = document.getElementById('continue-btn');
    if (!continueBtn) return;

    if (bookedSeatList.length > 0) {
        continueBtn.disabled = false;
        continueBtn.textContent = `Tiếp tục với ${bookedSeatList.length} ghế`;
    } else {
        continueBtn.disabled = true;
        continueBtn.textContent = 'Vui lòng chọn ghế';
    }
}


function fetchSeatData() {
    const seatTable = document.getElementById("seat-table");
    if (!seatTable) {
        console.error("Element 'seat-table' not found");
        return;
    }

    // Hiển thị trạng thái đang tải
    seatTable.innerHTML = '<div class="loading-seats">Đang tải sơ đồ ghế...</div>';

    fetch(`/api/booking/seats?movieId=${movieId}&showDateId=${showDateId}&scheduleId=${scheduleId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            if (data && Object.keys(data).length > 0) {
                genSeatMap(data);
                // Kiểm tra và khôi phục ghế đã lưu sau khi sơ đồ ghế được tạo
                checkSavedSeats();
            } else {
                console.warn('No seat data available for this schedule.');
                const seatTable = document.getElementById('seat-table');
                seatTable.innerHTML = '<tr><td colspan="100%" class="text-center">Không có ghế nào để chọn</td></tr>';
            }

        })
        .catch(error => console.error('Error fetching booking data:', error));
}

function genSeatMap(data) {
    nestedMap = new Map();
    const seatTable = document.getElementById("seat-table");
    if (!seatTable) return;

    // Xóa nội dung hiện tại
    seatTable.innerHTML = '';

    // Tạo nested map từ dữ liệu API
    for (const [row, seats] of Object.entries(data)) {
        const seatMap = new Map(Object.entries(seats));
        nestedMap.set(Number(row), seatMap);
    }

    console.log('Seat data loaded:', nestedMap);

    // Tạo header với column labels (chỉ một lần)
    const headerRow = document.createElement("div");
    headerRow.className = "header-row";

    // Thêm ô trống cho vị trí row-label
    const emptyCell = document.createElement("div");
    emptyCell.className = "row-label";
    emptyCell.textContent = "";
    headerRow.appendChild(emptyCell);

    // Lấy danh sách các cột từ hàng đầu tiên để tạo header
    if (nestedMap.size > 0) {
        const firstRow = nestedMap.values().next().value;
        const sortedCols = Array.from(firstRow.keys()).sort((a, b) => parseInt(a) - parseInt(b));

        // Sử dụng index để tạo chữ cái A, B, C...
        sortedCols.forEach((col, index) => {
            const colLabel = document.createElement("div");
            colLabel.className = "col-label";
            colLabel.textContent = String.fromCharCode(65 + index); // 65 = 'A', tăng dần theo index
            headerRow.appendChild(colLabel);
        });
    }

    seatTable.appendChild(headerRow);

    // Tạo bản đồ chỗ ngồi
    for (const [row, seats] of nestedMap) {
        const rowElement = document.createElement("div");
        rowElement.className = "row";

        // Thêm nhãn hàng (số thứ tự)
        const rowLabel = document.createElement("div");
        rowLabel.className = "row-label";
        rowLabel.textContent = row; // Hiển thị số thứ tự
        rowElement.appendChild(rowLabel);

        // Sắp xếp ghế theo cột
        const sortedSeats = Array.from(seats.entries())
            .sort((a, b) => parseInt(a[0]) - parseInt(b[0]));

        for (const [col, seat] of sortedSeats) {
            const seatButton = document.createElement("button");
            seatButton.type = "button";
            seatButton.className = "seat";
            seatButton.textContent = seat.seatColumn + seat.seatRow;
            seatButton.value = seat.seatId;

            // Lưu thông tin bổ sung vào data attributes
            seatButton.dataset.row = seat.seatRow;
            seatButton.dataset.col = seat.seatColumn;
            seatButton.dataset.type = seat.seatType;
            seatButton.dataset.price = seat.seatPrice;

            if (seat.seatType === "VIP") {
                seatButton.classList.add("vip");
            }

            if (seat.seatStatus === 1) {
                seatButton.classList.add("occupied");
            } else {
                seatButton.classList.add("available");
                seatButton.onclick = function () {
                    addSeat(this);
                };
            }

            rowElement.appendChild(seatButton);
        }
        seatTable.appendChild(rowElement);
    }
}


// Hàm kiểm tra xem có để trống 1 ghế không
function checkSingleEmptySeats() {
    const invalidSeats = [];

    console.log('Bắt đầu kiểm tra ghế trống. Ghế đã chọn:', bookedSeatList);

    // Duyệt qua tất cả các ghế đã chọn
    bookedSeatList.forEach(seatId => {
        const seatButton = document.querySelector(`button[value="${seatId}"]`);
        if (!seatButton) return;

        const row = parseInt(seatButton.dataset.row); // Hàng là số
        const col = seatButton.dataset.col; // Cột là chữ cái (A, B, C...)

        console.log(`Kiểm tra ghế ${seatButton.textContent} - Hàng: ${row}, Cột: ${col}`);

        // Lấy tất cả ghế trong cùng hàng để kiểm tra
        const seatsInRow = nestedMap.get(row);
        if (!seatsInRow) return;

        const allColsInRow = Array.from(seatsInRow.keys()).sort();
        const currentColIndex = allColsInRow.indexOf(col);

        // Kiểm tra ghế bên trái
        if (currentColIndex > 0) {
            const leftCol = allColsInRow[currentColIndex - 1];
            const leftSeat = findSeatByRowCol(row, leftCol);

            if (leftSeat && leftSeat.classList.contains('available') &&
                !leftSeat.classList.contains('selected')) {

                // Kiểm tra ghế bên trái của ghế trái (nếu có)
                let hasLeftNeighbor = false;
                if (currentColIndex > 1) {
                    const leftLeftCol = allColsInRow[currentColIndex - 2];
                    const leftLeftSeat = findSeatByRowCol(row, leftLeftCol);
                    if (leftLeftSeat && (leftLeftSeat.classList.contains('occupied') ||
                        leftLeftSeat.classList.contains('selected'))) {
                        hasLeftNeighbor = true;
                    }
                }

                // Nếu ghế bên trái trống và có ghế kế bên nó đã được đặt
                if (hasLeftNeighbor) {
                    invalidSeats.push({
                        selected: seatButton.textContent,
                        empty: leftSeat.textContent,
                        position: 'bên trái'
                    });
                    console.log(`Phát hiện ghế trống bên trái: ${leftSeat.textContent}`);
                }
            }
        }

        // Kiểm tra ghế bên phải
        if (currentColIndex < allColsInRow.length - 1) {
            const rightCol = allColsInRow[currentColIndex + 1];
            const rightSeat = findSeatByRowCol(row, rightCol);

            if (rightSeat && rightSeat.classList.contains('available') &&
                !rightSeat.classList.contains('selected')) {

                // Kiểm tra ghế bên phải của ghế phải (nếu có)
                let hasRightNeighbor = false;
                if (currentColIndex < allColsInRow.length - 2) {
                    const rightRightCol = allColsInRow[currentColIndex + 2];
                    const rightRightSeat = findSeatByRowCol(row, rightRightCol);
                    if (rightRightSeat && (rightRightSeat.classList.contains('occupied') ||
                        rightRightSeat.classList.contains('selected'))) {
                        hasRightNeighbor = true;
                    }
                }

                // Nếu ghế bên phải trống và có ghế kế bên nó đã được đặt
                if (hasRightNeighbor) {
                    invalidSeats.push({
                        selected: seatButton.textContent,
                        empty: rightSeat.textContent,
                        position: 'bên phải'
                    });
                    console.log(`Phát hiện ghế trống bên phải: ${rightSeat.textContent}`);
                }
            }
        }
    });

    // Kiểm tra ghế trống ở giữa các ghế đã chọn trong cùng hàng
    const selectedSeatsInRow = {};
    bookedSeatList.forEach(seatId => {
        const seatButton = document.querySelector(`button[value="${seatId}"]`);
        if (!seatButton) return;

        const row = parseInt(seatButton.dataset.row);
        const col = seatButton.dataset.col;

        if (!selectedSeatsInRow[row]) {
            selectedSeatsInRow[row] = [];
        }
        selectedSeatsInRow[row].push(col);
    });

    // Kiểm tra từng hàng xem có ghế trống ở giữa không
    Object.keys(selectedSeatsInRow).forEach(rowKey => {
        const row = parseInt(rowKey);
        const selectedCols = selectedSeatsInRow[rowKey];

        if (selectedCols.length < 2) return; // Cần ít nhất 2 ghế để có khoảng giữa

        const seatsInRow = nestedMap.get(row);
        if (!seatsInRow) return;

        const allColsInRow = Array.from(seatsInRow.keys()).sort();

        // Sắp xếp các cột đã chọn theo thứ tự trong hàng
        const sortedSelectedCols = selectedCols.sort((a, b) => {
            return allColsInRow.indexOf(a) - allColsInRow.indexOf(b);
        });

        // Kiểm tra khoảng cách giữa các ghế đã chọn
        for (let i = 0; i < sortedSelectedCols.length - 1; i++) {
            const currentCol = sortedSelectedCols[i];
            const nextCol = sortedSelectedCols[i + 1];

            const currentIndex = allColsInRow.indexOf(currentCol);
            const nextIndex = allColsInRow.indexOf(nextCol);

            // Nếu có đúng 1 ghế trống ở giữa
            if (nextIndex - currentIndex === 2) {
                const emptyCol = allColsInRow[currentIndex + 1];
                const emptySeat = findSeatByRowCol(row, emptyCol);

                if (emptySeat && emptySeat.classList.contains('available') &&
                    !emptySeat.classList.contains('selected')) {
                    invalidSeats.push({
                        selected: `${findSeatByRowCol(row, currentCol).textContent} và ${findSeatByRowCol(row, nextCol).textContent}`,
                        empty: emptySeat.textContent,
                        position: 'ở giữa'
                    });
                    console.log(`Phát hiện ghế trống ở giữa: ${emptySeat.textContent}`);
                }
            }
        }
    });

    console.log('Kết quả kiểm tra:', invalidSeats);
    return invalidSeats;
}

// Hàm tìm ghế theo hàng và cột
function findSeatByRowCol(row, col) {
    return document.querySelector(`button[data-row="${row}"][data-col="${col}"]`);
}

document.getElementById('continue-btn').addEventListener('click', function () {
    if (bookedSeatList.length > 0) {
        // Kiểm tra logic ghế trống
        const invalidSeats = checkSingleEmptySeats();

        if (invalidSeats.length > 0) {
            let errorMessage = 'Không được để trống 1 ghế! ';

            if (invalidSeats.length === 1) {
                const invalid = invalidSeats[0];
                errorMessage += `Ghế ${invalid.selected} để trống ghế ${invalid.empty} ${invalid.position}.`;
            } else {
                errorMessage += `Phát hiện ${invalidSeats.length} vị trí không hợp lệ.`;
            }

            errorMessage += ' Vui lòng chọn lại!';

            if (typeof alertSystem !== 'undefined') {
                alertSystem.warning(errorMessage, 4000);
            } else {
                alert(errorMessage);
            }
            return;
        }


        const seats = bookedSeatList.map((seatId) => {
            const seatButton = document.querySelector(`button[value="${seatId}"]`);
            if (!seatButton) return null;

            const row = seatButton.dataset.row;
            const col = seatButton.dataset.col;
            const isVip = seatButton.classList.contains('vip');

            // Lấy giá ghế
            let seatPrice;
            try {
                seatPrice = Number(seatButton.dataset.price) ||
                    nestedMap.get(Number(row))?.get(col)?.seatPrice ||
                    (isVip ? vipSeatPrice : normalSeatPrice);
            } catch (error) {
                seatPrice = isVip ? vipSeatPrice : normalSeatPrice;
            }

            return {
                seatId: seatId,
                seatRow: row,
                seatColumn: col,
                seatName: `${col}${row}`,
                type: isVip ? 'VIP' : 'STANDARD',
                price: seatPrice
            };
        }).filter(seat => seat !== null);

        // Lấy giá vé từ tổng ghế đã chọn
        const ticketPrice = seats.reduce((total, seat) => total + seat.price, 0);
        // Tạo đối tượng dữ liệu đầy đủ
        const bookingData = {
            seats: seats,
            ticketPrice: ticketPrice,
            movieId: movieId,
            showDateId: showDateId,
            scheduleId: scheduleId
        };

        // Lưu dữ liệu vào sessionStorage
        sessionStorage.setItem('bookingData', JSON.stringify(bookingData));

        // Hiển thị thông báo
        if (typeof alertSystem !== 'undefined') {
            alertSystem.success('Đang chuyển đến trang chọn combo...', 1000);
        }

        // Chuyển đến trang chọn combo với tham số
        setTimeout(() => {
            window.location.href = `/combo-selection`;
        }, 1000);
    } else {
        // Hiển thị thông báo
        if (typeof alertSystem !== 'undefined') {
            alertSystem.warning('Vui lòng chọn ít nhất một ghế trước khi tiếp tục!', 3000);
        } else {
            alert('Vui lòng chọn ít nhất một ghế trước khi tiếp tục!');
        }
    }


});


// Cập nhật trạng thái nút tiếp tục
function updateContinueButton() {
    const continueBtn = document.getElementById('continue-btn');
    if (!continueBtn) return;

    if (bookedSeatList.length > 0) {
        continueBtn.disabled = false;
        continueBtn.textContent = `Tiếp tục với ${bookedSeatList.length} ghế`;
    } else {
        continueBtn.disabled = true;
        continueBtn.textContent = 'Vui lòng chọn ghế';
    }
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