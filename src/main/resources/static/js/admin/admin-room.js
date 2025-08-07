document.addEventListener('DOMContentLoaded', () => {
    const roomTableBody = document.getElementById('roomTableBody');
    const noResultsRow = document.getElementById('noResultsRow');
    const addRoomButton = document.getElementById('addRoomButton');
    const saveAddRoomButton = document.getElementById('saveAddRoomButton');
    const searchInput = document.getElementById('searchInput');
    const searchButton = document.getElementById('searchButton');
    const saveSeatLayoutButton = document.getElementById('saveSeatLayoutButton');

    // Sample data for cinema rooms
    let cinemaRooms = [
        {
            id: 1,
            roomId: 'CR001',
            roomName: 'Room 1',
            roomType: 'Standard',
            seatQuantity: 100,
            standardSeats: 30,
            vipSeats: 70,
            lastUpdated: '15/05/2024',
            seatLayout: null // Will be generated
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
            seatLayout: null // Will be generated
        },
        {
            id: 3,
            roomId: 'CR003',
            roomName: 'Room 3',
            roomType: 'VIP',
            seatQuantity: 40,
            standardSeats: 20,
            vipSeats: 20,
            lastUpdated: '05/05/2024',
            seatLayout: null // Will be generated
        },
        {
            id: 4,
            roomId: 'CR004',
            roomName: 'Room 4',
            roomType: 'Standard',
            seatQuantity: 100,
            standardSeats: 30,
            vipSeats: 70,
            lastUpdated: '01/05/2024',
            seatLayout: null // Will be generated
        },
        {
            id: 5,
            roomId: 'CR005',
            roomName: 'Room 5',
            roomType: 'Standard',
            seatQuantity: 100,
            standardSeats: 30,
            vipSeats: 70,
            lastUpdated: '28/04/2024',
            seatLayout: null // Will be generated
        }
    ];

    // Check if we have saved data in localStorage
    const savedRooms = localStorage.getItem('cinemaRooms');
    if (savedRooms) {
        cinemaRooms = JSON.parse(savedRooms);
    }

    // Room configurations
    const roomConfigs = {
        normal: {
            rows: 10,
            seatsPerRow: 10,
            totalSeats: 100,
            standardSeats: 30,
            vipSeats: 70,
            vipRowsStart: 4, // VIP seats start from row 4
        },
        vip: {
            rows: 4,
            seatsPerRow: 10,
            totalSeats: 40,
            standardSeats: 20,
            vipSeats: 20,
            vipRowsStart: 3, // VIP seats start from row 3
        }
    };

    // Function to generate default seat layout
    function generateDefaultSeatLayout(roomType) {
        const config = roomConfigs[roomType === 'Standard' ? 'normal' : 'vip'];
        const layout = [];

        for (let i = 0; i < config.rows; i++) {
            const row = [];
            for (let j = 0; j < config.seatsPerRow; j++) {
                // Determine if this is a VIP seat based on row number
                const isVip = i >= config.vipRowsStart - 1;
                row.push({
                    row: i + 1,
                    seat: j + 1,
                    type: isVip ? 'vip' : 'standard'
                });
            }
            layout.push(row);
        }

        return layout;
    }

    // Initialize seat layouts for existing rooms
    cinemaRooms.forEach(room => {
        if (!room.seatLayout) {
            room.seatLayout = generateDefaultSeatLayout(room.roomType);
        }
    });

    // Function to generate room layout for add modal with row labels
    function generateRoomLayout(type) {
        const config = roomConfigs[type];
        const container = document.querySelector(`.${type}-layout .seats-container`);
        container.innerHTML = '';

        // Create exit indicators container
        const exitContainer = document.createElement('div');
        exitContainer.className = 'exit-indicators';

        // Add left exit
        // const leftExit = document.createElement('div');
        // leftExit.className = 'exit-left';
        // leftExit.innerHTML = '<i class="fas fa-sign-out-alt"></i> EXIT';
        // exitContainer.appendChild(leftExit);

        // Add right exit
        // const rightExit = document.createElement('div');
        // rightExit.className = 'exit-right';
        // rightExit.innerHTML = 'EXIT <i class="fas fa-sign-out-alt"></i>';
        // exitContainer.appendChild(rightExit);

        // Generate seat rows with labels
        for (let i = 0; i < config.rows; i++) {
            const row = document.createElement('div');
            row.className = 'preview-seat-row';

            // Add row label
            const rowLabel = document.createElement('div');
            rowLabel.className = 'preview-row-label';
            rowLabel.textContent = String.fromCharCode(65 + i); // A, B, C, etc.
            row.appendChild(rowLabel);

            // Create seats wrapper
            const seatsWrapper = document.createElement('div');
            seatsWrapper.className = 'preview-seats-wrapper';

            // Add seats
            for (let j = 0; j < config.seatsPerRow; j++) {
                const seat = document.createElement('div');
                seat.className = 'seat';

                // Determine if this is a VIP seat based on row number
                if (i >= config.vipRowsStart - 1) {
                    seat.classList.add('vip');
                } else {
                    seat.classList.add('standard');
                }

                seatsWrapper.appendChild(seat);
            }

            row.appendChild(seatsWrapper);
            exitContainer.appendChild(row);
        }

        container.appendChild(exitContainer);
    }

    // Function to select room type
    window.selectRoomType = function(type) {
        // Update card selection
        document.getElementById('normalRoomCard').classList.remove('selected');
        document.getElementById('vipRoomCard').classList.remove('selected');

        if (type === 'normal') {
            document.getElementById('normalRoomCard').classList.add('selected');
        } else {
            document.getElementById('vipRoomCard').classList.add('selected');
        }

        document.getElementById('addRoomType').value = type;
    };

    // Function to show seat detail modal
    window.showSeatDetail = function(roomIndex) {
        const room = cinemaRooms[roomIndex];

        // Set room information
        document.getElementById('detailRoomId').textContent = room.roomId;
        document.getElementById('detailRoomName').textContent = room.roomName;
        document.getElementById('detailRoomType').textContent = room.roomType;
        document.getElementById('editingRoomIndex').value = roomIndex;

        // Generate editable seat layout
        generateEditableSeatLayout(room);

        // Update seat statistics
        updateSeatStatistics(room);

        // Show modal
        const modal = new bootstrap.Modal(document.getElementById('seatDetailModal'));
        modal.show();
    };

    // Function to generate editable seat layout with exits
    function generateEditableSeatLayout(room) {
        const container = document.getElementById('editableSeatsGrid');
        container.innerHTML = '';

        if (!room.seatLayout) {
            room.seatLayout = generateDefaultSeatLayout(room.roomType);
        }

        // Add left exit indicator
        // const leftExit = document.createElement('div');
        // leftExit.className = 'detail-exit-left';
        // leftExit.innerHTML = '<i class="fas fa-sign-out-alt"></i> EXIT';
        // container.appendChild(leftExit);

        // Add right exit indicator
        // const rightExit = document.createElement('div');
        // rightExit.className = 'detail-exit-right';
        // rightExit.innerHTML = 'EXIT <i class="fas fa-sign-out-alt"></i>';
        // container.appendChild(rightExit);

        room.seatLayout.forEach((row, rowIndex) => {
            const rowDiv = document.createElement('div');
            rowDiv.className = 'editable-seat-row';

            // Add row label
            const rowLabel = document.createElement('div');
            rowLabel.className = 'row-label';
            rowLabel.textContent = String.fromCharCode(65 + rowIndex); // A, B, C, etc.
            rowDiv.appendChild(rowLabel);

            // Add seats
            row.forEach((seatData, seatIndex) => {
                const seat = document.createElement('div');
                seat.className = `editable-seat ${seatData.type}`;
                seat.dataset.row = rowIndex;
                seat.dataset.seat = seatIndex;
                seat.title = `Row ${String.fromCharCode(65 + rowIndex)}, Seat ${seatIndex + 1}`;

                // Add click event to toggle seat type
                seat.addEventListener('click', () => {
                    toggleSeatType(seat, rowIndex, seatIndex);
                });

                rowDiv.appendChild(seat);
            });

            container.appendChild(rowDiv);
        });
    }

    // Function to toggle seat type
    function toggleSeatType(seatElement, rowIndex, seatIndex) {
        const roomIndex = document.getElementById('editingRoomIndex').value;
        const room = cinemaRooms[roomIndex];

        // Toggle between standard and vip
        const currentType = room.seatLayout[rowIndex][seatIndex].type;
        const newType = currentType === 'standard' ? 'vip' : 'standard';

        // Update data
        room.seatLayout[rowIndex][seatIndex].type = newType;

        // Update visual
        seatElement.className = `editable-seat ${newType}`;

        // Update statistics
        updateSeatStatistics(room);
    }

    // Function to update seat statistics
    function updateSeatStatistics(room) {
        let standardCount = 0;
        let vipCount = 0;

        room.seatLayout.forEach(row => {
            row.forEach(seat => {
                if (seat.type === 'standard') {
                    standardCount++;
                } else {
                    vipCount++;
                }
            });
        });

        const totalCount = standardCount + vipCount;

        document.getElementById('totalSeatsCount').textContent = totalCount;
        document.getElementById('standardSeatsCount').textContent = standardCount;
        document.getElementById('vipSeatsCount').textContent = vipCount;

        // Update room data
        room.seatQuantity = totalCount;
        room.standardSeats = standardCount;
        room.vipSeats = vipCount;
    }

    // Populate table
    function populateTable(data) {
        roomTableBody.innerHTML = '';
        // Add no results row back
        roomTableBody.appendChild(noResultsRow);
        if (data.length === 0) {
            noResultsRow.style.display = 'table-row';
            return;
        }
        noResultsRow.style.display = 'none';
        data.forEach((room, index) => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${index + 1}</td>
                <td>${room.roomId}</td>
                <td>${room.roomName}</td>
                <td>${room.roomType}</td>
                <td><button class="btn btn-sm btn-primary" onclick="showSeatDetail(${index})"><i class="fas fa-info-circle me-1"></i>Seat Detail</button></td>
            `;
            roomTableBody.appendChild(row);
        });
    }

    // Search functionality
    searchButton.addEventListener('click', () => {
        const keyword = searchInput.value.toLowerCase();
        const filteredRooms = cinemaRooms.filter(room =>
            room.roomId.toLowerCase().includes(keyword) ||
            room.roomName.toLowerCase().includes(keyword) ||
            room.roomType.toLowerCase().includes(keyword)
        );
        populateTable(filteredRooms);
    });

    // Show add room modal
    addRoomButton.addEventListener('click', () => {
        // Reset form
        document.getElementById('addRoomName').value = '';

        // Reset room type selection
        selectRoomType('normal');

        // Generate room layouts
        generateRoomLayout('normal');
        generateRoomLayout('vip');

        const modal = new bootstrap.Modal(document.getElementById('addRoomModal'));
        modal.show();
    });

    // Save new room
    saveAddRoomButton.addEventListener('click', () => {
        const roomName = document.getElementById('addRoomName').value.trim();
        const roomType = document.getElementById('addRoomType').value;

        // Get configuration based on selected room type
        const config = roomConfigs[roomType];

        if (roomName) {
            // Generate a unique room ID
            const lastRoomId = cinemaRooms.length > 0
                ? cinemaRooms[cinemaRooms.length - 1].roomId
                : 'CR000';

            const lastNumber = parseInt(lastRoomId.substring(2));
            const newRoomId = 'CR' + String(lastNumber + 1).padStart(3, '0');

            const newRoom = {
                id: cinemaRooms.length + 1,
                roomId: newRoomId,
                roomName,
                roomType: roomType === 'normal' ? 'Standard' : 'VIP',
                seatQuantity: config.totalSeats,
                standardSeats: config.standardSeats,
                vipSeats: config.vipSeats,
                lastUpdated: new Date().toLocaleDateString('en-GB'),
                seatLayout: generateDefaultSeatLayout(roomType === 'normal' ? 'Standard' : 'VIP')
            };

            cinemaRooms.push(newRoom);

            // Save to localStorage
            localStorage.setItem('cinemaRooms', JSON.stringify(cinemaRooms));

            populateTable(cinemaRooms);
            bootstrap.Modal.getInstance(document.getElementById('addRoomModal')).hide();
        } else {
            alert('Please fill in Room Name field.');
        }
    });

    // Save seat layout
    saveSeatLayoutButton.addEventListener('click', () => {
        const roomIndex = document.getElementById('editingRoomIndex').value;

        // Save to localStorage
        localStorage.setItem('cinemaRooms', JSON.stringify(cinemaRooms));

        // Update table to reflect changes
        populateTable(cinemaRooms);

        // Hide modal
        bootstrap.Modal.getInstance(document.getElementById('seatDetailModal')).hide();

        alert('Seat layout saved successfully!');
    });

    // Initial table population
    populateTable(cinemaRooms);

    // Generate initial room layouts
    generateRoomLayout('normal');
    generateRoomLayout('vip');
});