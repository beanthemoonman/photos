/**
 * JavaScript for the photo gallery.
 */
document.addEventListener('DOMContentLoaded', function () {
    // Elements
    const photoCarousel = document.getElementById('photo-carousel');
    const loadingMoreElement = document.getElementById('loading-more');
    const modal = document.getElementById('photo-modal');
    const fullSizeImage = document.getElementById('full-size-image');
    const closeButton = document.querySelector('.close-button');
    const themeToggle = document.getElementById('theme-toggle');

    // State
    let currentPage = 0;
    let pageSize = 24;
    let totalPages = 0;
    let isLoading = false;
    let allPhotosLoaded = false;
    let modalOpen = false;

    // Theme functionality
    function initTheme() {
        // Check for saved theme preference or use the system preference
        const savedTheme = localStorage.getItem('theme');

        if (savedTheme) {
            // Apply saved theme
            document.documentElement.setAttribute('data-theme', savedTheme);
            updateThemeIcon(savedTheme);
        } else if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
            // Apply dark theme if user's system preference is dark
            document.documentElement.setAttribute('data-theme', 'dark');
            updateThemeIcon('dark');
        }

        // Listen for system theme changes
        window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
            if (!localStorage.getItem('theme')) {
                const newTheme = e.matches ? 'dark' : 'light';
                document.documentElement.setAttribute('data-theme', newTheme);
                updateThemeIcon(newTheme);
            }
        });
    }

    function toggleTheme() {
        const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
        const newTheme = currentTheme === 'light' ? 'dark' : 'light';

        // Apply the new theme
        document.documentElement.setAttribute('data-theme', newTheme);

        // Save the theme preference
        localStorage.setItem('theme', newTheme);

        // Update the toggle button icon
        updateThemeIcon(newTheme);
    }

    function updateThemeIcon(theme) {
        themeToggle.querySelector('.theme-toggle-icon').textContent = theme === 'dark' ? '☀️' : '🌙';
    }

    // Load photos for the current page
    function loadPhotos(append = false) {
        if (isLoading || allPhotosLoaded) return;
        
        isLoading = true;
        
        // Show loading indicator
        if (!append) {
            photoCarousel.innerHTML = '<div class="loading">Loading photos...</div>';
        } else {
            loadingMoreElement.style.display = 'flex';
        }

        // Fetch photos from the API
        fetch(`/api/photos?page=${currentPage}&size=${pageSize}`)
            .then(response => response.json())
            .then(data => {
                // Update state
                totalPages = data.totalPages;
                
                // Check if we've reached the end
                if (currentPage >= totalPages - 1 || data.photos.length === 0) {
                    allPhotosLoaded = true;
                }

                // Clear the carousel on first load
                if (!append) {
                    photoCarousel.innerHTML = '';
                    
                    // If no photos at all, show a message
                    if (data.photos.length === 0) {
                        photoCarousel.innerHTML = '<div class="no-photos">No photos found</div>';
                        isLoading = false;
                        return;
                    }
                }

                // Add photos to the carousel
                data.photos.forEach(photo => {
                    const photoElement = document.createElement('div');
                    photoElement.className = 'photo';

                    const img = document.createElement('img');
                    img.src = photo.thumbnailUrl;
                    img.alt = photo.filename;
                    img.dataset.fullSizeUrl = photo.fullSizeUrl;

                    // Add click event to show the full-size image
                    img.addEventListener('click', function () {
                        showFullSizeImage(this.dataset.fullSizeUrl);
                    });

                    photoElement.appendChild(img);
                    photoCarousel.appendChild(photoElement);
                });
                
                // Hide loading indicator
                loadingMoreElement.style.display = 'none';
                isLoading = false;
            })
            .catch(error => {
                console.error('Error loading photos:', error);
                if (!append) {
                    photoCarousel.innerHTML = '<div class="error">Error loading photos</div>';
                } else {
                    loadingMoreElement.style.display = 'none';
                }
                isLoading = false;
            });
    }

    // Load more photos when scrolling near bottom
    function loadMorePhotos() {
        if (!isLoading && !allPhotosLoaded) {
            currentPage++;
            loadPhotos(true);
        }
    }
    
    // Check if user has scrolled near the bottom
    function checkScrollPosition() {
        const scrollPosition = window.innerHeight + window.scrollY;
        const documentHeight = document.documentElement.offsetHeight;
        const threshold = 200; // Load more when 200px from bottom
        
        if (scrollPosition >= documentHeight - threshold) {
            loadMorePhotos();
        }
    }

    // Show full-size image in modal
    function showFullSizeImage(url) {
        fullSizeImage.src = url;
        modal.style.display = 'block';
        modalOpen = true;
        
        // Add a history entry for the modal
        history.pushState({ modalOpen: true, imageUrl: url }, '', '');
    }
    
    // Close the modal
    function closeModal() {
        modal.style.display = 'none';
        modalOpen = false;
        
        // If we're closing from a history state, don't add another history entry
        if (history.state && history.state.modalOpen) {
            history.back();
        }
    }

    // Event listener for infinite scroll
    window.addEventListener('scroll', checkScrollPosition);
    
    // Also check on resize in case content height changes
    window.addEventListener('resize', checkScrollPosition);

    // Handle browser back/forward buttons
    window.addEventListener('popstate', function(event) {
        if (modalOpen && (!event.state || !event.state.modalOpen)) {
            // Back button pressed while modal is open - close the modal
            modal.style.display = 'none';
            modalOpen = false;
        } else if (!modalOpen && event.state && event.state.modalOpen) {
            // Forward button pressed to reopen modal
            fullSizeImage.src = event.state.imageUrl;
            modal.style.display = 'block';
            modalOpen = true;
        }
    });

    // Event listeners for modal
    closeButton.addEventListener('click', function () {
        closeModal();
    });

    window.addEventListener('click', function (event) {
        if (event.target === modal) {
            closeModal();
        }
    });
    
    // Handle escape key to close modal
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape' && modalOpen) {
            closeModal();
        }
    });

    // Event listener for theme toggle
    themeToggle.addEventListener('click', toggleTheme);

    // Initialize theme
    initTheme();

    // Initial load
    loadPhotos();
});
