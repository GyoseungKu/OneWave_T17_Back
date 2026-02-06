package org.syu_likelion.OneWave.user;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ProfileImageStorage {
    private final AmazonS3 s3Client;
    private final String bucket;
    private final String profilePrefix;

    public ProfileImageStorage(
        AmazonS3 s3Client,
        @Value("${r2.bucket}") String bucket,
        @Value("${r2.profile-prefix:profile}") String profilePrefix
    ) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.profilePrefix = normalizePrefix(profilePrefix);
    }

    public SavedProfileImage save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("PROFILE_IMAGE_EMPTY");
        }

        BufferedImage image;
        try (InputStream in = file.getInputStream()) {
            image = ImageIO.read(in);
        } catch (IOException e) {
            throw new IllegalStateException("PROFILE_IMAGE_READ_FAILED", e);
        }

        if (image == null) {
            throw new IllegalArgumentException("PROFILE_IMAGE_NOT_IMAGE");
        }

        byte[] payload = toWebpBytes(image);
        String storedName = UUID.randomUUID().toString().replace("-", "") + ".webp";
        String objectKey = profilePrefix + storedName;
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("image/webp");
            metadata.setContentLength(payload.length);
            PutObjectRequest request = new PutObjectRequest(
                bucket,
                objectKey,
                new ByteArrayInputStream(payload),
                metadata
            );
            s3Client.putObject(request);
        } catch (Exception e) {
            throw new IllegalStateException("PROFILE_IMAGE_SAVE_FAILED", e);
        }

        long fileSize = payload.length;
        String relativePath = objectKey;
        return new SavedProfileImage(
            file.getOriginalFilename(),
            storedName,
            "webp",
            "image/webp",
            fileSize,
            relativePath
        );
    }

    private static byte[] toWebpBytes(BufferedImage image) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            boolean written = ImageIO.write(image, "webp", out);
            if (!written) {
                throw new IllegalStateException("WEBP_WRITE_UNSUPPORTED");
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("PROFILE_IMAGE_SAVE_FAILED", e);
        }
    }

    private static String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "";
        }
        String trimmed = prefix.trim();
        if (!trimmed.endsWith("/")) {
            trimmed += "/";
        }
        if (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        return trimmed;
    }

    public record SavedProfileImage(
        String originalFilename,
        String savedFilename,
        String fileExtension,
        String contentType,
        long fileSize,
        String relativePath
    ) {}
}
