package io.beanthemoonman.photos.runner;

import io.beanthemoonman.photos.utility.ThumbnailHasher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ThumbnailHasherRunner implements CommandLineRunner {

  private final ThumbnailHasher thumbnailHasher;

  public ThumbnailHasherRunner(ThumbnailHasher thumbnailHasher) {
    this.thumbnailHasher = thumbnailHasher;
  }

  @Override
  public void run(String... args) throws Exception {
    thumbnailHasher.boot();
  }
}
