document.addEventListener('DOMContentLoaded', () => {
    const searchInput = document.getElementById('searchBox');
    const resultsList = document.getElementById('searchResults');

    const debounce = (func, delay) => {
        let timer;
        return function (...args) {
            clearTimeout(timer);
            timer = setTimeout(() => func.apply(this, args), delay);
        };
    };

    const fetchResults = debounce(async function () {
        const query = this.value.trim();
        if (query.length === 0) {
            resultsList.innerHTML = '';
            resultsList.style.display = 'none';
            return;
        }

        try {
            const response = await fetch(`/movies/search?query=${encodeURIComponent(query)}`);
            const movies = await response.json();
            console.log('🚀 ~ fetchResults ~ movies:', movies)

            resultsList.innerHTML = '';
            resultsList.style.display = 'block';

            if (movies.length === 0) {
                resultsList.innerHTML = '<li class="list-group-item text-muted">Không tìm thấy phim</li>';
                return;
            }

            movies.forEach(movie => {
                const li = document.createElement('li');
                li.classList.add('list-group-item', 'd-flex', 'align-items-center', 'gap-3');

                // Poster ảnh nhỏ
                const img = document.createElement('img');
                img.src = movie.posterUrl || '/images/default-poster.jpg'; // fallback nếu không có ảnh
                img.alt = movie.movieNameVn;
                img.style.width = '50px';
                img.style.height = '75px';
                img.classList.add('rounded');

                // Tên phim với highlight
                const regex = new RegExp(`(${query})`, 'gi');
                const titleHtml = movie.movieNameVn.replace(regex, '<mark>$1</mark>');
                const titleDiv = document.createElement('div');
                titleDiv.innerHTML = `<strong>${titleHtml}</strong>`;

                li.appendChild(img);
                li.appendChild(titleDiv);

                li.addEventListener('click', () => {
                    window.location.href = `/movie-detail/${movie.movieId}`;
                });

                resultsList.appendChild(li);
            });
        } catch (err) {
            console.error('Lỗi tìm kiếm:', err);
            resultsList.innerHTML = '<li class="list-group-item text-danger">Lỗi khi tải kết quả</li>';
        }
    }, 300);

    searchInput.addEventListener('input', fetchResults);

    // Ẩn dropdown khi click ra ngoài
    document.addEventListener('click', (e) => {
        if (!searchInput.contains(e.target) && !resultsList.contains(e.target)) {
            resultsList.style.display = 'none';
        }
    });
});
