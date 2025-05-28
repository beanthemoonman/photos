package io.beanthemoonman.photos.controller;

import io.beanthemoonman.photos.model.Photo;
import io.beanthemoonman.photos.model.PhotoPage;
import io.beanthemoonman.photos.service.PhotoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for photo operations.
 */
@RestController
@RequestMapping("/api/photos")
public class PhotoController {

  private static final Logger logger = LoggerFactory.getLogger(PhotoController.class);

  private final PhotoService photoService;

  @Autowired
  public PhotoController(PhotoService photoService) {
    this.photoService = photoService;
  }

  /**
   * Get a paginated list of photos.
   *
   * @param page Page number (0-based)
   * @param size Number of photos per page
   * @return A page of photos
   */
  @GetMapping
  public ResponseEntity<PhotoPage> getPhotos(@RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    logger.info("Getting photos page {} with size {}", page, size);
    PhotoPage photoPage = photoService.getPhotos(page, size);
    return ResponseEntity.ok(photoPage);
  }

  /**
   * Get a photo by its ID.
   *
   * @param id The photo ID
   * @return The photo
   */
  @GetMapping("/{id}")
  public ResponseEntity<Photo> getPhoto(@PathVariable String id) {
    logger.info("Getting photo with id: {}", id);
    Photo photo = photoService.getPhoto(id);

    if (photo != null) {
      return ResponseEntity.ok(photo);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Get a thumbnail image for a photo.
   *
   * @param id The photo ID
   * @return The thumbnail image
   */
  @GetMapping(value = "/{id}/thumbnail", produces = MediaType.IMAGE_JPEG_VALUE)
  public ResponseEntity<byte[]> getThumbnail(@PathVariable String id) {
    logger.info("Getting thumbnail for photo with id: {}", id);
    byte[] imageData = photoService.getThumbnailImage(id);

    if (imageData != null) {
      return ResponseEntity.ok(imageData);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Get a full-size image for a photo.
   *
   * @param id The photo ID
   * @return The full-size image
   */
  @GetMapping(value = "/{id}/full", produces = MediaType.IMAGE_JPEG_VALUE)
  public ResponseEntity<byte[]> getFullSizeImage(@PathVariable String id) {
    logger.info("Getting full-size image for photo with id: {}", id);
    byte[] imageData = photoService.getFullSizeImage(id);

    if (imageData != null) {
      return ResponseEntity.ok(imageData);
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}