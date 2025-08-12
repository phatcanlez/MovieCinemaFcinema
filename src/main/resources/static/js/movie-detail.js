// Xóa lớp 'loading' khi trang đã tải xong
document.addEventListener("DOMContentLoaded", function () {
    const loadingElements = document.querySelectorAll(".loading");

    setTimeout(() => {
        loadingElements.forEach((element) => {
            element.classList.remove("loading");
        });
    }, 500);
});

// Animation on load
document.addEventListener('DOMContentLoaded', function () {
    const loadingElements = document.querySelectorAll('.loading');
    loadingElements.forEach((element, index) => {
        setTimeout(() => {
            element.style.animationDelay = `${index * 0.1}s`;
        }, 100);
    });
});

// Booking functionality
let selectedBookingData = {
    date: null,
    showtime: null,
    type: null,
    price: 0,
    cinemaRoomId: null,
    showDateId: null,
    scheduleId: null
};

function openBookingModal(button) {
    const modal = document.getElementById('bookingModal');
    modal.style.display = 'block';
    document.body.style.overflow = 'hidden';

    const dateGrid = document.getElementById('dateGrid');
    const dayNames = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'];
    let html = '';

    for (let i = 0; i < 10; i++) {
        const currentDate = new Date();
        currentDate.setDate(currentDate.getDate() + i);

        // Convert sang giờ Việt Nam
        const vnDate = new Date(currentDate.toLocaleString("en-US", { timeZone: "Asia/Ho_Chi_Minh" }));

        const day = dayNames[vnDate.getDay()];
        const date = String(vnDate.getDate()).padStart(2, '0');
        const month = String(vnDate.getMonth() + 1).padStart(2, '0');
        const year = vnDate.getFullYear();

        const fullDate = `${year}-${month}-${date}`; // yyyy-mm-dd theo giờ Việt Nam


        html += `
            <button class="date-option" onclick="selectDate(this, '${fullDate}')">
                <div class="date-day">${day}</div>
                <div class="date-date">${date}/${month}</div>
            </button>
        `;
    }

    dateGrid.innerHTML = html;
}

function closeBookingModal() {
    const modal = document.getElementById('bookingModal');
    modal.style.display = 'none';
    document.body.style.overflow = 'auto';

    // Reset selections
    selectedBookingData = { date: null, showtime: null, type: null, price: 0, cinemaRoomId: null, showDateId: null, scheduleId: null };
    updateBookingSummary();

    // Remove selected classes
    document.querySelectorAll('.date-option.selected').forEach(el => el.classList.remove('selected'));
    document.querySelectorAll('.showtime-option.selected').forEach(el => el.classList.remove('selected'));
}

function selectDate(element, date) {
    const modal = document.getElementById('bookingModal');
    const movieId = modal.dataset.movieId;
    console.log('Selected date before fetch:', date);
    let html = '';
    fetch(`/api/showtimes?movieId=${movieId}&date=${date}`)
        .then(response => {
            if (!response.ok) throw new Error('Network response was not ok ' + response.statusText);
            return response.json();
        })
        .then(data => {
            console.log('Showtimes data from API:', data);

            // Filter showtimes to only show future times if the selected date is today
            const now = new Date();
            const currentHour = now.getHours();
            const currentMinute = now.getMinutes();

            const selectedDate = new Date(date);
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            selectedDate.setHours(0, 0, 0, 0);
            const isToday = selectedDate.getTime() === today.getTime();

            let availableShowtimes = data;
            if (isToday) {
                availableShowtimes = data.filter(schedule => {
                    const [hours, minutes] = schedule.time.split(':').map(Number);
                    return hours > currentHour || (hours === currentHour && minutes > currentMinute);
                });
            }

            if (availableShowtimes && availableShowtimes.length > 0) {
                availableShowtimes.forEach(schedule => {
                    const time = schedule.time;
                    html += `
                        <button class="showtime-option" onclick="selectShowtime(this, '${time}', '${schedule.label || '2D'}', ${schedule.price || 150000}, ${schedule.cinemaRoomId}, ${schedule.showDateId}, ${schedule.scheduleId})">
                            <div class="showtime-time">${time}</div>
                        </button>
                    `;
                });
            } else {
                html = isToday ?
                    '<p class="text-warning">Không có suất chiếu còn trống cho ngày hôm nay.</p>' :
                    '<p class="text-warning">Không có suất chiếu cho ngày này.</p>';
            }
            const showtimeGrid = document.getElementById('showtimeGrid');
            showtimeGrid.innerHTML = html;
        })
        .catch(error => {
            console.error('Lỗi khi lấy suất chiếu:', error);
            const showtimeGrid = document.getElementById('showtimeGrid');
            showtimeGrid.innerHTML = '<p class="text-danger">Lỗi khi tải suất chiếu. Vui lòng thử lại.</p>';
        });

    document.querySelectorAll('.date-option.selected').forEach(el => el.classList.remove('selected'));
    element.classList.add('selected');
    selectedBookingData.date = date;
    console.log('Selected date after update:', selectedBookingData.date);
    updateBookingSummary();
}

function selectShowtime(element, time, type, price, cinemaRoomId, showDateId, scheduleId) {
    // Remove previous selection
    document.querySelectorAll('.showtime-option.selected').forEach(el => el.classList.remove('selected'));

    // Add selection to current element
    element.classList.add('selected');

    // Update booking data with all fields from API
    selectedBookingData.showtime = time;
    selectedBookingData.type = type;
    selectedBookingData.price = price;
    selectedBookingData.cinemaRoomId = cinemaRoomId;
    selectedBookingData.showDateId = showDateId;
    selectedBookingData.scheduleId = scheduleId;
    console.log('Selected booking data after selectShowtime:', selectedBookingData); // Debug dữ liệu đã chọn
    updateBookingSummary();
}

function updateBookingSummary() {
    const dateDisplay = document.getElementById('selectedDate');
    const showtimeDisplay = document.getElementById('selectedShowtime');
    const continueBtn = document.getElementById('continueBtn');

    // Debug dữ liệu trước khi cập nhật
    console.log('Updating summary with:', selectedBookingData);

    // Update date display
    if (selectedBookingData.date) {
        const dateParts = selectedBookingData.date.split('-'); // Phân tách yyyy-MM-dd từ API
        if (dateParts.length === 3) {
            const day = dateParts[2];
            const month = dateParts[1];
            const year = dateParts[0];
            dateDisplay.textContent = `${day}/${month}/${year}`; // Định dạng dd/MM/yyyy
        } else {
            dateDisplay.textContent = 'Định dạng ngày không hợp lệ';
            console.warn('Invalid date format:', selectedBookingData.date);
        }
    } else {
        dateDisplay.textContent = 'Chưa chọn';
    }

    // Update showtime display with date and time only
    if (selectedBookingData.date && selectedBookingData.showtime) {
        const dateParts = selectedBookingData.date.split('-');
        if (dateParts.length === 3) {
            const day = dateParts[2];
            const month = dateParts[1];
            const year = dateParts[0];
            showtimeDisplay.textContent = `${day}-${month}-${year} ${selectedBookingData.showtime}`; // Định dạng dd/MM/yyyy HH:mm
        } else {
            showtimeDisplay.textContent = 'Định dạng ngày không hợp lệ';
            console.warn('Invalid date format:', selectedBookingData.date);
        }
    } else {
        showtimeDisplay.textContent = 'Chưa chọn';
    }

    // Enable/disable continue button
    continueBtn.disabled = !selectedBookingData.showtime;
    continueBtn.style.opacity = selectedBookingData.showtime ? '1' : '0.5';
}

function goToSeatSelection() {
    if (selectedBookingData.scheduleId && selectedBookingData.showDateId && selectedBookingData.cinemaRoomId) {
        const movieId = document.getElementById('bookingModal').dataset.movieId;
        const url = `/selection-seat?movieId=${movieId}&scheduleId=${selectedBookingData.scheduleId}&showDateId=${selectedBookingData.showDateId}`;
        console.log("Redirecting to: ", url); // Debug log
        window.location.href = url; // Redirect tới file HTML code cứng
    } else {
        alert('Vui lòng chọn suất chiếu trước khi tiếp tục.');
    }
}
function openTrailerFromButton(button) {
    const movieName = button.getAttribute('data-movie-name');
    const trailerId = button.getAttribute('data-trailer-id');
    openTrailer(movieName, trailerId);
}
// Hàm xử lý nút trailer
function openTrailer(movieName, youtubeId) {
    // Hiển thị modal với trailer
    const modal = document.getElementById('trailerModal');
    const modalTitle = document.getElementById('modalTitle');
    const trailerFrame = document.getElementById('trailerFrame');
    
    modalTitle.textContent = 'Trailer: ' + movieName;
    trailerFrame.src = 'https://www.youtube.com/embed/' + youtubeId;
    
    modal.style.display = 'block';
}

        function closeTrailer() {
            const modal = document.getElementById('trailerModal');
            const trailerFrame = document.getElementById('trailerFrame');

            modal.style.display = 'none';
            trailerFrame.src = '';
            document.body.style.overflow = 'auto';
        }

        // Đóng modal khi click bên ngoài
        window.onclick = function (event) {
            const modal = document.getElementById('trailerModal');
            if (event.target === modal) {
                closeTrailer();
            }
        }

        // Đóng modal bằng phím ESC
        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape') {
                closeTrailer();
            }
        });

        // Thêm hiệu ứng loading cho poster images
        document.addEventListener('DOMContentLoaded', function () {
            const images = document.querySelectorAll('.movie-poster');
            images.forEach(img => {
                img.addEventListener('load', function () {
                    this.style.opacity = '1';
                });
            });
        });
// Đóng modal khi click bên ngoài
window.onclick = function (event) {
    const trailerModal = document.getElementById('trailerModal');
    const bookingModal = document.getElementById('bookingModal');

    if (event.target === trailerModal) {
        closeTrailer();
    } else if (event.target === bookingModal) {
        closeBookingModal();
    }
}

// Đóng modal bằng phím ESC
document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape') {
        if (document.getElementById('trailerModal').style.display === 'block') {
            closeTrailer();
        } else if (document.getElementById('bookingModal').style.display === 'block') {
            closeBookingModal();
        }
    }
});

// Smooth scroll cho các section
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        document.querySelector(this.getAttribute('href')).scrollIntoView({
            behavior: 'smooth'
        });
    });
});

// Lazy loading cho images
const images = document.querySelectorAll('img');
const imageObserver = new IntersectionObserver((entries, observer) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            const img = entry.target;
            img.style.opacity = '1';
            observer.unobserve(img);
        }
    });
});

images.forEach(img => {
    img.style.opacity = '0';
    img.style.transition = 'opacity 0.5s ease';
    imageObserver.observe(img);
});

// Gắn sự kiện cho nút mở modal
document.querySelectorAll('.btn-primary').forEach(btn => {
    btn.addEventListener('click', (e) => {
        const movieId = btn.getAttribute('data-movie-id');
        const movieName = btn.getAttribute('data-movie-name');
        openBookingModal({ movieId, movieName });
    });
});