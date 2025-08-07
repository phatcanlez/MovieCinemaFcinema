// Admin Dashboard JavaScript - Top Movies
// Lấy và hiển thị dữ liệu top movies từ API

document.addEventListener('DOMContentLoaded', function () {
    console.log('DOM loaded, loading initial data...');
    loadTopMovies();
    loadTopCustomers();
    loadMovieStats();
    loadMonthlyRevenueStats();
    loadEmployeeStats();
    loadMemberStats();
    loadBookingStats();

    // REVISED: Khởi tạo biểu đồ và tải dữ liệu doanh thu mặc định (theo ngày)
    initializeRevenueChart();
    loadRevenueData('date'); // Tải dữ liệu theo ngày làm mặc định

    // ADDED: Thêm trình xử lý sự kiện cho các nút chọn khoảng thời gian của biểu đồ doanh thu
    document.getElementById('daily').addEventListener('change', () => loadRevenueData('date'));
    document.getElementById('monthly').addEventListener('change', () => loadRevenueData('month'));
    document.getElementById('yearly').addEventListener('change', () => loadRevenueData('year'));
});

// Hàm chính để lấy dữ liệu top movies
async function loadTopMovies() {
    console.log('Starting loadTopMovies...');
    try {
        const response = await fetch('http://localhost:8081/api/statistics/top-movies', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        displayTopMovies(data);

    } catch (error) {
        console.error('Error in loadTopMovies:', error);
        showError('Cannot load movies. Please try again!.');
    }
}

// Hàm lấy dữ liệu top customers
async function loadTopCustomers() {
    console.log('Starting loadTopCustomers...');
    try {
        const response = await fetch('http://localhost:8081/api/statistics/top-customers', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        displayTopCustomers(data);

    } catch (error) {
        console.error('Error in loadTopCustomers:', error);
        showError('Cannot load member. Please try again.');
    }
}

// Hàm lấy thống kê tổng số phim
async function loadMovieStats() {
    console.log('Starting loadMovieStats...');
    try {
        const response = await fetch('http://localhost:8081/api/statistics/movies', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        updateMovieStats(data);

    } catch (error) {
        console.error('Error in loadMovieStats:', error);
        showError('Could not load movie statistics. Please try again!');
    }
}

// Hàm lấy và so sánh doanh thu tháng hiện tại với tháng trước
async function loadMonthlyRevenueStats() {
    console.log('Starting loadMonthlyRevenueStats with new API logic...');
    try {
        const today = new Date();
        const year = today.getFullYear();
        const month = today.getMonth() + 1; // getMonth() trả về giá trị từ 0-11

        const url = `http://localhost:8081/api/statistics/revenue/month?year=${year}&month=${month}`;
        const response = await fetch(url);

        if (!response.ok) {
            throw new Error(`HTTP error for ${url}! status: ${response.status}`);
        }

        const data = await response.json();
        // Truyền toàn bộ đối tượng data vào hàm cập nhật
        updateMonthlyRevenueStats(data);

    } catch (error) {
        console.error('Error in loadMonthlyRevenueStats:', error);
        showError('Could not load monthly revenue statistics.');
    }
}

// Hàm lấy thống kê nhân viên
async function loadEmployeeStats() {
    console.log('Starting loadEmployeeStats...');
    try {
        const today = new Date();
        const year = today.getFullYear();
        const month = today.getMonth() + 1; // getMonth() trả về giá trị từ 0-11

        const response = await fetch(`http://localhost:8081/api/statistics/employee-statistics?year=${year}&month=${month}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        updateEmployeeStats(data);

    } catch (error) {
        console.error('Error in loadEmployeeStats:', error);
        showError('Could not load employee statistics. Please try again!');
    }
}

// Hàm lấy thống kê thành viên
async function loadMemberStats() {
    console.log('Starting loadMemberStats...');
    try {
        const today = new Date();
        const year = today.getFullYear();
        const month = today.getMonth() + 1; // getMonth() trả về giá trị từ 0-11

        const response = await fetch(`http://localhost:8081/api/statistics/member-statistics?year=${year}&month=${month}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        updateMemberStats(data);

    } catch (error) {
        console.error('Error in loadMemberStats:', error);
        showError('Could not load member statistics. Please try again!');
    }
}

// Hàm lấy thống kê booking
async function loadBookingStats() {
    console.log('Starting loadBookingStats...');
    try {
        const response = await fetch('http://localhost:8081/api/statistics/bookings', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        updateBookingStats(data);

    } catch (error) {
        console.error('Error in loadBookingStats:', error);
        showError('Unable to load booking statistics. Please try again!');
    }
}

// Cập nhật thống kê booking vào dashboard
function updateBookingStats(data) {
    const { totalBookings = 0, successfulBookings = 0, pendingBookings = 0, cancelledBookings = 0 } = data;

    const bookingData = {
        confirmed: successfulBookings,
        pending: pendingBookings,
        cancelled: cancelledBookings,
    };

    updateBookingPieChart(bookingData);
    updateBookingLegend(bookingData);
}

// Cập nhật Booking Pie Chart
function updateBookingPieChart(bookingData) {
    const chartCanvas = document.getElementById('bookingPieChart');
    if (!chartCanvas) return;

    if (window.bookingPieChart && typeof window.bookingPieChart.destroy === 'function') {
        window.bookingPieChart.destroy();
    }

    const ctx = chartCanvas.getContext('2d');
    window.bookingPieChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Confirmed', 'Pending', 'Cancelled'],
            datasets: [{
                data: [
                    bookingData.confirmed,
                    bookingData.pending,
                    bookingData.cancelled,
                ],
                backgroundColor: ['#28a745', '#ffc107', '#dc3545'],
                borderWidth: 0,
                cutout: '60%'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            const label = context.label || '';
                            const value = context.parsed;
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = total > 0 ? Math.round((value / total) * 100) : 0;
                            return `${label}: ${value} (${percentage}%)`;
                        }
                    }
                }
            },
            animation: { animateScale: true, animateRotate: true }
        }
    });
}

// Cập nhật booking legend với số liệu thực
function updateBookingLegend(bookingData) {
    document.querySelector('.legend-item:nth-child(1) strong').textContent = formatNumber(bookingData.confirmed);
    document.querySelector('.legend-item:nth-child(2) strong').textContent = formatNumber(bookingData.pending);
    document.querySelector('.legend-item:nth-child(3) strong').textContent = formatNumber(bookingData.cancelled);
}

// Cập nhật thống kê phim vào dashboard
function updateMovieStats(data) {
    // Lấy giá trị totalShowingMovies từ đối tượng data
    const { totalShowingMovies } = data;
    // Chọn phần tử bằng ID mới của nó
    const totalMoviesElement = document.getElementById('totalMovies');
    // Kiểm tra xem phần tử có tồn tại không và cập nhật nội dung văn bản của nó
    if (totalMoviesElement) {
        // Sử dụng hàm formatNumber để định dạng số liệu
        totalMoviesElement.textContent = formatNumber(totalShowingMovies);
    }
}

// Cập nhật thẻ thống kê doanh thu tháng dựa trên API mới
function updateMonthlyRevenueStats(data) {
    // Lấy các giá trị cần thiết từ đối tượng data
    const { totalRevenue, revenueStatus, revenueChangePercent } = data;

    const monthlyRevenueElement = document.getElementById('monthlyRevenue');
    const revenueChangeElement = document.getElementById('revenueChange');

    if (monthlyRevenueElement) {
        monthlyRevenueElement.textContent = formatCurrency(totalRevenue);
    }

    if (revenueChangeElement) {
        let changeIndicatorHtml = '';

        // Sử dụng switch case với revenueStatus để xác định hiển thị
        switch (revenueStatus) {
            case 'increase':
                changeIndicatorHtml = `
                    <i class="fas fa-arrow-up text-success me-1"></i>
                    <span class="text-success">${revenueChangePercent.toFixed(1)}% vs last month</span>
                `;
                break;
            case 'decrease':
                changeIndicatorHtml = `
                    <i class="fas fa-arrow-down text-warning me-1"></i>
                    <span class="text-warning">${Math.abs(revenueChangePercent).toFixed(1)}% vs last month</span>
                `;
                break;
            case 'no_change':
            default:
                changeIndicatorHtml = `<span class="text-muted">No change vs last month</span>`;
                break;
        }
        revenueChangeElement.innerHTML = changeIndicatorHtml;
    }
}

// Cập nhật thống kê nhân viên vào dashboard
function updateEmployeeStats(data) {
    const { totalMembers, increasedMembers } = data;

    const totalEmployeesElement = document.getElementById('totalEmployees');
    const employeeChangeElement = document.getElementById('employeeChange');

    if (totalEmployeesElement) {
        totalEmployeesElement.textContent = formatNumber(totalMembers);
    }

    if (employeeChangeElement) {
        // Xóa nội dung cũ
        employeeChangeElement.innerHTML = '';

        let changeIndicatorHtml = '';
        if (increasedMembers > 0) {
            // Mũi tên xanh lá đi lên cho số dương
            changeIndicatorHtml = `
                <i class="fas fa-arrow-up text-success me-1"></i>
                <span class="text-success">${formatNumber(increasedMembers)} vs last month</span>
            `;
        } else if (increasedMembers < 0) {
            // Mũi tên vàng đi xuống cho số âm
            changeIndicatorHtml = `
                <i class="fas fa-arrow-down text-warning me-1"></i>
                <span class="text-warning">${formatNumber(Math.abs(increasedMembers))} than last month</span>
            `;
        } else {
            // Không thay đổi
            changeIndicatorHtml = `<span class="text-muted">No change vs last month</span>`;
        }
        employeeChangeElement.innerHTML = changeIndicatorHtml;
    }
}

// Cập nhật thống kê thành viên vào dashboard
function updateMemberStats(data) {
    const { totalMembers, increasedMembers } = data;

    const totalMembersElement = document.getElementById('totalMembersStat');
    const memberChangeElement = document.getElementById('memberChange');

    if (totalMembersElement) {
        totalMembersElement.textContent = formatNumber(totalMembers);
    }

    if (memberChangeElement) {
        let changeIndicatorHtml = '';
        if (increasedMembers > 0) {
            // Mũi tên xanh lá đi lên cho số dương
            changeIndicatorHtml = `
                <i class="fas fa-arrow-up text-success me-1"></i>
                <span class="text-success">${formatNumber(increasedMembers)} vs last month</span>
            `;
        } else if (increasedMembers < 0) {
            // Mũi tên vàng đi xuống cho số âm
            changeIndicatorHtml = `
                <i class="fas fa-arrow-down text-warning me-1"></i>
                <span class="text-warning">${formatNumber(Math.abs(increasedMembers))} than last month</span>
            `;
        } else {
            // Không thay đổi
            changeIndicatorHtml = `<span class="text-muted">No change vs last month</span>`;
        }
        memberChangeElement.innerHTML = changeIndicatorHtml;
    }
}

// Tạo hàng bảng cho mỗi phim
function createMovieRow(movie, rank) {
    const row = document.createElement('tr');
    let rankDisplay;

    // Sử dụng câu lệnh switch để xác định hiển thị huy chương hay số thứ hạng
    switch (rank) {
        case 1:
            // Huy chương Vàng
            rankDisplay = '<i class="fas fa-medal" style="color: #FFD700; font-size: 1.8rem;"></i>';
            break;
        case 2:
            // Huy chương Bạc
            rankDisplay = '<i class="fas fa-medal" style="color: #C0C0C0; font-size: 1.8rem;"></i>';
            break;
        case 3:
            // Huy chương Đồng
            rankDisplay = '<i class="fas fa-medal" style="color: #CD7F32; font-size: 1.8rem;"></i>';
            break;
        default:
            // Các thứ hạng khác sẽ hiển thị số
            rankDisplay = `<span class="badge text-dark fs-6">${rank}</span>`;
            break;
    }

    // Cập nhật innerHTML của hàng với phần tử hiển thị thứ hạng mới
    row.innerHTML = `
        <td class="text-center align-middle">${rankDisplay}</td>
        <td>
            <div class="d-flex align-items-center">
                <img src="${movie.posterUrl}" class="rounded me-3" width="40" height="60" alt="Poster" onerror="this.src='/images/default-poster.jpg'">
                <div>
                    <h6 class="mb-0 d-flex align-items-flex-start">${movie.movieNameVn}</h6>
                    <small class="mb-0 d-flex align-items-flex-start text-muted">${movie.movieNameEnglish}</small>
                </div>
            </div>
        </td>
        <td class="align-middle"><strong>${formatNumber(movie.totalBookings)}</strong></td>
        <td class="align-middle"><strong class="text-success">${formatCurrency(movie.totalRevenue)}</strong></td>
    `;
    return row;
}

// Hiển thị danh sách top movies trong bảng
function displayTopMovies(data) {
    const { topMovies } = data;
    const tableBody = document.getElementById('topMoviesTable');
    if (!tableBody) return;
    tableBody.innerHTML = '';
    topMovies.forEach((movie, index) => {
        tableBody.appendChild(createMovieRow(movie, index + 1));
    });
}

// Tạo hàng bảng cho mỗi khách hàng
function createCustomerRow(customer, rank) {
    const row = document.createElement('tr');
    let rankDisplay;

    // Sử dụng câu lệnh switch để xác định hiển thị huy chương hay số thứ hạng
    switch (rank) {
        case 1:
            // Huy chương Vàng
            rankDisplay = '<i class="fas fa-medal" style="color: #FFD700; font-size: 1.8rem;"></i>';
            break;
        case 2:
            // Huy chương Bạc
            rankDisplay = '<i class="fas fa-medal" style="color: #C0C0C0; font-size: 1.8rem;"></i>';
            break;
        case 3:
            // Huy chương Đồng
            rankDisplay = '<i class="fas fa-medal" style="color: #CD7F32; font-size: 1.8rem;"></i>';
            break;
        default:
            // Các thứ hạng khác sẽ hiển thị số
            rankDisplay = `<span class="badge text-dark fs-6">${rank}</span>`;
            break;
    }

    // Cập nhật innerHTML của hàng với phần tử hiển thị thứ hạng mới
    row.innerHTML = `
        <td class="text-center align-middle">${rankDisplay}</td>
        <td>
            <div class="d-flex align-items-center">
                <div class="bg-primary rounded-circle d-flex align-items-center justify-content-center me-3" style="width: 40px; height: 40px;">
                    <i class="fas fa-user text-white"></i>
                </div>
                <div>
                    <h6 class="mb-0 d-flex align-items-flex-start">${customer.customerName || 'N/A'}</h6>
                    <small class="mb-0 d-flex align-items-flex-start text-muted">${customer.email || 'N/A'}</small>
                </div>
            </div>
        </td>
        <td class="align-middle"><strong class="text-success">${formatCurrency(customer.totalSpent)}</strong></td>
    `;
    return row;
}

// Hiển thị danh sách top customers trong bảng
function displayTopCustomers(data) {
    const { topCustomers } = data;
    const tableBody = document.getElementById('topCustomersTable');
    if (!tableBody) return;
    tableBody.innerHTML = '';
    topCustomers.forEach((customer, index) => {
        tableBody.appendChild(createCustomerRow(customer, index + 1));
    });
}

// Hiển thị danh sách top customers trong bảng
function displayTopCustomers(data) {
    const { topCustomers } = data;
    const tableBody = document.getElementById('topCustomersTable');
    if (!tableBody) return;
    tableBody.innerHTML = '';
    topCustomers.forEach((customer, index) => {
        tableBody.appendChild(createCustomerRow(customer, index + 1));
    });
}

// Format số và tiền tệ
function formatNumber(num) { return new Intl.NumberFormat('vi-VN').format(num); }
function formatCurrency(amount) { return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount); }

// Hiển thị thông báo lỗi
function showError(message) {
    // (Implementation for showing toast error can be kept as is)
    console.error("SHOWING ERROR:", message);
}

// Event listener cho các tab
document.addEventListener('DOMContentLoaded', function () {
    const topMoviesTab = document.getElementById('top-movies-tab');
    if (topMoviesTab) {
        topMoviesTab.addEventListener('shown.bs.tab', () => loadTopMovies());
    }
    const topCustomersTab = document.getElementById('top-customers-tab');
    if (topCustomersTab) {
        topCustomersTab.addEventListener('shown.bs.tab', () => loadTopCustomers());
    }
});

// ====================================================================
// REVENUE CHART LOGIC - COMPLETELY REVISED
// ====================================================================

// Khởi tạo biểu đồ doanh thu với dữ liệu trống ban đầu
function initializeRevenueChart() {
    const ctx = document.getElementById('revenueChart').getContext('2d');
    if (!ctx) {
        console.error("Không tìm thấy canvas với ID 'revenueChart'");
        return;
    }

    window.revenueLineChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: 'Revenue (₫)',
                data: [],
                borderColor: '#dc3545',
                backgroundColor: 'rgba(220, 53, 69, 0.1)',
                tension: 0.3,
                fill: true,
                pointBackgroundColor: '#ffffff',
                pointBorderColor: '#dc3545',
                pointHoverRadius: 7,
                pointHoverBackgroundColor: '#dc3545',
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    title: { display: true, text: 'Revenue (₫)', color: '#ffffff' },
                    ticks: { color: '#cccccc', callback: value => formatCurrency(value) },
                    grid: { color: 'rgba(255, 255, 255, 0.1)' }
                },
                x: {
                    title: { display: true, text: 'Time', color: '#ffffff' },
                    ticks: { color: '#cccccc' },
                    grid: { color: 'rgba(255, 255, 255, 0.1)' }
                }
            },
            plugins: {
                legend: { display: true, position: 'top', labels: { color: '#ffffff' } },
                tooltip: {
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    titleColor: '#ffffff',
                    bodyColor: '#dddddd',
                    callbacks: {
                        label: context => `${context.dataset.label}: ${formatCurrency(context.raw)}`
                    }
                }
            }
        }
    });
}

/**
 * Hàm chính để tải dữ liệu doanh thu.
 * Nó sẽ gọi API nhiều lần cho các khoảng thời gian khác nhau và tổng hợp kết quả.
 * @param {'date' | 'month' | 'year'} periodType Loại khoảng thời gian
 */
async function loadRevenueData(periodType) {
    console.log(`Loading revenue data for period type: ${periodType}`);
    const promises = [];
    const today = new Date();

    // Hàm trợ giúp để fetch và parse JSON
    const fetchRevenue = async (url) => {
        try {
            const response = await fetch(url);
            if (!response.ok) {
                console.error(`HTTP error for ${url}! status: ${response.status}`);
                return { period: 'Error', totalRevenue: 0 }; // Trả về giá trị mặc định khi có lỗi
            }
            return await response.json();
        } catch (error) {
            console.error(`Fetch failed for ${url}:`, error);
            return { period: 'Error', totalRevenue: 0 };
        }
    };

    switch (periodType) {
        case 'date':
            // Lấy dữ liệu cho 7 ngày gần nhất
            for (let i = 0; i < 7; i++) {
                const date = new Date();
                date.setDate(today.getDate() - i);
                const yyyy = date.getFullYear();
                const mm = String(date.getMonth() + 1).padStart(2, '0');
                const dd = String(date.getDate()).padStart(2, '0');
                const formattedDate = `${yyyy}-${mm}-${dd}`;
                const url = `http://localhost:8081/api/statistics/revenue/date?date=${formattedDate}`;
                promises.push(fetchRevenue(url));
            }
            break;

        case 'month':
            // Lấy dữ liệu cho 6 tháng gần nhất
            for (let i = 0; i < 6; i++) {
                const date = new Date();
                date.setMonth(today.getMonth() - i);
                const year = date.getFullYear();
                const month = date.getMonth() + 1;
                const url = `http://localhost:8081/api/statistics/revenue/month?year=${year}&month=${month}`;
                promises.push(fetchRevenue(url));
            }
            break;

        case 'year':
            // Lấy dữ liệu cho 5 năm gần nhất
            for (let i = 0; i < 5; i++) {
                const year = today.getFullYear() - i;
                const url = `http://localhost:8081/api/statistics/revenue/year?year=${year}`;
                promises.push(fetchRevenue(url));
            }
            break;
    }

    try {
        // Chờ tất cả các API call hoàn thành
        const results = await Promise.all(promises);
        console.log('Aggregated revenue results:', results);
        updateRevenueChart(periodType, results);

        // Cập nhật thẻ "Today Revenue" nếu đang xem theo ngày
        if (periodType === 'date' && results.length > 0) {
            const todayRevenueEl = document.getElementById('todayRevenue');
            if (todayRevenueEl) {
                // Tìm doanh thu của ngày hôm nay trong mảng kết quả
                const todayFormatted = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
                const todayData = results.find(r => r.period === todayFormatted);
                todayRevenueEl.textContent = todayData ? formatCurrency(todayData.totalRevenue) : formatCurrency(0);
            }
        }

    } catch (error) {
        console.error('Error processing revenue promises:', error);
        showError('Unable to aggregate revenue data.');
    }
}

/**
 * Cập nhật biểu đồ với một MẢNG dữ liệu từ API
 * @param {'date' | 'month' | 'year'} periodType
 * @param {Array<Object>} apiDataArray Mảng các object trả về từ API
 */
function updateRevenueChart(periodType, apiDataArray) {
    const chart = window.revenueLineChart;
    if (!chart) return;

    // Sắp xếp dữ liệu theo thứ tự thời gian tăng dần (cũ nhất -> mới nhất)
    const sortedData = apiDataArray.sort((a, b) => new Date(a.period) - new Date(b.period));

    // Trích xuất nhãn và dữ liệu từ mảng đã sắp xếp
    const labels = sortedData.map(d => d.period);
    const newData = sortedData.map(d => d.totalRevenue);

    // Cập nhật tiêu đề trục X
    switch (periodType) {
        case 'date':
            chart.options.scales.x.title.text = 'Revenue for last 7 days';
            break;
        case 'month':
            chart.options.scales.x.title.text = 'Revenue for the last 6 months';
            break;
        case 'year':
            chart.options.scales.x.title.text = 'Revenue for the last 5 years';
            break;
    }

    chart.data.labels = labels;
    chart.data.datasets[0].data = newData;
    chart.update();
    console.log('✅ Revenue chart updated with historical data.');
}