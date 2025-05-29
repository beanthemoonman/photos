package io.beanthemoonman.photos.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PhotosConfigTest {

  @Autowired
  private PhotosConfig photosConfig;

  @Test
  void testConfigurationProperties() {
    // Test directory property
    assertEquals("photos", photosConfig.getDirectory());

    // Test directory path
    Path expectedPath = Paths.get("photos");
    assertEquals(expectedPath, photosConfig.getDirectoryPath());

    // Test thumbnail properties
    assertEquals(400, photosConfig.getThumbnail().getWidth());
    assertEquals(400, photosConfig.getThumbnail().getHeight());
  }

  @Test
  void testSetters() {
    // Test directory setter
    PhotosConfig config = new PhotosConfig();
    config.setDirectory("test-photos");
    assertEquals("test-photos", config.getDirectory());
    assertEquals(Paths.get("test-photos"), config.getDirectoryPath());

    // Test thumbnail setters
    PhotosConfig.Thumbnail thumbnail = new PhotosConfig.Thumbnail();
    thumbnail.setWidth(500);
    thumbnail.setHeight(300);
    assertEquals(500, thumbnail.getWidth());
    assertEquals(300, thumbnail.getHeight());

    config.setThumbnail(thumbnail);
    assertEquals(thumbnail, config.getThumbnail());
  }
}