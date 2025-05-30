package io.beanthemoonman.photos.utility;

import net.coobird.thumbnailator.Thumbnails;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.beanthemoonman.photos.utility.Utility.bytesToSha256;

public class ThumbnailHasher {

  public static volatile ThumbnailHasher instance;

  private final ConcurrentMap<String, String> shaCache = new ConcurrentHashMap<>();

  public static final String CACHE_DIR = "cache";

  public static ThumbnailHasher getInstance() {
    if (instance == null) {
      synchronized (ThumbnailHasher.class) {
        if (instance == null) {
          instance = new ThumbnailHasher();
        }
      }
    }
    return instance;
  }

  public ConcurrentMap<String, String> getShaCache() {
    return shaCache;
  }

  public void boot() throws IOException {
    try (var listing = Files.list(Paths.get("photos"))) {
      listing.forEach(photo -> {
        try {
          var shaCache = ThumbnailHasher.getInstance().getShaCache();
          String id = photo.getFileName().toString();
          String photoHash;
          if (shaCache.containsKey(id)) {
            photoHash = shaCache.get(id);
          } else {
            photoHash = bytesToSha256(Files.readAllBytes(photo));
            shaCache.put(id, photoHash);
          }
          Path cachePath = Paths.get(CACHE_DIR + "/" + photoHash + ".jpg");
          if (!Files.exists(cachePath)) {
            byte[] imageData = createThumbnail(photo);
            Files.write(cachePath, imageData);
          }
        } catch (IOException | NoSuchAlgorithmException _) {
        }
      });
    }
  }

  private byte[] createThumbnail(Path imagePath) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    Thumbnails.of(imagePath.toFile())
        .size(400, 400)
        .keepAspectRatio(true)
        .outputFormat("jpg")
        .toOutputStream(outputStream);

    return outputStream.toByteArray();
  }
}
