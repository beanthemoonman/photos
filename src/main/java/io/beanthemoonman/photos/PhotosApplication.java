package io.beanthemoonman.photos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("io.beanthemoonman.photos.config")
public class PhotosApplication {

  public static void main(String[] args) {
    SpringApplication.run(PhotosApplication.class, args);
  }

}
