const cinemaRoomId = new URLSearchParams(window.location.search).get("cinemaRoomId");
let nestedMap = new Map();
let originalSeatMap = new Map();

/*----------Load seatMap--------------------------------------------------------------*/
document.addEventListener("DOMContentLoaded", function () {
  const seatTable = document.getElementById("seat-table");

  fetch(`/api/getSeatMap?cinemaRoomId=${cinemaRoomId}`)
    .then((response) => response.json())
    .then((data) => {
      nestedMap = new Map();
      originalSeatMap = new Map(); // Reset original map

      for (const [row, seats] of Object.entries(data)) {
        const seatMap = new Map(Object.entries(seats));
        const originalRow = new Map();

        seatMap.forEach((seat, col) => {
          originalRow.set(col, { ...seat }); // Deep clone
        });

        nestedMap.set(Number(row), seatMap);
        originalSeatMap.set(Number(row), originalRow);
      }

      renderSeatTable();
    });
});

/*----------Render seat table---------------------------------------------------------*/
function renderSeatTable() {
  const seatTable = document.getElementById("seat-table");
  seatTable.innerHTML = ""; // Clear existing content

  for (const [row, seats] of nestedMap) {
    const rowElement = document.createElement("tr");

    for (const [col, seat] of seats) {
      const seatElement = document.createElement("td");
      const seatButton = document.createElement("button");

      seatButton.className = "btn";
      seatButton.textContent = seat.seatColumn + seat.seatRow;
      seatButton.value = seat.seatColumn + seat.seatRow;

      seatButton.classList.add(seat.seatType === "VIP" ? "vip" : "normal");

      seatButton.onclick = function () {
        const newType = seat.seatType === "VIP" ? "STANDARD" : "VIP";
        changeTypeSeatOfRow(row, newType);
      };

      seatElement.appendChild(seatButton);
      rowElement.appendChild(seatElement);
    }

    seatTable.appendChild(rowElement);
  }
}

/*----------Change type of all seats in a row-----------------------------------------*/
function changeTypeSeatOfRow(row, type) {
  const seats = nestedMap.get(row);

  for (const [col, seat] of seats) {
    const seatButton = document.querySelector(
      `button[value="${seat.seatColumn}${seat.seatRow}"]`
    );

    if (seatButton) {
      seatButton.classList.remove("vip", "normal");
      seatButton.classList.add(type === "VIP" ? "vip" : "normal");
      seat.seatType = type; // Update seat type in nestedMap
    }
  }
}

/*----------Save only changed seats----------------------------------------------------*/
function saveSeatMap() {
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

  if (updatedSeats.length === 0) {
    alert("No changes detected.");
    saveBtn.disabled = false;
    saveBtn.innerText = "Save Seat Map";
    return;
  }

  console.log("Saving updated seats:", updatedSeats);

  fetch(`/api/saveSeatMap?cinemaRoomId=${cinemaRoomId}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(updatedSeats),
  })
    .then((response) => response.text())
    .then((data) => {
      console.log("Seat map saved successfully:", data);
      alert("Seat map saved successfully!");
    })
    .catch((error) => {
      console.error("Error saving seat map:", error);
      alert("Error saving seat map. Please try again.");
    })
    .finally(() => {
      saveBtn.disabled = false;
      saveBtn.innerText = "Save Seat Map";
    });
}
