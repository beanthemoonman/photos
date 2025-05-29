package io.beanthemoonman.photos.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PhotoTest {

  @Test
  void testConstructorAndGetters() {
    // Create a photo with the constructor
    Photo photo = new Photo("test-id", "test.jpg", "/thumbnail/url", "/fullsize/url");

    // Test getters
    assertEquals("test-id", photo.getId());
    assertEquals("test.jpg", photo.getFilename());
    assertEquals("/thumbnail/url", photo.getThumbnailUrl());
    assertEquals("/fullsize/url", photo.getFullSizeUrl());
  }

  @Test
  void testSetters() {
    // Create an empty photo
    Photo photo = new Photo();

    // Set properties
    photo.setId("new-id");
    photo.setFilename("new.jpg");
    photo.setThumbnailUrl("/new/thumbnail");
    photo.setFullSizeUrl("/new/fullsize");

    // Verify properties were set correctly
    assertEquals("new-id", photo.getId());
    assertEquals("new.jpg", photo.getFilename());
    assertEquals("/new/thumbnail", photo.getThumbnailUrl());
    assertEquals("/new/fullsize", photo.getFullSizeUrl());
  }
}