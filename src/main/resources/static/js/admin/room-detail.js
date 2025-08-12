const cinemaRoomId = new URLSearchParams(window.location.search).get(
  "cinemaRoomId"
);
function handleDateChange() {
  const dateInput = document.getElementById("showDate");
  if (!dateInput) return;

  const selectedDate = dateInput.value;
  if (!selectedDate) {
    alert("Please select a date.");
    return;
  }
  //console.log("Selected date:", selectedDate);
  // Load schedules for the selected date
  loadSchedulesForDate(selectedDate);
}
function loadSchedulesForDate(date) {
  const scheduleSelect = document.getElementById("schedule");
  if (!scheduleSelect) return;

  const apiUrl = `/admin/movie-schedule/schedules?cinemaRoomId=${cinemaRoomId}&showDate=${date}`;
  console.log("Fetching schedules from:", apiUrl);

  fetch(apiUrl)
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }
      return response.json();
    })
    .then((data) => {
      // Xóa các option cũ
      console.log("Received data:", data);
      scheduleSelect.innerHTML = "";

      if (!data || data.length === 0) {
        const option = document.createElement("option");
        option.textContent = "No schedules available";
        option.disabled = true;
        option.selected = true;
        scheduleSelect.appendChild(option);
        return;
      }

      // Thêm option mặc định
      const defaultOption = document.createElement("option");
      defaultOption.textContent = "-- Select Schedule --";
      defaultOption.disabled = true;
      defaultOption.selected = true;
      scheduleSelect.appendChild(defaultOption);

      // Thêm option từ API
      data.forEach((item) => {
        const option = document.createElement("option");
        option.dataset.scheduleId = item.scheduleId;
        option.dataset.movieId = item.movieId;
        option.dataset.showDateId = item.showDateId;
        option.dataset.largeImg = item.largeImg;
        option.textContent = `${item.time} - ${item.movieName}`;
        scheduleSelect.appendChild(option);
      });
    })
    .catch((error) => {
      console.error("Error loading schedules:", error);
      alert("Failed to load schedules. Please try again later.");
    });
}
function handleScheduleSelect() {
  const scheduleSelect = document.getElementById("schedule");
  if (!scheduleSelect) return;

  const selectedOption = scheduleSelect.options[scheduleSelect.selectedIndex];
  if (!selectedOption || selectedOption.disabled) {
    alert("Please select a valid schedule.");
    return;
  }

  const scheduleId = selectedOption.dataset.scheduleId;
  const movieId = selectedOption.dataset.movieId;
  const showDateId = selectedOption.dataset.showDateId;
  const largeImg = selectedOption.dataset.largeImg;

  const bgContainer = document.body;
  if (bgContainer && largeImg) {
    bgContainer.style.backgroundImage = `linear-gradient(rgba(0, 0, 0, 0.8), rgba(0,0,0,0.8)), url('${largeImg}')`;
    bgContainer.style.backgroundSize = "cover";
    bgContainer.style.backgroundPosition = "center";
  }

  fetch(
    `/api/booking/seats?movieId=${movieId}&showDateId=${showDateId}&scheduleId=${scheduleId}`
  )
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.json();
    })
    .then((data) => {
      console.log("Seats data:", data);
      genSeatMap(data);
    })
    .catch((error) => {
      console.error("Error fetching seats:", error);
      alert("An error occurred while loading seat information.");
    });
}

function genSeatMap(data) {
  nestedMap = new Map();
  const seatTable = document.getElementById("seat-table");
  if (!seatTable) return;

  seatTable.innerHTML = "";

  // Chuyển dữ liệu thành nestedMap (row → Map(col → seat))
  for (const [row, seats] of Object.entries(data)) {
    const seatMap = new Map(Object.entries(seats));
    nestedMap.set(Number(row), seatMap);
  }

  console.log("Seat data loaded:", nestedMap);

  // Render table
  for (const [row, seats] of nestedMap) {
    const rowElement = document.createElement("tr");

    for (const [col, seat] of seats) {
      const seatElement = document.createElement("td");
      const seatButton = document.createElement("button");

      // Style loại ghế
      seatButton.className = `btn ${
        seat.seatType?.toUpperCase() === "VIP" ? "vip" : "normal"
      }`;

      seatButton.textContent = seat.seatColumn + seat.seatRow;
      seatButton.value = seat.seatId;

      // Thêm data attribute để khi cần biết thông tin
      seatButton.dataset.row = seat.seatRow;
      seatButton.dataset.col = seat.seatColumn;
      seatButton.dataset.type = seat.seatType;
      seatButton.dataset.price = seat.seatPrice ?? "";

      // Nếu ghế đã được đặt
      if (seat.seatStatus === 1) {
        //seatButton.classList.add("occupied");
        seatButton.style.border = "1px solid gold";
      }
      seatButton.disabled = true;
      seatElement.appendChild(seatButton);
      rowElement.appendChild(seatElement);
    }

    seatTable.appendChild(rowElement);
  }
}

let nestedMap = new Map();
let originalSeatMap = new Map();
let canEditRoom = false; // trạng thái có thể chỉnh sửa hay không

document.addEventListener("DOMContentLoaded", async () => {
  await checkCanRenameRoom();
  await loadSeatMap();
});

/* ---------- Kiểm tra và render UI Rename ---------- */
async function checkCanRenameRoom() {
  try {
    const res = await fetch(`/api/can-rename-room`, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: `cinemaRoomId=${cinemaRoomId}`,
    });
    const canRename = await res.text();
    const titleElem = document.querySelector(".page-title");
    if (!titleElem) return;

    if (canRename === "true") {
      canEditRoom = true;
      renderRenameUI(
        titleElem,
        document.querySelector(".page-title span")?.textContent || ""
      );
      const scheduleSelect = document.getElementById("selectSchedule");
      if (scheduleSelect) {
        scheduleSelect.style.display = "none";
      }
    } else {
      canEditRoom = false;
      const usedMsg = document.createElement("div");
      usedMsg.className = "fst-italic text-white";
      usedMsg.style.marginTop = "6px";
      usedMsg.style.fontSize = "0.92rem";
      usedMsg.style.opacity = "0.95";
      usedMsg.textContent =
        "Room already has schedules, cannot rename or modify seats.";
      titleElem.appendChild(usedMsg);

      // Disable nút save
      const saveBtn = document.getElementById("save-seat-map");
      if (saveBtn) {
        saveBtn.disabled = true;
        saveBtn.innerText = "Cannot modify seats";
      }
    }
  } catch {
    console.error("Error checking rename status");
  }
}

/* ---------- Render UI Rename ---------- */
function renderRenameUI(container, currentName) {
  const wrapper = document.createElement("div");
  wrapper.className = "mt-2 d-flex align-items-center";

  const info = document.createElement("span");
  info.className = "fst-italic text-white";
  info.style.fontSize = "0.92rem";
  info.textContent = "Room has no schedules, you can rename.";

  const renameBtn = document.createElement("button");
  renameBtn.className = "btn btn-warning btn-sm ms-2";
  renameBtn.textContent = "Rename";

  renameBtn.onclick = () => showRenameInput(wrapper, currentName);

  wrapper.append(info, renameBtn);
  container.appendChild(wrapper);
}

/* ---------- Hiển thị input đổi tên ---------- */
function showRenameInput(container, currentName) {
  container.innerHTML = "";

  const input = document.createElement("input");
  input.type = "text";
  input.className = "form-control d-inline-block";
  input.style.width = "220px";
  input.placeholder = "Enter new room name...";
  input.value = currentName;

  const confirmBtn = document.createElement("button");
  confirmBtn.className = "btn btn-success btn-sm ms-2";
  confirmBtn.textContent = "Confirm";

  const msg = document.createElement("span");
  msg.className = "fst-italic text-white ms-2";
  msg.style.fontSize = "0.92rem";

  confirmBtn.onclick = async () => {
    const newName = input.value.trim();
    if (!newName) {
      msg.textContent = "Please enter a new room name.";
      return;
    }

    try {
      const res = await fetch(`/api/rename-room`, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: `cinemaRoomId=${cinemaRoomId}&newRoomName=${encodeURIComponent(
          newName
        )}`,
      });
      const result = await res.text();

      if (result === "success") {
        document.querySelector(".page-title span").textContent = newName;
        container.innerHTML = "";
        renderRenameUI(container.parentElement, newName);
      } else {
        msg.textContent = result;
      }
    } catch {
      msg.textContent = "An error occurred.";
    }
  };

  container.append(input, confirmBtn, msg);
}

/* ---------- Load seat map ---------- */
async function loadSeatMap() {
  try {
    const res = await fetch(`/api/getSeatMap?cinemaRoomId=${cinemaRoomId}`);
    const data = await res.json();

    nestedMap.clear();
    originalSeatMap.clear();

    for (const [row, seats] of Object.entries(data)) {
      const seatMap = new Map(Object.entries(seats));
      const originalRow = new Map();

      seatMap.forEach((seat, col) => {
        originalRow.set(col, { ...seat });
      });

      nestedMap.set(Number(row), seatMap);
      originalSeatMap.set(Number(row), originalRow);
    }
    renderSeatTable();
  } catch {
    console.error("Error loading seat map");
  }
}

/* ---------- Render seat table ---------- */
function renderSeatTable() {
  const seatTable = document.getElementById("seat-table");
  seatTable.innerHTML = "";

  for (const [row, seats] of nestedMap) {
    const rowElement = document.createElement("tr");

    for (const [col, seat] of seats) {
      const seatElement = document.createElement("td");
      const seatButton = document.createElement("button");
      seatButton.className = `btn ${
        seat.seatType === "VIP" ? "vip" : "normal"
      }`;
      seatButton.textContent = seat.seatColumn + seat.seatRow;
      seatButton.value = seat.seatColumn + seat.seatRow;

      if (canEditRoom) {
        seatButton.onclick = () => {
          const newType = seat.seatType === "VIP" ? "STANDARD" : "VIP";
          changeTypeSeatOfRow(row, newType);
        };
      } else {
        seatButton.disabled = true;
      }

      seatElement.appendChild(seatButton);
      rowElement.appendChild(seatElement);
    }
    seatTable.appendChild(rowElement);
  }
}

/* ---------- Đổi loại ghế trong một hàng ---------- */
function changeTypeSeatOfRow(row, type) {
  if (!canEditRoom) return; // chặn khi không được phép
  const seats = nestedMap.get(row);
  for (const [col, seat] of seats) {
    const seatButton = document.querySelector(
      `button[value="${seat.seatColumn}${seat.seatRow}"]`
    );
    if (seatButton) {
      seatButton.classList.remove("vip", "normal");
      seatButton.classList.add(type === "VIP" ? "vip" : "normal");
      seat.seatType = type;
    }
  }
}

/* ---------- Lưu ghế đã thay đổi ---------- */
async function saveSeatMap() {
  if (!canEditRoom) {
    alert("Cannot modify seats for a room with existing schedules.");
    return;
  }

  const saveBtn = document.getElementById("save-seat-map");
  saveBtn.disabled = true;
  saveBtn.innerText = "Saving...";

  const updatedSeats = [];

  nestedMap.forEach((seats, row) => {
    const originalSeats = originalSeatMap.get(row);
    seats.forEach((seat, col) => {
      const originalSeat = originalSeats?.get(col);
      if (!originalSeat || originalSeat.seatType !== seat.seatType) {
        updatedSeats.push({
          seatId: seat.seatId,
          seatColumn: seat.seatColumn,
          seatRow: seat.seatRow,
          seatType: seat.seatType,
          active: true,
        });
      }
    });
  });

  if (!updatedSeats.length) {
    alert("No changes detected.");
    saveBtn.disabled = false;
    saveBtn.innerText = "Save Seat Map";
    return;
  }

  try {
    const res = await fetch(`/api/saveSeatMap?cinemaRoomId=${cinemaRoomId}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(updatedSeats),
    });
    const result = await res.text();
    console.log("Seat map saved:", result);
    alert("Seat map saved successfully!");
  } catch (err) {
    console.error("Error saving seat map:", err);
    alert("Error saving seat map. Please try again.");
  } finally {
    saveBtn.disabled = false;
    saveBtn.innerText = "Save Seat Map";
  }
}

//---------------------------------------------------------
