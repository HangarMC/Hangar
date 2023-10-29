package io.papermc.hangar.components.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QueryMergerTest {

    @Test
    void mergeOne() throws JsonProcessingException {
        final List<Map<String, String>> input = new ArrayList<>();
        input.add(Map.of(
            "projects_name", "Test2",
            "projects_dum", "dum",
            "projects_pages_name", "Resource Page",
            "projects_homepage_name", "Resource Page",
            "projects_pages_contents", "# Test2 Welcome to your new project!",
            "projects_homepage_contents", "# Test2 Welcome to your new project!"
        ));
        final Map<String, Object> expected = Map.of(
            "projects", List.of(
                Map.of(
                    "dum", "dum",
                    "name", "Test2",
                    "pages", List.of(
                        Map.of("name", "Resource Page", "contents", "# Test2 Welcome to your new project!")
                    ),
                    "homepage", Map.of(
                        "name", "Resource Page",
                        "contents", "# Test2 Welcome to your new project!"
                    )
                )
            )
        );

        compare(expected, QueryMerger.merge(input));
    }

    @Test
    void mergeTwoChild() throws JsonProcessingException {
        final List<Map<String, String>> input = new ArrayList<>();
        input.add(Map.of(
            "projects_name", "Test",
            "projects_pages_name", "Test",
            "projects_homepage_name", "Resource Page",
            "projects_pages_contents", "# Test Welcome to your new page",
            "projects_homepage_contents", "# Test Welcome to your new project!"
        ));
        input.add(Map.of(
            "projects_name", "Test",
            "projects_pages_name", "Resource Page",
            "projects_homepage_name", "Resource Page",
            "projects_pages_contents", "# Test Welcome to your new project!",
            "projects_homepage_contents", "# Test Welcome to your new project!"
        ));

        final Map<String, Object> expected = Map.of(
            "projects", List.of(
                Map.of(
                    "name", "Test",
                    "pages", List.of(
                        Map.of("name", "Test", "contents", "# Test Welcome to your new page"),
                        Map.of("name", "Resource Page", "contents", "# Test Welcome to your new project!")
                    ),
                    "homepage", Map.of(
                        "name", "Resource Page",
                        "contents", "# Test Welcome to your new project!"
                    )
                )
            )
        );

        compare(expected, QueryMerger.merge(input));
    }

    @Test
    void mergeTwoParent() throws JsonProcessingException {
        final List<Map<String, String>> input = new ArrayList<>();
        input.add(Map.of(
            "projects_name", "Test",
            "projects_pages_name", "Test",
            "projects_homepage_name", "Resource Page",
            "projects_pages_contents", "# Test Welcome to your new page",
            "projects_homepage_contents", "# Test Welcome to your new project!"
        ));
        input.add(Map.of(
            "projects_name", "Test",
            "projects_pages_name", "Resource Page",
            "projects_homepage_name", "Resource Page",
            "projects_pages_contents", "# Test Welcome to your new project!",
            "projects_homepage_contents", "# Test Welcome to your new project!"
        ));
        input.add(Map.of(
            "projects_name", "Test2",
            "projects_pages_name", "Resource Page",
            "projects_homepage_name", "Resource Page",
            "projects_pages_contents", "# Test2 Welcome to your new project!",
            "projects_homepage_contents", "# Test2 Welcome to your new project!"
        ));

        final Map<String, Object> expected = Map.of(
            "projects", List.of(
                Map.of(
                    "name", "Test",
                    "pages", List.of(
                        Map.of("name", "Test", "contents", "# Test Welcome to your new page"),
                        Map.of("name", "Resource Page", "contents", "# Test Welcome to your new project!")
                    ),
                    "homepage", Map.of(
                        "name", "Resource Page",
                        "contents", "# Test Welcome to your new project!"
                    )
                ),
                Map.of(
                    "name", "Test2",
                    "pages", List.of(
                        Map.of("name", "Resource Page", "contents", "# Test2 Welcome to your new project!")
                    ),
                    "homepage", Map.of(
                        "name", "Resource Page",
                        "contents", "# Test2 Welcome to your new project!"
                    )
                )
            )
        );

        compare(expected, QueryMerger.merge(input));
    }

    @Test
    void mergeNoPrimaryKeyNamespace() throws JsonProcessingException {
        final List<Map<String, String>> input = new ArrayList<>();
        input.add(Map.of(
            "projectbyslug_name", "Test",
            "projectbyslug_namespace_slug", "Test",
            "projectbyslug_namespace_owner", "MiniDigger"
        ));

        final Map<String, Object> expected = Map.of(
            "projectbyslug", Map.of(
                "name", "Test",
                "namespace", Map.of(
                    "owner", "MiniDigger",
                    "slug", "Test"
                )
            )
        );

        compare(expected, QueryMerger.merge(input));
    }

    // TODO solve these by always adding PKs to query on join
    @Test
    void mergeNoPrimaryKey() throws JsonProcessingException {
        final List<Map<String, String>> input = new ArrayList<>();
        input.add(Map.of("projects_id", "1"));
        input.add(Map.of("projects_id", "2"));

        final Map<String, Object> expected = Map.of(
            "projects", List.of(
                Map.of("id", "1"),
                Map.of("id", "2")
            )
        );

        compare(expected, QueryMerger.merge(input));
    }

    @Test
    void mergeNoPrimaryKey2() throws JsonProcessingException {
        final List<Map<String, String>> input = new ArrayList<>();
        input.add(Map.of("projects_id", "1", "projects_owner_name", "MiniDigger"));
        input.add(Map.of("projects_id", "2", "projects_owner_name", "MiniDigger"));

        final Map<String, Object> expected = Map.of(
            "projects", List.of(
                Map.of("id", "1", "owner", Map.of("name", "MiniDigger")),
                Map.of("id", "2", "owner", Map.of("name", "MiniDigger"))
            )
        );

        compare(expected, QueryMerger.merge(input));
    }

    // TODO idk whats going on here
    @Test
    void mergeDeep() throws JsonProcessingException {
        final List<Map<String, String>> input = new ArrayList<>();
        input.add(Map.of(
            "projects_name", "Test",
            "projects_owner_name", "MiniDigger",
            "projects_owner_projects_name", "Test"
        ));
        input.add(Map.of(
            "projects_name", "Test",
            "projects_owner_name", "MiniDigger",
            "projects_owner_projects_name", "Test2"
        ));
        input.add(Map.of(
            "projects_name", "Test2",
            "projects_owner_name", "MiniDigger",
            "projects_owner_projects_name", "Test"
        ));
        input.add(Map.of(
            "projects_name", "Test2",
            "projects_owner_name", "MiniDigger",
            "projects_owner_projects_name", "Test2"
        ));

        final Map<String, Object> expected = Map.of(
            "projects", List.of(
                Map.of("name", "Test", "owner", Map.of("name", "MiniDigger", "projects", List.of(
                    Map.of("name", "Test"),
                    Map.of("name", "Test2")
                ))),
                Map.of("name", "Test2", "owner", Map.of("name", "MiniDigger", "projects", List.of(
                    Map.of("name", "Test"),
                    Map.of("name", "Test2")
                )))
            )
        );

        compare(expected, QueryMerger.merge(input));
    }

    private static void compare(final Map<String, Object> expected, final Map<String, Object> output) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

        final ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValueAsString(expected);

        assertEquals(objectWriter.writeValueAsString(expected), objectWriter.writeValueAsString(output));
    }
}
