package io.beanthemoonman.photos.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utility {

  private static final Logger logger = LoggerFactory.getLogger(Utility.class);

  /**
   * Creates the specified directory if it does not already exist.
   *
   * @param directoryPath the path of the directory to create
   */
  public static void createDirectoryIfNotExists(Path directoryPath) {
    try {
      if (!Files.exists(directoryPath)) {
        Files.createDirectories(directoryPath);
        logger.info("Created directory: {}", directoryPath);
      }
    } catch (IOException e) {
      logger.error("Failed to create directory: {}", directoryPath, e);
    }
  }

  /**
   * Converts the given byte array into a SHA-256 hash and returns it as a hexadecimal string.
   *
   * @param bytes the input byte array to be hashed
   * @return a hexadecimal string representing the SHA-256 hash of the input byte array
   * @throws NoSuchAlgorithmException if the SHA-256 algorithm is not available in the environment
   */
  public static String bytesToSha256(byte[] bytes) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hashBytes = digest.digest(bytes);

    return bytesToHex(hashBytes);
  }

  /**
   * Converts an array of bytes into a hexadecimal string representation.
   *
   * @param hash the byte array to be converted into a hexadecimal string
   * @return a string representing the hexadecimal equivalent of the provided byte array
   */
  private static String bytesToHex(byte[] hash) {
    StringBuilder hexString = new StringBuilder(2 * hash.length);
    for (byte b : hash) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }
}
