package io.beanthemoonman.photos.runner;

import io.beanthemoonman.photos.utility.ThumbnailHasher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ThumbnailHasherRunner implements CommandLineRunner {

  @Override
  public void run(String... args) throws Exception {
    ThumbnailHasher.instance.boot();
  }
}
