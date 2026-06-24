package io.beanthemoonman.photos.model;

/**
 * A single label/value row of EXIF metadata shown in the photo modal panel.
 */
public record ExifEntry(String label, String value) {
}
