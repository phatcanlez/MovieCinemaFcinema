// Combo API Integration Script

// Load combos from API
async function loadCombos() {
  const loadingElement = document.getElementById('combos-loading');
  const carouselElement = document.getElementById('combos-carousel');
  const errorElement = document.getElementById('combos-error');

  try {
    console.log('Fetching combos from API...');
    const response = await fetch('/api/booking/combos');

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    console.log('Combos data received:', data);

    // Hide loading, show carousel
    loadingElement.style.display = 'none';
    carouselElement.style.display = 'block';
    errorElement.style.display = 'none';

    // Render combos
    renderCombos(data.combos || []);

  } catch (error) {
    console.error('Error loading combos:', error);

    // Show error state
    loadingElement.style.display = 'none';
    carouselElement.style.display = 'none';
    errorElement.style.display = 'block';
  }
}

// Render combo cards in carousel
function renderCombos(combos) {
  const container = document.getElementById('combos-container');

  if (!combos || combos.length === 0) {
    container.innerHTML = `
      <div class="carousel-item active">
        <div class="container">
          <div class="row justify-content-center">
            <div class="col-12">
              <div class="alert alert-info text-center">
                <i class="bi bi-info-circle me-2"></i>
                Hiện tại chưa có combo khuyến mãi nào.
              </div>
            </div>
          </div>
        </div>
      </div>
    `;
    return;
  }

  // Chia combos thành các nhóm 3 cards mỗi slide
  const comboChunks = [];
  for (let i = 0; i < combos.length; i += 3) {
    comboChunks.push(combos.slice(i, i + 3));
  }

  container.innerHTML = comboChunks.map((chunk, slideIndex) => {
    const isActive = slideIndex === 0 ? 'active' : '';

    return `
      <div class="carousel-item ${isActive}">
        <div class="container">
          <div class="row g-4 justify-content-center">
            ${chunk.map((combo, index) => {
              const badgeText = getBadgeText(combo);
              const badgeClass = getBadgeClass(combo);
              const comboItems = getComboItems(combo.comboName);
              const animationDelay = (index + 1) * 0.1;

              return `
                <div class="col-lg-4 col-md-6">
                  <div class="combo-card ${combo.comboStatus === 'HOT' ? 'combo-popular' : ''} fade-in-up" style="animation-delay: ${animationDelay}">
                    ${badgeText ? `
                      <div class="combo-badge ${badgeClass}">
                        <span class="badge-text">${badgeText}</span>
                      </div>
                    ` : ''}
                    <div class="combo-image">
                      <img src="${combo.imageUrl}" alt="${combo.comboName}" class="img-fluid" onerror="this.src='https://via.placeholder.com/400x300?text=No+Image'" />
                      <div class="combo-overlay">
                        <div class="combo-items">
                          ${comboItems.map(item => `
                            <div class="item">
                              <i class="${item.icon}"></i>
                              <span>${item.text}</span>
                            </div>
                          `).join('')}
                        </div>
                      </div>
                    </div>
                    <div class="combo-content">
                      <h4 class="combo-title text-warning">${combo.comboName}</h4>
                      <p class="combo-description">${combo.description}</p>
                      
                    </div>
                  </div>
                </div>
              `;
            }).join('')}
          </div>
        </div>
      </div>
    `;
  }).join('');
}

// Get badge text based on combo data
function getBadgeText(combo) {
  if (combo.discountPercentage > 0) {
    return `Tiết kiệm ${combo.discountPercentage}%`;
  }
  if (combo.comboStatus === 'HOT') {
    return 'Phổ biến nhất';
  }
  return '';
}

// Get badge CSS class
function getBadgeClass(combo) {
  if (combo.discountPercentage > 0) {
    return 'popular';
  }
  return '';
}

// Get combo items based on combo name
function getComboItems(comboName) {
  const name = comboName.toLowerCase();

  if (name.includes('solo')) {
    return [
      { icon: 'bi bi-cup-straw', text: '1 Bắp rang' },
      { icon: 'bi bi-cup', text: '1 Nước ngọt' }
    ];
  } else if (name.includes('couple')) {
    return [
      { icon: 'bi bi-cup-straw', text: '1 Bắp rang lớn' },
      { icon: 'bi bi-cup', text: '2 Nước ngọt' }
    ];
  } else if (name.includes('family')) {
    return [
      { icon: 'bi bi-cup-straw', text: '2 Bắp rang' },
      { icon: 'bi bi-cup', text: '2 Nước ngọt' }
    ];
  }

  // Default items
  return [
    { icon: 'bi bi-cup-straw', text: 'Bắp rang' },
    { icon: 'bi bi-cup', text: 'Nước ngọt' }
  ];
}

// Load combos when page is ready
document.addEventListener('DOMContentLoaded', function () {
  loadCombos();
});
