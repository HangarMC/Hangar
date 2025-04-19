package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.controller.helper.ControllerTest;
import io.papermc.hangar.controller.helper.TestData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VersionsControllerTest extends ControllerTest {

    @Test
    @Disabled
    void testUpload() throws Exception {
        // first check
        this.mockMvc.perform(get("/api/v1/projects/TestProject/versions")
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.pagination.count", is(2)))
            .andExpect(jsonPath("$.result[0].name", is("1.0")))
            .andExpect(jsonPath("$.result[1].name", is("2.0")));

        // TODO
        // then upload
        this.mockMvc.perform(post("/api/v1/projects/TestProject/upload")
                .with(this.apiKey(TestData.KEY_ADMIN))
                .header("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().is(201))
            .andExpect(jsonPath("$.url", is("")));

        // then check again
        this.mockMvc.perform(get("/api/v1/projects/TestProject/versions")
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.pagination.count", is(3)))
            .andExpect(jsonPath("$.result[0].name", is("1.0")))
            .andExpect(jsonPath("$.result[1].name", is("2.0")))
            .andExpect(jsonPath("$.result[2].name", is("3.0")));
    }

    @Test
    void testGetVersion() throws Exception {
        this.mockMvc.perform(get("/api/v1/projects/TestProject/versions/1.0")
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.id", is((int) TestData.VERSION.getId())))
            .andExpect(jsonPath("$.name", is("1.0")));
    }

    @Test
    void testGetVersionById() throws Exception {
        this.mockMvc.perform(get("/api/v1/projects/TestProject/versions/" + TestData.VERSION.getId())
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.id", is((int) TestData.VERSION.getId())))
            .andExpect(jsonPath("$.name", is("1.0")));
    }

    @Test
    void testGetVersionByIdHidden() throws Exception {
        this.mockMvc.perform(get("/api/v1/projects/TestProject/versions/" + TestData.VERSION_HIDDEN.getId())
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.id", is((int) TestData.VERSION_HIDDEN.getId() )))
            .andExpect(jsonPath("$.name", is("2.0")));
    }

    @Test
    void testGetVersionByIdHidden2() throws Exception {
        this.mockMvc.perform(get("/api/v1/projects/TestProject/versions/" + TestData.VERSION_HIDDEN.getId()))
            .andExpect(status().is(404));
    }

    @Test
    void testGetVersionById2() throws Exception {
        this.mockMvc.perform(get("/api/v1/versions/" + TestData.VERSION.getId())
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.id", is((int) TestData.VERSION.getId())))
            .andExpect(jsonPath("$.name", is("1.0")));
    }

    @Test
    void testGetVersions() throws Exception {
        this.mockMvc.perform(get("/api/v1/projects/PrivateProject/versions")
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.pagination.count", is(0)));
    }

    @Test
    void testGetVersionsHiddenProject() throws Exception {
        this.mockMvc.perform(get("/api/v1/projects/PrivateProject/versions"))
            .andExpect(status().is(404));
    }

    @Test
    void testGetVersionsHiddenVersion() throws Exception {
        this.mockMvc.perform(get("/api/v1/projects/TestProject/versions"))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.pagination.count", is(1)))
            .andExpect(jsonPath("$.result[0].name", is("1.0")));
    }

    @Test
    void testGetLatestRelease() throws Exception {
        this.mockMvc.perform(get("/api/v1/projects/TestProject/latestrelease")
                .with(this.apiKey(TestData.KEY_ADMIN)))
            .andExpect(status().is(200))
            .andExpect(content().string("1.0"));
    }

    @Test
    @Disabled
    void testGetStats() throws Exception {
        // TODO testGetStats
        throw new RuntimeException();
    }

    @Test
    @Disabled
    void testDownload() throws Exception {
        // TODO testDownload
        throw new RuntimeException();
    }
}
