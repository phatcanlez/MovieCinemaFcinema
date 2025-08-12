document.addEventListener('DOMContentLoaded', function() {
    // Di chuyển các thông báo lỗi/thành công vào vị trí đúng
    const successAlert = document.querySelector('.alert-success');
    const errorAlert = document.querySelector('.alert-danger');
    const mainContent = document.querySelector('.main-content');
    const header = document.querySelector('.header');
    
    // Xử lý thông báo thành công/lỗi (giữ nguyên code hiện tại)
    if (successAlert && mainContent && header) {
        mainContent.insertBefore(successAlert, header.nextSibling);
        
        // Tự động ẩn thông báo thành công sau 5 giây
        setTimeout(() => {
            if (successAlert && successAlert.parentNode) {
                successAlert.style.opacity = '0';
                successAlert.style.transition = 'opacity 1s';
                setTimeout(() => {
                    if (successAlert.parentNode) {
                        successAlert.parentNode.removeChild(successAlert);
                    }
                }, 1000);
            }
        }, 3000);
    }
    
    if (errorAlert && mainContent && header) {
        mainContent.insertBefore(errorAlert, header.nextSibling);
        
        // Tự động ẩn thông báo lỗi sau 5 giây
        setTimeout(() => {
            if (errorAlert && errorAlert.parentNode) {
                errorAlert.style.opacity = '0';
                errorAlert.style.transition = 'opacity 1s';
                setTimeout(() => {
                    if (errorAlert.parentNode) {
                        errorAlert.parentNode.removeChild(errorAlert);
                    }
                }, 1000);
            }
        }, 3000);
    }
    
    // Áp dụng style cho tất cả các input
    const formInputs = document.querySelectorAll('.form-input');
    
    // Kiểm tra input có nội dung khi trang được tải
    formInputs.forEach(input => {
        // Set màu nền trắng và chữ đen cho input có giá trị
        if (input.value && input.value.trim() !== '') {
            input.style.backgroundColor = '#ffffff';
            input.style.color = '#000000';
        }
        
        // Xử lý khi người dùng nhập vào input
        input.addEventListener('input', function() {
            if (this.value && this.value.trim() !== '') {
                this.style.backgroundColor = '#ffffff';
                this.style.color = '#000000';
            } else {
                this.style.backgroundColor = '';
                this.style.color = '';
            }
        });
        
        // Xử lý khi focus vào input
        input.addEventListener('focus', () => {
            input.style.transition = 'all 0.3s';
            input.style.backgroundColor = '#ffffff';
            input.style.color = '#000000';
            input.style.boxShadow = '0 0 0 0.25rem rgba(13, 110, 253, 0.25)';
        });
        
        // Xử lý khi blur (mất focus) khỏi input
        input.addEventListener('blur', () => {
            input.style.boxShadow = '';
            
            // Nếu input không có giá trị, đặt lại màu nền và màu chữ mặc định
            if (!input.value || input.value.trim() === '') {
                input.style.backgroundColor = '';
                input.style.color = '';
            }
        });
    });
    
    // Form validation và submit
    const form = document.querySelector('.password-form');
    if (form) {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            
            // Reset previous errors
            hideValidationAlerts();
            
            const currentPasswordInput = form.querySelector('input[name="currentPassword"]');
            const newPasswordInput = form.querySelector('input[name="newPassword"]');
            const confirmPasswordInput = form.querySelector('input[name="confirmPassword"]');
            
            const currentPassword = currentPasswordInput ? currentPasswordInput.value : '';
            const newPassword = newPasswordInput ? newPasswordInput.value : '';
            const confirmPassword = confirmPasswordInput ? confirmPasswordInput.value : '';
            
            // Validate inputs (giữ nguyên code hiện tại)
            if (!currentPassword) {
                showValidationAlert('Vui lòng nhập mật khẩu hiện tại');
                return;
            }
            
            // Validate new password
            if (!newPassword) {
                showValidationAlert('Vui lòng nhập mật khẩu mới');
                return;
            }
            
            if (newPassword.length < 6) {
                showValidationAlert('Mật khẩu mới phải có ít nhất 6 ký tự');
                return;
            }
            
            // Thêm kiểm tra pattern mật khẩu phức tạp
            const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{6,30}$/;
            if (!passwordPattern.test(newPassword)) {
                showValidationAlert('Mật khẩu phải chứa ít nhất một chữ cái viết hoa, một chữ cái viết thường, một số và một ký tự đặc biệt');
                return;
            }
            
            // Validate password confirmation
            if (newPassword !== confirmPassword) {
                showValidationAlert('Mật khẩu mới và xác nhận mật khẩu không khớp');
                return;
            }
            
            // Thay đổi trạng thái nút submit
            const submitBtn = form.querySelector('button[type="submit"]');
            const originalText = submitBtn.textContent;
            submitBtn.textContent = 'Đang đổi...';
            
            console.log('Submitting form');
    
            // Submit form after validation passes
            form.submit();
        });
    }
    
    // Áp dụng style ngay khi trang load để đảm bảo các input có dữ liệu từ trước
    // (ví dụ như khi submit form không thành công và quay lại trang)
    formInputs.forEach(input => {
        if (input.value && input.value.trim() !== '') {
            input.style.backgroundColor = '#ffffff';
            input.style.color = '#000000';
        }
    });
});

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

/**
 * Thêm CSS cho trang
 */
function addFormStyles() {
    // Kiểm tra xem đã có style này chưa
    if (!document.getElementById('form-input-styles')) {
        const styleElement = document.createElement('style');
        styleElement.id = 'form-input-styles';
        styleElement.textContent = `
            .form-input:not(:placeholder-shown) {
                background-color: #ffffff !important;
                color: #000000 !important;
            }
            
            .form-input:focus {
                background-color: #ffffff !important;
                color: #000000 !important;
                box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25) !important;
            }
        `;
        document.head.appendChild(styleElement);
    }
}

// Chạy hàm thêm CSS ngay khi script được load
addFormStyles();