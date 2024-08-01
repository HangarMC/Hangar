package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.controller.api.v1.helper.ControllerTest;
import io.papermc.hangar.controller.api.v1.helper.TestData;
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
    void testGetUsers() throws Exception {
        this.mockMvc.perform(get("/api/v1/users?query=Test")
                        .with(this.apiKey(TestData.KEY_ADMIN)))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.pagination.count", is(3)))
                .andExpect(jsonPath("$.result[*].name", contains("TestUser", "TestMember", "TestAdmin")));
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
            .andExpect(jsonPath("$.pagination.count", is(1)));
    }

    @Test
    void testGetWatching() throws Exception {
        this.mockMvc.perform(get("/api/v1/users/TestUser/watching")
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.pagination.count", is(1)))
            .andExpect(jsonPath("$.result[*].name", contains("TestProject")));
    }

    @Test
    void testGetPinned() throws Exception {
        this.mockMvc.perform(get("/api/v1/users/TestUser/pinned")
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200));
    }

    @Test
    @Disabled // TODO fix this
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
