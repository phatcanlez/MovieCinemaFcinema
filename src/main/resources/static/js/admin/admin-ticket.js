// Biến phân trang 
let currentPage = 0;
let pageSize = 10;
let totalPages = 0;
let totalElements = 0;
let isLoading = false;

// Lưu trữ dữ liệu hiện tại
let currentPageBookings = []; // Data từ server cho trang hiện tại

// Thống kê tổng từ PagedBookingInAdmin
let totalStats = {
    total: 0,
    totalCompletedBookings: 0,
    totalCancelledBookings: 0,
    totalPendingBookings: 0
};

// Biến lọc/tìm kiếm
let currentSearchTerm = '';
let currentStatus = '';

document.addEventListener('DOMContentLoaded', function() {

    const bookingTableBody = document.getElementById('bookingTableBody');
    const pagination = document.getElementById('pagination');
    const paginationContainer = document.getElementById('paginationContainer');
    
    if (!bookingTableBody || !pagination || !paginationContainer) {
        console.error('Missing required DOM elements');
        return;
    }

    loadBookingsPage(0);

    // Event listeners cho tìm kiếm và lọc
    const searchForm = document.getElementById('searchForm');
    const searchInput = document.getElementById('searchInput');
    const statusFilter = document.getElementById('statusFilter');

    if (searchForm) {
        searchForm.addEventListener('submit', function(e) {
            e.preventDefault();
            applyFilters();
        });
    }
    
    if (searchInput) {
        searchInput.addEventListener('input', debounce(function() {
            applyFilters();
        }, 500));
    }
    
    if (statusFilter) {
        statusFilter.addEventListener('change', function() {
            applyFilters();
        });
    }
    
 

    function applyFilters() {
        currentSearchTerm = searchInput ? searchInput.value.trim() : '';
        currentStatus = statusFilter ? statusFilter.value : '';
        
        // Reset về trang đầu tiên khi thay đổi filter
        loadBookingsPage(0);
    }

    function loadBookingsPage(page) {
    if (isLoading) return;
    
    console.log(`Loading bookings page ${page} with filters:`, {
        search: currentSearchTerm,
        status: currentStatus
    });

    isLoading = true;
    
    // Hiển thị loading
    showLoadingState();

    // Xây dựng URL với các tham số tìm kiếm và lọc
    let url = `/api/booking/getAllBooking?page=${page}&size=${pageSize}`;
    if (currentSearchTerm) url += `&search=${encodeURIComponent(currentSearchTerm)}`;
    if (currentStatus) url += `&status=${encodeURIComponent(currentStatus)}`;
    
    fetch(url)
        .then(response => {
            // Kiểm tra response status trước
            console.log('Response status:', response.status);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('API Response:', data);
            
            // Kiểm tra nếu data là string (error message)
            if (typeof data === 'string' || (data && data.message)) {
                const errorMessage = typeof data === 'string' ? data : data.message;
                throw new Error(errorMessage || 'Unknown error occurred');
            }
            
            // Cập nhật thông tin phân trang từ server
            currentPage = data.currentPage !== undefined ? data.currentPage : page;
            totalPages = data.totalPages || 0;
            totalElements = data.totalCount || 0;
            
            // Validate pagination data
            if (currentPage >= totalPages && totalPages > 0) {
                console.warn(`Current page ${currentPage} >= totalPages ${totalPages}, adjusting...`);
                currentPage = totalPages - 1;
            }
            
            // Cập nhật thống kê tổng từ PagedBookingInAdmin
            totalStats = {
                total: data.totalCount || 0,
                totalCompletedBookings: data.totalCompletedBookings || 0,
                totalCancelledBookings: data.totalCancelledBookings || 0,
                totalPendingBookings: data.totalPendingBookings || 0
            };
            
            // Lưu data cho trang hiện tại
            currentPageBookings = data.bookings || [];
            
            console.log(`Loaded page ${currentPage + 1}/${totalPages} with ${currentPageBookings.length} bookings, total: ${totalElements}`);
            
            // Cập nhật thống kê UI
            updateTotalStatistics();
            
            // Hiển thị dữ liệu
            displayBookings(currentPageBookings);
            
            // Cập nhật pagination
            updatePagination();
        })
        .catch(error => {
            console.error('Error loading bookings:', error);
            showError(error.message || 'Unknown error occurred');
        })
        .finally(() => {
            isLoading = false;
        });
}
    
    function updateTotalStatistics() {
    document.getElementById('totalBookings').textContent = totalStats.total.toLocaleString('vi-VN');
    document.getElementById('pendingBookings').textContent = totalStats.totalPendingBookings.toLocaleString('vi-VN');
    document.getElementById('completedBookings').textContent = totalStats.totalCompletedBookings.toLocaleString('vi-VN');
    document.getElementById('cancelledBookings').textContent = totalStats.totalCancelledBookings.toLocaleString('vi-VN');
    }   
    function displayBookings(bookings) {
        const tableBody = document.getElementById('bookingTableBody');
        tableBody.innerHTML = '';

        if (bookings.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="8" class="text-center">No bookings found</td></tr>';
            return;
        }

        bookings.forEach(booking => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${booking.bookingId}</td>
                <td>${booking.movieName || 'N/A'}</td>
                <td>${formatDate(booking.showDate)}</td>
                <td>${formatTime(booking.showTime)}</td>
                <td>${booking.roomName || 'N/A'}</td>
                <td>${formatPrice(booking.totalPrice)}</td>
                <td>
                    <span class="badge ${getStatusBadgeClass(booking.status)}">
                        ${booking.status}
                    </span>
                </td>
                <td>
                    <div class="btn-group btn-group-sm">
                        <button class="btn btn-primary view-booking" data-id="${booking.bookingId}" title="View Details">
                            <i class="fas fa-eye"></i>
                        </button>
                    </div>
                </td>
            `;
            tableBody.appendChild(row);
        });
        
        // Thêm event listeners cho các nút xem chi tiết
        document.querySelectorAll('.view-booking').forEach(btn => {
            btn.addEventListener('click', function() {
                const bookingId = this.getAttribute('data-id');
                viewBookingDetails(bookingId);
            });
        });
    }
    
    function updatePagination() {
        const paginationElement = document.getElementById('pagination');
        const paginationContainer = document.getElementById('paginationContainer');
        
        if (!paginationElement || !paginationContainer) {
            return;
        }
        
        paginationElement.innerHTML = '';
        
        if (totalPages <= 1) {
            paginationContainer.style.display = 'none';
            return;
        }
        
        paginationContainer.style.display = 'block';
        
        // Nút Previous
        const prevLi = document.createElement('li');
        prevLi.className = `page-item ${currentPage == 0 ? 'disabled' : ''}`;
        const prevLink = document.createElement('a');
        prevLink.className = 'page-link';
        prevLink.href = '#';
        prevLink.textContent = 'Previous';
        if (currentPage > 0) {
            prevLink.addEventListener('click', function(e) {
                e.preventDefault();
                loadBookingsPage(currentPage - 1);
            });
        }
        prevLi.appendChild(prevLink);
        paginationElement.appendChild(prevLi);
        
        // Quyết định trang nào hiển thị
        const pagesToShow = new Set();
        
        // Luôn hiển thị trang đầu tiên
        pagesToShow.add(0);
        
        // Luôn hiển thị trang cuối cùng
        pagesToShow.add(totalPages - 1);
        
        // Hiển thị trang hiện tại
        pagesToShow.add(currentPage);
        
        // Hiển thị trang trước trang hiện tại (nếu có)
        if (currentPage > 0) {
            pagesToShow.add(currentPage - 1);
        }
        
        // Hiển thị trang sau trang hiện tại (nếu có)
        if (currentPage < totalPages - 1) {
            pagesToShow.add(currentPage + 1);
        }
        
        // Sắp xếp các trang theo thứ tự tăng dần
        const sortedPages = Array.from(pagesToShow).sort((a, b) => a - b);
        
        // Tạo các nút trang và dấu "..."
        let prevPage = -1;
        
        for (const page of sortedPages) {
            // Nếu có khoảng cách giữa các trang, thêm dấu "..."
            if (prevPage !== -1 && page > prevPage + 1) {
                const ellipsisLi = document.createElement('li');
                ellipsisLi.className = 'page-item disabled';
                const ellipsisLink = document.createElement('a');
                ellipsisLink.className = 'page-link';
                ellipsisLink.href = '#';
                ellipsisLink.textContent = '...';
                ellipsisLi.appendChild(ellipsisLink);
                paginationElement.appendChild(ellipsisLi);
            }
            
            // Tạo nút cho trang
            const pageLi = document.createElement('li');
            pageLi.className = `page-item ${currentPage == page ? 'active' : ''}`;
            const pageLink = document.createElement('a');
            pageLink.className = 'page-link';
            pageLink.href = '#';
            pageLink.textContent = page + 1; // Hiển thị số trang bắt đầu từ 1, không phải 0
            
            // Thêm event listener chỉ khi không phải trang hiện tại
            if (currentPage != page) {
                pageLink.addEventListener('click', function(e) {
                    e.preventDefault();
                    loadBookingsPage(page);
                });
            }
            
            pageLi.appendChild(pageLink);
            paginationElement.appendChild(pageLi);
            
            prevPage = page;
        }
        
        // Nút Next
        const nextLi = document.createElement('li');
        nextLi.className = `page-item ${currentPage == totalPages - 1 ? 'disabled' : ''}`;
        const nextLink = document.createElement('a');
        nextLink.className = 'page-link';
        nextLink.href = '#';
        nextLink.textContent = 'Next';
        if (currentPage < totalPages - 1) {
            nextLink.addEventListener('click', function(e) {
                e.preventDefault();
                loadBookingsPage(currentPage + 1);
            });
        }
        nextLi.appendChild(nextLink);
        paginationElement.appendChild(nextLi);
    }

    function viewBookingDetails(bookingId) {
        // Hiển thị loading state trên button
        const viewButton = document.querySelector(`button[data-id="${bookingId}"]`);
        if (viewButton) {
            const originalContent = viewButton.innerHTML;
            viewButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
            viewButton.disabled = true;
            
            // Fetch ticket details from API
            fetch(`/api/booking/ticket-detail/${bookingId}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Failed to fetch booking details');
                    }
                    return response.json();
                })
                .then(booking => {
                    console.log('Booking details:', booking);
                    fillBookingModal(booking);
                    
                    // Open modal
                    const modal = new bootstrap.Modal(document.getElementById('bookingDetailsModal'));
                    modal.show();
                })
                .catch(error => {
                    console.error('Error fetching booking details:', error);
                    alert('Error loading booking details: ' + error.message);
                })
                .finally(() => {
                    // Restore button state
                    viewButton.innerHTML = originalContent;
                    viewButton.disabled = false;
                });
        }
    }

    function fillBookingModal(booking) {
        // Booking Information
        document.getElementById('modalBookingId').textContent = booking.bookingId || 'N/A';
        document.getElementById('modalBookingDate').textContent = formatDateTime(booking.bookingDate);
        document.getElementById('modalStatus').innerHTML = `
            <span class="badge ${getStatusBadgeClass(booking.status)}">${booking.status}</span>
        `;
        document.getElementById('modalPaymentMethod').textContent = booking.paymentMethod || 'N/A';
        document.getElementById('modalTotalAmount').textContent = formatPrice(booking.totalAmount);
        
        // Customer Information
        document.getElementById('modalCustomerId').textContent = booking.customerId || 'N/A';
        document.getElementById('modalCustomerName').textContent = booking.customerName || 'N/A';
        document.getElementById('modalCustomerEmail').textContent = booking.customerEmail || 'N/A';
        document.getElementById('modalCustomerPhone').textContent = booking.customerPhone || 'N/A';
        document.getElementById('modalCustomerPoints').textContent = 
            booking.memberPoints ? booking.memberPoints.toLocaleString('vi-VN') + ' points' : 'N/A';
        
        // Ticket Information
        document.getElementById('modalMovie').textContent = booking.movieName || 'N/A';
        document.getElementById('modalShowDate').textContent = formatDate(booking.showDate);
        document.getElementById('modalShowTime').textContent = formatTime(booking.showTime);
        document.getElementById('modalRoom').textContent = booking.roomName || 'N/A';
        document.getElementById('modalSeats').textContent = booking.seats || 'N/A';
        document.getElementById('modalTicketPrice').textContent = formatPrice(booking.ticketPrice);
        document.getElementById('modalFoodPrice').textContent = formatPrice(booking.foodBeveragePrice);
        document.getElementById('modalDiscount').textContent = formatPrice(booking.discountAmount);
        
        // Food & Beverage Details
        const foodList = document.getElementById('modalFoodList');
        foodList.innerHTML = '';
        
        // Xóa nội dung promotion/points cũ
        const promotionPointsContainer = document.getElementById('promotionPointsContainer');
        if (promotionPointsContainer) {
            promotionPointsContainer.innerHTML = '';
        }
        
        if (booking.combos && booking.combos.length > 0) {
            booking.combos.forEach(combo => {
                const li = document.createElement('li');
                li.innerHTML = `
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <i class="fas fa-utensils me-2 text-primary"></i>
                            <strong>${combo.comboName}</strong>
                            <small class="text-muted"> - ${formatPrice(combo.unitPrice)} each</small>
                        </div>
                        <div class="text-end">
                            <span class="badge bg-secondary">x${combo.quantity}</span>
                            <div class="fw-bold text-success">${formatPrice(combo.totalPrice)}</div>
                        </div>
                    </div>
                `;
                foodList.appendChild(li);
            });
        } else {
            foodList.innerHTML = '<li class="text-muted"><i class="fas fa-info-circle me-2"></i>No food & beverage items</li>';
        }
        
        // Promotion Information (if exists)
        if (booking.promotionCode && promotionPointsContainer) {
            const promoInfo = document.createElement('div');
            promoInfo.className = 'alert alert-success mt-3';
            promoInfo.innerHTML = `
                <h6><i class="fas fa-tag me-2"></i>Promotion Applied</h6>
                <div><strong>Code:</strong> ${booking.promotionCode}</div>
                <div><strong>Name:</strong> ${booking.promotionName || 'N/A'}</div>
                <div><strong>Discount:</strong> ${formatPrice(booking.promotionDiscount)}</div>
            `;
            promotionPointsContainer.appendChild(promoInfo);
        }
        
        // Points Information (if used)
        if (booking.usedPoints && booking.usedPoints > 0 && promotionPointsContainer) {
            const pointsInfo = document.createElement('div');
            pointsInfo.className = 'alert alert-info mt-3';
            pointsInfo.innerHTML = `
                <h6><i class="fas fa-star me-2"></i>Points Used</h6>
                <div><strong>Points:</strong> ${booking.usedPoints.toLocaleString('vi-VN')} points</div>
                <div><strong>Discount:</strong> ${formatPrice(booking.usedPoints * 10)}</div>
            `;
            promotionPointsContainer.appendChild(pointsInfo);
        }
    }

    function formatDateTime(dateTimeString) {
        if (!dateTimeString) return 'N/A';
        const date = new Date(dateTimeString);
        return date.toLocaleDateString('vi-VN') + ' ' + date.toLocaleTimeString('vi-VN');
    }

    function formatDate(dateString) {
        if (!dateString) return 'N/A';
        const date = new Date(dateString);
        return date.toLocaleDateString('vi-VN');
    }

    function formatTime(timeString) {
        if (!timeString) return 'N/A';
        if (typeof timeString === 'string') {
            return timeString.substring(0, 5);
        } else if (timeString && timeString.hour !== undefined && timeString.minute !== undefined) {
            return `${String(timeString.hour).padStart(2, '0')}:${String(timeString.minute).padStart(2, '0')}`;
        }
        return 'N/A';
    }

    function formatPrice(price) {
        if (!price && price !== 0) return '0 VND';
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(price);
    }

    function getStatusBadgeClass(status) {
        switch (status) {
            case 'PENDING':
                return 'bg-warning';
            case 'SUCCESS':
                return 'bg-success';
            case 'CANCELLED':
                return 'bg-danger';
            default:
                return 'bg-secondary';
        }
    }

    function showLoadingState() {
        document.getElementById('bookingTableBody').innerHTML = `
            <tr>
                <td colspan="8" class="loading-cell">
                    <div class="loading-spinner"></div>
                    <div>Loading bookings...</div>
                </td>
            </tr>
        `;
    }

    function showError(message) {
        document.getElementById('bookingTableBody').innerHTML = `
            <tr>
                <td colspan="8" class="text-center text-danger">
                    <i class="fas fa-exclamation-triangle me-2"></i>
                    Error loading bookings: ${message}
                    <br>
                    <button class="btn btn-sm btn-outline-danger mt-2" onclick="location.reload()">
                        <i class="fas fa-redo me-1"></i>Retry
                    </button>
                </td>
            </tr>
        `;
    }
    
    function debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }
});
