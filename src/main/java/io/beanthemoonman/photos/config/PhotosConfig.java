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

  private Website website = new Website();

  public Website getWebsite() {
    return website;
  }

  public void setWebsite(Website website) {
    this.website = website;
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

  public static class Website {
    private String title = "Photos Gallery";
    private String description = "A beautiful photo gallery";
    private String faviconPath = "/favicon.ico";
    private String ogImage = "";
    private String ogUrl = "";
    private String ogSiteName = "";

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public String getFaviconPath() {
      return faviconPath;
    }

    public void setFaviconPath(String faviconPath) {
      this.faviconPath = faviconPath;
    }

    public String getOgImage() {
      return ogImage;
    }

    public void setOgImage(String ogImage) {
      this.ogImage = ogImage;
    }

    public String getOgUrl() {
      return ogUrl;
    }

    public void setOgUrl(String ogUrl) {
      this.ogUrl = ogUrl;
    }

    public String getOgSiteName() {
      return ogSiteName;
    }

    public void setOgSiteName(String ogSiteName) {
      this.ogSiteName = ogSiteName;
    }
  }
}