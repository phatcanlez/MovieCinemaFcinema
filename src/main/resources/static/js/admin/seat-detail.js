document.addEventListener('DOMContentLoaded', () => {
    // Lấy index phòng đang chỉnh sửa
    const roomIndex = localStorage.getItem('editingRoomIndex');
    let cinemaRooms = JSON.parse(localStorage.getItem('cinemaRooms') || '[]');

    // Nếu chưa có dữ liệu mẫu thì gán dữ liệu mẫu
    if (!cinemaRooms || cinemaRooms.length === 0) {
        cinemaRooms = [
            {
                id: 1,
                roomId: 'CR001',
                roomName: 'Room 1',
                roomType: 'Standard',
                seatQuantity: 100,
                standardSeats: 30,
                vipSeats: 70,
                lastUpdated: '15/05/2024',
                seatLayout: null
            },
            {
                id: 2,
                roomId: 'CR002',
                roomName: 'Room 2',
                roomType: 'VIP',
                seatQuantity: 40,
                standardSeats: 20,
                vipSeats: 20,
                lastUpdated: '10/05/2024',
                seatLayout: null
            }
        ];
        // Hàm tạo seatLayout mẫu (bạn có thể copy từ admin-room.js)
        function generateDefaultSeatLayout(roomType) {
            const config = roomType === 'Standard'
                ? { rows: 10, seatsPerRow: 10, vipRowsStart: 4 }
                : { rows: 4, seatsPerRow: 10, vipRowsStart: 3 };
            const layout = [];
            for (let i = 0; i < config.rows; i++) {
                const row = [];
                for (let j = 0; j < config.seatsPerRow; j++) {
                    row.push({
                        type: i >= config.vipRowsStart ? 'vip' : 'standard'
                    });
                }
                layout.push(row);
            }
            return layout;
        }
        cinemaRooms.forEach(room => {
            if (!room.seatLayout) {
                room.seatLayout = generateDefaultSeatLayout(room.roomType);
            }
        });
        localStorage.setItem('cinemaRooms', JSON.stringify(cinemaRooms));
    }

    if (!roomIndex || !cinemaRooms[roomIndex]) {
        alert('Room not found!');
        window.location.href = 'admin-room.html';
        return;
    }
    const room = cinemaRooms[roomIndex];

    // Copy các hàm generateEditableSeatLayout, toggleSeatType, updateSeatStatistics từ admin-room.js
    // ... (giữ nguyên logic, chỉ thay đổi selector nếu cần) ...

    // Ví dụ:
    function generateEditableSeatLayout(room) {
        const container = document.getElementById('editableSeatsGrid');
        container.innerHTML = '';
        if (!room.seatLayout) return;
        room.seatLayout.forEach((row, rowIndex) => {
            const rowDiv = document.createElement('div');
            rowDiv.className = 'editable-seat-row';
            const rowLabel = document.createElement('div');
            rowLabel.className = 'row-label';
            rowLabel.textContent = String.fromCharCode(65 + rowIndex);
            rowDiv.appendChild(rowLabel);
            row.forEach((seatData, seatIndex) => {
                const seat = document.createElement('div');
                seat.className = `editable-seat ${seatData.type}`;
                seat.dataset.row = rowIndex;
                seat.dataset.seat = seatIndex;
                seat.title = `Row ${String.fromCharCode(65 + rowIndex)}, Seat ${seatIndex + 1}`;
                seat.addEventListener('click', () => {
                    toggleSeatType(seat, rowIndex, seatIndex);
                });
                rowDiv.appendChild(seat);
            });
            container.appendChild(rowDiv);
        });
    }

    function toggleSeatType(seatElement, rowIndex, seatIndex) {
        const currentType = room.seatLayout[rowIndex][seatIndex].type;
        const newType = currentType === 'standard' ? 'vip' : 'standard';
        room.seatLayout[rowIndex][seatIndex].type = newType;
        seatElement.className = `editable-seat ${newType}`;
        updateSeatStatistics(room);
    }

    function updateSeatStatistics(room) {
        let standardCount = 0, vipCount = 0;
        room.seatLayout.forEach(row => {
            row.forEach(seat => {
                if (seat.type === 'standard') standardCount++;
                else vipCount++;
            });
        });
        const totalCount = standardCount + vipCount;
        document.getElementById('totalSeatsCount').textContent = totalCount;
        document.getElementById('standardSeatsCount').textContent = standardCount;
        document.getElementById('vipSeatsCount').textContent = vipCount;
        room.seatQuantity = totalCount;
        room.standardSeats = standardCount;
        room.vipSeats = vipCount;
    }

    // Set room info
    document.getElementById('detailRoomId').textContent = room.roomId;
    document.getElementById('detailRoomName').textContent = room.roomName;
    document.getElementById('detailRoomType').textContent = room.roomType;
    document.getElementById('editingRoomIndex').value = roomIndex;

    generateEditableSeatLayout(room);
    updateSeatStatistics(room);

    // Save
    document.getElementById('saveSeatLayoutButton').addEventListener('click', () => {
        cinemaRooms[roomIndex] = room;
        localStorage.setItem('cinemaRooms', JSON.stringify(cinemaRooms));
        alert('Seat layout saved successfully!');
        window.location.href = 'admin-room.html';
    });
});