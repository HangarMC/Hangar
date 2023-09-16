package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.controller.api.v1.helper.ControllerTest;
import io.papermc.hangar.controller.api.v1.helper.TestData;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.internal.api.requests.CreateAPIKeyForm;
import java.util.Set;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Disabled
class VersionsControllerTest extends ControllerTest {

    @Test
    void testUpload() throws Exception {
        // TODO
        this.mockMvc.perform(post("/api/v1/projects/TestProject/upload")
                        .with(this.apiKey(TestData.KEY_ADMIN))
                        .header("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.url", is("")));
    }

    @Test
    void testGetVersion() throws Exception {
        // TODO
        throw new RuntimeException();
    }

    @Test
    void testGetVersions() throws Exception {
        // TODO
        throw new RuntimeException();
    }

    @Test
    void testGetLatestRelease() throws Exception {
        // TODO
        throw new RuntimeException();
    }

    @Test
    void testGetLatestVersion() throws Exception {
        // TODO
        throw new RuntimeException();
    }

    @Test
    void testGetStats() throws Exception {
        // TODO
        throw new RuntimeException();
    }

    @Test
    void testDownload() throws Exception {
        // TODO
        throw new RuntimeException();
    }
}
