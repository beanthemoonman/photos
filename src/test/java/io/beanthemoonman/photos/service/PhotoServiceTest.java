package io.beanthemoonman.photos.service;

import io.beanthemoonman.photos.config.PhotosConfig;
import io.beanthemoonman.photos.model.Photo;
import io.beanthemoonman.photos.model.PhotoPage;
import io.beanthemoonman.photos.utility.ThumbnailHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PhotoServiceTest {

  @TempDir
  Path tempDir;

  @Mock
  private PhotosConfig photosConfig;

  @Mock
  private PhotosConfig.Thumbnail thumbnail;

  @Mock
  private ThumbnailService thumbnailService;

  @Mock
  private ThumbnailHasher thumbnailHasher;

  private PhotoService photoService;

  @BeforeEach
  void setUp() throws IOException {
    MockitoAnnotations.openMocks(this);

    // Configure mocks
    when(photosConfig.getDirectory()).thenReturn(tempDir.toString());
    when(photosConfig.getDirectoryPath()).thenReturn(tempDir);
    when(photosConfig.getThumbnail()).thenReturn(thumbnail);
    when(thumbnail.getWidth()).thenReturn(400);
    when(thumbnail.getHeight()).thenReturn(400);
    when(thumbnailHasher.getCacheDir()).thenReturn(tempDir.resolve("cache"));
    
    // Mock the SHA cache
    ConcurrentMap<String, String> mockShaCache = new ConcurrentHashMap<>();
    when(thumbnailHasher.getShaCache()).thenReturn(mockShaCache);
    
    // Mock thumbnail creation to return dummy data for any path
    when(thumbnailService.createThumbnail(any(Path.class)))
        .thenReturn("mock thumbnail data".getBytes());

    // Create a test image file
    Path testImagePath = tempDir.resolve("test.jpg");
    Files.write(testImagePath, "test image data".getBytes());

    // Initialize service with mocked dependencies
    photoService = new PhotoService(photosConfig, thumbnailService, thumbnailHasher);
  }

  @Test
  void testGetPhotos() {
    // Test getting photos with pagination
    PhotoPage page = photoService.getPhotos(0, 10);

    // Verify results
    assertNotNull(page);
    assertEquals(0, page.getPage());
    assertEquals(10, page.getSize());
    assertEquals(1, page.getTotalElements());
    assertEquals(1, page.getTotalPages());
    assertEquals(1, page.getPhotos().size());
    assertEquals("test.jpg", page.getPhotos().getFirst().getId());
  }

  @Test
  void testGetPhoto() {
    // Test getting a specific photo
    Photo photo = photoService.getPhoto("test.jpg");

    // Verify results
    assertNotNull(photo);
    assertEquals("test.jpg", photo.getId());
    assertEquals("test.jpg", photo.getFilename());
    assertEquals("/api/photos/test.jpg/thumbnail", photo.getThumbnailUrl());
    assertEquals("/api/photos/test.jpg/full", photo.getFullSizeUrl());
  }

  @Test
  void testGetPhotoNotFound() {
    // Test getting a non-existent photo
    Photo photo = photoService.getPhoto("nonexistent.jpg");

    // Verify results
    assertNull(photo);
  }

  @Test
  void testGetFullSizeImage() {
    // Test getting the full-size image
    byte[] imageData = photoService.getFullSizeImage("test.jpg");

    // Verify results
    assertNotNull(imageData);
    assertArrayEquals("test image data".getBytes(), imageData);
  }

  @Test
  void testGetFullSizeImageNotFound() {
    // Test getting a non-existent full-size image
    byte[] imageData = photoService.getFullSizeImage("nonexistent.jpg");

    // Verify results
    assertNull(imageData);
  }

  @Test
  void testGetThumbnailImage() {
    // This test is more complex because it involves image processing
    // For simplicity, we'll just verify that the method doesn't throw an exception
    // and returns some data
    byte[] thumbnailData = photoService.getThumbnailImage("test.jpg");

    // Verify that thumbnail data is returned
    assertNotNull(thumbnailData);
    assertArrayEquals("mock thumbnail data".getBytes(), thumbnailData);
  }
}