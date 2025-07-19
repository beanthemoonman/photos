package io.beanthemoonman.photos.service;

import io.beanthemoonman.photos.config.PhotosConfig;
import io.beanthemoonman.photos.model.Photo;
import io.beanthemoonman.photos.model.PhotoPage;
import io.beanthemoonman.photos.utility.FileFilter;
import io.beanthemoonman.photos.utility.ThumbnailHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.beanthemoonman.photos.utility.FileFilter.getNameWithoutExtension;
import static io.beanthemoonman.photos.utility.Utility.bytesToSha256;
import static io.beanthemoonman.photos.utility.Utility.createDirectoryIfNotExists;

/**
 * Service for handling photo operations.
 */
@Service
public class PhotoService {

  private static final Logger logger = LoggerFactory.getLogger(PhotoService.class);

  private static final String[] SUPPORTED_EXTENSIONS = { ".jpg", ".jpeg", ".png", ".gif" };

  private final PhotosConfig config;

  private final ThumbnailService thumbnailService;

  private final ThumbnailHasher thumbnailHasher;

  @Autowired
  public PhotoService(PhotosConfig config, ThumbnailService thumbnailService, ThumbnailHasher thumbnailHasher) {
    this.config = config;
    this.thumbnailService = thumbnailService;
    this.thumbnailHasher = thumbnailHasher;

    createDirectoryIfNotExists(config.getDirectoryPath());
    createDirectoryIfNotExists(thumbnailHasher.getCacheDir());
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
   * Retrieves the thumbnail image associated with the given image ID. If a cached thumbnail exists, it is returned;
   * otherwise, a new thumbnail is generated, cached, and returned.
   *
   * @param id the unique identifier for the image
   * @return a byte array containing the thumbnail image, or null if an error occurs
   */
  public byte[] getThumbnailImage(String id) {
    try {
      var shaCache = thumbnailHasher.getShaCache();
      Path photoPath = findPhotoById(id);
      if (photoPath != null) {
        String photoHash;
        if (shaCache.containsKey(id)) {
          photoHash = shaCache.get(id);
        } else {
          photoHash = bytesToSha256(Files.readAllBytes(photoPath));
          shaCache.put(id, photoHash);
        }
        Path cachePath = thumbnailHasher.getCacheDir().resolve(photoHash + ".jpg");
        if (Files.exists(cachePath) && Files.isRegularFile(cachePath)) {
          return Files.readAllBytes(cachePath);
        } else {
          byte[] imageData = thumbnailService.createThumbnail(photoPath);
          Files.write(cachePath, imageData);
          return imageData;
        }
      }
    } catch (IOException | NoSuchAlgorithmException e) {
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
          .filter(FileFilter::isImageFile).sorted((path1, path2) -> {
            try {
              // Get the last modified time attribute for each file
              // Use "lastModifiedTime" or "creationTime" based on your needs
              long time1 = Files.getLastModifiedTime(path1).toMillis();
              long time2 = Files.getLastModifiedTime(path2).toMillis();
              // Sort in descending order (newest first)
              return Long.compare(time2, time1);
            } catch (IOException e) {
              logger.error("Error comparing file timestamps", e);
              return 0;
            }
          })
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

    if (Files.exists(photoPath) && Files.isRegularFile(photoPath) && FileFilter.isImageFile(photoPath)) {
      return photoPath;
    }

    // If the exact filename wasn't found, try to find a file with the same name but different extension
    try (Stream<Path> paths = Files.list(directoryPath)) {
      return paths.filter(Files::isRegularFile)
          .filter(FileFilter::isImageFile)
          .filter(path -> {
            String filename = path.getFileName().toString();
            String nameWithoutExt = getNameWithoutExtension(filename);
            return nameWithoutExt.equals(getNameWithoutExtension(id));
          })
          .findFirst()
          .orElse(null);
    }
  }

}