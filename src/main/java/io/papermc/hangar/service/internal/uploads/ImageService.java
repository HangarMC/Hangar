package io.papermc.hangar.service.internal.uploads;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.exceptions.InternalHangarException;
import io.papermc.hangar.service.HangarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class ImageService extends HangarService {

    private final ProjectFiles projectFiles;

    @Autowired
    public ImageService(ProjectFiles projectFiles) {
        this.projectFiles = projectFiles;
    }

    public ResponseEntity<byte[]> getProjectIcon(String author, String slug) {
        Path iconPath = projectFiles.getIconPath(author, slug);
        if (iconPath == null) {
            throw new InternalHangarException("Default to avatar url");
        }
        try {
            byte[] lastModified = Files.getLastModifiedTime(iconPath).toString().getBytes(StandardCharsets.UTF_8);
            byte[] lastModifiedHash = MessageDigest.getInstance("MD5").digest(lastModified);
            String hashString = Base64.getEncoder().encodeToString(lastModifiedHash);
            return ResponseEntity.ok().header(HttpHeaders.ETAG, hashString).header(HttpHeaders.CACHE_CONTROL, "max-age=" + 3600).body(Files.readAllBytes(iconPath));
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new HangarApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to fetch project icon");
        }
    }

    public String getUserIcon(String author) {
        return String.format(config.security.api.getAvatarUrl(), author);
    }


}
