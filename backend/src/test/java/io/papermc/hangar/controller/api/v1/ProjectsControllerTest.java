package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.controller.api.v1.helper.ControllerTest;
import io.papermc.hangar.controller.api.v1.helper.TestData;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.internal.api.requests.CreateAPIKeyForm;
import java.util.Set;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProjectsControllerTest extends ControllerTest {

    @Test
    void testGetProject() throws Exception {
        this.mockMvc.perform(get("/api/v1/projects/TestProject")
                        .with(this.apiKey(TestData.KEY_ADMIN)))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.name", is("TestProject")))
                .andExpect(jsonPath("$.namespace.owner", is("PaperMC")));
    }

    @Test
    void testGetHiddenProject() throws Exception {
        this.mockMvc.perform(get("/api/v1/projects/TestProject")
                        .with(this.apiKey(TestData.KEY_PROJECT_ONLY)))
                .andExpect(status().is(404));
    }

    @Test
    void testGetMembers() throws Exception {
        this.mockMvc.perform(get("/api/v1/projects/TestProject/members")
                        .with(this.apiKey(TestData.KEY_ADMIN)))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.pagination.count", is(1)))
                .andExpect(jsonPath("$.result[0].user", is("PaperMC")))
                .andExpect(jsonPath("$.result[0].roles[0].title", is("Owner")));
    }

    @Test
    @Disabled
    void testGetStats() throws Exception {
        // TODO
        throw new RuntimeException();
    }

    @Test
    @Disabled
    void testGetStargazers() throws Exception {
        // TODO
        throw new RuntimeException();
    }

    @Test
    @Disabled
    void testGetWatchers() throws Exception {
        // TODO
        throw new RuntimeException();
    }

    @Test
    void testGetProjectsByAuthor() throws Exception {
        this.mockMvc.perform(get("/api/v1/projects?owner=PaperMC")
                        .with(this.apiKey(TestData.KEY_ADMIN)))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.pagination.count", is(1)))
                .andExpect(jsonPath("$.result[0].name", is("TestProject")))
                .andExpect(jsonPath("$.result[0].namespace.owner", is("PaperMC")));
    }

    @Test
    void testGetProjectsByQuery() throws Exception {
        this.mockMvc.perform(get("/api/v1/projects?q=Test")
                        .with(this.apiKey(TestData.KEY_ADMIN)))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.pagination.count", is(1)))
                .andExpect(jsonPath("$.result[0].name", is("TestProject")))
                .andExpect(jsonPath("$.result[0].namespace.owner", is("PaperMC")));
    }
}
