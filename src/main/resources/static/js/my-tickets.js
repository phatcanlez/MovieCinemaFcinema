let currentPage = 0;
const pageSize = 6;
let totalPages = 0;
let totalElements = 0;
let isLoading = false;

document.addEventListener('DOMContentLoaded', function() {
    fetchTicketsPage(0);
});

function fetchTicketsPage(page) {
    if (isLoading) return;

    console.log(`Fetching tickets page ${page} from API...`);
    isLoading = true;

    showLoadingState();
    hideError();

    fetch(`/customer/api/my-tickets?page=${page}&size=${pageSize}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch tickets');
            }
            return response.json();
        })
        .then(data => {
            console.log('API Response:', data);

            currentPage = data.currentPage;
            totalPages = data.totalPages;
            totalElements = data.totalElements;

            displayTickets(data.tickets);
            displayPagination();
            addCardEffects();
        })
        .catch(error => {
            console.error('Error fetching tickets:', error);
            showError('Không thể tải thông tin vé. Vui lòng thử lại sau.');
        })
        .finally(() => {
            isLoading = false;
        });
}

function displayTickets(tickets) {
    const grid = document.querySelector('.tickets-grid');
    const noTicketsMessage = document.querySelector('.text-center.my-4');

    if (tickets.length === 0) {
        grid.innerHTML = '';
        noTicketsMessage.style.display = 'block';
        document.getElementById('pagination-nav').style.display = 'none';
        document.getElementById('pagination-wrapper').style.display = 'none';
        return;
    }

    noTicketsMessage.style.display = 'none';

    grid.innerHTML = tickets.map(ticket => {
        const statusClass = getStatusClass(ticket.status);
        const statusText = getStatusText(ticket.status);

        // Parse combos from JSON string
        let combosObj = {};
        try {
            combosObj = ticket.combos && ticket.combos !== '{}' ? JSON.parse(ticket.combos) : {};
        } catch (e) {
            console.warn('Failed to parse combos:', ticket.combos);
        }

        return `
            <div class="ticket-card">
                <div class="ticket-left">
                    <div class="ticket-movie-title">${ticket.movieTitle}</div>
                    <div class="ticket-booking-id">Mã: ${ticket.bookingId}</div>
                    <div class="ticket-cinema">${ticket.cinemaName}</div>
                </div>
                <div class="ticket-right">
                    <div class="ticket-details-grid">
                        <div class="ticket-detail-item">
                            <div class="ticket-detail-label">Ngày chiếu</div>
                            <div class="ticket-detail-value">${formatDate(ticket.bookingDateTime)}</div>
                        </div>
                        <div class="ticket-detail-item">
                            <div class="ticket-detail-label">Giờ chiếu</div>
                            <div class="ticket-detail-value">${formatTime(ticket.bookingDateTime)}</div>
                        </div>
                        <div class="ticket-detail-item">
                            <div class="ticket-detail-label">Ghế</div>
                            <div class="ticket-detail-value">${ticket.seats}</div>
                        </div>
                    </div>

                    ${Object.keys(combosObj).length > 0 ? `
                        <div class="ticket-combo">
                            <div class="ticket-combo-title">Combo đã chọn:</div>
                            <ul class="ticket-combo-list">
                                ${Object.entries(combosObj).map(([name, quantity]) =>
                                    `<li>${name} x${quantity}</li>`
                                ).join('')}
                            </ul>
                        </div>
                    ` : ''}

                    <div class="ticket-bottom">
                        <div class="ticket-price">${ticket.totalPrice.toLocaleString('vi-VN')}đ</div>
                        <div class="ticket-status ${statusClass}">${statusText}</div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function displayPagination() {
    const paginationContainer = document.querySelector('.pagination');

    if (totalPages <= 1) {
        paginationContainer.parentElement.style.display = 'none';
        return;
    }

    paginationContainer.parentElement.style.display = 'block';
    paginationContainer.innerHTML = '';

    // Previous button
    const prevLi = document.createElement('li');
    prevLi.className = `page-item ${currentPage === 0 ? 'disabled' : ''}`;
    prevLi.innerHTML = `
        <a class="page-link" href="#" aria-label="Previous">
            <span aria-hidden="true">&laquo;</span>
            <span class="sr-only">Previous</span>
        </a>
    `;
    if (currentPage > 0) {
        prevLi.querySelector('.page-link').onclick = (e) => {
            e.preventDefault();
            changePage(currentPage - 1);
        };
    } else {
        prevLi.querySelector('.page-link').onclick = (e) => e.preventDefault();
    }
    paginationContainer.appendChild(prevLi);

    // Calculate page range (show max 5 pages)
    let startPage = Math.max(0, currentPage - 2);
    let endPage = Math.min(totalPages - 1, startPage + 4);

    // Adjust start if we're near the end
    if (endPage - startPage < 4) {
        startPage = Math.max(0, endPage - 4);
    }

    // Add page numbers
    for (let i = startPage; i <= endPage; i++) {
        const pageLi = document.createElement('li');
        pageLi.className = `page-item ${i === currentPage ? 'active' : ''}`;
        pageLi.innerHTML = `<a class="page-link" href="#">${i + 1}</a>`;
        pageLi.querySelector('.page-link').onclick = (e) => {
            e.preventDefault();
            changePage(i);
        };
        paginationContainer.appendChild(pageLi);
    }

    // Next button
    const nextLi = document.createElement('li');
    nextLi.className = `page-item ${currentPage === totalPages - 1 ? 'disabled' : ''}`;
    nextLi.innerHTML = `
        <a class="page-link" href="#" aria-label="Next">
            <span aria-hidden="true">&raquo;</span>
            <span class="sr-only">Next</span>
        </a>
    `;
    if (currentPage < totalPages - 1) {
        nextLi.querySelector('.page-link').onclick = (e) => {
            e.preventDefault();
            changePage(currentPage + 1);
        };
    } else {
        nextLi.querySelector('.page-link').onclick = (e) => e.preventDefault();
    }
    paginationContainer.appendChild(nextLi);
}

function changePage(page) {
    if (page < 0 || page >= totalPages || page === currentPage || isLoading) {
        return;
    }

    console.log(`Changing to page ${page}`);
    fetchTicketsPage(page);

    // Smooth scroll to top of tickets
    document.querySelector('.tickets-grid').scrollIntoView({
        behavior: 'smooth',
        block: 'start'
    });
}

function initializePaginationAnimation() {
    const wrapper = document.getElementById('pagination-wrapper');
    const activeIndex = document.querySelector('.pagination-index.active');

    if (!wrapper || !activeIndex) return;

    wrapper.classList.remove('open', 'i1', 'i2', 'i3', 'i4', 'i5');

    setTimeout(() => {
        wrapper.classList.add('open');

        const allIndexes = document.querySelectorAll('.pagination-index:not(.disabled)');
        const activePosition = Array.from(allIndexes).indexOf(activeIndex);

        if (activePosition >= 0 && activePosition < 5) {
            wrapper.classList.add(`i${activePosition + 1}`);
        }
    }, 100);
}

function changePage(page) {
    if (page < 0 || page >= totalPages || page === currentPage || isLoading) {
        return;
    }

    console.log(`Changing to page ${page}`);

    // Add flip animation
    const wrapper = document.getElementById('pagination-wrapper');
    if (wrapper) {
        if (page > currentPage) {
            wrapper.classList.add('flip');
        } else {
            wrapper.classList.remove('flip');
        }
    }

    fetchTicketsPage(page);

    // Smooth scroll to top of tickets
    document.querySelector('.tickets-grid').scrollIntoView({
        behavior: 'smooth',
        block: 'start'
    });
}

function getStatusClass(status) {
    switch (status) {
        case 'SUCCESS':
        case 'CONFIRMED':
            return 'status-confirmed';
        case 'PENDING':
            return 'status-pending';
        case 'CANCELLED':
            return 'status-cancelled';
        case 'USED':
            return 'status-used';
        default:
            return 'status-unknown';
    }
}

function getStatusText(status) {
    switch (status) {
        case 'SUCCESS':
        case 'CONFIRMED':
            return 'Đã xác nhận';
        case 'PENDING':
            return 'Chờ xử lý';
        case 'CANCELLED':
            return 'Đã hủy';
        case 'USED':
            return 'Đã sử dụng';
        default:
            return 'Không xác định';
    }
}

function showLoadingState() {
    const grid = document.querySelector('.tickets-grid');
    grid.innerHTML = '<div class="simple-loading">Đang tải vé của bạn...</div>';
}

function hideError() {
    const alert = document.querySelector('.alert-danger');
    if (alert) {
        alert.style.display = 'none';
    }
}

function showError(message) {
    const alert = document.querySelector('.alert-danger');
    if (alert) {
        alert.textContent = message;
        alert.style.display = 'block';
        alert.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
}

function formatDate(date) {
    return new Date(date).toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });
}

function formatTime(date) {
    return new Date(date).toLocaleTimeString('vi-VN', {
        hour: '2-digit',
        minute: '2-digit'
    });
}

function addCardEffects() {
    document.querySelectorAll('.ticket-card').forEach(card => {
        card.addEventListener('mouseenter', () => {
            card.style.transform = 'translateY(-5px) scale(1.02)';
        });

        card.addEventListener('mouseleave', () => {
            card.style.transform = 'translateY(0) scale(1)';
        });
    });
}