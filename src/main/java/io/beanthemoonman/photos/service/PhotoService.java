package io.beanthemoonman.photos.service;

import io.beanthemoonman.photos.config.PhotosConfig;
import io.beanthemoonman.photos.model.Photo;
import io.beanthemoonman.photos.model.PhotoPage;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for handling photo operations.
 */
@Service
public class PhotoService {

  private static final Logger logger = LoggerFactory.getLogger(PhotoService.class);

  private static final String[] SUPPORTED_EXTENSIONS = { ".jpg", ".jpeg", ".png", ".gif" };

  private final PhotosConfig config;

  @Autowired
  public PhotoService(PhotosConfig config) {
    this.config = config;

    createDirectoryIfNotExists(config.getDirectoryPath());
    createDirectoryIfNotExists(Paths.get("cache"));
  }

  private void createDirectoryIfNotExists(Path directoryPath) {
    // Create photos directory if it doesn't exist
    try {
      if (!Files.exists(directoryPath)) {
        Files.createDirectories(directoryPath);
        logger.info("Created photos directory: {}", directoryPath);
      }
    } catch (IOException e) {
      logger.error("Failed to create photos directory", e);
    }
  }

  /**
   * Get a paginated list of photos.
   *
   * @param page Page number (0-based)
   * @param size Number of photos per page
   * @return A page of photos
   */
  public PhotoPage getPhotos(int page, int size) {
    try {
      List<Path> allPhotoPaths = listPhotoFiles();
      int totalElements = allPhotoPaths.size();
      int totalPages = (int) Math.ceil((double) totalElements / size);

      // Adjust page if out of bounds
      if (page < 0) {
        page = 0;
      } else if (page >= totalPages && totalPages > 0) {
        page = totalPages - 1;
      }

      // Calculate start and end indices for the current page
      int start = page * size;
      int end = Math.min(start + size, totalElements);

      // Get photos for the current page
      List<Photo> photos = new ArrayList<>();
      if (start < totalElements) {
        for (int i = start; i < end; i++) {
          Path photoPath = allPhotoPaths.get(i);
          String filename = photoPath.getFileName().toString();

          // Create URLs for thumbnail and full-size image
          String thumbnailUrl = "/api/photos/" + filename + "/thumbnail";
          String fullSizeUrl = "/api/photos/" + filename + "/full";

          photos.add(new Photo(filename, filename, thumbnailUrl, fullSizeUrl));
        }
      }

      return new PhotoPage(photos, page, size, totalPages, totalElements);
    } catch (IOException e) {
      logger.error("Error listing photos", e);
      return new PhotoPage(new ArrayList<>(), page, size, 0, 0);
    }
  }

  /**
   * Get a photo by its ID (filename).
   *
   * @param id The photo ID (filename)
   * @return The photo, or null if not found
   */
  public Photo getPhoto(String id) {
    try {
      Path photoPath = findPhotoById(id);
      if (photoPath != null) {
        String filename = photoPath.getFileName().toString();
        String thumbnailUrl = "/api/photos/" + id + "/thumbnail";
        String fullSizeUrl = "/api/photos/" + id + "/full";

        return new Photo(id, filename, thumbnailUrl, fullSizeUrl);
      }
    } catch (IOException e) {
      logger.error("Error finding photo with id: {}", id, e);
    }

    return null;
  }

  /**
   * Get the full-size image data for a photo.
   *
   * @param id The photo ID (filename)
   * @return The image data as bytes, or null if not found
   */
  public byte[] getFullSizeImage(String id) {
    try {
      Path photoPath = findPhotoById(id);
      if (photoPath != null) {
        return Files.readAllBytes(photoPath);
      }
    } catch (IOException e) {
      logger.error("Error reading full-size image with id: {}", id, e);
    }

    return null;
  }

  /**
   * Get the thumbnail image data for a photo.
   *
   * @param id The photo ID (filename)
   * @return The thumbnail image data as bytes, or null if not found
   */
  public byte[] getThumbnailImage(String id) {
    try {
      Path photoPath = findPhotoById(id);
      if (photoPath != null) {
        Path cachePath = Paths.get("cache/" + id);
        if (Files.exists(cachePath) && Files.isRegularFile(cachePath)) {
          return Files.readAllBytes(cachePath);
        } else {
          byte[] imageData = createThumbnail(photoPath);
          Files.write(cachePath, imageData);
          return imageData;
        }
      }
    } catch (IOException e) {
      logger.error("Error creating thumbnail for image with id: {}", id, e);
    }
    return null;
  }

  /**
   * List all photo files in the configured directory.
   *
   * @return A list of paths to photo files
   * @throws IOException If an I/O error occurs
   */
  private List<Path> listPhotoFiles() throws IOException {
    Path directoryPath = config.getDirectoryPath();
    if (!Files.exists(directoryPath)) {
      return new ArrayList<>();
    }

    try (Stream<Path> paths = Files.list(directoryPath)) {
      return paths.filter(Files::isRegularFile)
          .filter(this::isImageFile)
          .collect(Collectors.toList());
    }
  }

  /**
   * Find a photo by its ID (filename).
   *
   * @param id The photo ID (filename)
   * @return The path to the photo, or null if not found
   * @throws IOException If an I/O error occurs
   */
  private Path findPhotoById(String id) throws IOException {
    Path directoryPath = config.getDirectoryPath();
    Path photoPath = directoryPath.resolve(id);

    if (Files.exists(photoPath) && Files.isRegularFile(photoPath) && isImageFile(photoPath)) {
      return photoPath;
    }

    // If the exact filename wasn't found, try to find a file with the same name but different extension
    try (Stream<Path> paths = Files.list(directoryPath)) {
      return paths.filter(Files::isRegularFile)
          .filter(this::isImageFile)
          .filter(path -> {
            String filename = path.getFileName().toString();
            String nameWithoutExt = getNameWithoutExtension(filename);
            return nameWithoutExt.equals(getNameWithoutExtension(id));
          })
          .findFirst()
          .orElse(null);
    }
  }

  /**
   * Check if a file is an image based on its extension.
   *
   * @param path The file path
   * @return true if the file is an image, false otherwise
   */
  private boolean isImageFile(Path path) {
    String filename = path.getFileName().toString().toLowerCase();
    for (String ext : SUPPORTED_EXTENSIONS) {
      if (filename.endsWith(ext)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Get the filename without its extension.
   *
   * @param filename The filename
   * @return The filename without extension
   */
  private String getNameWithoutExtension(String filename) {
    int lastDotIndex = filename.lastIndexOf('.');
    if (lastDotIndex > 0) {
      return filename.substring(0, lastDotIndex);
    }
    return filename;
  }

  /**
   * Create a thumbnail for an image.
   *
   * @param imagePath The path to the original image
   * @return The thumbnail image data as bytes
   * @throws IOException If an I/O error occurs
   */
  private byte[] createThumbnail(Path imagePath) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    Thumbnails.of(imagePath.toFile())
        .size(config.getThumbnail().getWidth(), config.getThumbnail().getHeight())
        .keepAspectRatio(true)
        .outputFormat("jpg")
        .toOutputStream(outputStream);

    return outputStream.toByteArray();
  }
}