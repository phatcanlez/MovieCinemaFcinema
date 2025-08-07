document.addEventListener('DOMContentLoaded', () => {
    console.log("DOM loaded, initializing promotion management...");
    
    // Selectors
    const promotionTableBody = document.getElementById('promotionTableBody');
    const noResultsRow = document.getElementById('noResultsRow');
    const addPromotionButton = document.getElementById('addPromotionButton');
    const searchInput = document.getElementById('searchInput');
    const searchButton = document.getElementById('searchButton');
    
    // Check modal and form exist
    const addPromotionModal = document.getElementById('addPromotionModal'); 
    const addPromotionForm = document.getElementById('addPromotionForm');
    console.log("Add button exists:", addPromotionButton !== null);
    console.log("Modal exists:", addPromotionModal !== null);
    console.log("Form exists:", addPromotionForm !== null);
    
    // Initialize date inputs but don't set default end time
    initDateInputs();
    
    // Handle Add Promotion button click
    if (addPromotionButton) {
        addPromotionButton.addEventListener('click', () => {
            console.log("Add button clicked");
            try {
                if (typeof bootstrap !== 'undefined' && bootstrap.Modal) {
                    const modal = new bootstrap.Modal(addPromotionModal);
                    modal.show();
                    console.log("Modal shown");
                } else {
                    console.error("Bootstrap Modal is not available");
                }
            } catch (error) {
                console.error("Error showing modal:", error);
                alert("Không thể mở cửa sổ thêm khuyến mãi. Vui lòng thử lại.");
            }
        });
    }

    // Form validation for the add promotion form
    if (addPromotionForm) {
        console.log("Add form found, setting up validation");
        
        addPromotionForm.addEventListener('submit', function(event) {
            // Validate form fields
            const promotionCode = document.getElementById('promotionCode')?.value?.trim();
            const title = document.getElementById('title')?.value?.trim();
            const startTime = document.getElementById('startTime')?.value;
            const endTime = document.getElementById('endTime')?.value;
            const discountLevel = parseInt(document.getElementById('discountLevel')?.value);
            const maxAmountForPercentDiscount = parseInt(document.getElementById('maxAmountForPercentDiscount')?.value);
            const discountAmount = parseInt(document.getElementById('discountAmount')?.value);
            const detail = document.getElementById('detail')?.value?.trim();
            const fileInput = document.getElementById('promotionImage');
            
            console.log("Validating form data:", { 
                promotionCode, 
                title, 
                startTime, 
                endTime, 
                discountLevel,
                maxAmountForPercentDiscount,
                discountAmount, 
                detail,
                hasFile: fileInput && fileInput.files.length > 0
            });
            
            // Basic validation
            if (!promotionCode || !title || !startTime || !endTime || !detail) {
                event.preventDefault();
                alert('Vui lòng điền đầy đủ thông tin cơ bản.');
                return;
            }
            // Validate discount type - cần có ít nhất một loại giảm giá
            const hasPercentDiscount = !isNaN(discountLevel) && discountLevel > 0;
            const hasDirectDiscount = !isNaN(discountAmount) && discountAmount > 0;
            // Validate discount level
            if (!hasPercentDiscount && !hasDirectDiscount) {
                event.preventDefault();
                alert('Vui lòng chọn một loại giảm giá (% hoặc trực tiếp).');
                return;
            }

            if (hasPercentDiscount && (isNaN(maxAmountForPercentDiscount) || maxAmountForPercentDiscount <= 0)) {
                event.preventDefault();
                alert('Vui lòng nhập số tiền giảm tối đa cho mức giảm giá theo %.');
                return;
            }

            if (hasPercentDiscount && (discountLevel < 1 || discountLevel > 20)) {
                event.preventDefault();
                alert('Mức giảm giá phải từ 1% đến 20%.');
                return;
            }

            if (hasPercentDiscount && (maxAmountForPercentDiscount < 1000)) {
                event.preventDefault();
                alert('Số tiền giảm tối đa phải từ 1.000đ trở lên.');
                return;
            }

            if (hasDirectDiscount && (discountAmount < 1000)) {
                event.preventDefault();
                alert('Số tiền giảm trực tiếp phải từ 1.000đ trở lên.');
                return;
            }
            
            // Validate dates
            const startTimeDate = new Date(startTime);
            const endTimeDate = new Date(endTime);
            
            if (endTimeDate <= startTimeDate) {
                event.preventDefault();
                alert('Thời gian kết thúc phải sau thời gian bắt đầu.');
                return;
            }
            
            // If validation passes, form will submit normally
            console.log("Form validation passed, submitting...");
        });
    }
    
    // Auto-dismiss alerts after 5 seconds if they exist
    setTimeout(() => {
        const alerts = document.querySelectorAll('.alert');
        if (alerts.length > 0 && typeof bootstrap !== 'undefined') {
            alerts.forEach(alert => {
                if (bootstrap.Alert) {
                    try {
                        const bsAlert = new bootstrap.Alert(alert);
                        bsAlert.close();
                    } catch (error) {
                        console.error("Error closing alert:", error);
                    }
                }
            });
        }
    }, 5000);
      // Function to initialize date inputs
    function initDateInputs() {
        const startTimeInput = document.getElementById('startTime');
        
        if (startTimeInput) {
            // Set min date to today
            const now = new Date();
            const year = now.getFullYear();
            const month = String(now.getMonth() + 1).padStart(2, '0');
            const day = String(now.getDate()).padStart(2, '0');
            const hours = String(now.getHours()).padStart(2, '0');
            const minutes = String(now.getMinutes()).padStart(2, '0');
            
            const formattedDateTime = `${year}-${month}-${day}T${hours}:${minutes}`;
            
            // Set min date and default value for start time only
            startTimeInput.min = formattedDateTime;
            startTimeInput.value = formattedDateTime;
            
            // Set min date for end time but don't set a default value
            // Let the user choose when promotion ends
            const endTimeInput = document.getElementById('endTime');
            if (endTimeInput) {
                endTimeInput.min = formattedDateTime;
            }
        }
    }
    
    function setupDiscountTypeToggle() {
        // Get discount fields
        const discountLevelInput = document.getElementById('discountLevel');
        const maxAmountInput = document.getElementById('maxAmountForPercentDiscount');
        const discountAmountInput = document.getElementById('discountAmount');
        
        if (discountLevelInput && maxAmountInput && discountAmountInput) {
            // Khi thay đổi discountLevel hoặc maxAmount (giảm theo %)
            [discountLevelInput, maxAmountInput].forEach(input => {
                input.addEventListener('input', function() {
                    if (this.value && this.value > 0) {
                        // Nếu nhập giảm theo %, disable giảm trực tiếp
                        discountAmountInput.value = '';
                        discountAmountInput.disabled = true;
                        
                        // Đảm bảo cả 2 trường giảm theo % đều được điền
                        if (discountLevelInput.value && discountLevelInput.value > 0 &&
                            maxAmountInput.value && maxAmountInput.value > 0) {
                            // Highlight percent discount group
                            document.getElementById('percentDiscountGroup').classList.add('bg-light', 'p-2', 'border', 'rounded');
                            document.getElementById('directDiscountGroup').classList.add('opacity-50');
                        }
                    } else {
                        // Nếu cả 2 trường % đều rỗng, enable giảm trực tiếp
                        if (!discountLevelInput.value && !maxAmountInput.value) {
                            discountAmountInput.disabled = false;
                            
                            // Remove highlighting
                            document.getElementById('percentDiscountGroup').classList.remove('bg-light', 'p-2', 'border', 'rounded');
                            document.getElementById('directDiscountGroup').classList.remove('opacity-50');
                        }
                    }
                });
            });
            
            // Khi thay đổi discountAmount (giảm trực tiếp)
            discountAmountInput.addEventListener('input', function() {
                if (this.value && this.value > 0) {
                    // Nếu nhập giảm trực tiếp, disable giảm theo %
                    discountLevelInput.value = '';
                    maxAmountInput.value = '';
                    discountLevelInput.disabled = true;
                    maxAmountInput.disabled = true;
                    
                    // Highlight direct discount group
                    document.getElementById('directDiscountGroup').classList.add('bg-light', 'p-2', 'border', 'rounded');
                    document.getElementById('percentDiscountGroup').classList.add('opacity-50');
                } else {
                    // Nếu trường giảm trực tiếp rỗng, enable giảm theo %
                    discountLevelInput.disabled = false;
                    maxAmountInput.disabled = false;
                    
                    // Remove highlighting
                    document.getElementById('directDiscountGroup').classList.remove('bg-light', 'p-2', 'border', 'rounded');
                    document.getElementById('percentDiscountGroup').classList.remove('opacity-50');
                }
            });
        }
    }
    
    // Reset form when modal opens
    function resetForm() {
        const form = document.getElementById('addPromotionForm');
        if (form) {
            form.reset();
            
            // Reset discount fields state
            const discountLevelInput = document.getElementById('discountLevel');
            const maxAmountInput = document.getElementById('maxAmountForPercentDiscount');
            const discountAmountInput = document.getElementById('discountAmount');
            
            if (discountLevelInput && maxAmountInput && discountAmountInput) {
                discountLevelInput.disabled = false;
                maxAmountInput.disabled = false;
                discountAmountInput.disabled = false;
                
                // Reset styling
                document.getElementById('percentDiscountGroup').classList.remove('bg-light', 'p-2', 'border', 'rounded', 'opacity-50');
                document.getElementById('directDiscountGroup').classList.remove('bg-light', 'p-2', 'border', 'rounded', 'opacity-50');
            }
            
            // Clear image preview
            const imagePreviewContainer = document.getElementById('imagePreviewContainer');
            if (imagePreviewContainer) {
                imagePreviewContainer.innerHTML = '';
            }
            
            // Re-initialize date inputs
            initDateInputs();
        }
    }

    // Image preview functionality
    const promotionImageInput = document.getElementById('promotionImage');
    if (promotionImageInput) {
        promotionImageInput.addEventListener('change', function() {
            const imagePreviewContainer = document.getElementById('imagePreviewContainer');
            if (!imagePreviewContainer) {
                // Tạo container cho xem trước hình ảnh nếu chưa có
                const container = document.createElement('div');
                container.id = 'imagePreviewContainer';
                container.classList.add('mt-2', 'text-center');
                promotionImageInput.parentNode.appendChild(container);
            }
            
            const previewContainer = document.getElementById('imagePreviewContainer');
            previewContainer.innerHTML = '';
            
            if (this.files && this.files[0]) {
                const reader = new FileReader();
                
                reader.onload = function(e) {
                    const img = document.createElement('img');
                    img.src = e.target.result;
                    img.classList.add('img-thumbnail', 'promotion-preview');
                    img.style.maxHeight = '150px';
                    previewContainer.appendChild(img);
                    
                    console.log('Image preview created');
                }
                
                reader.readAsDataURL(this.files[0]);
            }
        });
    }
    
    // Search functionality
    if (searchButton && searchInput) {
        searchButton.addEventListener('click', () => {
            const keyword = searchInput.value.toLowerCase();
            console.log("Searching for:", keyword);
            // Implement search functionality
        });
    }
    
    // QUAN TRỌNG: Gọi hàm thiết lập bộ chọn loại discount ở đây
    setupDiscountTypeSelector(); // Thêm dòng này để khởi tạo
    
    // Form validation
    setupFormValidation();
    
    // Các định nghĩa hàm...
});

// Các hàm được định nghĩa bên ngoài event listener DOMContentLoaded
// Thiết lập logic chọn loại discount
function setupDiscountTypeSelector() {
    console.log("Setting up discount type selector...");
    const percentRadio = document.getElementById('percentDiscountRadio');
    const directRadio = document.getElementById('directDiscountRadio');
    const percentContainer = document.getElementById('percentDiscountContainer');
    const directContainer = document.getElementById('directDiscountContainer');
    
    console.log("Elements found:", {
        percentRadio: percentRadio !== null,
        directRadio: directRadio !== null,
        percentContainer: percentContainer !== null,
        directContainer: directContainer !== null
    });
    
    if (!percentRadio || !directRadio || !percentContainer || !directContainer) {
        console.error("Missing discount selector elements");
        return;
    }
    
    // Xử lý khi người dùng chọn loại discount
    percentRadio.addEventListener('change', function() {
        console.log("Percent radio selected");
        if (this.checked) {
            percentContainer.style.display = 'block';
            directContainer.style.display = 'none';
            
            // Reset và disable các trường giảm trực tiếp
            resetDiscountFields('direct');
            
            // Focus vào trường đầu tiên
            const discountLevel = document.getElementById('discountLevel');
            if (discountLevel) discountLevel.focus();
        }
    });
    
    directRadio.addEventListener('change', function() {
        console.log("Direct radio selected");
        if (this.checked) {
            directContainer.style.display = 'block';
            percentContainer.style.display = 'none';
            
            // Reset và disable các trường giảm theo %
            resetDiscountFields('percent');
            
            // Focus vào trường đầu tiên
            const discountAmount = document.getElementById('discountAmount');
            if (discountAmount) discountAmount.focus();
        }
    });
    
    // Đồng bộ trường usageLimit giữa 2 form
    const usageLimitPercent = document.getElementById('usageLimitPercent');
    const usageLimitDirect = document.getElementById('usageLimitDirect');
    
    if (usageLimitPercent && usageLimitDirect) {
        usageLimitPercent.addEventListener('input', function() {
            usageLimitDirect.value = this.value;
        });
        
        usageLimitDirect.addEventListener('input', function() {
            usageLimitPercent.value = this.value;
        });
    }
}

// Reset các trường của loại không được chọn
function resetDiscountFields(type) {
    if (type === 'direct') {
        const discountAmount = document.getElementById('discountAmount');
        if (discountAmount) {
            discountAmount.value = '';
        }
    } else if (type === 'percent') {
        const discountLevel = document.getElementById('discountLevel');
        const maxAmount = document.getElementById('maxAmountForPercentDiscount');
        if (discountLevel) discountLevel.value = '';
        if (maxAmount) maxAmount.value = '';
    }
}

// Thiết lập form validation
function setupFormValidation() {
    const addPromotionForm = document.getElementById('addPromotionForm');
    
    if (!addPromotionForm) {
        console.error("Form not found");
        return;
    }
    
    addPromotionForm.addEventListener('submit', function(event) {
        // Basic required fields validation based on RequestPromotionDTO
        const promotionCode = document.getElementById('promotionCode')?.value?.trim();
        const title = document.getElementById('title')?.value?.trim();
        const startTime = document.getElementById('startTime')?.value;
        const endTime = document.getElementById('endTime')?.value;
        const detail = document.getElementById('detail')?.value?.trim();
        
        // Validate required fields from RequestPromotionDTO
        if (!promotionCode || promotionCode.length > 30) {
            event.preventDefault();
            alert('Mã khuyến mãi không được để trống và không quá 30 ký tự.');
            return;
        }
        
        if (!title || title.length > 255) {
            event.preventDefault();
            alert('Tiêu đề khuyến mãi không được để trống và không quá 255 ký tự.');
            return;
        }
        
        if (!detail || detail.length > 255) {
            event.preventDefault();
            alert('Chi tiết khuyến mãi không được để trống và không quá 255 ký tự.');
            return;
        }
        
        if (!startTime || !endTime) {
            event.preventDefault();
            alert('Thời gian bắt đầu và kết thúc không được để trống.');
            return;
        }
        
        // Validate dates
        const startTimeDate = new Date(startTime);
        const endTimeDate = new Date(endTime);
        
        if (endTimeDate <= startTimeDate) {
            event.preventDefault();
            alert('Thời gian kết thúc phải sau thời gian bắt đầu.');
            return;
        }
        
        // Kiểm tra xem người dùng đã chọn loại discount chưa
        const percentRadio = document.getElementById('percentDiscountRadio');
        const directRadio = document.getElementById('directDiscountRadio');
        
        if (!percentRadio.checked && !directRadio.checked) {
            event.preventDefault();
            alert('Vui lòng chọn một loại giảm giá (% hoặc trực tiếp).');
            return;
        }
        
        // Validate dựa vào loại discount được chọn
        if (percentRadio.checked) {
            // Validate cho discount by percent
            const discountLevel = parseFloat(document.getElementById('discountLevel').value);
            const maxAmount = parseFloat(document.getElementById('maxAmountForPercentDiscount').value);
            const usageLimit = parseInt(document.getElementById('usageLimitPercent').value);
            
            if (isNaN(discountLevel) || discountLevel < 1 || discountLevel > 20) {
                event.preventDefault();
                alert('Mức giảm giá phải từ 1% đến 20%.');
                return;
            }
            
            if (isNaN(maxAmount) || maxAmount < 1000) {
                event.preventDefault();
                alert('Số tiền giảm tối đa phải lớn hơn 1.000đ.');
                return;
            }
            
            if (isNaN(usageLimit) || usageLimit < 1) {
                event.preventDefault();
                alert('Số lần sử dụng tối đa phải ít nhất là 1.');
                return;
            }
            
        } else if (directRadio.checked) {
            // Validate cho direct discount
            const discountAmount = parseFloat(document.getElementById('discountAmount').value);
            const usageLimit = parseInt(document.getElementById('usageLimitDirect').value);
            
            if (isNaN(discountAmount) || discountAmount < 1000 ) {
                event.preventDefault();
                alert('Số tiền giảm trực tiếp phải lớn hơn 1.000đ.');
                return;
            }
            
            if (isNaN(usageLimit) || usageLimit < 1) {
                event.preventDefault();
                alert('Số lần sử dụng tối đa phải ít nhất là 1.');
                return;
            }
        }
        
        // If validation passes, form will submit normally
        console.log("Form validation passed, submitting...");
    });
}

// Khi modal hiển thị, reset tất cả các trường
if (addPromotionModal) { // Sử dụng biến đã khai báo trước đó
    addPromotionModal.addEventListener('show.bs.modal', function() {
        // Reset radio buttons
        const percentRadio = document.getElementById('percentDiscountRadio');
        const directRadio = document.getElementById('directDiscountRadio');
        
        if (percentRadio) percentRadio.checked = false;
        if (directRadio) directRadio.checked = false;
        
        // Hide cả hai container
        const percentContainer = document.getElementById('percentDiscountContainer');
        const directContainer = document.getElementById('directDiscountContainer');
        
        if (percentContainer) percentContainer.style.display = 'none';
        if (directContainer) directContainer.style.display = 'none';
        
        // Reset các trường dữ liệu
        resetDiscountFields('percent');
        resetDiscountFields('direct');
        
        // Reset các trường usageLimit
        const usageLimitPercent = document.getElementById('usageLimitPercent');
        const usageLimitDirect = document.getElementById('usageLimitDirect');
        
        if (usageLimitPercent) usageLimitPercent.value = '';
        if (usageLimitDirect) usageLimitDirect.value = '';
    });
}