package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.controller.helper.ControllerTest;
import io.papermc.hangar.controller.helper.TestData;
import io.papermc.hangar.model.api.project.PageEditForm;
import io.papermc.hangar.model.internal.api.requests.StringContent;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PagesControllerTest extends ControllerTest {

    @Test
    void testGetMainPage() throws Exception {
        this.mockMvc.perform(get("/api/v1/pages/main/" + TestData.PROJECT.getSlug())
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(content().string(startsWith("# Test")));
    }

    @Test
    void testEditMainPage() throws Exception {
        // edit
        this.mockMvc.perform(patch("/api/v1/pages/editmain/" + TestData.PROJECT.getSlug())
                .content(this.objectMapper.writeValueAsBytes(new StringContent("# Test\nEdited")))
                .contentType(MediaType.APPLICATION_JSON)
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200));

        // validate
        this.mockMvc.perform(get("/api/v1/pages/main/" + TestData.PROJECT.getSlug())
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(content().string(containsString("Edited")));
    }

    @Test
    void testGetOtherPage() throws Exception {
        this.mockMvc.perform(get("/api/v1/pages/page/" + TestData.PROJECT.getSlug() + "?path=" + TestData.PAGE_PARENT.getSlug())
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(content().string(startsWith("# TestParentPage")));
    }

    @Test
    void testSlashes() throws Exception {
        this.mockMvc.perform(get("/api/v1/pages/page/" + TestData.PROJECT.getSlug() + "?path=/" + TestData.PAGE_PARENT.getSlug() + "/")
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(content().string(startsWith("# TestParentPage")));
    }

    @Test
    void testEditOtherPage() throws Exception {
        // edit
        this.mockMvc.perform(patch("/api/v1/pages/edit/" + TestData.PROJECT.getSlug())
                .content(this.objectMapper.writeValueAsBytes(new PageEditForm(TestData.PAGE_PARENT.getSlug(), "# TestParentPage\nEdited")))
                .contentType(MediaType.APPLICATION_JSON)
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200));

        // validate
        this.mockMvc.perform(get("/api/v1/pages/page/" + TestData.PROJECT.getSlug() + "?path=" + TestData.PAGE_PARENT.getSlug())
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(content().string(containsString("Edited")));
    }

    @Test
    void testGetChildPage() throws Exception {
        this.mockMvc.perform(get("/api/v1/pages/page/" + TestData.PROJECT.getSlug() + "?path=" + TestData.PAGE_CHILD.getSlug())
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(content().string(startsWith("# TestChildPage")));
    }

    @Test
    void testEditChildPage() throws Exception {
        // edit
        this.mockMvc.perform(patch("/api/v1/pages/edit/" + TestData.PROJECT.getSlug())
                .content(this.objectMapper.writeValueAsBytes(new PageEditForm(TestData.PAGE_CHILD.getSlug(), "# TestChildPage\nEdited")))
                .contentType(MediaType.APPLICATION_JSON)
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200));

        // validate
        this.mockMvc.perform(get("/api/v1/pages/page/" + TestData.PROJECT.getSlug() + "?path=" + TestData.PAGE_CHILD.getSlug())
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(content().string(containsString("Edited")));
    }

    @Test
    void testGetInvalidProject() throws Exception {
        this.mockMvc.perform(get("/api/v1/pages/main/Dum")
            .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(404));
    }

    @Test
    void testGetInvalidPage() throws Exception {
        this.mockMvc.perform(get("/api/v1/pages/page/" + TestData.PROJECT.getSlug() + "?path=Dum")
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(404));
    }

    @Test
    void testEditInvalidProject() throws Exception {
        this.mockMvc.perform(patch("/api/v1/pages/editmain/Dum")
                .content(this.objectMapper.writeValueAsBytes(new StringContent("# Dum")))
                .contentType(MediaType.APPLICATION_JSON)
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(404));
    }

    @Test
    void testEditInvalidPage() throws Exception {
        this.mockMvc.perform(patch("/api/v1/pages/edit/" + TestData.PROJECT.getSlug())
                .content(this.objectMapper.writeValueAsBytes(new PageEditForm(TestData.PAGE_PARENT.getSlug(), "# TestParentPage\nEdited")))
                .contentType(MediaType.APPLICATION_JSON)
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200));
    }

    // Authorization tests for @PermissionRequired annotation
    @Test
    void testGetMainPageWithoutPermission() throws Exception {
        // User without VIEW_PUBLIC_INFO permission should be denied
        this.mockMvc.perform(get("/api/v1/pages/main/" + TestData.PRIVATE_PROJECT.getSlug())
                .with(this.apiKey(TestData.KEY_NO_PERMISSIONS)))
            .andExpect(status().is(404));
    }

    @Test
    void testGetMainPageOfHiddenWithoutAuth() throws Exception {
        this.mockMvc.perform(get("/api/v1/pages/main/" + TestData.PRIVATE_PROJECT.getSlug()))
            .andExpect(status().is(404));
    }

    @Test
    void testEditMainPageWithoutPermission() throws Exception {
        // User without EDIT_PAGE permission should be denied
        this.mockMvc.perform(patch("/api/v1/pages/editmain/" + TestData.PROJECT.getSlug())
                .content(this.objectMapper.writeValueAsBytes(new StringContent("# Test\nUnauthorized")))
                .contentType(MediaType.APPLICATION_JSON)
                .with(this.apiKey(TestData.KEY_PROJECT_ONLY)))
            .andExpect(status().is(404));
    }

    @Test
    void testEditMainPageWithoutAuth() throws Exception {
        // Unauthenticated user should be denied
        this.mockMvc.perform(patch("/api/v1/pages/editmain/" + TestData.PROJECT.getSlug())
                .content(this.objectMapper.writeValueAsBytes(new StringContent("# Test\nUnauthorized")))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is(403));
    }

    @Test
    void testEditPageWithoutPermission() throws Exception {
        // User without EDIT_PAGE permission should be denied
        this.mockMvc.perform(patch("/api/v1/pages/edit/" + TestData.PROJECT.getSlug())
                .content(this.objectMapper.writeValueAsBytes(new PageEditForm(TestData.PAGE_PARENT.getSlug(), "# Unauthorized")))
                .contentType(MediaType.APPLICATION_JSON)
                .with(this.apiKey(TestData.KEY_PROJECT_ONLY)))
            .andExpect(status().is(404));
    }

    @Test
    void testEditPageWithoutAuth() throws Exception {
        // Unauthenticated user should be denied
        this.mockMvc.perform(patch("/api/v1/pages/edit/" + TestData.PROJECT.getSlug())
                .content(this.objectMapper.writeValueAsBytes(new PageEditForm(TestData.PAGE_PARENT.getSlug(), "# Unauthorized")))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is(403));
    }

    // Authorization tests for @Unlocked annotation
    @Test
    void testEditMainPageWithLockedUser() throws Exception {
        // Locked/banned user should be denied by @Unlocked annotation
        this.mockMvc.perform(patch("/api/v1/pages/editmain/" + TestData.PROJECT.getSlug())
                .content(this.objectMapper.writeValueAsBytes(new StringContent("# Locked user edit")))
                .contentType(MediaType.APPLICATION_JSON)
                .with(this.apiKey(TestData.KEY_BANNED)))
            .andExpect(status().is(404));
    }

    @Test
    void testEditPageWithLockedUser() throws Exception {
        // Locked/banned user should be denied by @Unlocked annotation
        this.mockMvc.perform(patch("/api/v1/pages/edit/" + TestData.PROJECT.getSlug())
                .content(this.objectMapper.writeValueAsBytes(new PageEditForm(TestData.PAGE_PARENT.getSlug(), "# Locked user edit")))
                .contentType(MediaType.APPLICATION_JSON)
                .with(this.apiKey(TestData.KEY_BANNED)))
            .andExpect(status().is(404));
    }
}
