package io.papermc.hangar.controller.api.v1;

import io.papermc.hangar.controller.api.v1.helper.ControllerTest;
import io.papermc.hangar.controller.api.v1.helper.TestData;
import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.internal.api.requests.CreateAPIKeyForm;
import java.util.Set;
import org.junit.jupiter.api.Test;

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
                .content(this.objectMapper.writeValueAsBytes(new CreateAPIKeyForm("cool_key", Set.of(NamedPermission.CREATE_PROJECT, NamedPermission.CREATE_ORGANIZATION)))))
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
}
