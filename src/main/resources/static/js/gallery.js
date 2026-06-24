/**
 * JavaScript for the photo gallery.
 */
document.addEventListener('DOMContentLoaded', function () {
    // Elements
    const photoCarousel = document.getElementById('photo-carousel');
    const loadingMoreElement = document.getElementById('loading-more');
    const modal = document.getElementById('photo-modal');
    const fullSizeImage = document.getElementById('full-size-image');
    const modalDate = document.getElementById('modal-date');
    const modalExif = document.getElementById('modal-exif');
    const modalFullSize = document.getElementById('modal-fullsize');
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
        // Pill shows the theme you can switch TO.
        const dark = theme === 'dark';
        themeToggle.querySelector('.theme-toggle-icon').textContent = dark ? '☀' : '☾';
        themeToggle.querySelector('.theme-toggle-label').textContent = dark ? 'Light' : 'Dark';
    }

    // Grid masonry: a tile spans ceil(height / rowSlot) of the 8px auto-rows.
    // Must match grid-auto-rows (8px) and gap (responsive: 18 / 10) in styles.css.
    function rowSlot() {
        const styles = getComputedStyle(photoCarousel);
        const row = parseFloat(styles.gridAutoRows) || 8;
        const gap = parseFloat(styles.rowGap) || 0;
        return { row, gap };
    }

    function setSpan(figure) {
        const { row, gap } = rowSlot();
        const h = figure.getBoundingClientRect().height;
        if (!h) return; // not laid out yet (e.g. unknown aspect ratio) — onload retries
        figure.style.gridRowEnd = `span ${Math.ceil((h + gap) / (row + gap))}`;
    }

    // Recompute every tile's span (column count / width changed on resize).
    let resizeTimer;
    function recomputeSpans() {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(() => {
            photoCarousel.querySelectorAll('.photo').forEach(setSpan);
        }, 120);
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
                    const figure = document.createElement('figure');
                    figure.className = 'photo';

                    const img = document.createElement('img');
                    img.src = photo.thumbnailUrl;
                    img.alt = photo.title || photo.filename;
                    img.loading = 'lazy';
                    // Reserve native proportions so masonry doesn't reflow once images load.
                    if (photo.width > 0 && photo.height > 0) {
                        img.style.aspectRatio = `${photo.width} / ${photo.height}`;
                        // Very wide panoramas become unreadable slivers in one narrow column,
                        // so let them span the full gallery width instead.
                        if (photo.width / photo.height > 2.4) {
                            figure.classList.add('panorama');
                        }
                    }

                    const caption = document.createElement('figcaption');
                    caption.textContent = photo.title || photo.filename;

                    figure.appendChild(img);
                    figure.appendChild(caption);
                    figure.addEventListener('click', function () {
                        showFullSizeImage(photo);
                    });

                    photoCarousel.appendChild(figure);
                    // Size the grid span from the (aspect-ratio-reserved) layout height.
                    setSpan(figure);
                    // Fallback for photos without known dimensions: span once loaded.
                    if (!(photo.width > 0 && photo.height > 0)) {
                        img.addEventListener('load', () => setSpan(figure), { once: true });
                    }
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

    // Populate and show the split modal for a photo
    function openModal(photo) {
        fullSizeImage.src = photo.fullSizeUrl;
        fullSizeImage.alt = photo.title || photo.filename;
        modalFullSize.href = photo.fullSizeUrl;
        modalDate.textContent = photo.dateTaken || '';

        // Rebuild the EXIF table from whatever rows the backend supplied.
        modalExif.innerHTML = '';
        (photo.exif || []).forEach(row => {
            const dt = document.createElement('dt');
            dt.textContent = row.label;
            const dd = document.createElement('dd');
            dd.textContent = row.value;
            modalExif.appendChild(dt);
            modalExif.appendChild(dd);
        });

        modal.classList.add('open');
        modalOpen = true;
    }

    // Show full-size image in modal (from a tile click) and push a history entry
    function showFullSizeImage(photo) {
        openModal(photo);
        history.pushState({ modalOpen: true, photo: photo }, '', '');
    }

    // Close the modal
    function closeModal() {
        modal.classList.remove('open');
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

    // Column width changes on resize → recompute every tile's row span.
    window.addEventListener('resize', recomputeSpans);

    // Handle browser back/forward buttons
    window.addEventListener('popstate', function(event) {
        if (modalOpen && (!event.state || !event.state.modalOpen)) {
            // Back button pressed while modal is open - close the modal
            modal.classList.remove('open');
            modalOpen = false;
        } else if (!modalOpen && event.state && event.state.modalOpen) {
            // Forward button pressed to reopen modal
            openModal(event.state.photo);
        }
    });

    // Event listeners for modal
    closeButton.addEventListener('click', function () {
        closeModal();
    });

    // Click on the backdrop (anywhere outside the image and info panel) closes the modal.
    // The close button has its own handler, so exclude it here to avoid double-closing.
    modal.addEventListener('click', function (event) {
        if (!event.target.closest('.modal-content, .modal-info, .close-button')) {
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
