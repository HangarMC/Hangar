package io.papermc.hangar.controller.internal;

import io.papermc.hangar.controller.helper.ControllerTest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class BackendDataControllerTest extends ControllerTest {

    @Test
    void testBackendData() throws Exception {
        Map<String, Object> stuff = new LinkedHashMap<>();
        stuff.put("categories", doGet("/api/internal/data/categories"));
        stuff.put("permissions", doGet("/api/internal/data/permissions"));
        stuff.put("platforms", doGet("/api/internal/data/platforms"));
        stuff.put("validations", doGet("/api/internal/data/validations"));
        stuff.put("prompts", doGet("/api/internal/data/prompts"));
        stuff.put("announcements", doGet("/api/internal/data/announcements"));
        stuff.put("visibilities", doGet("/api/internal/data/visibilities"));
        stuff.put("licenses", doGet("/api/internal/data/licenses"));
        stuff.put("orgRoles", doGet("/api/internal/data/orgRoles"));
        stuff.put("projectRoles", doGet("/api/internal/data/projectRoles"));
        stuff.put("globalRoles", doGet("/api/internal/data/globalRoles"));
        stuff.put("channelColors", doGet("/api/internal/data/channelColors"));
        stuff.put("flagReasons", doGet("/api/internal/data/flagReasons"));
        stuff.put("loggedActions", doGet("/api/internal/data/loggedActions"));
        stuff.put("security", doGet("/api/internal/data/security"));

        @SuppressWarnings("unchecked")
        Map<String, Object> expected = objectMapper.readValue(getClass().getResourceAsStream("backendData.json"), Map.class);

        // compare each method individually
        for (final String key : expected.keySet()) {
            assertEquals(expected.get(key), stuff.get(key));
        }

        // System.out.println(objectMapper.writeValueAsString(stuff));
    }

    private Object doGet(final String url) throws Exception {
        String content = this.mockMvc.perform(get(url)).andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(content, Object.class);
    }
}
