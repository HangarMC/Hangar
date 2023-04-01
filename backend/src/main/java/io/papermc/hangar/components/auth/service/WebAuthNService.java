package io.papermc.hangar.components.auth.service;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.UserIdentity;
import io.papermc.hangar.components.auth.model.credential.CredentialType;
import io.papermc.hangar.components.auth.model.credential.WebAuthNCredential;
import io.papermc.hangar.components.auth.model.db.UserCredentialTable;
import io.papermc.hangar.model.db.UserTable;
import io.papermc.hangar.service.internal.users.UserService;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WebAuthNService implements CredentialRepository {

    private final SecureRandom random = new SecureRandom();
    private final CredentialsService credentialsService;
    private final UserService userService;

    public WebAuthNService(final CredentialsService credentialsService, final UserService userService) {
        this.credentialsService = credentialsService;
        this.userService = userService;
    }

    public void addDevice(final long userId, final WebAuthNCredential.WebAuthNDevice device) {
        final WebAuthNCredential webAuthNCredential = this.getWebAuthNCredential(userId);
        webAuthNCredential.credentials().add(device);
        this.credentialsService.updateCredential(userId, webAuthNCredential);
    }

    public void removeDevice(final long userId, final String id) {
        final WebAuthNCredential webAuthNCredential = this.getWebAuthNCredential(userId);
        webAuthNCredential.credentials().removeIf(e -> e.id().equals(id));
        this.credentialsService.updateCredential(userId, webAuthNCredential);
    }

    public UserIdentity getExistingUserOrCreate(final long userId, final String name) {
        final UserCredentialTable credential = this.credentialsService.getCredential(userId, CredentialType.WEBAUTHN);
        if (credential == null) {
            return this.createNewUser(userId, name);
        }

        final WebAuthNCredential webAuthNCredential = credential.getCredential().get(WebAuthNCredential.class);
        if (webAuthNCredential == null) {
            return this.createNewUser(userId, name);
        }

        return UserIdentity.builder().name(name).displayName(name).id(ByteArray.fromBase64(webAuthNCredential.userHandle())).build();
    }

    private UserIdentity createNewUser(final long userId, final String name) {
        final UserIdentity userIdentity = UserIdentity.builder()
            .name(name)
            .displayName(name)
            .id(this.generateRandom(32))
            .build();

        this.credentialsService.registerCredential(userId, new WebAuthNCredential(userIdentity.getId().getBase64(), List.of(), null, null));

        return userIdentity;
    }

    public ByteArray generateRandom(final int length) {
        final byte[] bytes = new byte[length];
        this.random.nextBytes(bytes);
        return new ByteArray(bytes);
    }

    public void storeSetupRequest(final long userId, final String publicKeyCredentialCreationOptions, final String authenticatorName) {
        final WebAuthNCredential webAuthNCredential = this.getWebAuthNCredential(userId);

        final WebAuthNCredential updated = new WebAuthNCredential(webAuthNCredential.userHandle(), webAuthNCredential.credentials(), new WebAuthNCredential.PendingSetup(authenticatorName, publicKeyCredentialCreationOptions), webAuthNCredential.pendingLogin());
        this.credentialsService.updateCredential(userId, updated);
    }

    public WebAuthNCredential.PendingSetup retrieveSetupRequest(final long userId) {
        final WebAuthNCredential webAuthNCredential = this.getWebAuthNCredential(userId);

        if (webAuthNCredential.pendingSetup() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No pending");
        }

        final WebAuthNCredential updated = new WebAuthNCredential(webAuthNCredential.userHandle(), webAuthNCredential.credentials(), null, webAuthNCredential.pendingLogin());
        this.credentialsService.updateCredential(userId, updated);

        return webAuthNCredential.pendingSetup();
    }

    public void storeLoginRequest(final long userId, final String json) {
        final WebAuthNCredential webAuthNCredential = this.getWebAuthNCredential(userId);

        final WebAuthNCredential updated = new WebAuthNCredential(webAuthNCredential.userHandle(), webAuthNCredential.credentials(), webAuthNCredential.pendingSetup(), new WebAuthNCredential.PendingLogin(json));
        this.credentialsService.updateCredential(userId, updated);
    }

    public WebAuthNCredential.PendingLogin retrieveLoginRequest(final long userId) {
        final WebAuthNCredential webAuthNCredential = this.getWebAuthNCredential(userId);

        if (webAuthNCredential.pendingLogin() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No pending");
        }

        final WebAuthNCredential updated = new WebAuthNCredential(webAuthNCredential.userHandle(), webAuthNCredential.credentials(), webAuthNCredential.pendingSetup(), null);
        this.credentialsService.updateCredential(userId, updated);

        return webAuthNCredential.pendingLogin();
    }

    public void updateCredential(final long userId, final String credentialId, final long signatureCount) {
        final WebAuthNCredential webAuthNCredential = this.getWebAuthNCredential(userId);
        final var any = webAuthNCredential.credentials().stream().filter(c -> c.id().equals(credentialId)).findAny();
        if (any.isPresent()) {
            final WebAuthNCredential.WebAuthNDevice oldDevice = any.get();
            final WebAuthNCredential.Authenticator patchedAuthenticator = new WebAuthNCredential.Authenticator(oldDevice.authenticator().aaguid(), signatureCount, oldDevice.authenticator().cloneWarning());
            final WebAuthNCredential.WebAuthNDevice patchedDevice = new WebAuthNCredential.WebAuthNDevice(oldDevice.id(), oldDevice.addedAt(), oldDevice.publicKey(), oldDevice.displayName(), patchedAuthenticator, oldDevice.isPasswordLess(), oldDevice.attestationType());
            webAuthNCredential.credentials().remove(oldDevice);
            webAuthNCredential.credentials().add(patchedDevice);
            this.credentialsService.updateCredential(userId, webAuthNCredential);
        }

    }

    private WebAuthNCredential getWebAuthNCredential(final long userId) {
        final UserCredentialTable credential = this.credentialsService.getCredential(userId, CredentialType.WEBAUTHN);
        if (credential == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No credential");
        }

        final WebAuthNCredential webAuthNCredential = credential.getCredential().get(WebAuthNCredential.class);
        if (webAuthNCredential == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Malformed credential");
        }

        return webAuthNCredential;
    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(final String username) {
        final UserTable userTable = this.userService.getUserTable(username);
        if (userTable == null) {
            return Set.of();
        }
        final UserCredentialTable credential = this.credentialsService.getCredential(userTable.getUserId(), CredentialType.WEBAUTHN);
        if (credential == null) {
            return Set.of();
        }

        final WebAuthNCredential webAuthNCredential = credential.getCredential().get(WebAuthNCredential.class);
        if (webAuthNCredential == null) {
            return Set.of();
        }

        return webAuthNCredential.credentials().stream()
            .map((device) ->  PublicKeyCredentialDescriptor.builder().id(ByteArray.fromBase64(device.id())).build())
            .collect(Collectors.toSet());
    }

    @Override
    public Optional<String> getUsernameForUserHandle(final ByteArray userHandle) {
        final UserCredentialTable userCredentialTable = this.credentialsService.getCredentialByUserHandle(userHandle);
        if (userCredentialTable == null) {
            return Optional.empty();
        }
        final UserTable userTable = this.userService.getUserTable(userCredentialTable.getUserId());
        if (userTable == null) {
            return Optional.empty();
        }

        return Optional.of(userTable.getName());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(final String username) {
        final UserTable userTable = this.userService.getUserTable(username);
        if (userTable == null) {
            return Optional.empty();
        }
        final UserCredentialTable credential = this.credentialsService.getCredential(userTable.getUserId(), CredentialType.WEBAUTHN);
        if (credential == null) {
            return Optional.empty();
        }

        final WebAuthNCredential webAuthNCredential = credential.getCredential().get(WebAuthNCredential.class);
        if (webAuthNCredential == null) {
            return Optional.empty();
        }

        return Optional.of(ByteArray.fromBase64(webAuthNCredential.userHandle()));
    }

    @Override
    public Optional<RegisteredCredential> lookup(final ByteArray credentialId, final ByteArray userHandle) {
        final UserCredentialTable userCredentialTable = this.credentialsService.getCredentialByUserHandle(userHandle);
        if (userCredentialTable == null) {
            return Optional.empty();
        }

        final WebAuthNCredential webAuthNCredential = userCredentialTable.getCredential().get(WebAuthNCredential.class);
        if (webAuthNCredential == null) {
            return Optional.empty();
        }

        final var maybe = webAuthNCredential.credentials().stream().filter(c -> c.id().equals(credentialId.getBase64())).findAny();
        return maybe.map(
            registration ->
                RegisteredCredential.builder()
                    .credentialId(ByteArray.fromBase64(registration.id()))
                    .userHandle(ByteArray.fromBase64(webAuthNCredential.userHandle()))
                    .publicKeyCose(ByteArray.fromBase64(registration.publicKey()))
                    .signatureCount(registration.authenticator().signCount())
                    .build());
    }

    @Override
    public Set<RegisteredCredential> lookupAll(final ByteArray credentialId) {
        return Set.of(); // we dont care about this
    }
}
