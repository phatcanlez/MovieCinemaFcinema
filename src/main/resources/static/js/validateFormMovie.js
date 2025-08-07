// Khi DOM đã tải xong thì khởi tạo các chức năng
document.addEventListener('DOMContentLoaded', function () {
    setupEditModalBinding();      // Bắt sự kiện khi bấm nút chỉnh sửa
    setupDateMinValidation();    // Thiết lập ngày tối thiểu cho fromDate
    setupToDateMaxLimit();       // Thiết lập max toDate (không vượt quá 30 ngày)
    setupSaveButtons();          // Bắt sự kiện nút Lưu (Add/Edit)
});

// ===== 1. Setup binding khi mở modal edit =====
function setupEditModalBinding() {
    const editButtons = document.querySelectorAll('.editMovieButton');

    editButtons.forEach(button => {
        button.addEventListener('click', function () {
            const data = this.dataset;

            // Gán dữ liệu ngày + giới hạn max/min cho toDate
            const fromInput = document.getElementById('editFromDate');
            const toInput = document.getElementById('editToDate');

            fromInput.value = data.fromDate || "";
            toInput.value = data.toDate || "";

            if (data.fromDate) {
                const fromDate = new Date(data.fromDate);
                const maxToDate = new Date(fromDate);
                maxToDate.setDate(fromDate.getDate() + 30);

                const maxStr = maxToDate.toISOString().split('T')[0];
                const minStr = fromDate.toISOString().split('T')[0];

                toInput.setAttribute('max', maxStr);
                toInput.setAttribute('min', minStr);

                // Nếu toDate không hợp lệ thì reset
                if (data.toDate) {
                    const toDate = new Date(data.toDate);
                    if (toDate > maxToDate || toDate < fromDate) {
                        toInput.value = "";
                    }
                }
            }

            // Đổ dữ liệu còn lại vào form
            document.getElementById('editMovieId').value = data.movieId || "";
            document.getElementById('editTitleEng').value = data.movieNameEn || "";
            document.getElementById('editTitleVn').value = data.movieNameVn || "";
            document.getElementById('editFromDate').setAttribute('data-old', data.fromDate || "");
            document.getElementById('editToDate').setAttribute('data-old', data.toDate || "");
            document.getElementById('editDuration').value = data.duration || "";
            document.getElementById('editVersion').value = '_' + data.version || "";
            document.getElementById('editActor').value = data.actor || "";
            document.getElementById('editDirector').value = data.director || "";
            document.getElementById('editContent').value = data.content || "";
            document.getElementById('editStatus').value = data.status || "";
            document.getElementById('editPrice').value = data.price || "";
            document.getElementById('editProductionCompany').value = data.movieProductionCompany || "";

            // Hiển thị hình ảnh preview
            document.getElementById('currentBannerImage').src = data.bannerImage || "";
            document.getElementById('currentPosterImage').src = data.posterImage || "";

            // Reset và gán lại checkbox loại phim
            document.querySelectorAll("input[name='selectedTypeIds']").forEach(cb => cb.checked = false);
            if (data.selectedTypes) {
                data.selectedTypes.split(',').forEach(typeId => {
                    const checkbox = document.getElementById('edit_type_' + typeId.trim());
                    if (checkbox) checkbox.checked = true;
                });
            }
        });
    });
}

// ===== 2. Thiết lập ngày min cho fromDate (hôm nay) =====
function setupDateMinValidation() {
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');
    const todayStr = `${yyyy}-${mm}-${dd}`;

    document.getElementById("addFromDate")?.setAttribute("min", todayStr);
    document.getElementById("editFromDate")?.setAttribute("min", todayStr);
}

// ===== 3. Giới hạn toDate không vượt quá fromDate + 30 ngày =====
function setupToDateMaxLimit() {
    const fromDateInputs = [document.getElementById('addFromDate'), document.getElementById('editFromDate')];
    const toDateInputs = [document.getElementById('addToDate'), document.getElementById('editToDate')];

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const todayStr = today.toISOString().split('T')[0];

    // Thiết lập min cho toDate là hôm nay
    toDateInputs.forEach(input => input?.setAttribute('min', todayStr));

    fromDateInputs.forEach((fromInput, i) => {
        const toInput = toDateInputs[i];
        if (!fromInput || !toInput) return;

        fromInput.addEventListener('change', () => {
            const fromStr = fromInput.value;
            if (!fromStr) return;

            const from = new Date(fromStr);
            const max = new Date(from);
            max.setDate(from.getDate() + 30);

            const maxStr = max.toISOString().split('T')[0];
            const minStr = from > today ? fromStr : todayStr;

            toInput.setAttribute('min', minStr);
            toInput.setAttribute('max', maxStr);

            // Nếu toDate hiện tại vượt giới hạn → reset
            if (toInput.value) {
                const toDate = new Date(toInput.value);
                if (toDate < new Date(minStr) || toDate > max) {
                    toInput.value = "";
                }
            }
        });

        // Khởi động sự kiện change ngay từ đầu nếu form có dữ liệu
        if (fromInput.value) {
            fromInput.dispatchEvent(new Event('change'));
        }
    });
}

// ===== 4. Bắt sự kiện bấm nút Lưu (Add/Edit) =====
function setupSaveButtons() {
    document.getElementById("saveAddMovieButton")?.addEventListener("click", () => validateMovieForm("add"));
    document.getElementById("saveEditMovieButton")?.addEventListener("click", () => validateMovieForm("edit"));
}

// ===== 5. Validate form trước khi submit =====
function validateMovieForm(mode) {
    const prefix = mode === "add" ? "add" : "edit";
    const getVal = id => document.getElementById(`${prefix}${id}`)?.value.trim();
    const getFile = id => document.getElementById(`${prefix}${id}`)?.files;
    const setError = (id, msg) => {
        const el = document.getElementById(`error-${prefix}${id}`);
        if (el) el.innerText = msg;
    };

    // Reset lỗi cũ
    const fields = ["TitleEng", "TitleVn", "FromDate", "ToDate", "ProductionCompany", "Duration", "Price", "Actor", "Director", "Content", "MovieType", "BannerUrl", "PosterUrl", "TrailerUrl"];
    fields.forEach(field => setError(field, ""));

    let isValid = true;

    // Lấy dữ liệu
    const titleEng = getVal("TitleEng");
    const titleVn = getVal("TitleVn");
    const fromDateStr = getVal("FromDate");
    const toDateStr = getVal("ToDate");
    const company = getVal("ProductionCompany");
    const duration = parseInt(getVal("Duration"));
    const price = parseFloat(getVal("Price"));
    const actor = getVal("Actor");
    const director = getVal("Director");
    const content = getVal("Content");
    const trailerUrl = getVal("TrailerUrl");
    const bannerFile = getFile("BannerUrl");
    const posterFile = getFile("PosterUrl");

    // Kiểm tra từng trường
    if (!titleEng) { setError("TitleEng", "English title không được trống."); isValid = false; }
    if (!titleVn) { setError("TitleVn", "Vietnamese title không được trống."); isValid = false; }

    if (!fromDateStr) {
        setError("FromDate", "Vui lòng chọn ngày bắt đầu."); isValid = false;
    } else if (mode === 'add') {
        const today = new Date(); today.setHours(0, 0, 0, 0);
        const fromDate = new Date(fromDateStr);
        if (fromDate < today) {
            setError("FromDate", "Ngày bắt đầu không được trong quá khứ."); isValid = false;
        }
    }

    if (!toDateStr) {
        setError("ToDate", "Vui lòng chọn ngày kết thúc."); isValid = false;
    } else if (fromDateStr && toDateStr) {
        const today = new Date(); today.setHours(0, 0, 0, 0);
        const from = new Date(fromDateStr);
        const to = new Date(toDateStr);
        if (to < from) {
            setError("ToDate", "Ngày kết thúc phải sau ngày bắt đầu."); isValid = false;
        } else if (to < today) {
            setError("ToDate", "Ngày kết thúc không được trong quá khứ."); isValid = false;

        } else {
            const max = new Date(from);
            max.setDate(from.getDate() + 30);
            if (to > max) {
                setError("ToDate", "Ngày kết thúc không được quá 1 tháng so với ngày bắt đầu."); isValid = false;
            }
        }
    }


    if (!company) { setError("ProductionCompany", "Production company không được trống."); isValid = false; }
    if (!duration || duration < 1) { setError("Duration", "Duration phải ít nhất 1 phút."); isValid = false; }
    if (!price || price < 1) { setError("Price", "Price phải lớn hơn 1."); isValid = false; }
    if (!actor) { setError("Actor", "Actor không được trống."); isValid = false; }
    if (!director) { setError("Director", "Director không được trống."); isValid = false; }
    if (!content) { setError("Content", "Content không được trống."); isValid = false; }

    const checkedTypes = document.querySelectorAll(`input[name="selectedTypeIds"]:checked`);
    if (checkedTypes.length === 0) {
        setError("MovieType", "Vui lòng chọn ít nhất 1 thể loại phim."); isValid = false;
    }

    if (mode === "add") {
        if (!trailerUrl) { setError("TrailerUrl", "Trailer Url không được trống."); isValid = false; }
        if (!bannerFile || bannerFile.length === 0) {
            setError("BannerUrl", "Banner image không được trống."); isValid = false;
        }
        if (!posterFile || posterFile.length === 0) {
            setError("PosterUrl", "Poster image không được trống."); isValid = false;
        }
    }

    // Submit nếu hợp lệ
    if (isValid) {
        document.querySelectorAll('.text-danger.small').forEach(el => el.textContent = '');
        const form = document.getElementById(`${prefix}MovieForm`);
        form.submit();
    }
}
