document.addEventListener('DOMContentLoaded', function() {
    // Menu active state handling
    const currentPath = window.location.pathname;
    document.querySelectorAll('.menu-item').forEach(item => {
        if (item.getAttribute('href') === currentPath.split('/').pop()) {
            item.classList.add('active');
        }
    });

    // User modal handling
    const userMenuTrigger = document.getElementById('userMenuTrigger');
    const userModal = document.getElementById('userModal');
    const logoutBtn = document.getElementById('logoutBtn');

    // Toggle modal when clicking user menu
    userMenuTrigger.addEventListener('click', (e) => {
        e.stopPropagation();
        userModal.classList.toggle('show');
    });

    // Close modal when clicking outside
    document.addEventListener('click', (e) => {
        if (!userMenuTrigger.contains(e.target) && !userModal.contains(e.target)) {
            userModal.classList.remove('show');
        }
    });

    // Handle logout
    logoutBtn.addEventListener('click', (e) => {
        e.preventDefault();
        if (confirm('Bạn có chắc chắn muốn đăng xuất?')) {
            // Add your logout logic here
            window.location.href = 'login.html'; // Redirect to login page
        }
    });

    // Mobile menu toggle
    const menuToggle = document.querySelector('.menu-toggle');
    const sidebar = document.querySelector('.sidebar');
    
    if (menuToggle) {
        menuToggle.addEventListener('click', () => {
            sidebar.classList.toggle('mobile-open');
        });
    }
});