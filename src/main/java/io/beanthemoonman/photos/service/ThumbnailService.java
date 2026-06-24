package io.beanthemoonman.photos.service;

import io.beanthemoonman.photos.config.PhotosConfig;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

@Service
public class ThumbnailService {

  // Matches the frontend's panorama threshold (gallery.js): images wider than this span the
  // full gallery width on screen, so they need a proportionally larger thumbnail to stay sharp.
  private static final double PANORAMA_RATIO = 2.4;
  private static final int PANORAMA_SCALE = 4; // panoramas display ~4 columns wide

  private final PhotosConfig config;

  public ThumbnailService(PhotosConfig config) {
    this.config = config;
  }

  public byte[] createThumbnail(Path imagePath) throws IOException {
    int width = config.getThumbnail().getWidth();
    int height = config.getThumbnail().getHeight();

    // Scale the thumbnail box up for wide panoramas so their resolution roughly tracks how
    // large they actually render on the page, instead of leaving them blurry at normal size.
    Dimension src = sourceDimension(imagePath);
    if (src != null && (double) src.width / src.height > PANORAMA_RATIO) {
      width *= PANORAMA_SCALE;
      height *= PANORAMA_SCALE;
    }

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    Thumbnails.of(imagePath.toFile())
        .size(width, height)
        .keepAspectRatio(true)
        .outputFormat("jpg")
        .toOutputStream(outputStream);

    return outputStream.toByteArray();
  }

  // Reads only the image header (no full decode) to get source dimensions. Null if unreadable.
  private static Dimension sourceDimension(Path path) {
    try (ImageInputStream in = ImageIO.createImageInputStream(path.toFile())) {
      var readers = ImageIO.getImageReaders(in);
      if (readers.hasNext()) {
        ImageReader reader = readers.next();
        try {
          reader.setInput(in);
          return new Dimension(reader.getWidth(0), reader.getHeight(0));
        } finally {
          reader.dispose();
        }
      }
    } catch (IOException ignored) {
      // fall through to default sizing
    }
    return null;
  }
}