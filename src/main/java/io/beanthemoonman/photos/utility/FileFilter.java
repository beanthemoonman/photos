package io.beanthemoonman.photos.utility;

import java.nio.file.Path;

public class FileFilter {

    private static final String[] SUPPORTED_EXTENSIONS = { ".jpg", ".jpeg", ".png", ".gif" };

    public static boolean isImageFile(Path path) {
        String filename = path.getFileName().toString().toLowerCase();
        for (String ext : SUPPORTED_EXTENSIONS) {
            if (filename.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    public static String getNameWithoutExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(0, lastDotIndex);
        }
        return filename;
    }
}