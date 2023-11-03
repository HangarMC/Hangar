package io.papermc.hangar.components.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import graphql.schema.idl.SchemaGenerator;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

class QueryMergerTest {

    private static QueryMerger merger;

    @BeforeAll
    static void setup() {
        try (final InputStream resourceAsStream = QueryMergerTest.class.getClassLoader().getResourceAsStream("graphql/schema.graphqls")) {
            assert resourceAsStream != null;
            merger = new QueryMerger(SchemaGenerator.createdMockedSchema(new String(resourceAsStream.readAllBytes())));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

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

        compare(expected, merger.merge(input));
    }

    @Test
    void mergeHomepage() throws JsonProcessingException {
        final List<Map<String, String>> input = new ArrayList<>();
        input.add(Map.of(
            "projects_name", "Test2",
            "projects_homepage_name", "Resource Page"
        ));
        final Map<String, Object> expected = Map.of(
            "projects", List.of(
                Map.of(
                    "name", "Test2",
                    "homepage", Map.of(
                        "name", "Resource Page"
                    )
                )
            )
        );

        compare(expected, merger.merge(input));
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

        compare(expected, merger.merge(input));
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

        compare(expected, merger.merge(input));
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

        compare(expected, merger.merge(input));
    }

    // TODO solve these by always adding PKs to query on join
    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
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

        compare(expected, merger.merge(input));
    }

    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
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

        compare(expected, merger.merge(input));
    }

    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    void mergeNoPrimaryKey3() throws JsonProcessingException {
        final List<Map<String, String>> input = new ArrayList<>();
        input.add(Map.of("projects_name", "Test", "projects_owner_email", "Dum", "projects_owner_id", "1"));
        input.add(Map.of("projects_name", "Test2", "projects_owner_email", "Dum", "projects_owner_id", "1"));

        final Map<String, Object> expected = Map.of(
            "projects", List.of(
                Map.of("name", "Test", "owner", Map.of("email", "Dum", "id", "1")),
                Map.of("name", "Test2", "owner", Map.of("email", "Dum", "id", "1"))
            )
        );

        compare(expected, merger.merge(input));
    }

    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    void mergeEmptyProjects() throws JsonProcessingException {
        final List<Map<String, String>> input = new ArrayList<>();
        final Map<String, String> thing = new HashMap<>();
        thing.put("users_name", "JarScanner");
        thing.put("users_projects_name", null);
        thing.put("users_projects_stats_stars", null);
        input.add(thing);

        final Map<String, Object> expected = Map.of(
            "users", List.of(Map.of(
                "projects", List.of(),
                "name", "JarScanner"
            )));

        compare(expected, merger.merge(input));
    }

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

        compare(expected, merger.merge(input));
    }

    @Test
    void mergeList() throws JsonProcessingException {
        final List<Map<String, String>> input = new ArrayList<>();
        input.add(Map.of(
            "users_name", "MiniDigger"
        ));
        input.add(Map.of(
            "users_name", "JarScanner"
        ));

        final Map<String, Object> expected = Map.of(
            "users", List.of(
                Map.of("name", "MiniDigger"),
                Map.of("name", "JarScanner")
            )
        );

        compare(expected, merger.merge(input));
    }

    @Test
    void mergeScalarLeftJoin() throws JsonProcessingException {
        final List<Map<String, String>> input = new ArrayList<>();
        final Map<String, String> dum = new HashMap<>();
        dum.put("users_projects_name", null);
        dum.put("users_name", "JarScanner");
        dum.put("users_id", "1");
        input.add(dum);
        input.add(Map.of(
            "users_projects_name", "Test",
            "users_name", "MiniDigger",
            "users_id", "2"
        ));
        input.add(Map.of(
            "users_projects_name", "Test2",
            "users_name", "MiniDigger",
            "users_id", "2"
        ));

        final Map<String, Object> expected = Map.of(
            "users", List.of(
                Map.of("name", "MiniDigger", "id", "2", "projects", List.of(Map.of("name", "Test"), Map.of("name", "Test2"))),
                Map.of("name", "JarScanner", "id", "1", "projects", List.of())
            )
        );

        compare(expected, merger.merge(input));
    }

    private static void compare(final Map<String, Object> expected, final Map<String, Object> output) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

        final ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValueAsString(expected);

        assertEquals(objectWriter.writeValueAsString(expected), objectWriter.writeValueAsString(output));
    }
}
