package io.beanthemoonman.photos;

import io.beanthemoonman.photos.utility.ThumbnailHasher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.io.IOException;

@SpringBootApplication
@ConfigurationPropertiesScan("io.beanthemoonman.photos.config")
public class PhotosApplication {

  public static void main(String[] args) throws IOException {
    ThumbnailHasher.getInstance().boot();
    SpringApplication.run(PhotosApplication.class, args);
  }

}
