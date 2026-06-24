package io.beanthemoonman.photos.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.gif.GifImageDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import io.beanthemoonman.photos.model.ExifEntry;
import io.beanthemoonman.photos.model.Photo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static io.beanthemoonman.photos.utility.FileFilter.getNameWithoutExtension;

/**
 * Extracts display metadata (title, date taken, dimensions, EXIF) from image files.
 * Results are cached by filename + last-modified time so files are parsed at most once per edit.
 */
@Service
public class MetadataService {

  private static final Logger logger = LoggerFactory.getLogger(MetadataService.class);

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy");

  // ponytail: unbounded map keyed by filename+mtime, mirrors ThumbnailHasher's shaCache.
  // Single-user gallery; add eviction only if memory becomes a concern.
  private final ConcurrentHashMap<String, Meta> cache = new ConcurrentHashMap<>();

  /** Populate the metadata fields on the given photo. */
  public void enrich(Path file, Photo photo) {
    Meta m = forFile(file);
    photo.setTitle(m.title);
    photo.setDateTaken(m.dateTaken);
    photo.setWidth(m.width);
    photo.setHeight(m.height);
    photo.setExif(m.exif);
  }

  private Meta forFile(Path file) {
    String key;
    try {
      key = file.getFileName() + ":" + Files.getLastModifiedTime(file).toMillis();
    } catch (Exception e) {
      key = file.getFileName().toString();
    }
    return cache.computeIfAbsent(key, k -> extract(file));
  }

  private Meta extract(Path file) {
    String filename = file.getFileName().toString();
    Meta meta = new Meta();
    meta.title = prettifyTitle(filename);

    try {
      Metadata metadata = ImageMetadataReader.readMetadata(file.toFile());

      ExifIFD0Directory ifd0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
      ExifSubIFDDirectory sub = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

      // Date taken
      if (sub != null) {
        Date date = sub.getDateOriginal();
        if (date != null) {
          meta.dateTaken = DATE_FORMAT.format(date);
        }
      }
      if (meta.dateTaken == null) {
        try {
          meta.dateTaken = DATE_FORMAT.format(new Date(Files.getLastModifiedTime(file).toMillis()));
        } catch (Exception ignored) {
          // leave null
        }
      }

      // Dimensions
      int[] dims = readDimensions(metadata);
      meta.width = dims[0];
      meta.height = dims[1];

      // EXIF rows (only those present)
      List<ExifEntry> exif = new ArrayList<>();
      addCamera(exif, ifd0);
      add(exif, "Lens", sub, ExifSubIFDDirectory.TAG_LENS_MODEL);
      add(exif, "Focal", sub, ExifSubIFDDirectory.TAG_FOCAL_LENGTH);
      add(exif, "Aperture", sub, ExifSubIFDDirectory.TAG_FNUMBER);
      add(exif, "Shutter", sub, ExifSubIFDDirectory.TAG_EXPOSURE_TIME);
      add(exif, "ISO", sub, ExifSubIFDDirectory.TAG_ISO_EQUIVALENT);
      meta.exif = exif;
    } catch (Exception e) {
      // Non-image, EXIF-less, or unreadable metadata: title + (best-effort) date already set.
      logger.debug("No readable metadata for {}: {}", filename, e.getMessage());
    }
    return meta;
  }

  private static void addCamera(List<ExifEntry> exif, ExifIFD0Directory ifd0) {
    if (ifd0 == null) {
      return;
    }
    String make = ifd0.getDescription(ExifIFD0Directory.TAG_MAKE);
    String model = ifd0.getDescription(ExifIFD0Directory.TAG_MODEL);
    String camera;
    if (model != null && make != null && !model.toLowerCase().contains(make.toLowerCase())) {
      camera = (make + " " + model).trim();
    } else {
      camera = model != null ? model : make;
    }
    if (camera != null && !camera.isBlank()) {
      exif.add(new ExifEntry("Camera", camera.trim()));
    }
  }

  private static void add(List<ExifEntry> exif, String label, Directory dir, int tag) {
    if (dir == null) {
      return;
    }
    String value = dir.getDescription(tag);
    if (value != null && !value.isBlank()) {
      exif.add(new ExifEntry(label, value.trim()));
    }
  }

  /** Try the common per-format directories for pixel dimensions; returns {width, height} or {0,0}. */
  private static int[] readDimensions(Metadata metadata) {
    JpegDirectory jpeg = metadata.getFirstDirectoryOfType(JpegDirectory.class);
    if (jpeg != null) {
      int w = jpeg.getInteger(JpegDirectory.TAG_IMAGE_WIDTH) != null ? jpeg.getInteger(JpegDirectory.TAG_IMAGE_WIDTH) : 0;
      int h = jpeg.getInteger(JpegDirectory.TAG_IMAGE_HEIGHT) != null ? jpeg.getInteger(JpegDirectory.TAG_IMAGE_HEIGHT) : 0;
      if (w > 0 && h > 0) {
        return new int[] {w, h};
      }
    }
    PngDirectory png = metadata.getFirstDirectoryOfType(PngDirectory.class);
    if (png != null) {
      Integer w = png.getInteger(PngDirectory.TAG_IMAGE_WIDTH);
      Integer h = png.getInteger(PngDirectory.TAG_IMAGE_HEIGHT);
      if (w != null && h != null && w > 0 && h > 0) {
        return new int[] {w, h};
      }
    }
    GifImageDirectory gif = metadata.getFirstDirectoryOfType(GifImageDirectory.class);
    if (gif != null) {
      Integer w = gif.getInteger(GifImageDirectory.TAG_WIDTH);
      Integer h = gif.getInteger(GifImageDirectory.TAG_HEIGHT);
      if (w != null && h != null && w > 0 && h > 0) {
        return new int[] {w, h};
      }
    }
    ExifSubIFDDirectory sub = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
    if (sub != null) {
      Integer w = sub.getInteger(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH);
      Integer h = sub.getInteger(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT);
      if (w != null && h != null && w > 0 && h > 0) {
        return new int[] {w, h};
      }
    }
    return new int[] {0, 0};
  }

  /** Filename without extension, with separators turned into spaces. */
  static String prettifyTitle(String filename) {
    String name = getNameWithoutExtension(filename).replaceAll("[_-]+", " ").trim();
    return name.replaceAll("\\s+", " ");
  }

  private static final class Meta {
    String title;
    String dateTaken;
    int width;
    int height;
    List<ExifEntry> exif = List.of();
  }
}
