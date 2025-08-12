document.getElementById("showDate").addEventListener("change", function () {
    const selectedDate = this.value;
    const movieId = /* lấy movieId từ th:data hoặc JS biến */
      document.querySelector("[data-movie-id]").getAttribute("data-movie-id");
            console.log( 'data')
    fetch(`/api/showtimes?movieId=${movieId}&date=${selectedDate}`)
      .then(response => response.json())
      .then(data => {
        const select = document.getElementById("showtimeSelect");
        select.innerHTML = ""; // clear old options
        console.log(data, 'data')

        if (data.length === 0) {
          select.innerHTML = "<option disabled>Không có suất chiếu</option>";
          return;
        }
        data.forEach(item => {
          const opt = document.createElement("option");
          opt.value = item.time;
          opt.text = `${item.time}`;
          select.appendChild(opt);
        });
      })
      .catch(err => {
        console.error("Lỗi khi lấy suất chiếu:", err);
      });
  });