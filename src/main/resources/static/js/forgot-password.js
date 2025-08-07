// Form validation and smooth transition
(() => {
    'use strict';
    const forms = document.querySelectorAll('.needs-validation');
    Array.from(forms).forEach(form => {
        form.addEventListener('submit', event => {
            const submitBtn = form.querySelector('.btn-primary');
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            } else {
                event.preventDefault(); // Prevent default for transition
                submitBtn.style.backgroundColor = '#e0e0e0';
                submitBtn.style.color = '#c80000';
                setTimeout(() => {
                    form.submit(); // Submit after a short delay
                }, 300);
            }
            form.classList.add('was-validated');
        }, false);
    });
})();

// Enhanced form interactions
document.addEventListener('DOMContentLoaded', () => {
    const inputs = document.querySelectorAll('.form-control');
    inputs.forEach(input => {
        input.addEventListener('focus', function() {
            this.parentElement.querySelector('.form-label').style.color = '#ffffff';
        });
        input.addEventListener('blur', function() {
            this.parentElement.querySelector('.form-label').style.color = '#ffffff';
        });
    });

    const emailInput = document.getElementById('email');
    const otpInput = document.getElementById('otp');
    const newPasswordInput = document.getElementById('newPassword');
    const confirmPasswordInput = document.getElementById('confirmPassword');

    [emailInput, otpInput, newPasswordInput, confirmPasswordInput].forEach(input => {
        input.addEventListener('input', function() {
            if (this.value.length > 0) {
                this.style.transform = 'translateY(-2px)';
            } else {
                this.style.transform = 'translateY(0)';
            }
        });
    });
});