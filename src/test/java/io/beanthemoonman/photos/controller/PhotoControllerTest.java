package io.beanthemoonman.photos.controller;

import io.beanthemoonman.photos.model.Photo;
import io.beanthemoonman.photos.model.PhotoPage;
import io.beanthemoonman.photos.service.PhotoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PhotoControllerTest {

  private MockMvc mockMvc;

  @Mock
  private PhotoService photoService;

  @InjectMocks
  private PhotoController photoController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(photoController).build();
  }

  @Test
  void testGetPhotos() throws Exception {
    // Create test data
    Photo photo1 = new Photo("photo1.jpg",
        "photo1.jpg",
        "/api/photos/photo1.jpg/thumbnail",
        "/api/photos/photo1.jpg/full");
    Photo photo2 = new Photo("photo2.jpg",
        "photo2.jpg",
        "/api/photos/photo2.jpg/thumbnail",
        "/api/photos/photo2.jpg/full");
    PhotoPage photoPage = new PhotoPage(Arrays.asList(photo1, photo2), 0, 12, 1, 2);

    // Mock service response
    when(photoService.getPhotos(0, 12)).thenReturn(photoPage);

    // Test endpoint
    mockMvc.perform(get("/api/photos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.photos.length()").value(2))
        .andExpect(jsonPath("$.photos[0].id").value("photo1.jpg"))
        .andExpect(jsonPath("$.photos[1].id").value("photo2.jpg"))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(12))
        .andExpect(jsonPath("$.totalPages").value(1))
        .andExpect(jsonPath("$.totalElements").value(2));
  }

  @Test
  void testGetPhoto() throws Exception {
    // Create test data
    Photo photo = new Photo("photo1.jpg",
        "photo1.jpg",
        "/api/photos/photo1.jpg/thumbnail",
        "/api/photos/photo1.jpg/full");

    // Mock service response
    when(photoService.getPhoto("photo1.jpg")).thenReturn(photo);

    // Test endpoint
    mockMvc.perform(get("/api/photos/photo1.jpg"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("photo1.jpg"))
        .andExpect(jsonPath("$.filename").value("photo1.jpg"))
        .andExpect(jsonPath("$.thumbnailUrl").value("/api/photos/photo1.jpg/thumbnail"))
        .andExpect(jsonPath("$.fullSizeUrl").value("/api/photos/photo1.jpg/full"));
  }

  @Test
  void testGetPhotoNotFound() throws Exception {
    // Mock service response for non-existent photo
    when(photoService.getPhoto("nonexistent.jpg")).thenReturn(null);

    // Test endpoint
    mockMvc.perform(get("/api/photos/nonexistent.jpg")).andExpect(status().isNotFound());
  }

  @Test
  void testGetThumbnail() throws Exception {
    // Create test image data
    byte[] imageData = "test image data".getBytes();

    // Mock service response
    when(photoService.getThumbnailImage("photo1.jpg")).thenReturn(imageData);

    // Test endpoint
    mockMvc.perform(get("/api/photos/photo1.jpg/thumbnail"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.IMAGE_JPEG))
        .andExpect(content().bytes(imageData));
  }

  @Test
  void testGetThumbnailNotFound() throws Exception {
    // Mock service response for non-existent thumbnail
    when(photoService.getThumbnailImage("nonexistent.jpg")).thenReturn(null);

    // Test endpoint
    mockMvc.perform(get("/api/photos/nonexistent.jpg/thumbnail")).andExpect(status().isNotFound());
  }

  @Test
  void testGetFullSizeImage() throws Exception {
    // Create test image data
    byte[] imageData = "test full size image data".getBytes();

    // Mock service response
    when(photoService.getFullSizeImage("photo1.jpg")).thenReturn(imageData);

    // Test endpoint
    mockMvc.perform(get("/api/photos/photo1.jpg/full"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.IMAGE_JPEG))
        .andExpect(content().bytes(imageData));
  }

  @Test
  void testGetFullSizeImageNotFound() throws Exception {
    // Mock service response for non-existent image
    when(photoService.getFullSizeImage("nonexistent.jpg")).thenReturn(null);

    // Test endpoint
    mockMvc.perform(get("/api/photos/nonexistent.jpg/full")).andExpect(status().isNotFound());
  }
}
