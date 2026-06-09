package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.controller.helper.ControllerTest;
import io.papermc.hangar.controller.helper.TestData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest extends ControllerTest {

    @Test
    void testGetUserOrg() throws Exception {
        this.mockMvc.perform(get("/api/v1/users/" + TestData.ORG.getName())
                        .with(this.apiKey(TestData.KEY_ADMIN)))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.name", is(TestData.ORG.getName())))
                .andExpect(jsonPath("$.isOrganization", is(true)));
    }

    @Test
    void testGetUser() throws Exception {
        this.mockMvc.perform(get("/api/v1/users/" + TestData.USER_ADMIN.getName())
                        .with(this.apiKey(TestData.KEY_ADMIN)))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.name", is(TestData.USER_ADMIN.getName())))
                .andExpect(jsonPath("$.isOrganization", is(false)));
    }

    @Test
    void testGetInvalidUser() throws Exception {
        this.mockMvc.perform(get("/api/v1/users/Dum")
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(404));
    }

    @Test
    void testGetUserById() throws Exception {
        this.mockMvc.perform(get("/api/v1/users/" + TestData.USER_ADMIN.getId())
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.name", is(TestData.USER_ADMIN.getName())))
            .andExpect(jsonPath("$.isOrganization", is(false)));
    }

    @Test
    void testGetBannedUser() throws Exception {
        this.mockMvc.perform(get("/api/v1/users/" + TestData.USER_BANNED.getId())
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.name", is(TestData.USER_BANNED.getName())))
            .andExpect(jsonPath("$.isOrganization", is(false)));
    }

    @Test
    @Disabled // TODO should banned users be hidden?
    void testGetBannedUserHidden() throws Exception {
        this.mockMvc.perform(get("/api/v1/users/" + TestData.USER_BANNED.getId()))
            .andExpect(status().is(404));
    }

    @Test
    void testGetUsers() throws Exception {
        this.mockMvc.perform(get("/api/v1/users?query=Test")
                        .with(this.apiKey(TestData.KEY_ADMIN)))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.pagination.count", is(5)))
                .andExpect(jsonPath("$.result[*].name", contains("TestUser", "TestMember", "TestAdmin", TestData.USER_BANNED.getName(), "TestOwner")));
    }

    @Test
    void testGetStarred() throws Exception {
        this.mockMvc.perform(get("/api/v1/users/TestUser/starred")
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.pagination.count", is(1)))
            .andExpect(jsonPath("$.result[*].name", contains("TestProject")));
        this.mockMvc.perform(get("/api/v1/users/TestAdmin/starred")
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.pagination.count", is(0)));
    }

    @Test
    void testGetWatching() throws Exception {
        this.mockMvc.perform(get("/api/v1/users/TestUser/watching")
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.pagination.count", is(1)))
            .andExpect(jsonPath("$.result[*].name", contains("TestProject")));
        this.mockMvc.perform(get("/api/v1/users/TestAdmin/watching")
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.pagination.count", is(0)));
    }

    @Test
    void testGetPinned() throws Exception {
        this.mockMvc.perform(get("/api/v1/users/TestUser/pinned")
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200));
    }

    @Test
    void testGetAuthors() throws Exception {
        this.mockMvc.perform(get("/api/v1/authors?query=PaperMC")
                        .with(this.apiKey(TestData.KEY_ADMIN)))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.pagination.count", is(1)))
                .andExpect(jsonPath("$.result[*].name", contains("PaperMC")));
    }

    @Test
    void testGetStaff() throws Exception {
        this.mockMvc.perform(get("/api/v1/staff?query=Test")
                        .with(this.apiKey(TestData.KEY_ADMIN)))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.pagination.count", is(1)))
                .andExpect(jsonPath("$.result[*].name", contains("TestAdmin")));
    }
}
