package io.beanthemoonman.photos.model;

import java.util.List;

/**
 * Represents a photo in the gallery with its metadata.
 */
public class Photo {

  private String id;

  private String filename;

  private String thumbnailUrl;

  private String fullSizeUrl;

  /** Human-friendly title derived from the filename. */
  private String title;

  /** Date the photo was taken (formatted), or null if unknown. */
  private String dateTaken;

  /** Pixel dimensions, used for masonry aspect-ratio. 0 if unknown. */
  private int width;

  private int height;

  /** EXIF rows to display in the modal panel; only present tags are included. */
  private List<ExifEntry> exif = List.of();

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

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDateTaken() {
    return dateTaken;
  }

  public void setDateTaken(String dateTaken) {
    this.dateTaken = dateTaken;
  }

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

  public List<ExifEntry> getExif() {
    return exif;
  }

  public void setExif(List<ExifEntry> exif) {
    this.exif = exif;
  }
}