package io.beanthemoonman.photos.model;

import java.util.List;

/**
 * Represents a paginated response of photos.
 */
public class PhotoPage {

  private List<Photo> photos;

  private int page;

  private int size;

  private int totalPages;

  private long totalElements;

  public PhotoPage() {
  }

  public PhotoPage(List<Photo> photos, int page, int size, int totalPages, long totalElements) {
    this.photos = photos;
    this.page = page;
    this.size = size;
    this.totalPages = totalPages;
    this.totalElements = totalElements;
  }

  public List<Photo> getPhotos() {
    return photos;
  }

  public void setPhotos(List<Photo> photos) {
    this.photos = photos;
  }

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public int getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(int totalPages) {
    this.totalPages = totalPages;
  }

  public long getTotalElements() {
    return totalElements;
  }

  public void setTotalElements(long totalElements) {
    this.totalElements = totalElements;
  }
}