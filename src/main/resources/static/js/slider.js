// Hàm hiển thị thông báo lỗi
function showError(message) {
    Swal.fire({
        icon: 'error',
        title: 'Lỗi',
        text: message,
        confirmButtonText: 'Đóng',
        confirmButtonColor: '#dc2626',
        backdrop: true,
        allowOutsideClick: false
    });
}

// Hàm hiển thị thông báo thành công
function showSuccess(message) {
    Swal.fire({
        icon: 'success',
        title: 'Thành công',
        text: message,
        confirmButtonText: 'Đóng',
        confirmButtonColor: '#16a34a',
        backdrop: true,
        allowOutsideClick: false
    });
}

// Hàm hiển thị thông báo thông tin
function showInfo(message) {
    Swal.fire({
        icon: 'info',
        title: 'Thông báo',
        text: message,
        confirmButtonText: 'Đóng',
        confirmButtonColor: '#dc2626',
        backdrop: true,
        allowOutsideClick: false
    });
}

  function confirmDeleteMovie(button){
    const movieId = button.getAttribute("data-id");
    Swal.fire({
        title: 'Are you sure?',
        text: 'Do you really want to delete this movie?',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Yes',
        cancelButtonText: 'No',
        confirmButtonColor: '#dc2626',
        cancelButtonColor: '#6b7280'
    }).then((result) => {
        if (result.isConfirmed) {
                   // Tạo form ẩn
                   const form = document.createElement('form');
                   form.method = 'POST'; // hoặc 'DELETE' nếu backend hỗ trợ
                   form.action = `/admin/movies/delete/${movieId}`;
                   document.body.appendChild(form);
                   form.submit();
               }
           });
}
