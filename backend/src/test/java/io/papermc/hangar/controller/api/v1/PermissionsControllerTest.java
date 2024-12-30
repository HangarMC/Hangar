package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.controller.helper.ControllerTest;
import io.papermc.hangar.controller.helper.TestData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class PermissionsControllerTest extends ControllerTest {

    @Test
    void testHasAllWithProjectOnly() throws Exception {
        this.mockMvc.perform(get("/api/v1/permissions/hasAll?permissions=create_organization&permissions=create_project")
                .with(this.apiKey(TestData.KEY_PROJECT_ONLY)))
            .andExpect(jsonPath("$.result").value(false));
    }

    @Test
    @Disabled //TODO fix wtf is going on here
    void testHasAllWithAll() throws Exception {
        this.mockMvc.perform(get("/api/v1/permissions/hasAll?permissions=create_organization&permissions=create_project")
                .with(this.apiKey(TestData.KEY_ALL)))
            .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    @Disabled //TODO fix wtf is going on here
    void testHasAnyWithProjectOnly() throws Exception {
        this.mockMvc.perform(get("/api/v1/permissions/hasAny?permissions=create_organization&permissions=create_project")
                .with(this.apiKey(TestData.KEY_PROJECT_ONLY)))
            .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    @Disabled //TODO fix wtf is going on here
    void testHasAnyWithAll() throws Exception {
        this.mockMvc.perform(get("/api/v1/permissions/hasAny?permissions=create_organization&permissions=create_project")
                .with(this.apiKey(TestData.KEY_ALL)))
            .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    @Disabled //TODO fix wtf is going on here
    void testHiddenProjectSeeHidden() throws Exception {
        this.mockMvc.perform(get("/api/v1/permissions?slug=" + TestData.PROJECT.getSlug())
                .with(this.apiKey(TestData.KEY_SEE_HIDDEN)))
            .andExpect(jsonPath("$.permissionBinString").value("10000000000000000000000000"));
    }

    @Test
    @Disabled //TODO fix wtf is going on here
    void testHiddenProjectProjectOnly() throws Exception {
        this.mockMvc.perform(get("/api/v1/permissions?slug=" + TestData.PROJECT.getSlug())
                .with(this.apiKey(TestData.KEY_PROJECT_ONLY)))
            .andExpect(jsonPath("$.permissionBinString").value("100000000"));
    }

    @Test
    @Disabled //TODO fix wtf is going on here
    void testHiddenProjectOrganizationOnly() throws Exception {
        this.mockMvc.perform(get("/api/v1/permissions?organization=" + TestData.ORG.getName())
                .with(this.apiKey(TestData.KEY_PROJECT_ONLY)))
            .andExpect(jsonPath("$.permissionBinString").value("100000000"));
    }
}
