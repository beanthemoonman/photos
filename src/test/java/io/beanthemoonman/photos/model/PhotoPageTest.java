package io.beanthemoonman.photos.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PhotoPageTest {

  @Test
  void testConstructorAndGetters() {
    // Create a list of photos
    List<Photo> photos = new ArrayList<>();
    photos.add(new Photo("id1", "photo1.jpg", "/thumb1", "/full1"));
    photos.add(new Photo("id2", "photo2.jpg", "/thumb2", "/full2"));

    // Create a photo page with the constructor
    PhotoPage photoPage = new PhotoPage(photos, 1, 10, 5, 42);

    // Test getters
    assertEquals(photos, photoPage.getPhotos());
    assertEquals(1, photoPage.getPage());
    assertEquals(10, photoPage.getSize());
    assertEquals(5, photoPage.getTotalPages());
    assertEquals(42, photoPage.getTotalElements());
  }

  @Test
  void testSetters() {
    // Create an empty photo page
    PhotoPage photoPage = new PhotoPage();

    // Create a list of photos
    List<Photo> photos = new ArrayList<>();
    photos.add(new Photo("id1", "photo1.jpg", "/thumb1", "/full1"));

    // Set properties
    photoPage.setPhotos(photos);
    photoPage.setPage(2);
    photoPage.setSize(20);
    photoPage.setTotalPages(10);
    photoPage.setTotalElements(100);

    // Verify properties were set correctly
    assertEquals(photos, photoPage.getPhotos());
    assertEquals(2, photoPage.getPage());
    assertEquals(20, photoPage.getSize());
    assertEquals(10, photoPage.getTotalPages());
    assertEquals(100, photoPage.getTotalElements());
  }

  @Test
  void testEmptyConstructor() {
    // Create an empty photo page
    PhotoPage photoPage = new PhotoPage();

    // Verify default values
    assertNull(photoPage.getPhotos());
    assertEquals(0, photoPage.getPage());
    assertEquals(0, photoPage.getSize());
    assertEquals(0, photoPage.getTotalPages());
    assertEquals(0, photoPage.getTotalElements());
  }
}