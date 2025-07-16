package io.beanthemoonman.photos.utility;

import io.beanthemoonman.photos.config.PhotosConfig;
import io.beanthemoonman.photos.service.ThumbnailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.beanthemoonman.photos.utility.Utility.bytesToSha256;
import static io.beanthemoonman.photos.utility.Utility.createDirectoryIfNotExists;

@Component
public class ThumbnailHasher {

  private static final Logger logger = LoggerFactory.getLogger(ThumbnailHasher.class);
  public static volatile ThumbnailHasher instance;

  private final ConcurrentMap<String, String> shaCache = new ConcurrentHashMap<>();
  private final PhotosConfig config;
  private final ThumbnailService thumbnailService;
  private final Path cacheDir;

  public ThumbnailHasher(PhotosConfig config, ThumbnailService thumbnailService) {
    this.config = config;
    this.thumbnailService = thumbnailService;
    this.cacheDir = Paths.get("cache");
    createDirectoryIfNotExists(cacheDir);
  }

  public static ThumbnailHasher getInstance() {
    if (instance == null) {
      throw new IllegalStateException("ThumbnailHasher not initialized. Use Spring injection instead.");
    }
    return instance;
  }

  public ConcurrentMap<String, String> getShaCache() {
    return shaCache;
  }

  public void boot() throws IOException {
    try (var listing = Files.list(config.getDirectoryPath())) {
      listing.forEach(photo -> {
        try {
          String id = photo.getFileName().toString();
          String photoHash;
          if (shaCache.containsKey(id)) {
            photoHash = shaCache.get(id);
          } else {
            photoHash = bytesToSha256(Files.readAllBytes(photo));
            shaCache.put(id, photoHash);
          }
          Path cachePath = cacheDir.resolve(photoHash + ".jpg");
          if (!Files.exists(cachePath)) {
            byte[] imageData = thumbnailService.createThumbnail(photo);
            Files.write(cachePath, imageData);
          }
        } catch (IOException | NoSuchAlgorithmException e) {
          logger.warn("Failed to process photo: {}", photo.getFileName(), e);
        }
      });
    }
  }

  public Path getCacheDir() {
    return cacheDir;
  }
}
