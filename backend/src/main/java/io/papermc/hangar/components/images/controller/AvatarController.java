package io.papermc.hangar.components.images.controller;

import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.images.service.AvatarService;
import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/avatar")
public class AvatarController extends HangarComponent {

    public static final MediaType WEBP = new MediaType("image", "webp");

    private final AvatarService avatarService;

    @Autowired
    public AvatarController(final AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    // only really called if storage = local
    @GetMapping("/{type}/{subject}.webp")
    public ResponseEntity<ByteArrayResource> getAvatar(@PathVariable final @NotNull String type, @PathVariable final @NotNull String subject) throws IOException {
        final byte[] image = this.avatarService.getAvatarForLocal(type, subject);
        return ResponseEntity.ok()
            .contentLength(image.length)
            .contentType(WEBP)
            .lastModified(Instant.now())
            .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
            .body(new ByteArrayResource(image));
    }
}
