package io.papermc.hangar.controller.api.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import io.papermc.hangar.model.api.auth.ApiSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class PermissionsControllerTest {

    private static final String all = "a02f13bb-4329-4c85-984a-a817daacedcd.a240400b-bde2-4f4b-a432-ad7f9fb3ee0b";
    private static final String seeHidden = "bcbca881-87ba-4136-880d-4b387cb6cf03.14424bbc-9f4c-460e-ae13-cb4c4bae76d2";
    private static final String projectOnly = "b28dbeee-e1b6-44db-aa0d-a641208517ea.71c46fb3-47fc-4a12-8421-a121649fcb1d";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private String getJwt(String apiKey) throws Exception {
        String response = this.mockMvc.perform(post("/api/v1/authenticate?apiKey=" + apiKey)).andReturn().getResponse().getContentAsString();
        ApiSession apiSession = objectMapper.readValue(response, ApiSession.class);
        return apiSession.getToken();
    }

    @Test
    void testHasAllWithProjectOnly() throws Exception {
        this.mockMvc.perform(get("/api/v1/permissions/hasAll?permissions=create_organization&permissions=create_project")
            .header("Authorization", "HangarAuth " + getJwt(projectOnly)))
            .andExpect(jsonPath("$.result").value(false));
    }

    @Test
    void testHasAllWithAll() throws Exception {
        this.mockMvc.perform(get("/api/v1/permissions/hasAll?permissions=create_organization&permissions=create_project")
                .header("Authorization", "HangarAuth " + getJwt(all)))
            .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    void testHasAnyWithProjectOnly() throws Exception {
        this.mockMvc.perform(get("/api/v1/permissions/hasAny?permissions=create_organization&permissions=create_project")
                .header("Authorization", "HangarAuth " + getJwt(projectOnly)))
            .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    void testHasAnyWithAll() throws Exception {
        this.mockMvc.perform(get("/api/v1/permissions/hasAny?permissions=create_organization&permissions=create_project")
                .header("Authorization", "HangarAuth " + getJwt(all)))
            .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    void testHiddenProjectSeeHidden() throws Exception {
        this.mockMvc.perform(get("/api/v1/permissions/?author=paper&slug=Test")
                .header("Authorization", "HangarAuth " + getJwt(seeHidden)))
            .andExpect(jsonPath("$.permissionBinString").value("10000000000000000000000000"));
    }

    @Test
    void testHiddenProjectProjectOnly() throws Exception {
        this.mockMvc.perform(get("/api/v1/permissions/?author=paper&slug=Test")
                .header("Authorization", "HangarAuth " + getJwt(projectOnly)))
            .andExpect(jsonPath("$.permissionBinString").value("100000000"));
    }
}
