package io.papermc.hangar.components.images.service;

import com.github.benmanes.caffeine.cache.Cache;
import io.papermc.hangar.HangarComponent;
import io.papermc.hangar.components.images.controller.AvatarController;
import io.papermc.hangar.components.images.dao.AvatarDAO;
import io.papermc.hangar.components.images.model.AvatarTable;
import io.papermc.hangar.config.CacheConfig;
import io.papermc.hangar.model.api.User;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.model.internal.user.HangarUser;
import io.papermc.hangar.service.internal.file.FileService;
import io.papermc.hangar.service.internal.users.UserService;
import io.papermc.hangar.util.CryptoUtils;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AvatarService extends HangarComponent {

    private static final Logger logger = LoggerFactory.getLogger(AvatarService.class);

    public static final String USER = "user";
    public static final String PROJECT = "project";

    private final FileService fileService;
    private final ImageService imageService;
    private final AvatarDAO avatarDAO;
    private final UserService userService;
    private final RestTemplate restTemplate;

    private String defaultAvatarUrl;
    private String defaultAvatarPath;

    private final Cache<String, String> usernameCache;

    public AvatarService(final FileService fileService, final ImageService imageService, final AvatarDAO avatarDAO, final UserService userService, @Qualifier(CacheConfig.USERNAME) final org.springframework.cache.Cache usernameCache, final RestTemplate restTemplate) {
        this.fileService = fileService;
        this.imageService = imageService;
        this.avatarDAO = avatarDAO;
        this.userService = userService;
        this.restTemplate = restTemplate;

        //noinspection unchecked
        this.usernameCache = (Cache<String, String>) usernameCache.getNativeCache();
    }

    @PostConstruct
    public void setupDefault() {
        this.defaultAvatarPath = this.getPath("default", "default");
        logger.info("Storing avatars to {}", this.defaultAvatarPath);
        if (!this.fileService.exists(this.defaultAvatarPath)) {
            logger.info("Default avatar didn't exist, saving...");
            try {
                this.fileService.write(AvatarService.class.getClassLoader().getResourceAsStream("avatar/default.webp"), this.defaultAvatarPath, AvatarController.WEBP.toString());
            } catch (final IOException e) {
                throw new RuntimeException("Error saving default avatar", e);
            }
        }
        this.defaultAvatarUrl = this.fileService.getAvatarUrl("default", "default", "0");
        logger.info("Default avatar url is {}", this.defaultAvatarUrl);
    }

    public String getDefaultAvatarUrl() {
        return this.defaultAvatarUrl;
    }

    /*
     * Get methods
     */
    public String getUserAvatarUrl(final UserTable userTable) {
        return this.getAvatarUrl(USER, userTable.getUuid().toString(), null);
    }

    public String getUserAvatarUrl(final UUID orgUserUuid) {
        return this.getAvatarUrl(USER, orgUserUuid.toString(), null);
    }

    public String getUserAvatarUrl(final User user) {
        final String uuid;
        if (user instanceof final HangarUser hangarUser) {
            uuid = hangarUser.getUuid().toString();
        } else {
            uuid = this.usernameCache.get(user.getName(), (key) -> Objects.requireNonNull(this.userService.getUserTable(user.getName())).getUuid().toString());
        }

        return this.getAvatarUrl(USER, uuid, null);
    }

    public String getProjectAvatarUrl(final long projectId, final String ownerName) {
        return this.getAvatarUrl(PROJECT, String.valueOf(projectId), () -> {
            final UserTable userTable = this.userService.getUserTable(ownerName);
            if (userTable != null) {
                return this.getUserAvatarUrl(userTable);
            }
            return null;
        });
    }

    private String getAvatarUrl(final String type, final String subject, final Supplier<String> fallbackSupplier) {
        if (type.equals("default") && subject.equals("default")) {
            return this.defaultAvatarUrl;
        }
        final AvatarTable table = this.avatarDAO.getAvatar(type, subject);
        if (table == null) {
            if (fallbackSupplier != null) {
                final String fallback = fallbackSupplier.get();
                if (fallback == null) {
                    return this.defaultAvatarUrl;
                } else {
                    return fallback;
                }
            } else {
                return this.defaultAvatarUrl;
            }
        } else {
            return this.fileService.getAvatarUrl(type, subject, String.valueOf(table.getVersion()));
        }
    }

    /*
     * change methods
     */
    public void changeUserAvatar(final UUID uuid, final byte[] avatar) throws IOException {
        this.changeAvatar(USER, uuid.toString(), avatar);
    }

    public void changeProjectAvatar(final long projectId, final byte[] avatar) throws IOException {
        this.changeAvatar(PROJECT, String.valueOf(projectId), avatar);
    }

    private void changeAvatar(final String type, final String subject, byte[] avatar) throws IOException {
        final String unoptimizedHash = CryptoUtils.md5ToHex(avatar);
        AvatarTable table = this.avatarDAO.getAvatar(type, subject);
        if (table != null && table.getUnoptimizedHash().equals(unoptimizedHash)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't upload the same avatar again!");
        }
        avatar = this.imageService.convertAndOptimize(avatar);
        final String optimizedHash = CryptoUtils.md5ToHex(avatar);
        if (table == null) {
            table = new AvatarTable(type, subject, optimizedHash, unoptimizedHash, 1);
            this.avatarDAO.createAvatar(table);
        } else {
            table.setOptimizedHash(optimizedHash);
            table.setUnoptimizedHash(unoptimizedHash);
            table.setVersion(table.getVersion() + 1);
            this.avatarDAO.updateAvatar(table);
        }
        this.fileService.write(new ByteArrayInputStream(avatar), this.getPath(type, subject), AvatarController.WEBP.toString());
    }

    /*
     * Delete methods
     */
    public void deleteProjectAvatar(final long projectId) {
        this.deleteAvatar(PROJECT, String.valueOf(projectId));
    }

    private void deleteAvatar(final String type, final String subject) {
        this.avatarDAO.deleteAvatar(type, subject);
        this.fileService.delete(this.getPath(type, subject));
    }

    private String getPath(final String type, final String subject) {
        return this.fileService.resolve(this.fileService.getRoot(), "avatars/" + type + "/" + subject + ".webp");
    }

    /*
     * misc
     */
    public void importProjectAvatar(final long projectId, final String avatarUrl) {
        try {
            final ResponseEntity<byte[]> avatar = this.restTemplate.getForEntity(avatarUrl, byte[].class);
            if (avatar.getStatusCode().is2xxSuccessful()) {
                this.changeProjectAvatar(projectId, avatar.getBody());
            } else {
                logger.warn("Couldn't import project avatar from {}, {}", avatarUrl, avatar.getStatusCode());
            }
        } catch (final Exception ex) {
            logger.warn("Couldn't import project avatar from " + avatarUrl, ex);
        }
    }

    public byte[] getAvatarForLocal(final String type, final String subject) throws IOException {
        if (type.equals("default") && subject.equals("default")) {
            return this.fileService.bytes(this.defaultAvatarPath);
        }
        final AvatarTable table = this.avatarDAO.getAvatar(type, subject);
        if (table == null) {
            return this.fileService.bytes(this.defaultAvatarPath);
        } else {
            return this.fileService.bytes(this.getPath(type, subject));
        }
    }
}
