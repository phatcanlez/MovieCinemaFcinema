
        function showTab(tabId) {
            // Ẩn tất cả tab content
            const tabContents = document.querySelectorAll('.tab-content');
            tabContents.forEach(content => {
                content.classList.remove('active');
            });

            // Bỏ active class khỏi tất cả tab buttons
            const tabButtons = document.querySelectorAll('.tab-button');
            tabButtons.forEach(button => {
                button.classList.remove('active');
            });

            // Hiển thị tab được chọn
            document.getElementById(tabId).classList.add('active');

            // Thêm active class cho button được click
            event.target.classList.add('active');
        }

function openTrailerFromButton(button) {
    const movieName = button.getAttribute('data-movie-name');
    const trailerId = button.getAttribute('data-trailer-id');
    openTrailer(movieName, trailerId);
}

function openTrailer(movieName, youtubeId) {
    // Hiển thị modal với trailer
    const modal = document.getElementById('trailerModal');
    const modalTitle = document.getElementById('modalTitle');
    const trailerFrame = document.getElementById('trailerFrame');
    
    modalTitle.textContent = 'Trailer: ' + movieName;
    trailerFrame.src = 'https://www.youtube.com/embed/' + youtubeId;
    
    modal.style.display = 'block';
}

        function closeTrailer() {
            const modal = document.getElementById('trailerModal');
            const trailerFrame = document.getElementById('trailerFrame');

            modal.style.display = 'none';
            trailerFrame.src = '';
            document.body.style.overflow = 'auto';
        }

        // Đóng modal khi click bên ngoài
        window.onclick = function (event) {
            const modal = document.getElementById('trailerModal');
            if (event.target === modal) {
                closeTrailer();
            }
        }

        // Đóng modal bằng phím ESC
        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape') {
                closeTrailer();
            }
        });

        // Thêm hiệu ứng loading cho poster images
        document.addEventListener('DOMContentLoaded', function () {
            const images = document.querySelectorAll('.movie-poster');
            images.forEach(img => {
                img.addEventListener('load', function () {
                    this.style.opacity = '1';
                });
            });
        });