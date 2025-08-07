const canvas = document.getElementById("scheduleCanvas");
const ctx = canvas.getContext("2d");

const startHour = 8;
const endHour = 24;
const hourWidth = 70; //Khoảng cách giờ
const roomHeight = 60; //Độ cao nhãn room
const labelWidth = 180; //Độ dài schedule
const headerHeight = 40; //Độ cao header
const spacing = 30; //Khoảng cách room với giờ
const gridStartX = labelWidth + spacing;

let rooms = [];
let filteredRooms = [];
let movies = [];
let movieBlocks = [];

// Hàm format thời gian từ số thập phân sang HH:MM
function formatTime(timeDecimal) {
  const hours = Math.floor(timeDecimal);
  const minutes = Math.round((timeDecimal - hours) * 60);
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
}

// Lấy ngày hiện tại yyyy-MM-dd
function getTodayStr() {
  const today = new Date();
  const yyyy = today.getFullYear();
  const mm = String(today.getMonth() + 1).padStart(2, "0");
  const dd = String(today.getDate()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}`;
}

const dateInput = document.getElementById("date-input");
dateInput.value = getTodayStr();

function fetchAndRenderAll(showDateStr) {
  fetch("/api/room-list")
    .then((res) => res.json())
    .then((roomData) => {
      rooms = roomData;
      filteredRooms = rooms;
      return fetch(`/api/schedule/movie-room-calender?showDate=${showDateStr}`);
    })
    .then((res) => res.json())
    .then((movieData) => {
      console.log("Movie data for date", showDateStr, movieData);
      movies = movieData
        .map((m) => ({
          name: m.movieName,
          room: rooms.indexOf(m.roomName),
          start:
            parseFloat(m.startTime.split(":")[0]) +
            parseFloat(m.startTime.split(":")[1]) / 60,
          end:
            parseFloat(m.endTime.split(":")[0]) +
            parseFloat(m.endTime.split(":")[1]) / 60,
          id: m.movieId,
          scheduleId: m.scheduleId,
          showDateId: m.showDateId,
        }))
        .filter((m) => m.room >= 0);
      renderSchedule();
    });
}

fetchAndRenderAll(dateInput.value);

dateInput.addEventListener("change", function () {
  fetchAndRenderAll(this.value);
});

function renderSchedule() {
  canvas.width = gridStartX + (endHour - startHour) * hourWidth + 40;
  canvas.height = headerHeight + filteredRooms.length * roomHeight + 40;

  ctx.clearRect(0, 0, canvas.width, canvas.height);

  // Vẽ nền trắng sọc xám nhẹ
  ctx.fillStyle = "#ffffff";
  ctx.fillRect(0, 0, canvas.width, canvas.height);
  
  // Vẽ các sọc xám nhẹ theo chiều dọc (theo room)
  ctx.fillStyle = "#f8f9fa";
  for (let i = 0; i < filteredRooms.length; i++) {
    if (i % 2 === 1) { // Chỉ tô màu xám cho các hàng lẻ
      const y = headerHeight + i * roomHeight;
      ctx.fillRect(0, y, canvas.width, roomHeight);
    }
  }
  
  // Vẽ các sọc xám nhẹ theo chiều ngang (theo giờ)
  ctx.fillStyle = "#f1f3f4";
  for (let h = startHour; h <= endHour; h += 2) { // Cứ 2 giờ một sọc
    const x = gridStartX + (h - startHour) * hourWidth;
    ctx.fillRect(x, headerHeight, hourWidth, filteredRooms.length * roomHeight);
  }

  //Tiêu đề "Room" - nền trắng chữ đỏ
  ctx.fillStyle = "#ffffff"; // Nền trắng cho header
  ctx.fillRect(0, 0, labelWidth, headerHeight);
  ctx.strokeStyle = "#dee2e6"; // Viền xám nhẹ
  ctx.strokeRect(0, 0, labelWidth, headerHeight);
  ctx.fillStyle = "rgba(230,33,52,0.86)"; // Chữ đỏ
  ctx.font = "bold 18px Arial";
  ctx.textAlign = "center";
  ctx.fillText("ROOM", labelWidth / 2, headerHeight / 2 + 7);

  //Nhãn room - chữ đỏ nền trắng
  filteredRooms.forEach((room, i) => {
    const y = headerHeight + i * roomHeight;
    ctx.fillStyle = "#ffffff"; // Nền trắng
    ctx.fillRect(0, y, labelWidth, roomHeight);
    ctx.strokeStyle = "#dee2e6"; // Viền xám nhẹ
    ctx.strokeRect(0, y, labelWidth, roomHeight);
    ctx.fillStyle = "rgba(230,33,52,0.86)"; // Chữ đỏ
    ctx.font = "bold 17px Arial";
    ctx.textAlign = "left";
    ctx.fillText(room, 15, y + roomHeight / 2 + 5);
  });

  //Đường giờ và nhãn giờ
  ctx.strokeStyle = "#dee2e6"; // Màu xám nhẹ cho grid lines
  ctx.fillStyle = "rgba(230,33,52,0.86)";
  ctx.font = "bold 18px Arial";
  ctx.textAlign = "center";

  for (let h = startHour; h <= endHour; h++) {
    const x = gridStartX + (h - startHour) * hourWidth;
    ctx.beginPath();
    ctx.moveTo(x, headerHeight);
    ctx.lineTo(x, headerHeight + filteredRooms.length * roomHeight);
    ctx.stroke();
    ctx.fillText(h + ":00", x, headerHeight - 10);
  }
  
  // Vẽ các đường ngang cho từng phòng
  ctx.strokeStyle = "#dee2e6";
  for (let i = 0; i <= filteredRooms.length; i++) {
    const y = headerHeight + i * roomHeight;
    ctx.beginPath();
    ctx.moveTo(gridStartX, y);
    ctx.lineTo(gridStartX + (endHour - startHour) * hourWidth, y);
    ctx.stroke();
  }

  //Khối phim
  movieBlocks.length = 0;
  movies.forEach((movie) => {
    const roomIdx = filteredRooms.indexOf(rooms[movie.room]);
    if (roomIdx === -1) return;

    const x = gridStartX + (movie.start - startHour) * hourWidth;
    const width = (movie.end - movie.start) * hourWidth;
    const y = headerHeight + roomIdx * roomHeight + 5;
    const height = roomHeight - 10;

    const gradient = ctx.createLinearGradient(x, y, x + width, y);
    gradient.addColorStop(0, "#dc3545");
    gradient.addColorStop(1, "#ff1744");
    ctx.fillStyle = gradient;
    ctx.fillRect(x, y, width, height);

    ctx.strokeStyle = "#ffffff";
    ctx.lineWidth = 1;
    ctx.strokeRect(x, y, width, height);

    ctx.fillStyle = "white";
    ctx.font = "13px Arial";
    ctx.textAlign = "center";

    let displayText = movie.name;
    const maxWidth = width - 15;
    if (ctx.measureText(displayText).width > maxWidth) {
      while (
        ctx.measureText(displayText + "...").width > maxWidth &&
        displayText.length > 0
      ) {
        displayText = displayText.slice(0, -1);
      }
      displayText += "...";
    }

    ctx.fillText(displayText, x + width / 2, y + height / 2 + 5);
    movieBlocks.push({ ...movie, x, y, width, height });
  });
}

// Search room event
const roomSearchInput = document.getElementById("room-search");
roomSearchInput.addEventListener("input", function () {
  const keyword = this.value.trim().toLowerCase();
  if (!keyword) {
    filteredRooms = rooms;
  } else {
    filteredRooms = rooms.filter((r) => r.toLowerCase().includes(keyword));
  }
  renderSchedule();
});

// Click event for modal
canvas.addEventListener("click", (e) => {
  const rect = canvas.getBoundingClientRect();
  const mouseX = e.clientX - rect.left;
  const mouseY = e.clientY - rect.top;

  const clicked = movieBlocks.find(
    (m) =>
      mouseX >= m.x &&
      mouseX <= m.x + m.width &&
      mouseY >= m.y &&
      mouseY <= m.y + m.height
  );

  if (clicked) {
    const modal = document.getElementById("modal");
    document.getElementById("modalTitle").textContent = clicked.name;
    document.getElementById("modalContent").textContent =
      "Đang tải sơ đồ ghế...";

    // Cập nhật thông tin giờ
    const startTimeFormatted = formatTime(clicked.start);
    const endTimeFormatted = formatTime(clicked.end);
    document.getElementById("startTime").textContent = startTimeFormatted;
    document.getElementById("endTime").textContent = endTimeFormatted;

    // Xóa sơ đồ ghế cũ nếu có
    const seatTable = document.getElementById("seat-table");
    if (seatTable) seatTable.innerHTML = "";
    modal.style.display = "block";

    // Gọi API lấy sơ đồ ghế với đủ tham số
    fetch(
      `/api/booking/seats?movieId=${clicked.id}&showDateId=${clicked.showDateId}&scheduleId=${clicked.scheduleId}`
    )
      .then((res) => res.json())
      .then((data) => {
        console.log("Seat map data:", data);
        renderSeatMapInModal(data);
      })
      .catch(() => {
        document.getElementById("modalContent").textContent =
          "Không thể tải sơ đồ ghế.";
      });
  }
});

// Hàm render sơ đồ ghế vào modal, không cho phép click chọn
function renderSeatMapInModal(data) {
  // Logic giống genSeatMap của seat-selection.js, chỉ khác là disable click
  let nestedMap = new Map();
  const seatTable = document.getElementById("seat-table");
  if (!seatTable) return;
  seatTable.innerHTML = "";

  // Tạo nested map từ dữ liệu API
  for (const [row, seats] of Object.entries(data)) {
    const seatMap = new Map(Object.entries(seats));
    nestedMap.set(Number(row), seatMap);
  }

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
    const sortedCols = Array.from(firstRow.keys()).sort(
      (a, b) => parseInt(a) - parseInt(b)
    );
    sortedCols.forEach((col, index) => {
      const colLabel = document.createElement("div");
      colLabel.className = "col-label";
      // Sử dụng index để tạo chữ cái A, B, C...
      colLabel.textContent = String.fromCharCode(65 + index);
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
    const sortedSeats = Array.from(seats.entries()).sort(
      (a, b) => parseInt(a[0]) - parseInt(b[0])
    );
    for (const [col, seat] of sortedSeats) {
      const seatBtn = document.createElement("button");
      seatBtn.className = "seat";
      seatBtn.value = seat.seatId;
      seatBtn.dataset.row = row;
      seatBtn.dataset.col = col;
      seatBtn.textContent = col + row;
      if (seat.seatType === "VIP") seatBtn.classList.add("vip");
      if (seat.seatStatus === 1) seatBtn.classList.add("occupied");
      // Không cho click chọn ghế
      seatBtn.disabled = true;
      rowElement.appendChild(seatBtn);
    }
    seatTable.appendChild(rowElement);
  }
  document.getElementById("modalContent").textContent = "Sơ đồ ghế";
}

// Modern UI Functions
function refreshSchedule() {
  console.log('Refreshing schedule...');
  showLoading();
  
  const selectedDate = document.getElementById("date-input").value || getTodayStr();
  
  // Hiển thị animation refresh
  const refreshBtn = document.querySelector('.btn-refresh i');
  refreshBtn.classList.add('fa-spin');
  
  setTimeout(() => {
    fetchAndRenderAll(selectedDate);
    refreshBtn.classList.remove('fa-spin');
    hideLoading();
  }, 1000);
}

function exportSchedule() {
  console.log('Exporting schedule...');
  
  const selectedDate = document.getElementById("date-input").value || getTodayStr();
  const exportData = {
    date: selectedDate,
    rooms: filteredRooms,
    movies: movies,
    movieBlocks: movieBlocks,
    exportTime: new Date().toISOString()
  };
  
  // Tạo file JSON để download
  const dataStr = JSON.stringify(exportData, null, 2);
  const dataBlob = new Blob([dataStr], { type: 'application/json' });
  const url = URL.createObjectURL(dataBlob);
  
  // Tạo link download
  const link = document.createElement('a');
  link.href = url;
  link.download = `schedule_${selectedDate}.json`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
  
  // Show success message
  showToast('Schedule exported successfully!', 'success');
}

function addNewSchedule() {
  console.log('Adding new schedule...');
  // Redirect to add schedule page hoặc mở modal add schedule
  window.location.href = '/admin/add-schedule';
}

function showLoading() {
  const loadingElement = document.getElementById('scheduleLoading');
  const emptyElement = document.getElementById('scheduleEmpty');
  
  if (loadingElement) {
    loadingElement.style.display = 'flex';
  }
  if (emptyElement) {
    emptyElement.style.display = 'none';
  }
}

function hideLoading() {
  const loadingElement = document.getElementById('scheduleLoading');
  
  if (loadingElement) {
    loadingElement.style.display = 'none';
  }
  
  // Kiểm tra nếu không có data thì hiển thị empty state
  if (movieBlocks.length === 0) {
    showEmptyState();
  }
}

function showEmptyState() {
  const emptyElement = document.getElementById('scheduleEmpty');
  
  if (emptyElement) {
    emptyElement.style.display = 'flex';
  }
}

function hideEmptyState() {
  const emptyElement = document.getElementById('scheduleEmpty');
  
  if (emptyElement) {
    emptyElement.style.display = 'none';
  }
}

function showToast(message, type = 'info') {
  // Tạo toast notification
  const toast = document.createElement('div');
  toast.className = `toast-notification toast-${type}`;
  toast.innerHTML = `
    <div class="toast-content">
      <i class="fas fa-${type === 'success' ? 'check-circle' : 'info-circle'} me-2"></i>
      <span>${message}</span>
    </div>
  `;
  
  // Thêm styles cho toast
  toast.style.cssText = `
    position: fixed;
    top: 20px;
    right: 20px;
    background: ${type === 'success' ? '#28a745' : '#17a2b8'};
    color: white;
    padding: 12px 20px;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.2);
    z-index: 10000;
    animation: slideInRight 0.3s ease-out;
  `;
  
  document.body.appendChild(toast);
  
  // Tự động xóa sau 3 giây
  setTimeout(() => {
    toast.style.animation = 'slideOutRight 0.3s ease-in';
    setTimeout(() => {
      if (toast.parentNode) {
        toast.parentNode.removeChild(toast);
      }
    }, 300);
  }, 3000);
}

// Cập nhật function fetchAndRenderAll để sử dụng loading states
const originalFetchAndRenderAll = fetchAndRenderAll;
fetchAndRenderAll = function(showDateStr) {
  showLoading();
  hideEmptyState();
  
  return originalFetchAndRenderAll(showDateStr)
    .then(() => {
      hideLoading();
      if (movieBlocks.length === 0) {
        showEmptyState();
      }
    })
    .catch((error) => {
      console.error('Error fetching schedule:', error);
      hideLoading();
      showEmptyState();
    });
};

// Thêm CSS animations cho toast
const style = document.createElement('style');
style.textContent = `
  @keyframes slideInRight {
    from {
      transform: translateX(100%);
      opacity: 0;
    }
    to {
      transform: translateX(0);
      opacity: 1;
    }
  }
  
  @keyframes slideOutRight {
    from {
      transform: translateX(0);
      opacity: 1;
    }
    to {
      transform: translateX(100%);
      opacity: 0;
    }
  }
  
  .toast-content {
    display: flex;
    align-items: center;
    font-weight: 500;
  }
`;
document.head.appendChild(style);
