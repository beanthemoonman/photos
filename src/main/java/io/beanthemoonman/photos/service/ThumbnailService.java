package io.beanthemoonman.photos.service;

import io.beanthemoonman.photos.config.PhotosConfig;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

@Service
public class ThumbnailService {

    private final PhotosConfig config;

    public ThumbnailService(PhotosConfig config) {
        this.config = config;
    }

    public byte[] createThumbnail(Path imagePath) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(imagePath.toFile())
                .size(config.getThumbnail().getWidth(), config.getThumbnail().getHeight())
                .keepAspectRatio(true)
                .outputFormat("jpg")
                .toOutputStream(outputStream);

        return outputStream.toByteArray();
    }
}