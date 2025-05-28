/**
 * JavaScript for the photo gallery.
 */
document.addEventListener('DOMContentLoaded', function () {
    // Elements
    const photoCarousel = document.getElementById('photo-carousel');
    const prevPageButton = document.getElementById('prev-page');
    const nextPageButton = document.getElementById('next-page');
    const pageInfo = document.getElementById('page-info');
    const modal = document.getElementById('photo-modal');
    const fullSizeImage = document.getElementById('full-size-image');
    const closeButton = document.querySelector('.close-button');
    const themeToggle = document.getElementById('theme-toggle');

    // State
    let currentPage = 0;
    let pageSize = 12;
    let totalPages = 0;

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
    function loadPhotos() {
        // Show loading indicator
        photoCarousel.innerHTML = '<div class="loading">Loading photos...</div>';

        // Fetch photos from the API
        fetch(`/api/photos?page=${currentPage}&size=${pageSize}`)
            .then(response => response.json())
            .then(data => {
                // Update state
                currentPage = data.page;
                totalPages = data.totalPages;

                // Update pagination controls
                updatePaginationControls();

                // Clear the carousel
                photoCarousel.innerHTML = '';

                // If no photos, show a message
                if (data.photos.length === 0) {
                    photoCarousel.innerHTML = '<div class="no-photos">No photos found</div>';
                    return;
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
            })
            .catch(error => {
                console.error('Error loading photos:', error);
                photoCarousel.innerHTML = '<div class="error">Error loading photos</div>';
            });
    }

    // Update pagination controls
    function updatePaginationControls() {
        pageInfo.textContent = `Page ${currentPage + 1} of ${totalPages || 1}`;

        // Enable/disable previous button
        prevPageButton.disabled = currentPage <= 0;

        // Enable/disable next button
        nextPageButton.disabled = currentPage >= totalPages - 1 || totalPages === 0;
    }

    // Show full-size image in modal
    function showFullSizeImage(url) {
        fullSizeImage.src = url;
        modal.style.display = 'block';
    }

    // Event listeners for pagination
    prevPageButton.addEventListener('click', function () {
        if (currentPage > 0) {
            currentPage--;
            loadPhotos();
        }
    });

    nextPageButton.addEventListener('click', function () {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadPhotos();
        }
    });

    // Event listeners for modal
    closeButton.addEventListener('click', function () {
        modal.style.display = 'none';
    });

    window.addEventListener('click', function (event) {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    });

    // Event listener for theme toggle
    themeToggle.addEventListener('click', toggleTheme);

    // Initialize theme
    initTheme();

    // Initial load
    loadPhotos();
});
