package io.beanthemoonman.photos.model;

/**
 * Represents a photo in the gallery with its metadata.
 */
public class Photo {

  private String id;

  private String filename;

  private String thumbnailUrl;

  private String fullSizeUrl;

  public Photo() {
  }

  public Photo(String id, String filename, String thumbnailUrl, String fullSizeUrl) {
    this.id = id;
    this.filename = filename;
    this.thumbnailUrl = thumbnailUrl;
    this.fullSizeUrl = fullSizeUrl;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  public String getFullSizeUrl() {
    return fullSizeUrl;
  }

  public void setFullSizeUrl(String fullSizeUrl) {
    this.fullSizeUrl = fullSizeUrl;
  }
}