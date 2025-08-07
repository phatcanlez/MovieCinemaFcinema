
document.addEventListener('DOMContentLoaded', function () {
    const roomSelect = document.getElementById('cinemaRoomSelect');
    const showDateInput = document.getElementById('showDate');
    const wrapper = document.getElementById('scheduleTimesWrapper');
    const overlay = document.getElementById('loading');
    const errorSchedule = document.getElementById('error-scheduleTimes');
    const errorRoom = document.getElementById('error-roomId');
    const scheduleForm = document.getElementById('scheduleForm');
    const trigger = document.getElementById('calendarTrigger');
    const selectedDateDisplay = document.getElementById('selected-date');

    // Tạo input giả để flatpickr hiển thị popup đúng vị trí
    const fakeInput = document.createElement('input');
    fakeInput.type = 'text';
    fakeInput.style.position = 'absolute';
    fakeInput.style.opacity = 0;
    fakeInput.style.pointerEvents = 'none';
    document.body.appendChild(fakeInput);

    const fp = flatpickr(fakeInput, {
        dateFormat: "Y-m-d",
        locale: "vn",
        minDate: "today",
        disableMobile: true,
        clickOpens: true,
        onChange: function (selectedDates, dateStr) {
            const formatted = selectedDates[0] ? formatLocalDate(selectedDates[0]) : '';
            showDateInput.value = formatted;
            selectedDateDisplay.textContent = formatted ? `🗓 ${formatted}` : '';
            fetchSchedulesIfReady();
        }
    });
    trigger.addEventListener('click', function (e) {
        // Định vị input ảo đúng vị trí icon
        const rect = trigger.getBoundingClientRect();
        fakeInput.style.top = (window.scrollY + rect.bottom) + "px";
        fakeInput.style.left = (window.scrollX + rect.left) + "px";

        fp.open();
    });

    let currentMovieId = null;
    const scheduleCache = {}; // Key: movieId_roomId_date -> data

    document.querySelectorAll('.openScheduleModal').forEach(btn => {

        btn.addEventListener('click', function () {

            currentMovieId = this.getAttribute('data-movie-id');
            const movieTitle = this.getAttribute('data-movie-name-vn');

            const minFromDate = this.getAttribute('data-from-date');
            const maxToDate = this.getAttribute('data-to-date');
            setupDateMinMaxValidation(minFromDate, maxToDate);
            fp.set('minDate', minFromDate);
            fp.set('maxDate', maxToDate);
            document.getElementById('scheduleMovieId').value = currentMovieId;
            document.getElementById('movieTitle').textContent = movieTitle;

            wrapper.innerHTML = '';
            roomSelect.value = '';
            showDateInput.value = '';
            errorRoom.textContent = '';
            errorSchedule.textContent = '';
            selectedDateDisplay.textContent = 'Chọn ngày';

            const warning = document.getElementById("warningSelectRoom");
            if (warning) warning.style.display = "block";
        });
    });
    function hideSmoothly(item, delay = 0) {
        setTimeout(() => {
            item.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
            item.style.opacity = '1';
            item.style.transform = 'translateX(0)';

            requestAnimationFrame(() => {
                item.style.opacity = '0';
                item.style.transform = 'translateX(40px)';
            });

            setTimeout(() => {
                item.style.display = 'none';
            }, 300);
        }, delay);
    }

    function showSmoothly(item) {
        item.style.display = '';
        item.style.opacity = '0';
        item.style.transform = 'translateX(40px)';
        item.style.transition = 'opacity 0.3s ease, transform 0.3s ease';

        // Trigger animation
        requestAnimationFrame(() => {
            item.style.opacity = '1';
            item.style.transform = 'translateX(0)';
        });
    }


    function attachScheduleListeners() {
        document.querySelectorAll('input[name="scheduleRoomMap"]').forEach(cb => {
            cb.addEventListener('change', function () {
                const [roomId, scheduleId] = this.value.split('_');
                const allCheckboxes = document.querySelectorAll(`.schedule-item[data-room-id="${roomId}"]`);
                const scheduleList = scheduleCache[`${currentMovieId}_${roomId}_${showDateInput.value}`];

                // Lấy danh sách giờ đang được chọn (trong cùng room)
                const selectedSchedules = Array.from(allCheckboxes)
                    .map(item => {
                        const input = item.querySelector('input[type="checkbox"]');
                        if (input.checked) {
                            const sId = input.value.split('_')[1];
                            const sData = scheduleList.find(s => s.scheduleId == sId);
                            return {
                                scheduleId: sId,
                                startMins: timeToMinutes(sData.startTime),
                                endMins: timeToMinutes(sData.endTime),
                            };
                        }
                        return null;
                    })
                    .filter(Boolean); // Bỏ null

                if (this.checked) {
                    // Nếu có một checkbox vừa được chọn
                    const selected = scheduleList.find(s => s.scheduleId == scheduleId);
                    const selectedStartMins = timeToMinutes(selected.startTime);
                    const selectedEndMins = timeToMinutes(selected.endTime);
                    let delay = 0
                    allCheckboxes.forEach(item => {
                        const input = item.querySelector('input[type="checkbox"]');
                        const sId = input.value.split('_')[1];
                        if (sId === scheduleId || input.checked) return;

                        const other = scheduleList.find(s => s.scheduleId == sId);
                        const otherStart = timeToMinutes(other.startTime);
                        const otherEnd = timeToMinutes(other.endTime);

                        // Nếu trùng khung giờ với giờ mới chọn thì ẩn đi
                        if (otherEnd > selectedStartMins && otherStart < selectedEndMins) {
                            hideSmoothly(item);
                            delay += 100; // Mỗi item ẩn cách nhau 100ms
                        }
                    });
                } else {
                    // Nếu bỏ chọn thì cần xem lại toàn bộ trạng thái
                    if (selectedSchedules.length === 0) {
                        // Không còn cái nào được chọn -> hiển thị lại tất cả
                        allCheckboxes.forEach(showSmoothly);
                    } else {
                        allCheckboxes.forEach(item => {
                            const input = item.querySelector('input[type="checkbox"]');
                            const sId = input.value.split('_')[1];
                            const other = scheduleList.find(s => s.scheduleId == sId);
                            const otherStart = timeToMinutes(other.startTime);
                            const otherEnd = timeToMinutes(other.endTime);

                            // Nếu bị ẩn trước đó, giờ kiểm tra lại: có bị trùng với giờ đang chọn nào không?
                            const isConflict = selectedSchedules.some(sel => (
                                otherEnd > sel.startMins && otherStart < sel.endMins
                            ));

                            // Nếu không trùng với bất kỳ giờ nào đang chọn thì hiển thị lại
                            if (!input.checked && !isConflict) {
                                showSmoothly(item);
                            }
                        });
                    }
                }
            });
        });
    }


    function formatLocalDate(date) {
        const yyyy = date.getFullYear();
        const mm = String(date.getMonth() + 1).padStart(2, '0');
        const dd = String(date.getDate()).padStart(2, '0');
        return `${yyyy}-${mm}-${dd}`;
    }
    function timeToMinutes(timeStr) {
        const [hour, minute] = timeStr.split(":").map(Number);
        return hour * 60 + minute;
    }

    function renderScheduleForRoom(roomId, schedules, selectedDate) {
        const today = new Date();
        const todayStr = today.toISOString().split('T')[0]; // YYYY-MM-DD
        const currentTime = today.getHours() * 60 + today.getMinutes();

        if (!schedules || schedules.length === 0) {
            return `<div class="col-12"><p class="text-warning">Không có giờ chiếu khả dụng.</p></div>`;
        }

        const roomName = schedules[0]?.roomName || `Phòng ${roomId}`;
        let html = `<div class="mb-3"><h6>🌟Phòng: ${roomName}</h6><div class="row">`;
        schedules.forEach(s => {

            const checkboxId = `room${roomId}_schedule_${s.scheduleId}`;
            const selected = s.selected;
            const checked = selected ? 'checked' : '';

            const startMins = timeToMinutes(s.startTime);
            const endMins = timeToMinutes(s.endTime);
            const closeMins = timeToMinutes("23:59");
            const overLap = endMins < startMins || endMins > closeMins;
            let disabled = '';

            // ❗ Nếu hôm nay và suất đang chiếu & đã được chọn thì disable
            if (
                todayStr === selectedDate &&
                selected &&
                currentTime >= startMins &&
                currentTime < endMins
            ) {
                disabled = 'disabled';
            } else if (overLap && !selected) {
                disabled = 'disabled';
            }
            let labelClass = '';

            if (selected && !disabled) {
                labelClass = 'btn btn-outline-primary w-60'; // 🟦 Xanh primary tối hơn: đã chọn nhưng bị khóa
            } else if (selected && disabled) {
                labelClass = 'btn btn-outline-success w-60'; // ✅ Xanh lá: đã chọn hợp lệ
            } else if (overLap) {
                labelClass = 'btn btn-outline-danger w-60';   // ❌ Đỏ: lỗi
            } else {
                labelClass = 'btn btn-outline-primary w-60';  // 🔵 Xanh nhạt: khả dụng
            }



            const timeRange = `<span class="text-info ${checked && 'text-white'}">${s.startTime}</span> <span class="text-warning small" style="opacity: 0.6;">~ ${s.endTime}</span>`;

            html += `
            <div class="col-md-3 mb-2 schedule-item" data-room-id="${roomId}" data-schedule-id="${s.scheduleId}">
             <div class="form-check">
             <input class="btn-check" type="checkbox" name="scheduleRoomMap" value="${roomId}_${s.scheduleId}"
            id="${checkboxId}" ${checked} ${selected && disabled ? 'onclick="return false;"' : disabled ? 'disabled' : ''}>
        
                <label class="form-check-label border-2 ${overLap && 'border-danger'} ${labelClass}" for="${checkboxId}">
            ${timeRange}
            </label>
            </div>
            </div>`;

        });

        html += `</div></div>`;
        return html;
    }

    function setupDateMinMaxValidation(minFromDate, maxToDate) {
        document.getElementById("showDate")?.setAttribute("min", minFromDate);
        document.getElementById("showDate")?.setAttribute("max", maxToDate);
    }
    async function fetchSchedulesIfReady() {
        const selectedRoomIds = Array.from(roomSelect.selectedOptions).map(opt => parseInt(opt.value));
        const selectedDate = showDateInput.value;

        wrapper.innerHTML = '';
        errorRoom.textContent = '';
        errorSchedule.textContent = '';
        const warning = document.getElementById("warningSelectRoom");

        // 👉 Kiểm tra điều kiện trước
        if (!selectedDate || selectedRoomIds.length === 0) {
            if (warning) warning.style.display = "block";
            return;
        }

        // ✅ Chỉ ẩn warning nếu điều kiện hợp lệ
        if (warning) warning.style.display = "none";

        document.getElementById("schedule-loading").style.display = "block";
        for (const roomId of selectedRoomIds) {
            const cacheKey = `${currentMovieId}_${roomId}_${selectedDate}`;
            if (!scheduleCache[cacheKey]) {
                try {
                    const res = await fetch(`/admin/movie-schedule?movieId=${currentMovieId}&roomId=${roomId}&showDate=${selectedDate}`);
                    const data = await res.json();
                    console.log('🚀 ~ fetchSchedulesIfReady ~ data:', data)
                    scheduleCache[cacheKey] = data;

                } catch (err) {
                    console.error("Lỗi khi fetch schedule:", err);
                    wrapper.innerHTML += `<p class="text-danger">Không thể tải giờ chiếu cho phòng ${roomId}.</p>`;
                    continue;
                }
            }
            wrapper.innerHTML += renderScheduleForRoom(roomId, scheduleCache[cacheKey], selectedDate);
            attachScheduleListeners();
        }

        document.getElementById("schedule-loading").style.display = "none";
    }

    roomSelect.addEventListener('change', fetchSchedulesIfReady);

    scheduleForm.addEventListener('submit', async function (e) {
        e.preventDefault(); // Ngăn form submit mặc định

        const selectedCheckboxes = wrapper.querySelectorAll('input[name="scheduleRoomMap"]:checked');
        const selectedRoomIds = Array.from(roomSelect.selectedOptions).map(opt => parseInt(opt.value));

        errorRoom.textContent = '';
        errorSchedule.textContent = '';

        let isValid = true;

        if (selectedRoomIds.length === 0) {
            errorRoom.textContent = 'Vui lòng chọn ít nhất một phòng.';
            isValid = false;
        }

        if (selectedCheckboxes.length === 0) {
            errorSchedule.textContent = 'Vui lòng chọn ít nhất 1 giờ chiếu.';
            isValid = false;
        }

        const selectedPairs = Array.from(selectedCheckboxes).map(cb => cb.value); // ["2_15", "4_15"]
        const selectedScheduleIds = selectedPairs.map(p => p.split('_')[1]);
        const duplicates = selectedScheduleIds.filter((id, idx, arr) => arr.indexOf(id) !== idx);

        if (duplicates.length > 0) {
            errorSchedule.textContent = "Không được chọn nhiều phòng có cùng giờ chiếu.";
            isValid = false;
        }

        if (!isValid) return;

        // Hiện overlay
        overlay.classList.remove('d-none');

        // Gửi fetch
        const payload = {
            movieId: currentMovieId,
            showDate: showDateInput.value,
            scheduleRoomMap: selectedPairs // ["2_15", "4_16",...]
        };

        try {
            const res = await fetch('/admin/movie-schedule/save', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            const result = await res.json();
            overlay.classList.add('d-none');

            if (res.ok && result.status === 'ok') {
                showSuccess("Lưu lịch chiếu thành công!");
                window.location.reload();

            } else {
                const errorMsg = result.errors?.join("\n") || "Có lỗi xảy ra.";
                showError("❌ Không thể lưu lịch chiếu:\n" + errorMsg);
            }

        } catch (err) {
            overlay.classList.add('d-none');
            alert("Lỗi kết nối hoặc máy chủ. Vui lòng thử lại.");
        }
    });

});
