async function loadComponent(elementId, path) {
    try {
        const response = await fetch(path);
        const html = await response.text();
        document.getElementById(elementId).innerHTML = html;
    } catch (error) {
        console.error(`Error loading component from ${path}:`, error);
    }
}

// Load all components when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    loadComponent('navbar', 'homeLayout/navbar.html');
    loadComponent('footer', 'homeLayout/footer.html');
});