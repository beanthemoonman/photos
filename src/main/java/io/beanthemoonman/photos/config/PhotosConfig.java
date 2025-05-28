package io.beanthemoonman.photos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration properties for the photo gallery application.
 */
@Component
@ConfigurationProperties(prefix = "photos")
public class PhotosConfig {

  private String directory;

  private Thumbnail thumbnail = new Thumbnail();

  public String getDirectory() {
    return directory;
  }

  public void setDirectory(String directory) {
    this.directory = directory;
  }

  public Path getDirectoryPath() {
    return Paths.get(directory);
  }

  public Thumbnail getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(Thumbnail thumbnail) {
    this.thumbnail = thumbnail;
  }

  public static class Thumbnail {
    private int width = 300;

    private int height = 200;

    public int getWidth() {
      return width;
    }

    public void setWidth(int width) {
      this.width = width;
    }

    public int getHeight() {
      return height;
    }

    public void setHeight(int height) {
      this.height = height;
    }
  }
}