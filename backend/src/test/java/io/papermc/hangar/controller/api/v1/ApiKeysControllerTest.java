package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.controller.helper.ControllerTest;
import io.papermc.hangar.controller.helper.TestData;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.internal.api.requests.CreateAPIKeyForm;
import io.papermc.hangar.model.internal.api.requests.StringContent;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ApiKeysControllerTest extends ControllerTest {

    @Test
    void testCreateGetDeleteKey() throws Exception {
        // create
        final String newKey = this.mockMvc.perform(post("/api/v1/keys")
                .with(this.apiKey(TestData.KEY_ADMIN))
                .header("Content-Type", "application/json")
                .content(this.objectMapper.writeValueAsBytes(new CreateAPIKeyForm("cool_key", Set.of(NamedPermission.CREATE_PROJECT, NamedPermission.CREATE_ORGANIZATION), null, null))))
            .andExpect(status().is(201))
            .andReturn().getResponse().getContentAsString();
        final String identifier = newKey.split("\\.")[0];

        // get to make sure create worked
        this.mockMvc.perform(get("/api/v1/keys").with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$[*].name").value(hasItem("cool_key")))
            .andExpect(jsonPath("$[*].tokenIdentifier").value(hasItem(identifier)));

        // delete
        this.mockMvc.perform(delete("/api/v1/keys?name=cool_key").with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(204));

        // get again to make sure delete worked
        this.mockMvc.perform(get("/api/v1/keys").with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$[*].name").value(not(hasItem("cool_key"))))
            .andExpect(jsonPath("$[*].tokenIdentifier").value(not(hasItem(identifier))));
    }

    @Test
    void testCreateProjectScopedKey() throws Exception {
        this.mockMvc.perform(post("/api/v1/keys")
                .with(this.apiKey(TestData.KEY_ADMIN))
                .header("Content-Type", "application/json")
                .content(this.objectMapper.writeValueAsBytes(new CreateAPIKeyForm("scoped_key", Set.of(NamedPermission.EDIT_PAGE), Set.of(TestData.PROJECT.getSlug()), null))))
            .andExpect(status().is(201));

        this.mockMvc.perform(get("/api/v1/keys").with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$[?(@.name == 'scoped_key')].projectScoped").value(hasItem(true)))
            .andExpect(jsonPath("$[?(@.name == 'scoped_key')].projects[*].slug").value(hasItem(TestData.PROJECT.getSlug())));

        this.mockMvc.perform(delete("/api/v1/keys?name=scoped_key").with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(204));
    }

    @Test
    void testCreateKeyWithUnknownProject() throws Exception {
        this.mockMvc.perform(post("/api/v1/keys")
                .with(this.apiKey(TestData.KEY_ADMIN))
                .header("Content-Type", "application/json")
                .content(this.objectMapper.writeValueAsBytes(new CreateAPIKeyForm("bad_scope_key", Set.of(NamedPermission.EDIT_PAGE), Set.of("NotAProject"), null))))
            .andExpect(status().is(400));
    }

    @Test
    void testCreateKeyWithExpiration() throws Exception {
        final OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(30).truncatedTo(ChronoUnit.SECONDS);
        this.mockMvc.perform(post("/api/v1/keys")
                .with(this.apiKey(TestData.KEY_ADMIN))
                .header("Content-Type", "application/json")
                .content(this.objectMapper.writeValueAsBytes(new CreateAPIKeyForm("expiring_key", Set.of(NamedPermission.EDIT_PAGE), null, expiresAt))))
            .andExpect(status().is(201));

        this.mockMvc.perform(get("/api/v1/keys").with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$[?(@.name == 'expiring_key')].expiresAt").value(hasSize(1)));

        this.mockMvc.perform(delete("/api/v1/keys?name=expiring_key").with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(204));
    }

    @Test
    void testCreateKeyWithExpirationInThePast() throws Exception {
        this.mockMvc.perform(post("/api/v1/keys")
                .with(this.apiKey(TestData.KEY_ADMIN))
                .header("Content-Type", "application/json")
                .content(this.objectMapper.writeValueAsBytes(new CreateAPIKeyForm("expired_key", Set.of(NamedPermission.EDIT_PAGE), null, OffsetDateTime.now().minusDays(1)))))
            .andExpect(status().is(400));
    }

    @Test
    void testScopedKeyIsLimitedToItsProjects() throws Exception {
        // in scope
        this.mockMvc.perform(patch("/api/v1/pages/editmain/" + TestData.PRIVATE_PROJECT.getSlug())
                .content(this.objectMapper.writeValueAsBytes(new StringContent("# PrivateProject\nEdited")))
                .contentType(MediaType.APPLICATION_JSON)
                .with(this.apiKey(TestData.KEY_ADMIN_SCOPED)))
            .andExpect(status().is(200));

        // out of scope, even though the same user may edit it with an unscoped key
        this.mockMvc.perform(patch("/api/v1/pages/editmain/" + TestData.PROJECT.getSlug())
                .content(this.objectMapper.writeValueAsBytes(new StringContent("# Test\nOut of scope")))
                .contentType(MediaType.APPLICATION_JSON)
                .with(this.apiKey(TestData.KEY_ADMIN_SCOPED)))
            .andExpect(status().is(404));
    }

    // Authorization tests for @PermissionRequired annotation
    @Test
    void testCreateKeyWithoutPermission() throws Exception {
        // User without EDIT_API_KEYS permission should be denied
        this.mockMvc.perform(post("/api/v1/keys")
                .with(this.apiKey(TestData.KEY_NO_PERMISSIONS))
                .header("Content-Type", "application/json")
                .content(this.objectMapper.writeValueAsBytes(new CreateAPIKeyForm("test_key", Set.of(NamedPermission.CREATE_PROJECT), null, null))))
            .andExpect(status().is(404));
    }

    @Test
    void testGetKeysWithoutPermission() throws Exception {
        // User without EDIT_API_KEYS permission should be denied
        this.mockMvc.perform(get("/api/v1/keys")
                .with(this.apiKey(TestData.KEY_NO_PERMISSIONS)))
            .andExpect(status().is(404));
    }

    @Test
    void testDeleteKeyWithoutPermission() throws Exception {
        // User without EDIT_API_KEYS permission should be denied
        this.mockMvc.perform(delete("/api/v1/keys?name=test")
                .with(this.apiKey(TestData.KEY_NO_PERMISSIONS)))
            .andExpect(status().is(404));
    }

    @Test
    void testCreateKeyWithoutAuth() throws Exception {
        // Unauthenticated user should be denied
        this.mockMvc.perform(post("/api/v1/keys")
                .header("Content-Type", "application/json")
                .content(this.objectMapper.writeValueAsBytes(new CreateAPIKeyForm("test_key", Set.of(NamedPermission.CREATE_PROJECT), null, null))))
            .andExpect(status().is(403));
    }

    @Test
    void testGetKeysWithoutAuth() throws Exception {
        // Unauthenticated user should be denied
        this.mockMvc.perform(get("/api/v1/keys"))
            .andExpect(status().is(404));
    }

    @Test
    void testDeleteKeyWithoutAuth() throws Exception {
        // Unauthenticated user should be denied
        this.mockMvc.perform(delete("/api/v1/keys?name=test"))
            .andExpect(status().is(403));
    }

    // Authorization tests for @Unlocked annotation
    @Test
    void testCreateKeyWithLockedUser() throws Exception {
        // Locked/banned user should be denied by @Unlocked annotation
        this.mockMvc.perform(post("/api/v1/keys")
                .with(this.apiKey(TestData.KEY_BANNED))
                .header("Content-Type", "application/json")
                .content(this.objectMapper.writeValueAsBytes(new CreateAPIKeyForm("test_key", Set.of(NamedPermission.CREATE_PROJECT), null, null))))
            .andExpect(status().is(401));
    }
}
