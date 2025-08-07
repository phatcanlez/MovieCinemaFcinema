document.addEventListener('DOMContentLoaded', () => {

    const addMovieButton = document.getElementById('addMovieButton');


    // Show add movie modal
    addMovieButton.addEventListener('click', () => {
        const modal = new bootstrap.Modal(document.getElementById('addMovieModal'));
        modal.show();
    });


});