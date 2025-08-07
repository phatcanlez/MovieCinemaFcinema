// Hero Video Control Functions
let isMuted = true;
let currentVideoIndex = 0;

// List of cinema-related YouTube videos for playlist
const cinemaVideos = [
    {
        id: 'iGg9PpNkRDk',
        title: 'FCine - Rạp chiếu phim hiện đại',
        description: 'Trải nghiệm điện ảnh đỉnh cao'
    },
    {
        id: 'dQw4w9WgXcQ', 
        title: 'Cinema Experience',
        description: 'Không gian giải trí hoàn hảo'
    },
    {
        id: 'ScMzIvxBSi4',
        title: 'Movie Theater Ambiance',
        description: 'Âm thanh rạp chiếu phim chuyên nghiệp'
    }
];

// Toggle video sound
function toggleVideoSound() {
    const iframe = document.getElementById('heroVideo');
    const volumeIcon = document.getElementById('volumeIcon');
    
    if (isMuted) {
        // Unmute video
        iframe.src = iframe.src.replace('mute=1', 'mute=0');
        volumeIcon.className = 'bi bi-volume-up';
        isMuted = false;
        
        // Show notification
        showVideoNotification('Đã bật âm thanh', 'success');
    } else {
        // Mute video
        iframe.src = iframe.src.replace('mute=0', 'mute=1');
        volumeIcon.className = 'bi bi-volume-mute';
        isMuted = true;
        
        // Show notification
        showVideoNotification('Đã tắt âm thanh', 'info');
    }
}

// Show video control notification
function showVideoNotification(message, type) {
    const notification = document.createElement('div');
    notification.className = `video-notification ${type}`;
    notification.innerHTML = `
        <i class="bi bi-${type === 'success' ? 'volume-up' : 'volume-mute'}"></i>
        <span>${message}</span>
    `;
    
    document.body.appendChild(notification);
    
    // Animate in
    setTimeout(() => {
        notification.classList.add('show');
    }, 100);
    
    // Remove after 2 seconds
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, 2000);
}

// Change video in playlist
function changeVideo(direction) {
    if (direction === 'next') {
        currentVideoIndex = (currentVideoIndex + 1) % cinemaVideos.length;
    } else {
        currentVideoIndex = (currentVideoIndex - 1 + cinemaVideos.length) % cinemaVideos.length;
    }
    
    const currentVideo = cinemaVideos[currentVideoIndex];
    const iframe = document.getElementById('heroVideo');
    const videoTitle = document.querySelector('.video-title');
    
    // Create playlist string for continuous play
    const playlistIds = cinemaVideos.map(video => video.id).join(',');
    
    // Update iframe src with new video
    iframe.src = `https://www.youtube.com/embed/${currentVideo.id}?autoplay=1&mute=${isMuted ? 1 : 0}&rel=0&loop=1&playlist=${playlistIds}`;
    
    // Update video info
    videoTitle.textContent = currentVideo.title;
    
    // Show change notification
    showVideoNotification(`Đang phát: ${currentVideo.title}`, 'success');
}

// Initialize video controls
document.addEventListener('DOMContentLoaded', function() {
    // Add video navigation controls
    const videoControls = document.querySelector('.video-controls-overlay');
    
    // Create navigation buttons
    const navButtons = document.createElement('div');
    navButtons.className = 'video-nav-controls';
    navButtons.innerHTML = `
        <button class="video-nav-btn" onclick="changeVideo('prev')" title="Video trước">
            <i class="bi bi-skip-backward"></i>
        </button>
        <button class="video-nav-btn" onclick="changeVideo('next')" title="Video tiếp theo">
            <i class="bi bi-skip-forward"></i>
        </button>
    `;
    
    // Insert navigation before video toggle button
    videoControls.insertBefore(navButtons, videoControls.firstChild);
    
    // Auto-rotate videos every 30 seconds if user doesn't interact
    let autoRotateTimer = setInterval(() => {
        changeVideo('next');
    }, 30000);
    
    // Clear auto-rotate when user interacts
    videoControls.addEventListener('click', () => {
        clearInterval(autoRotateTimer);
        
        // Restart auto-rotate after 60 seconds of inactivity
        setTimeout(() => {
            autoRotateTimer = setInterval(() => {
                changeVideo('next');
            }, 30000);
        }, 60000);
    });
    
    // Handle video loading states
    const iframe = document.getElementById('heroVideo');
    iframe.addEventListener('load', function() {
        // Video loaded successfully
        console.log('Hero video loaded');
    });
    
    // Add keyboard controls
    document.addEventListener('keydown', function(e) {
        if (e.target.tagName.toLowerCase() === 'input') return;
        
        switch(e.key) {
            case 'm':
            case 'M':
                toggleVideoSound();
                break;
            case 'ArrowLeft':
                changeVideo('prev');
                e.preventDefault();
                break;
            case 'ArrowRight':
                changeVideo('next');
                e.preventDefault();
                break;
        }
    });
});

// Add video notification styles
const videoNotificationStyles = `
<style>
.video-notification {
    position: fixed;
    top: 100px;
    right: 30px;
    background: rgba(0, 0, 0, 0.9);
    color: white;
    padding: 15px 20px;
    border-radius: 25px;
    display: flex;
    align-items: center;
    gap: 10px;
    z-index: 9999;
    transform: translateX(350px);
    transition: transform 0.3s ease;
    border: 1px solid rgba(220, 38, 38, 0.5);
    backdrop-filter: blur(10px);
}

.video-notification.show {
    transform: translateX(0);
}

.video-notification.success {
    border-color: rgba(34, 197, 94, 0.5);
}

.video-notification.info {
    border-color: rgba(59, 130, 246, 0.5);
}

.video-nav-controls {
    display: flex;
    gap: 8px;
    margin-right: 10px;
}

.video-nav-btn {
    background: linear-gradient(45deg, #374151, #1f2937);
    border: 1px solid rgba(220, 38, 38, 0.3);
    border-radius: 50%;
    width: 35px;
    height: 35px;
    color: white;
    font-size: 0.9em;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
}

.video-nav-btn:hover {
    background: linear-gradient(45deg, #dc2626, #b91c1c);
    border-color: rgba(220, 38, 38, 0.6);
    transform: scale(1.05);
}

@media (max-width: 768px) {
    .video-notification {
        right: 20px;
        padding: 12px 16px;
        font-size: 0.9em;
    }
    
    .video-nav-btn {
        width: 30px;
        height: 30px;
        font-size: 0.8em;
    }
}
</style>
`;

// Inject styles
document.head.insertAdjacentHTML('beforeend', videoNotificationStyles);
