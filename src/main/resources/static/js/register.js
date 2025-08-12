// Animation for enhanced particle systems
const canvas = document.getElementById('animation-canvas');
const ctx = canvas.getContext('2d');
const trailCanvas = document.getElementById('trail-canvas');
const trailCtx = trailCanvas.getContext('2d');

canvas.width = window.innerWidth;
canvas.height = window.innerHeight;
trailCanvas.width = window.innerWidth;
trailCanvas.height = window.innerHeight;

// Particle systems
const nebulaSwirls = [];
const softGlows = [];
const floatingSparks = [];
const formOrbits = [];
const cursorTrails = [];
const nebulaSwirlCount = 80;
const softGlowCount = 50;
const sparkCount = 30;
const formOrbitCount = 20;
const trailCount = 10;

// Nebula swirl particle
class NebulaSwirl {
    constructor() {
        this.x = canvas.width / 2;
        this.y = canvas.height / 2;
        this.angle = Math.random() * Math.PI * 2;
        this.radius = Math.random() * 220 + 60;
        this.size = Math.random() * 4 + 1;
        this.speed = Math.random() * 0.035 + 0.015;
        this.opacity = Math.random() * 0.5 + 0.3;
        this.color = Math.random() > 0.7 ? `rgba(100, 0, 0, ${this.opacity})` : `rgba(150, 0, 0, ${this.opacity})`;
    }

    update() {
        this.angle += this.speed;
        this.x = canvas.width / 2 + Math.cos(this.angle) * this.radius;
        this.y = canvas.height / 2 + Math.sin(this.angle) * this.radius;
        this.opacity = Math.sin(this.angle * 2) * 0.25 + 0.35;
        this.radius += Math.sin(this.angle * 0.4) * 0.6;
        if (this.radius > 320 || this.radius < 60) {
            this.radius = Math.random() * 220 + 60;
            this.angle = Math.random() * Math.PI * 2;
        }
    }

    draw() {
        ctx.fillStyle = this.color;
        ctx.beginPath();
        ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
        ctx.fill();
    }
}

// Soft glow particle
class SoftGlow {
    constructor() {
        this.x = Math.random() * canvas.width;
        this.y = Math.random() * canvas.height;
        this.size = Math.random() * 6 + 3;
        this.opacity = Math.random() * 0.4 + 0.2;
        this.color = `rgba(100, 0, 0, ${this.opacity})`;
        this.pulseSpeed = Math.random() * 0.02 + 0.01;
        this.phase = Math.random() * Math.PI * 2;
    }

    update() {
        this.opacity = Math.sin(this.phase += this.pulseSpeed) * 0.2 + 0.3;
        if (this.opacity < 0.1) {
            this.x = Math.random() * canvas.width;
            this.y = Math.random() * canvas.height;
            this.opacity = Math.random() * 0.4 + 0.2;
        }
    }

    draw() {
        ctx.fillStyle = this.color;
        ctx.beginPath();
        ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
        ctx.fill();
    }
}

// Floating spark particle
class FloatingSpark {
    constructor() {
        this.x = Math.random() * canvas.width;
        this.y = canvas.height;
        this.size = Math.random() * 3 + 1;
        this.speedY = -(Math.random() * 1.2 + 0.6);
        this.speedX = Math.random() * 0.4 - 0.2;
        this.opacity = Math.random() * 0.5 + 0.3;
        this.color = `rgba(100, 0, 0, ${this.opacity})`;
        this.phase = Math.random() * Math.PI * 2;
    }

    update() {
        this.y += this.speedY;
        this.x += Math.sin(this.phase += 0.04) * 1;
        this.opacity -= 0.0015;
        if (this.y < 0 || this.opacity <= 0) {
            this.x = Math.random() * canvas.width;
            this.y = canvas.height;
            this.opacity = Math.random() * 0.5 + 0.3;
            this.speedY = -(Math.random() * 1.2 + 0.6);
            this.phase = Math.random() * Math.PI * 2;
        }
    }

    draw() {
        ctx.fillStyle = this.color;
        ctx.beginPath();
        ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
        ctx.fill();
    }
}

// Form orbiting particle
class FormOrbit {
    constructor() {
        this.angle = Math.random() * Math.PI * 2;
        this.radius = Math.random() * 70 + 35;
        this.speed = Math.random() * 0.06 + 0.03;
        this.size = Math.random() * 2 + 0.8;
        this.opacity = Math.random() * 0.6 + 0.4;
        this.color = `rgba(100, 0, 0, ${this.opacity})`;
        this.mouseOffsetX = 0;
        this.mouseOffsetY = 0;
    }

    update() {
        this.angle += this.speed;
        const registerContainer = document.querySelector('.register-container');
        if (registerContainer) {
            const rect = registerContainer.getBoundingClientRect();
            this.x = rect.left + rect.width / 2 + Math.cos(this.angle) * (this.radius + this.mouseOffsetX);
            this.y = rect.top + rect.height / 2 + Math.sin(this.angle) * (this.radius + this.mouseOffsetY);
            this.opacity = Math.sin(this.angle * 1.8) * 0.25 + 0.45;
        }
    }

    draw() {
        ctx.fillStyle = this.color;
        ctx.beginPath();
        ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
        ctx.fill();
    }
}

// Cursor trail particle
class CursorTrail {
    constructor(x, y) {
        this.x = x;
        this.y = y;
        this.size = Math.random() * 4 + 2;
        this.opacity = 0.6;
        this.color = `rgba(100, 0, 0, ${this.opacity})`;
        this.life = 0;
        this.maxLife = 60;
    }

    update() {
        this.life++;
        this.opacity = 0.6 * (1 - this.life / this.maxLife);
        this.size *= 0.95;
    }

    draw() {
        trailCtx.fillStyle = this.color;
        trailCtx.beginPath();
        trailCtx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
        trailCtx.fill();
    }
}

// Initialize particle systems
function initAnimations() {
    for (let i = 0; i < nebulaSwirlCount; i++) {
        nebulaSwirls.push(new NebulaSwirl());
    }
    for (let i = 0; i < softGlowCount; i++) {
        softGlows.push(new SoftGlow());
    }
    for (let i = 0; i < sparkCount; i++) {
        floatingSparks.push(new FloatingSpark());
    }
    for (let i = 0; i < formOrbitCount; i++) {
        formOrbits.push(new FormOrbit());
    }
}

// Animation loop
function animate() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    trailCtx.clearRect(0, 0, trailCanvas.width, trailCanvas.height);

    nebulaSwirls.forEach(p => { p.update(); p.draw(); });
    softGlows.forEach(p => { p.update(); p.draw(); });
    floatingSparks.forEach(p => { p.update(); p.draw(); });
    formOrbits.forEach(p => { p.update(); p.draw(); });

    cursorTrails.forEach((p, i) => {
        p.update();
        p.draw();
        if (p.life >= p.maxLife) {
            cursorTrails.splice(i, 1);
        }
    });

    requestAnimationFrame(animate);
}

// Mouse interaction for form particles and cursor trail
let mouseX = 0, mouseY = 0;
document.addEventListener('mousemove', (e) => {
    mouseX = e.clientX;
    mouseY = e.clientY;

    // Add cursor trail particle
    if (cursorTrails.length < trailCount) {
        cursorTrails.push(new CursorTrail(mouseX, mouseY));
    }

    formOrbits.forEach(p => {
        const registerContainer = document.querySelector('.register-container');
        if (registerContainer) {
            const rect = registerContainer.getBoundingClientRect();
            const centerX = rect.left + rect.width / 2;
            const centerY = rect.top + rect.height / 2;
            const dx = mouseX - centerX;
            const dy = mouseY - centerY;
            p.mouseOffsetX = dx * 0.04;
            p.mouseOffsetY = dy * 0.04;
        }
    });
});

initAnimations();
animate();

// Resize canvas on window resize
window.addEventListener('resize', () => {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    trailCanvas.width = window.innerWidth;
    trailCanvas.height = window.innerHeight;
});

// Validation form đăng ký
(() => {
    'use strict';
    const forms = document.querySelectorAll('.needs-validation');
    Array.from(forms).forEach(form => {
        form.addEventListener('submit', event => {
            const submitBtn = document.getElementById('registerBtn');
            const overlay = document.querySelector('.transition-overlay');

            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
                console.log("Form submission failed validation:", form);
            } else {
                event.preventDefault();
                submitBtn.classList.add('loading');
                submitBtn.innerHTML = '';

                overlay.classList.add('active');

                setTimeout(() => {
                    form.submit();
                }, 600);
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
            const label = this.parentElement.querySelector('.form-label');
            if (label) {
                label.style.color = '#640000';
            }
        });
        input.addEventListener('blur', function() {
            const label = this.parentElement.querySelector('.form-label');
            if (label) {
                label.style.color = '#ffffff';
            }
        });
        input.addEventListener('input', function() {
            if (this.value.length > 0) {
                this.style.transform = 'translateY(-2px)';
            } else {
                this.style.transform = 'translateY(0)';
            }
        });
    });

    // Password suggestion logic
    const passwordInput = document.getElementById('password');
    const suggestions = document.getElementById('password-suggestions');

    passwordInput.addEventListener('focus', () => {
        suggestions.classList.add('show');
    });

    passwordInput.addEventListener('blur', () => {
        suggestions.classList.remove('show');
    });

    passwordInput.addEventListener('input', () => {
        const value = passwordInput.value;
        const lengthCheck = document.getElementById('length');
        const uppercaseCheck = document.getElementById('uppercase');
        const numberCheck = document.getElementById('number');
        const specialCheck = document.getElementById('special');

        // Check length
        if (value.length >= 6) {
            lengthCheck.classList.remove('invalid');
            lengthCheck.classList.add('valid');
        } else {
            lengthCheck.classList.remove('valid');
            lengthCheck.classList.add('invalid');
        }

        // Check uppercase
        if (/[A-Z]/.test(value)) {
            uppercaseCheck.classList.remove('invalid');
            uppercaseCheck.classList.add('valid');
        } else {
            uppercaseCheck.classList.remove('valid');
            uppercaseCheck.classList.add('invalid');
        }

        // Check number
        if (/[0-9]/.test(value)) {
            numberCheck.classList.remove('invalid');
            numberCheck.classList.add('valid');
        } else {
            numberCheck.classList.remove('valid');
            numberCheck.classList.add('invalid');
        }

        // Check special character
        if (/[!@#$%^&*]/.test(value)) {
            specialCheck.classList.remove('invalid');
            specialCheck.classList.add('valid');
        } else {
            specialCheck.classList.remove('valid');
            specialCheck.classList.add('invalid');
        }
    });
});

// Generate random password
function generatePassword() {
    const chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    const length = 12;
    let password = '';
    for (let i = 0; i < length; i++) {
        password += chars.charAt(Math.floor(Math.random() * chars.length));
    }

    const passwordField = document.getElementById('password');
    const confirmPasswordField = document.getElementById('confirmPassword');
    passwordField.value = password;
    confirmPasswordField.value = password;

    // Trigger input event to update suggestion checks
    passwordField.dispatchEvent(new Event('input'));

    Swal.fire({
        icon: 'success',
        title: 'Mật khẩu đã được tạo',
        html: `Mật khẩu của bạn là: <strong>${password}</strong><br>Vui lòng lưu lại mật khẩu này!`,
        confirmButtonColor: '#640000',
        background: 'linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%)',
        backdrop: 'rgba(0,0,0,0.8)',
        showClass: { popup: 'animate__animated animate__fadeInDown' },
        hideClass: { popup: 'animate__animated animate__fadeOutUp' }
    });
}

// Hiển thị thông báo lỗi bằng SweetAlert2
document.addEventListener('DOMContentLoaded', () => {
    const registerError = document.getElementById('register-error');
    const validationError = document.getElementById('validation-error');

    if (registerError && registerError.textContent.trim()) {
        showError(registerError.textContent.trim());
    }
    if (validationError && validationError.textContent.trim()) {
        showError(validationError.textContent.trim());
    }
});

// Hàm hiển thị thông báo lỗi
function showError(message) {
    Swal.fire({
        icon: 'error',
        title: 'Lỗi',
        text: message,
        confirmButtonColor: '#640000',
        confirmButtonText: 'Đóng',
        background: 'linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%)',
        backdrop: 'rgba(0,0,0,0.8)',
        showClass: { popup: 'animate__animated animate__fadeInDown' },
        hideClass: { popup: 'animate__animated animate__fadeOutUp' }
    });
}

// Hàm hiển thị thông báo thành công
function showSuccess(message) {
    Swal.fire({
        icon: 'success',
        title: 'Thành công',
        text: message,
        confirmButtonColor: '#3085d6',
        confirmButtonText: 'Đóng',
        background: 'linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%)',
        backdrop: 'rgba(0,0,0,0.8)',
        showClass: { popup: 'animate__animated animate__fadeInDown' },
        hideClass: { popup: 'animate__animated animate__fadeOutUp' }
    });
}

// Hàm hiển thị thông báo thông tin
function showInfo(message) {
    Swal.fire({
        icon: 'info',
        title: 'Thông báo',
        text: message,
        confirmButtonColor: '#3085d6',
        confirmButtonText: 'Đóng',
        background: 'linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%)',
        backdrop: 'rgba(0,0,0,0.8)',
        showClass: { popup: 'animate__animated animate__fadeInDown' },
        hideClass: { popup: 'animate__animated animate__fadeOutUp' }
    });
}