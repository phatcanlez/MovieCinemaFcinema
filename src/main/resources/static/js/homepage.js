document.addEventListener('DOMContentLoaded', function() {
    // Load navigation
    fetch('components/nav.html')
        .then(response => response.text())
        .then(data => {
            document.getElementById('nav-placeholder').innerHTML = data;
        });

    // Load footer
    fetch('components/footer.html')
        .then(response => response.text())
        .then(data => {
            document.getElementById('footer-placeholder').innerHTML = data;
        });

    // Initialize animations
    initializeAnimations();

    var navbar = document.querySelector('.navbar-custom');
    
    window.addEventListener('scroll', function() {
        if (window.scrollY > 50) {
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.remove('scrolled');
        }
    });
});

document.addEventListener('DOMContentLoaded', function() {
    // Set default date to today
    const dateInput = document.getElementById('quickDate');
    if (dateInput) {
        const today = new Date();
        const year = today.getFullYear();
        const month = String(today.getMonth() + 1).padStart(2, '0');
        const day = String(today.getDate()).padStart(2, '0');
        dateInput.value = `${year}-${month}-${day}`;
        dateInput.min = `${year}-${month}-${day}`; // Prevent selecting past dates
    }

    // Listen for movie and date selection changes
    const movieSelect = document.getElementById('quickMovie');
    if (movieSelect) {
        movieSelect.addEventListener('change', function() {
            checkFormCompletion();
            if (dateInput.value) {
                fetchShowtimes();
            }
        });
    }

    if (dateInput) {
        dateInput.addEventListener('change', function() {
            checkFormCompletion();
            if (movieSelect.value) {
                fetchShowtimes();
            }
        });
    }

    // Handle form submission
    const quickBookingForm = document.getElementById('quickBookingForm');
    if (quickBookingForm) {
        quickBookingForm.addEventListener('submit', function(event) {
            event.preventDefault();

            const movieId = document.getElementById('quickMovie').value;
            const date = document.getElementById('quickDate').value;
            const showtimeSelect = document.getElementById('quickShowtime');

            if (!movieId || !date || !showtimeSelect.value) {
                alert('Vui lòng chọn đầy đủ thông tin phim, ngày chiếu và suất chiếu');
                return;
            }

            // Get the selected option to extract schedule details
            const selectedOption = showtimeSelect.options[showtimeSelect.selectedIndex];
            const scheduleId = selectedOption.getAttribute('data-schedule-id');
            const showDateId = selectedOption.getAttribute('data-showdate-id');

            if (!scheduleId || !showDateId) {
                alert('Không thể xác định thông tin lịch chiếu. Vui lòng thử lại.');
                return;
            }

            console.log('Submitting booking with details:');
            // Redirect to seat selection page with all necessary parameters
            window.location.href = `/selection-seat?movieId=${movieId}&scheduleId=${scheduleId}&showDateId=${showDateId}`;
        });
    }
});

// Function to fetch showtimes from API
// Function to fetch showtimes from API
function fetchShowtimes() {
    const movieId = document.getElementById('quickMovie').value;
    const date = document.getElementById('quickDate').value;
    const showtimeSelect = document.getElementById('quickShowtime');
    const loadingSpinner = document.getElementById('showtimeLoading');

    if (!movieId || !date) return;

    // Show loading indicator
    showtimeSelect.disabled = true;
    if (loadingSpinner) loadingSpinner.style.display = 'block';

    // Clear previous options
    showtimeSelect.innerHTML = '<option value="">-- Chọn suất chiếu --</option>';

    // Fetch showtimes from API
    fetch(`/api/showtimes?movieId=${movieId}&date=${date}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok: ' + response.status);
            }
            return response.json();
        })
        .then(data => {
            console.log('Showtimes data:', data);

            if (data && data.length > 0) {
                // Get current time
                const now = new Date();
                const currentHour = now.getHours();
                const currentMinute = now.getMinutes();
                const selectedDate = new Date(date);
                const today = new Date();
                today.setHours(0, 0, 0, 0);
                selectedDate.setHours(0, 0, 0, 0);
                const isToday = selectedDate.getTime() === today.getTime();

                // Filter showtimes for today to only show future times
                let availableShowtimes = data;
                if (isToday) {
                    availableShowtimes = data.filter(schedule => {
                        const [hours, minutes] = schedule.time.split(':').map(Number);
                        return hours > currentHour || (hours === currentHour && minutes > currentMinute);
                    });
                }

                if (availableShowtimes.length > 0) {
                    // Add options for each showtime
                    availableShowtimes.forEach(schedule => {
                        const option = document.createElement('option');
                        option.value = schedule.time;
                        option.textContent = `${schedule.time}`;

                        // Store schedule data as attributes
                        option.setAttribute('data-schedule-id', schedule.scheduleId);
                        option.setAttribute('data-showdate-id', schedule.showDateId);
                        option.setAttribute('data-cinema-room-id', schedule.cinemaRoomId);
                        option.setAttribute('data-price', schedule.price || 0);

                        showtimeSelect.appendChild(option);
                    });

                    // Enable select
                    showtimeSelect.disabled = false;
                } else {
                    // No future showtimes available
                    const option = document.createElement('option');
                    option.value = "";
                    option.textContent = "Không có suất chiếu còn trống cho ngày này";
                    showtimeSelect.appendChild(option);
                }
            } else {
                // No showtimes available
                const option = document.createElement('option');
                option.value = "";
                option.textContent = "Không có suất chiếu cho ngày này";
                showtimeSelect.appendChild(option);
            }
        })
        .catch(error => {
            console.error('Error fetching showtimes:', error);
            const option = document.createElement('option');
            option.value = "";
            option.textContent = "Lỗi khi tải suất chiếu";
            showtimeSelect.appendChild(option);
        })
        .finally(() => {
            // Hide loading indicator
            if (loadingSpinner) loadingSpinner.style.display = 'none';
            checkFormCompletion();
        });
}

// Check if all form fields are filled to enable/disable submit button
function checkFormCompletion() {
    const movieId = document.getElementById('quickMovie').value;
    const date = document.getElementById('quickDate').value;
    const showtime = document.getElementById('quickShowtime').value;
    const submitBtn = document.getElementById('bookingSubmitBtn');

    if (submitBtn) {
        if (movieId && date && showtime) {
            submitBtn.disabled = false;
        } else {
            submitBtn.disabled = true;
        }
    }
}

// Event listener for showtime selection
document.getElementById('quickShowtime')?.addEventListener('change', checkFormCompletion);

function scrollToBooking() {
    document.getElementById('booking').scrollIntoView({ behavior: 'smooth' });
}

function initializeAnimations() {
    // Add your animation logic here
}

function togglePassword(inputId) {
    const passwordInput = document.getElementById(inputId);
    const toggleIcon = document.getElementById('toggleIcon');
    
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        toggleIcon.classList.replace('bi-eye', 'bi-eye-slash');
    } else {
        passwordInput.type = 'password';
        toggleIcon.classList.replace('bi-eye-slash', 'bi-eye');
    }
}

document.addEventListener("DOMContentLoaded", function () {
// Thêm event listener cho các nút đặt vé
const bookBtns = document.querySelectorAll(".btn-book-movie");
bookBtns.forEach(function (btn) {
    btn.addEventListener("click", function () {
    const movieId = this.getAttribute("data-movie-id");
    const movieName = this.getAttribute("data-movie-name");
    selectMovieForBooking(movieId, movieName);
    });
});
});

// Hàm xử lý khi người dùng chọn phim để đặt vé
function selectMovieForBooking(movieId, movieName) {
// Cuộn đến phần đặt vé
document
    .getElementById("booking")
    .scrollIntoView({ behavior: "smooth" });

// Chọn phim trong dropdown
const movieSelect = document.getElementById("quickMovie");
if (movieSelect) {
    movieSelect.value = movieId;

    // Kích hoạt sự kiện change để trigger các logic phụ thuộc (nếu có)
    const event = new Event("change");
    movieSelect.dispatchEvent(event);
}

// Đặt focus vào form chọn ngày
setTimeout(() => {
    const dateInput = document.getElementById("quickDate");
    if (dateInput) {
    dateInput.focus();
    }
}, 800);
}

// Thiết lập ngày mặc định cho form đặt vé (ngày hiện tại)
document.addEventListener("DOMContentLoaded", function () {
const dateInput = document.getElementById("quickDate");
if (dateInput) {
    const now = new Date();
    const vietnamOffset = 7 * 60; // phút GMT+7
    const localTime = new Date(
    now.getTime() + (vietnamOffset - now.getTimezoneOffset()) * 60000
    );
    const dateOnly = localTime.toISOString().split("T")[0];

    dateInput.value = dateOnly;
    dateInput.min = dateOnly; // Không cho phép chọn ngày trong quá khứ
}

// Xử lý form đặt vé nhanh
const quickBookingForm = document.getElementById("quickBookingForm");
if (quickBookingForm) {
    quickBookingForm.addEventListener("submit", function (event) {
    event.preventDefault();
    const movieId = document.getElementById("quickMovie").value;
    const scheduleId = selectedOption.getAttribute('data-schedule-id');
    const showDateId = selectedOption.getAttribute('data-showdate-id');



    if (!movieId || !date || !time) {
        alert(
        "Vui lòng chọn đầy đủ thông tin phim, ngày chiếu và suất chiếu"
        );
        return;
    }

    // Chuyển hướng đến trang chọn ghế với thông tin đã chọn
        window.location.href = `/selection-seat?movieId=${movieId}&scheduleId=${scheduleId}&showDateId=${showDateId}`;
    });
}
});
