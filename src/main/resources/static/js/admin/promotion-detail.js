document.addEventListener("DOMContentLoaded", function () {
        const startTimeInput = document.getElementById("editStartTime");
        const endTimeInput = document.getElementById("editEndTime");

        // Convert date format if needed
        if (startTimeInput && startTimeInput.value) {
          const startTime = startTimeInput.value;
          if (startTime.includes(" ")) {
            // Convert from "yyyy-MM-dd HH:mm:ss" to "yyyy-MM-ddTHH:mm"
            const dt = new Date(startTime.replace(" ", "T"));
            if (!isNaN(dt.getTime())) {
              startTimeInput.value = dt.toISOString().slice(0, 16);
            }
          }
        }

        if (endTimeInput && endTimeInput.value) {
          const endTime = endTimeInput.value;
          if (endTime.includes(" ")) {
            // Convert from "yyyy-MM-dd HH:mm:ss" to "yyyy-MM-ddTHH:mm"
            const dt = new Date(endTime.replace(" ", "T"));
            if (!isNaN(dt.getTime())) {
              endTimeInput.value = dt.toISOString().slice(0, 16);
            }
          }
        }
        // Highlight active discount type and setup radio buttons
        highlightActiveDiscountType();
        setupDiscountTypeRadios();
        
        // Setup event listeners for edit mode
        const editButton = document.querySelector('button[onclick="enableEditMode()"]');
        if (editButton) {
            editButton.addEventListener('click', function() {
                // Show form help text in edit mode
                document.querySelectorAll('.form-text').forEach(el => {
                    el.style.display = 'block';
                });
            });
        }
      });

// Thiết lập trạng thái ban đầu cho radio buttons
function setupDiscountTypeRadios() {
    const percentRadio = document.getElementById('percentDiscountRadio');
    const directRadio = document.getElementById('directDiscountRadio');
    const discountLevel = document.getElementById('editDiscountLevel');
    const discountAmount = document.getElementById('editDiscountAmount');
    
    if (percentRadio && directRadio) {
        if (discountLevel && discountLevel.value && parseInt(discountLevel.value) > 0) {
            percentRadio.checked = true;
        } else if (discountAmount && discountAmount.value && parseInt(discountAmount.value) > 0) {
            directRadio.checked = true;
        }
        
        // Add event listeners to radio buttons
        percentRadio.addEventListener('change', function() {
            if (this.checked) {
                document.getElementById('percentDiscountGroup').style.display = 'block';
                document.getElementById('directDiscountGroup').style.display = 'none';
                
                // Enable percent discount fields
                document.getElementById('editDiscountLevel').removeAttribute('readonly');
                document.getElementById('editMaxAmountForPercentDiscount').removeAttribute('readonly');
                
                // Reset and disable direct discount field
                document.getElementById('editDiscountAmount').value = '';
                document.getElementById('editDiscountAmount').setAttribute('readonly', 'readonly');
            }
        });
        
        directRadio.addEventListener('change', function() {
            if (this.checked) {
                document.getElementById('directDiscountGroup').style.display = 'block';
                document.getElementById('percentDiscountGroup').style.display = 'none';
                
                // Enable direct discount field
                document.getElementById('editDiscountAmount').removeAttribute('readonly');
                
                // Reset and disable percent discount fields
                document.getElementById('editDiscountLevel').value = '';
                document.getElementById('editMaxAmountForPercentDiscount').value = '';
                document.getElementById('editDiscountLevel').setAttribute('readonly', 'readonly');
                document.getElementById('editMaxAmountForPercentDiscount').setAttribute('readonly', 'readonly');
            }
        });
    }
}

/**
 * Highlight loại giảm giá đang được sử dụng
 */
function highlightActiveDiscountType() {
    const discountLevelInput = document.getElementById('editDiscountLevel');
    const discountAmountInput = document.getElementById('editDiscountAmount');
    
    const percentDiscountGroup = document.getElementById('percentDiscountGroup');
    const directDiscountGroup = document.getElementById('directDiscountGroup');
    
    // Kiểm tra loại giảm giá đang được sử dụng
    const hasPercentDiscount = discountLevelInput.value && parseInt(discountLevelInput.value) > 0;
    const hasDirectDiscount = discountAmountInput.value && parseInt(discountAmountInput.value) > 0;
    
    // Ẩn group không sử dụng trong chế độ xem
    if (hasPercentDiscount) {
        directDiscountGroup.style.display = 'none';
    } else if (hasDirectDiscount) {
        percentDiscountGroup.style.display = 'none';
    }
}

/**
 * Chuyển sang chế độ chỉnh sửa
 */
function enableEditMode() {
    // Make fields editable
    document.getElementById("editPromotionCode").readOnly = false;
    document.getElementById("editTitle").readOnly = false;
    document.getElementById("editStartTime").readOnly = false;
    document.getElementById("editEndTime").readOnly = false;
    document.getElementById("editDetail").readOnly = false;
    document.getElementById("editUsageLimit").readOnly = false;
    document.getElementById("isActive").disabled = false;
    
    // Hiển thị phần chọn loại discount
    document.getElementById("discountTypeSelector").style.display = "flex";
    
    // Show image upload
    document.getElementById("editImageGroup").style.display = "block";

    // Show edit buttons, hide view buttons
    document.getElementById("viewButtons").style.display = "none";
    document.getElementById("editButtons").style.display = "block";
    
    // Apply initial state based on data
    const percentRadio = document.getElementById('percentDiscountRadio');
    const directRadio = document.getElementById('directDiscountRadio');
    
    if (percentRadio.checked) {
        percentRadio.dispatchEvent(new Event('change'));
    } else if (directRadio.checked) {
        directRadio.dispatchEvent(new Event('change'));
    }
}

/**
 * Hủy chỉnh sửa và tải lại trang
 */
function cancelEdit() {
    // Reload the page to reset the form
    window.location.reload();
}

function toggleStatus() {
    document.getElementById('toggleStatusForm').submit();
}

// Image preview for new uploads
document.getElementById("promotionImageInput")
    ?.addEventListener("change", function (event) {
    const file = event.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function (e) {
            const img = document.getElementById("promotionImage");
            img.src = e.target.result;
            img.style.display = "block";

            // Hide no-image placeholder if it exists
            const placeholder = document.querySelector(".no-image-placeholder");
            if (placeholder) {
                placeholder.style.display = "none";
            }
        };
        reader.readAsDataURL(file);
    }
});

// Form validation before submit
document.getElementById("promotionForm")?.addEventListener("submit", function(event) {
    const percentRadio = document.getElementById('percentDiscountRadio');
    const directRadio = document.getElementById('directDiscountRadio');
    const discountLevel = document.getElementById('editDiscountLevel');
    const maxAmount = document.getElementById('editMaxAmountForPercentDiscount');
    const discountAmount = document.getElementById('editDiscountAmount');
    
    // Check if at least one discount type is selected
    if (percentRadio && directRadio && !percentRadio.checked && !directRadio.checked) {
        if (!discountLevel.value && !discountAmount.value) {
            event.preventDefault();
            alert('Vui lòng chọn một loại giảm giá (% hoặc trực tiếp).');
            return;
        }
    }
    
    // Validate dates
    const startTime = new Date(document.getElementById('editStartTime').value);
    const endTime = new Date(document.getElementById('editEndTime').value);
    
    if (endTime <= startTime) {
        event.preventDefault();
        alert('Thời gian kết thúc phải sau thời gian bắt đầu.');
        return;
    }
    
    // Validate percent discount
    if (percentRadio && percentRadio.checked) {
        const discountLevelValue = parseFloat(discountLevel.value);
        const maxAmountValue = parseFloat(maxAmount.value);
        
        if (isNaN(discountLevelValue) || discountLevelValue < 1 || discountLevelValue > 20) {
            event.preventDefault();
            alert('Mức giảm giá phải từ 1% đến 20%.');
            return;
        }
        
        if (isNaN(maxAmountValue) || maxAmountValue < 1000) {
            event.preventDefault();
            alert('Số tiền giảm tối đa phải từ 1.000đ trở lên.');
            return;
        }
    }
    
    // Validate direct discount
    if (directRadio && directRadio.checked) {
        const discountAmountValue = parseFloat(discountAmount.value);
        
        if (isNaN(discountAmountValue) || discountAmountValue < 1000 ) {
            event.preventDefault();
            alert('Số tiền giảm trực tiếp phải từ 1.000đ trở lên.');
            return;
        }
    }
});