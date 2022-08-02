package io.papermc.hangar.service.internal.uploads;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.exceptions.InternalHangarException;
import io.papermc.hangar.service.internal.file.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@Service
public class ImageService extends HangarComponent {

    private final ProjectFiles projectFiles;
    private final FileService fileService;

    @Autowired
    public ImageService(ProjectFiles projectFiles, FileService fileService) {
        this.projectFiles = projectFiles;
        this.fileService = fileService;
    }

    public ResponseEntity<byte[]> getProjectIcon(String author, String slug) {
        String iconPath = projectFiles.getIconPath(author, slug);
        if (iconPath == null) {
            throw new InternalHangarException("Default to avatar url");
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl(CacheControl.maxAge(3600, TimeUnit.SECONDS).getHeaderValue());
            return ResponseEntity.ok().headers(headers).body(fileService.bytes(iconPath));
        } catch (IOException e) {
            e.printStackTrace();
            throw new HangarApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to fetch project icon");
        }
    }

    public String getUserIcon(String author) {
        return String.format(config.security.api.getAvatarUrl(), author);
    }


}
