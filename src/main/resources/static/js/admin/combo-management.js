// Toggle sidebar on mobile
document.addEventListener('DOMContentLoaded', function () {
    const sidebar = document.getElementById('adminSidebar');
    const toggleBtn = document.createElement('button');
    toggleBtn.id = 'toggleSidebar';
    toggleBtn.className = 'btn btn-dark';
    toggleBtn.innerHTML = '<i class="fas fa-bars"></i>';
    document.body.appendChild(toggleBtn);

    toggleBtn.addEventListener('click', function () {
        sidebar.classList.toggle('active');
    });

    // Automatically show sidebar when accessing combo-management
    if (window.location.pathname.includes('/admin/combo-management')) {
        sidebar.classList.add('active');
    }

    // Hide sidebar when clicking outside on mobile
    document.addEventListener('click', function (event) {
        if (window.innerWidth <= 768) {
            if (!sidebar.contains(event.target) && !toggleBtn.contains(event.target)) {
                sidebar.classList.remove('active');
            }
        }
    });
});