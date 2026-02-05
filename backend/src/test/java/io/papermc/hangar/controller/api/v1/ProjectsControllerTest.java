package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.controller.helper.ControllerTest;
import io.papermc.hangar.controller.helper.TestData;
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
    void testGetProjectById() throws Exception {
        this.mockMvc.perform(get("/api/v1/projects/" + TestData.PROJECT.getProjectId())
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.name", is("TestProject")))
            .andExpect(jsonPath("$.namespace.owner", is("PaperMC")));
    }

    @Test
    void testGetHiddenProject() throws Exception {
        this.mockMvc.perform(get("/api/v1/projects/PrivateProject")
                .with(this.apiKey(TestData.KEY_PROJECT_ONLY)))
            .andExpect(status().is(404));
    }

    @Test
    void testGetHiddenProjectById() throws Exception {
        this.mockMvc.perform(get("/api/v1/projects/" + TestData.PRIVATE_PROJECT.getProjectId())
                .with(this.apiKey(TestData.KEY_PROJECT_ONLY)))
            .andExpect(status().is(404));
    }

    // Authorization tests for @VisibilityRequired annotation
    @Test
    void testGetHiddenProjectAsAdmin() throws Exception {
        // Admin should be able to see hidden projects
        this.mockMvc.perform(get("/api/v1/projects/PrivateProject")
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.name", is("PrivateProject")));
    }

    @Test
    void testGetHiddenProjectAsOwner() throws Exception {
        // Project owner should be able to see their own hidden project
        // Note: PaperMC org owns PrivateProject
        this.mockMvc.perform(get("/api/v1/projects/PrivateProject")
                .with(this.apiKey(TestData.KEY_ADMIN)))  // Admin is part of PaperMC org
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.name", is("PrivateProject")));
    }

    @Test
    void testGetHiddenProjectWithoutPermission() throws Exception {
        // Regular user without permission should get 404
        this.mockMvc.perform(get("/api/v1/projects/PrivateProject"))
            .andExpect(status().is(404));
    }

    @Test
    void testGetHiddenProjectByIdAsAdmin() throws Exception {
        // Admin should be able to see hidden projects by ID
        this.mockMvc.perform(get("/api/v1/projects/" + TestData.PRIVATE_PROJECT.getProjectId())
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.name", is("PrivateProject")));
    }

    @Test
    void testGetHiddenProjectByIdWithoutPermission() throws Exception {
        // Regular user without permission should get 404
        this.mockMvc.perform(get("/api/v1/projects/" + TestData.PRIVATE_PROJECT.getProjectId()))
            .andExpect(status().is(404));
    }

    // Authorization tests for @PermissionRequired annotation
    @Test
    void testGetProjectStatsWithPermission() throws Exception {
        // Admin with IS_SUBJECT_MEMBER permission should access stats
        this.mockMvc.perform(get("/api/v1/projects/TestProject/stats?fromDate=2020-01-01T00:00:00Z&toDate=2030-12-31T23:59:59Z")
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200));
    }

    @Test
    void testGetProjectStatsWithoutPermission() throws Exception {
        // User without IS_SUBJECT_MEMBER permission should be denied
        this.mockMvc.perform(get("/api/v1/projects/TestProject/stats?fromDate=2020-01-01T00:00:00Z&toDate=2030-12-31T23:59:59Z")
                .with(this.apiKey(TestData.KEY_PROJECT_ONLY)))
            .andExpect(status().is(403));
    }

    @Test
    void testGetProjectStatsWithoutAuth() throws Exception {
        // Unauthenticated user should be denied
        this.mockMvc.perform(get("/api/v1/projects/TestProject/stats?fromDate=2020-01-01T00:00:00Z&toDate=2030-12-31T23:59:59Z"))
            .andExpect(status().is(401));
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
    void testGetStargazers() throws Exception {
        this.mockMvc.perform(get("/api/v1/projects/TestProject/stargazers")
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.pagination.count", is(1)))
            .andExpect(jsonPath("$.result[0].name", is("TestUser")));
    }

    @Test
    void testGetWatchers() throws Exception {
        this.mockMvc.perform(get("/api/v1/projects/TestProject/watchers")
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.pagination.count", is(1)))
            .andExpect(jsonPath("$.result[0].name", is("TestUser")));
    }

    @Test
    void testGetProjectsByAuthor() throws Exception {
        this.mockMvc.perform(get("/api/v1/projects?owner=PaperMC")
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.pagination.count", is(2)))
            .andExpect(jsonPath("$.result[1].name", is("TestProject")))
            .andExpect(jsonPath("$.result[1].namespace.owner", is("PaperMC")))
            .andExpect(jsonPath("$.result[0].name", is("PrivateProject")))
            .andExpect(jsonPath("$.result[0].namespace.owner", is("PaperMC")))
            .andExpect(jsonPath("$.result[0].visibility", is("new")));
    }

    @Test
    void testGetProjectsByAuthorExcludingHidden() throws Exception {
        this.mockMvc.perform(get("/api/v1/projects?owner=PaperMC"))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.pagination.count", is(1)))
            .andExpect(jsonPath("$.result[0].name", is("TestProject")))
            .andExpect(jsonPath("$.result[0].namespace.owner", is("PaperMC")));
    }

    @Test
    void testGetProjectsByQuery() throws Exception {
        this.mockMvc.perform(get("/api/v1/projects?q=TestPr")
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.pagination.count", is(1)))
            .andExpect(jsonPath("$.result[0].name", is("TestProject")))
            .andExpect(jsonPath("$.result[0].namespace.owner", is("PaperMC")));
    }

    @Test
    @Disabled
    void testGetProjectFromVersionHash() throws Exception {
        // TODO
        this.mockMvc.perform(get("/api/v1/versions/hash/dum")
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.name", is("TestProject")));
    }
}
