/**
 * Danh sách các loại ghế - chỉ bao gồm STANDARD và VIP
 */
const SEAT_TYPES = ["STANDARD", "VIP"];

/**
 * Xử lý click trái - đổi loại ghế giữa STANDARD và VIP
 */
function handleLeftClick(button) {
  const row = button.getAttribute("data-row");
  // Lấy tất cả các button cùng hàng (cùng data-row)
  const rowButtons = document.querySelectorAll(`.btn[data-row='${row}']`);
  if (rowButtons.length === 0) return;
  // Xác định loại ghế hiện tại của hàng (lấy ghế đầu tiên làm chuẩn)
  const firstCol = rowButtons[0].getAttribute("data-col");
  const seatTypeInputFirst = document.getElementById(
    `seatType_${row}_${firstCol}`
  );
  const isActiveInputFirst = document.getElementById(
    `isActive_${row}_${firstCol}`
  );
  const currentSeatType = seatTypeInputFirst.value;
  const currentIsActive = isActiveInputFirst.value === "true";

  if (currentIsActive) {
    const newSeatType = currentSeatType === "STANDARD" ? "VIP" : "STANDARD";
    rowButtons.forEach((btn) => {
      const col = btn.getAttribute("data-col");
      const seatTypeInput = document.getElementById(`seatType_${row}_${col}`);
      const isActiveInput = document.getElementById(`isActive_${row}_${col}`);
      // Chỉ đổi loại ghế nếu ghế đang active
      if (isActiveInput.value === "true") {
        seatTypeInput.value = newSeatType;
        updateButtonUI(btn, newSeatType, true, row, col);
      }
    });
  }
}

/**
 * Cập nhật giao diện button
 */
function updateButtonUI(button, seatType, isActive, row, col) {
  // Only display seat label (e.g., "A1") without status text
  button.textContent = col + row;
  button.className = "btn ";
  if (isActive) {
    if (seatType === "VIP") {
      button.className += "vip";
    } else if (seatType === "STANDARD") {
      button.className += "normal";
    }
  } else {
    button.className += "inactive";
  }
}

/**
 * Hàm legacy để tương thích với code cũ (nếu cần)
 */
function toggleSeat(button) {
  handleLeftClick(button);
}

// Thêm event listener cho keyboard shortcuts (tùy chọn)
document.addEventListener("keydown", function (event) {
  if (event.key === "Escape") {
    const allButtons = document.querySelectorAll(".btn[data-row]");
    allButtons.forEach((button) => {
      const row = button.getAttribute("data-row");
      const col = button.getAttribute("data-col");
      const seatTypeInput = document.getElementById(`seatType_${row}_${col}`);
      const isActiveInput = document.getElementById(`isActive_${row}_${col}`);
      seatTypeInput.value = "STANDARD";
      isActiveInput.value = "true";
      updateButtonUI(button, "STANDARD", true, row, col);
    });
  }
});

// Thêm và điều chỉnh screen area động dựa trên độ rộng của table
document
  .querySelector("#createRoomForm")
  .addEventListener("submit", function (e) {
    setTimeout(() => {
      const seatMapForm = document.getElementById("seatForm");
      if (seatMapForm) {
        const seatTable = document.getElementById("seatTable");
        const dynamicScreen = document.getElementById("dynamicScreen");
        if (seatTable && !dynamicScreen.textContent) {
          // Lấy độ rộng của table dựa trên số cột
          const columns = seatTable.querySelectorAll("tr:first-child td");
          const totalWidth = columns.length * (80 + 10); // 80px width của btn + 10px border-spacing
          dynamicScreen.style.width = `${totalWidth}px`;
          dynamicScreen.textContent = "Screen";
          dynamicScreen.style.margin = "0 auto"; // Căn giữa
        }
      }
    }, 100); // Delay để đảm bảo Thymeleaf render xong
  });

console.log("Seat Management System loaded successfully!");
console.log("Available seat types:", SEAT_TYPES);

setTimeout(() => {
  const alerts = document.querySelectorAll(".alert");
  if (alerts.length > 0 && typeof bootstrap !== "undefined") {
    alerts.forEach((alert) => {
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
