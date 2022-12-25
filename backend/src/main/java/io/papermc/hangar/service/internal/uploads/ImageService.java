package io.papermc.hangar.service.internal.uploads;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.exceptions.InternalHangarException;
import io.papermc.hangar.service.internal.file.FileService;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ImageService extends HangarComponent {

    private final ProjectFiles projectFiles;
    private final FileService fileService;

    @Autowired
    public ImageService(final ProjectFiles projectFiles, final FileService fileService) {
        this.projectFiles = projectFiles;
        this.fileService = fileService;
    }

    public ResponseEntity<byte[]> getProjectIcon(final String author, final String slug) {
        final String iconPath = this.projectFiles.getIconPath(author, slug);
        if (iconPath == null || !this.fileService.exists(iconPath)) {
            throw new InternalHangarException("Default to avatar url");
        }
        try {
            return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(this.fileService.bytes(iconPath));
        } catch (final IOException e) {
            e.printStackTrace();
            throw new HangarApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to fetch project icon");
        }
    }

    public String getUserIcon(final String author) {
        return String.format(this.config.security.api().avatarUrl(), author);
    }
}
