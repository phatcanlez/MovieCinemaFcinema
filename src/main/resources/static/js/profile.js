/**
 * Chuyển đổi giữa chế độ xem và chỉnh sửa thông tin profile
 * @param {HTMLButtonElement} button - Button được nhấn (edit/save)
 */
function changeType(button) {
    // Lấy các phần tử form
    const inputElements = document.querySelectorAll(".form-input");
    const genderSelect = document.getElementById("gender");
    const imageInput = document.getElementById("imageFile"); // Sửa ID thành imageFile
    const form = document.querySelector(".profile-form");
    
    // Chuyển sang chế độ chỉnh sửa
    if (button.id === "edit") {
        button.textContent = "Lưu Thông Tin";
        button.id = "save";
        
        // Bỏ trạng thái readonly cho các trường input
        inputElements.forEach(input => {
            // Không cho phép chỉnh sửa họ tên, email, CMND/CCCD và điểm tích lũy
            if (input.id !== "points") {
                input.readOnly = false;
                input.classList.add("default_input");
                input.style.backgroundColor = "#ffffff";
                input.style.color = "#000000";
            }
        });
        
     
        // Bật select giới tính và input file ảnh
        genderSelect.disabled = false;
        genderSelect.classList.add("default_input");
        
        // Bật input file ảnh
        if (imageInput) {
            imageInput.disabled = false;
            // Thêm event listener để hiển thị preview ảnh khi chọn file
            imageInput.addEventListener('change', previewImage);
        }
    } 
    // Chuyển sang chế độ lưu thông tin
    else {
        // Kiểm tra validation trước khi submit
        if (!validateForm()) {
            return; // Dừng lại nếu form không hợp lệ
        }
        
        // Submit form để controller xử lý
        form.submit();
    }
}

/**
 * Hiển thị preview ảnh khi người dùng chọn file
 */
function previewImage() {
    const file = this.files[0];
    if (file) {
        // Kiểm tra file là hình ảnh
        if (!file.type.match('image.*')) {
            showValidationAlert("Vui lòng chọn file hình ảnh.");
            this.value = ''; // Xóa file đã chọn
            return;
        }
        
        // Kiểm tra kích thước file (tối đa 5MB)
        if (file.size > 5 * 1024 * 1024) {
            showValidationAlert("Kích thước ảnh quá lớn. Vui lòng chọn ảnh nhỏ hơn 5MB.");
            this.value = ''; // Xóa file đã chọn
            return;
        }
        
        const reader = new FileReader();
        reader.onload = function(e) {
            // Tìm thẻ img hiện tại hoặc tạo mới
            let imgPreview = document.querySelector('.current-avatar');
            
            if (!imgPreview) {
                // Nếu chưa có ảnh, tạo mới
                imgPreview = document.createElement('img');
                imgPreview.className = 'current-avatar';
                imgPreview.style.width = '100px';
                imgPreview.style.height = '100px';
                imgPreview.style.borderRadius = '50%';
                imgPreview.style.objectFit = 'cover';
                imgPreview.style.marginTop = '10px';
                
                // Thêm vào DOM
                const imageInputContainer = document.getElementById('imageFile').parentElement;
                const previewContainer = document.createElement('div');
                previewContainer.className = 'mt-2';
                previewContainer.appendChild(imgPreview);
                imageInputContainer.appendChild(previewContainer);
            }
            
            // Cập nhật src cho ảnh preview
            imgPreview.src = e.target.result;
            imgPreview.alt = 'Avatar Preview';
        };
        
        reader.readAsDataURL(file);
    }
}

/**
 * Kiểm tra tính hợp lệ của form
 * @returns {boolean} - Form có hợp lệ hay không
 */
function validateForm() {
    const emailInput = document.getElementById("email");
    const phoneInput = document.getElementById("phone");
    const imageInput = document.getElementById("imageFile");
    const birthDateInput = document.getElementById("birthDate");
    
    // Xóa các thông báo lỗi cũ (nếu có)
    hideValidationAlerts();
    
    // Kiểm tra định dạng email
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (emailInput && emailInput.value && !emailPattern.test(emailInput.value)) {
        showValidationAlert("Email không hợp lệ. Vui lòng nhập lại.");
        emailInput.focus();
        return false;
    }
    
    // Kiểm tra định dạng số điện thoại
    const phonePattern = /^[0-9]{10,11}$/;
    if (phoneInput && phoneInput.value && !phonePattern.test(phoneInput.value)) {
        showValidationAlert("Số điện thoại không hợp lệ. Vui lòng nhập 10-11 chữ số.");
        phoneInput.focus();
        return false;
    }
    
    // Kiểm tra ngày sinh
    if (birthDateInput && birthDateInput.value) {
        const selectedDate = new Date(birthDateInput.value);
        const today = new Date();
        
        if (selectedDate > today) {
            showValidationAlert("Ngày sinh không thể là ngày trong tương lai.");
            birthDateInput.focus();
            return false;
        }
    }
    
    // Kiểm tra file ảnh nếu có
    if (imageInput && imageInput.files && imageInput.files.length > 0) {
        const file = imageInput.files[0];
        
        // Kiểm tra loại file
        if (!file.type.match('image.*')) {
            showValidationAlert("Vui lòng chọn file hình ảnh.");
            return false;
        }
        
        // Kiểm tra kích thước file (tối đa 5MB)
        if (file.size > 5 * 1024 * 1024) {
            showValidationAlert("Kích thước ảnh quá lớn. Vui lòng chọn ảnh nhỏ hơn 5MB.");
            return false;
        }
    }
    
    return true;
}

/**
 * Hiển thị thông báo lỗi validation
 * @param {string} message - Nội dung thông báo
 */
function showValidationAlert(message) {
    // Kiểm tra xem đã có alert nào chưa
    let alertDiv = document.querySelector('.validation-alert');
    
    if (!alertDiv) {
        // Tạo alert mới nếu chưa có
        alertDiv = document.createElement('div');
        alertDiv.className = 'alert alert-danger validation-alert mt-3';
        alertDiv.role = 'alert';
        
        const mainContent = document.querySelector('.main-content');
        const header = document.querySelector('.header');
        
        // Chèn alert sau header
        if (mainContent && header) {
            mainContent.insertBefore(alertDiv, header.nextSibling);
        }
    }
    
    alertDiv.textContent = message;
    
  // Tự động ẩn sau 3 giây
    setTimeout(() => {
        if (alertDiv && alertDiv.parentNode) {
            alertDiv.parentNode.removeChild(alertDiv);
        }
 }, 3000);
}

/**
 * Ẩn tất cả các thông báo lỗi validation
 */
function hideValidationAlerts() {
    const alerts = document.querySelectorAll('.validation-alert');
    alerts.forEach(alert => {
        if (alert && alert.parentNode) {
            alert.parentNode.removeChild(alert);
        }
    });
}

// Khởi tạo khi trang được tải
document.addEventListener("DOMContentLoaded", function() {
    // Di chuyển các thông báo lỗi/thành công vào vị trí đúng
    const successAlert = document.querySelector('.alert-success');
    const errorAlert = document.querySelector('.alert-danger');
    const mainContent = document.querySelector('.main-content');
    const header = document.querySelector('.header');
    
    if (successAlert && mainContent && header) {
        mainContent.insertBefore(successAlert, header.nextSibling);
        
   // Tự động ẩn thông báo thành công sau 3 giây
        setTimeout(() => {
            if (successAlert && successAlert.parentNode) {
                successAlert.parentNode.removeChild(successAlert);
            }
  }, 3000);
    }
    
    if (errorAlert && mainContent && header) {
        mainContent.insertBefore(errorAlert, header.nextSibling);
        
  // Tự động ẩn thông báo lỗi sau 3 giây
        setTimeout(() => {
            if (errorAlert && errorAlert.parentNode) {
                errorAlert.parentNode.removeChild(errorAlert);
            }
  }, 3000);
    }
    
    // Cải thiện khả năng tiếp cận bằng cách thêm các thuộc tính ARIA
    const genderSelect = document.getElementById("gender");
    if (genderSelect && genderSelect.disabled) {
        genderSelect.setAttribute('aria-readonly', 'true');
    }
    
    // Thêm animation cho các phần tử form khi focus
    const formInputs = document.querySelectorAll('.form-input:not([readonly]):not([disabled])');
    formInputs.forEach(input => {
        input.addEventListener('focus', () => {
            input.style.transition = 'all 0.3s';
            input.style.backgroundColor = '#f8f9fa';
            input.style.boxShadow = '0 0 0 0.25rem rgba(13, 110, 253, 0.25)';
        });
        
        input.addEventListener('blur', () => {
            input.style.boxShadow = '';
            input.style.backgroundColor = '';
        });
    });
    
    // Thêm xử lý cho trường hình ảnh
    const imageInput = document.getElementById("imageFile");
    if (imageInput) {
        imageInput.addEventListener('change', previewImage);
    }
});
