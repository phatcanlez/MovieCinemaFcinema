// filepath: e:\FPT\OJT\MovieTheater_Profile\movieproject_gr1\src\main\resources\static\js\admin\combo-detail.js
document.addEventListener("DOMContentLoaded", function () {
    console.log("Combo detail page loaded");
    
    // Auto-dismiss alerts after 5 seconds
    setTimeout(() => {
        const alerts = document.querySelectorAll('.alert');
        alerts.forEach(alert => {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        });
    }, 5000);
});

/**
 * Enable edit mode for the form
 */
function enableEditMode() {
    console.log("Enabling edit mode");
    
    // Enable fields
    document.getElementById("comboName").readOnly = false;
    document.getElementById("description").readOnly = false;
    document.getElementById("price").readOnly = false;
    document.getElementById("discountPercentage").readOnly = false;
    
    // Enable select and checkbox
    document.getElementById("comboStatus").disabled = false;
    document.getElementById("active").disabled = false;
    
    // Show image upload
    document.getElementById("editImageGroup").style.display = "block";
    
    // Toggle button visibility
    document.getElementById("viewButtons").style.display = "none";
    document.getElementById("editButtons").style.display = "block";
}

/**
 * Cancel edit mode and reload the page
 */
function cancelEdit() {
    window.location.reload();
}

/**
 * Show the delete confirmation modal
 */
function confirmDelete() {
    const deleteModal = new bootstrap.Modal(document.getElementById('deleteConfirmModal'));
    deleteModal.show();
}

/**
 * Submit the toggle status form
 */
function toggleStatus() {
    document.getElementById("toggleStatusForm").submit();
}

// Handle image preview
document.getElementById('comboImageInput')?.addEventListener('change', function() {
    const file = this.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            const img = document.getElementById('comboImage');
            if (img) {
                img.src = e.target.result;
            } else {
                // If image element doesn't exist, create one
                const imgContainer = document.querySelector('.text-center.mb-4');
                const noImagePlaceholder = document.querySelector('.no-image-placeholder');
                
                if (noImagePlaceholder) {
                    noImagePlaceholder.style.display = 'none';
                }
                
                const newImg = document.createElement('img');
                newImg.src = e.target.result;
                newImg.id = 'comboImage';
                newImg.className = 'promotion-image mb-3';
                newImg.alt = 'Combo Image';
                
                imgContainer.prepend(newImg);
            }
        };
        reader.readAsDataURL(file);
    }
});

// Form validation
document.getElementById('comboForm')?.addEventListener('submit', function(event) {
    const comboName = document.getElementById('comboName').value.trim();
    const price = parseFloat(document.getElementById('price').value);
    const discountPercentage = parseFloat(document.getElementById('discountPercentage').value);
    
    // Validate combo name
    if (comboName.length < 2) {
        event.preventDefault();
        alert('Combo name must be at least 2 characters');
        return;
    }
    
    // Validate price
    if (isNaN(price) || price <= 0) {
        event.preventDefault();
        alert('Price must be greater than 0');
        return;
    }
    
    // Validate discount percentage
    if (isNaN(discountPercentage) || discountPercentage < 0 || discountPercentage > 100) {
        event.preventDefault();
        alert('Discount percentage must be between 0 and 100');
        return;
    }
    
    // All validation passed, form will submit
});