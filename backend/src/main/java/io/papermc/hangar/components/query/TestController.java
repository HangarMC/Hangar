package io.papermc.hangar.components.query;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import graphql.execution.instrumentation.parameters.InstrumentationFieldFetchParameters;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.PropertyDataFetcher;
import io.papermc.hangar.components.images.service.AvatarService;
import io.papermc.hangar.service.internal.file.FileService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.SqlStatements;
import org.jetbrains.annotations.NotNull;
import org.postgresql.jdbc.PgArray;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
@Controller
public class TestController {

    private static final Object EMPTY = new Object();

    private final Jdbi jdbi;
    private final FileService fileService;

    public TestController(final Jdbi jdbi, @Lazy final FileService fileService) {
        this.jdbi = jdbi;
        this.fileService = fileService;

        jdbi.getConfig(SqlStatements.class).setUnusedBindingAllowed(true);
    }

    // queries
    @QueryMapping
    public Object projectBySlug(final DataFetchingEnvironment environment) {
        final QueryBuilder queryBuilder = environment.getGraphQlContext().get("queryBuilder");
        queryBuilder.rootTable = "projects";
        queryBuilder.from = "FROM " + queryBuilder.rootTable;
        queryBuilder.condition = "WHERE slug = :projectBySlug_slug";
        return EMPTY;
    }

    @QueryMapping
    public Object projects(final DataFetchingEnvironment environment) {
        final QueryBuilder queryBuilder = environment.getGraphQlContext().get("queryBuilder");
        queryBuilder.rootTable = "projects";
        queryBuilder.from = "FROM " + queryBuilder.rootTable;
        queryBuilder.condition = "";
        return List.of(EMPTY);
    }

    @QueryMapping
    public Object users(final DataFetchingEnvironment environment) {
        final QueryBuilder queryBuilder = environment.getGraphQlContext().get("queryBuilder");
        queryBuilder.rootTable = "users";
        queryBuilder.from = "FROM " + queryBuilder.rootTable;
        queryBuilder.condition = "";
        return List.of(EMPTY);
    }

    // special schemas

    @SchemaMapping(typeName = "User", field = "projects")
    public Object userProjects(final DataFetchingEnvironment environment) {
        final QueryBuilder queryBuilder = environment.getGraphQlContext().get("queryBuilder");
        final String parentTable = this.getParentTable(environment.getExecutionStepInfo(), queryBuilder);
        final String parentAlias = this.getParentAlias(environment.getExecutionStepInfo(), queryBuilder);
        queryBuilder.joins.add(STR."JOIN projects \{parentAlias}projects ON \{parentAlias}projects.owner_id = \{parentTable}id");
        return List.of(EMPTY);
    }

    @SchemaMapping(typeName = "Project", field = "owner")
    public Object projectOwner(final DataFetchingEnvironment environment) {
        final QueryBuilder queryBuilder = environment.getGraphQlContext().get("queryBuilder");
        final String parentTable = this.getParentTable(environment.getExecutionStepInfo(), queryBuilder);
        final String parentAlias = this.getParentAlias(environment.getExecutionStepInfo(), queryBuilder);
        queryBuilder.joins.add(STR."JOIN users \{parentAlias}owner ON \{parentAlias}owner.id = \{parentTable}owner_id");
        return EMPTY;
    }

    @SchemaMapping(typeName = "Project", field = "avatarUrl")
    public Object projectAvatarUrl(final DataFetchingEnvironment environment) {
        final QueryBuilder queryBuilder = environment.getGraphQlContext().get("queryBuilder");
        final String parentAlias = this.getParentAlias(environment.getExecutionStepInfo(), queryBuilder);
        queryBuilder.fields.add("avatar.version AS ext_avatarversion");
        queryBuilder.fields.add("project.id AS ext_projectid");
        // TODO needs prefixes
        queryBuilder.joins.add("JOIN avatars avatar ON avatar.type = 'project' AND avatar.subject = project.id::varchar");
        queryBuilder.resolver.put(parentAlias + "avatarUrl", (r) -> this.fileService.getAvatarUrl(AvatarService.PROJECT, String.valueOf(r.get("ext_projectid")), (Integer) r.get("ext_avatarversion")));
        return EMPTY;
    }

    @SchemaMapping(typeName = "Project", field = "namespace")
    public Object projectNamespace(final DataFetchingEnvironment environment) {
        final QueryBuilder queryBuilder = environment.getGraphQlContext().get("queryBuilder");
        final String parentAlias = this.getParentAlias(environment.getExecutionStepInfo(), queryBuilder);
        final String parentTable = this.getParentTable(environment.getExecutionStepInfo(), queryBuilder);
        if (environment.getSelectionSet().contains("owner")) {
            queryBuilder.fields.add(STR."\{parentTable}owner_name AS \{parentAlias}namespace_owner");
        }
        if (environment.getSelectionSet().contains("slug")) {
            queryBuilder.fields.add(STR."\{parentTable}slug AS \{parentAlias}namespace_slug");
        }
        return null; // no need to dig deeper
    }

    @Bean
    public SimpleModule dum() {
        final SimpleModule module = new SimpleModule();
        module.addSerializer(new StdSerializer<>(PgArray.class) {

            @Override
            public void serialize(final PgArray value, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
                gen.writeStartArray();
                final Object array;
                try {
                    array = value.getArray();
                } catch (final SQLException e) {
                    throw new RuntimeException(e);
                }
                if (array instanceof final Object[] arr) {
                    for (final Object o : arr) {
                        gen.writeObject(o);
                    }
                } else if (array instanceof final Iterable<?> it) {
                    for (final Object o : it) {
                        gen.writeObject(o);
                    }
                } else {
                    throw new RuntimeException("Unknown array type: " + array.getClass());
                }
                gen.writeEndArray();
            }
        });
        return module;
    }


    private String getParentAlias(final ExecutionStepInfo info, final QueryBuilder queryBuilder) {
        final ExecutionStepInfo parent = info.getParent();
        if (parent == null || parent.getObjectType() == null) {
            return queryBuilder.rootTable + "_";
        } else if (parent.getObjectType().getName().equals("Query")) {
            return queryBuilder.rootTable + "_";
        } else if (parent.getType() instanceof GraphQLList) {
            // skip lists, else we would match them twice
            return this.getParentTable(parent, queryBuilder, true);
        } else {
            return this.getParentAlias(parent, queryBuilder) + parent.getField().getName() + "_";
        }
    }

    private String getParentTable(final ExecutionStepInfo info, final QueryBuilder queryBuilder) {
        return this.getParentTable(info, queryBuilder, false);
    }

    private String getParentTable(final ExecutionStepInfo info, final QueryBuilder queryBuilder, final boolean deep) {
        final ExecutionStepInfo parent = info.getParent();
        if (parent == null || parent.getObjectType() == null) {
            return "";
        } else if (parent.getType() instanceof GraphQLList) {
            // skip lists, else we would match them twice
            return this.getParentTable(parent, queryBuilder, true);
        } else if (parent.getObjectType().getName().equals("Query")) {
            return queryBuilder.rootTable + (deep ? "_" : ".");
        } else {
            return this.getParentTable(parent, queryBuilder, true) + parent.getField().getName() + (deep ? "_" : ".");
        }
    }

    @Bean
    public Instrumentation instrumentation() {
        return new Instrumentation() {

            @Override
            public @NotNull GraphQLSchema instrumentSchema(final GraphQLSchema schema, final InstrumentationExecutionParameters parameters, final InstrumentationState state) {
                return Instrumentation.super.instrumentSchema(schema, parameters, state);
            }

            @Override
            public @NotNull ExecutionInput instrumentExecutionInput(final ExecutionInput executionInput, final InstrumentationExecutionParameters parameters, final InstrumentationState state) {
                System.out.println("start!");
                parameters.getGraphQLContext().put("queryBuilder", new QueryBuilder());
                return Instrumentation.super.instrumentExecutionInput(executionInput, parameters, state);
            }

            @Override
            public @NotNull DataFetcher<?> instrumentDataFetcher(final DataFetcher<?> dataFetcher, final InstrumentationFieldFetchParameters parameters, final InstrumentationState state) {
                System.out.println(STR."fetch \{parameters.getEnvironment().getField().getName()} using \{dataFetcher.getClass().getName()} \{parameters.getEnvironment().getExecutionStepInfo().getPath()}");
                // replace the default property data fetcher with our own
                if (dataFetcher instanceof final PropertyDataFetcher<?> propertyDataFetcher) {
                    final QueryBuilder queryBuilder = parameters.getEnvironment().getGraphQlContext().get("queryBuilder");
                    final String parentAlias = TestController.this.getParentAlias(parameters.getEnvironment().getExecutionStepInfo(), queryBuilder);
                    final String parentTable = TestController.this.getParentTable(parameters.getEnvironment().getExecutionStepInfo(), queryBuilder);
                    queryBuilder.fields.add(STR."\{parentTable}\{propertyDataFetcher.getPropertyName()} AS \{parentAlias}\{propertyDataFetcher.getPropertyName()}");

                    // find return type
                    if (parameters.getField().getType() instanceof final GraphQLScalarType scalarType) {
                        if (scalarType.getName().equals("Int")) {
                            return (DataFetcher<Integer>) _ -> 0;
                        }
                    }
                    return (DataFetcher<Object>) _ -> EMPTY;
                }
                return Instrumentation.super.instrumentDataFetcher(dataFetcher, parameters, state);
            }

            @Override
            public @NotNull CompletableFuture<ExecutionResult> instrumentExecutionResult(final ExecutionResult executionResult, final InstrumentationExecutionParameters parameters, final InstrumentationState state) {
                final QueryBuilder queryBuilder = parameters.getGraphQLContext().get("queryBuilder");
                System.out.println("execute " + queryBuilder.fields);

                if (parameters.getOperation() != null && parameters.getOperation().equals("IntrospectionQuery")) {
                    return Instrumentation.super.instrumentExecutionResult(executionResult, parameters, state);
                }

                // todo sorting the joins by length is a hack to make sure we join the tables in the right order
                final String sql = STR."""
                        SELECT

                        \{queryBuilder.fields.stream().reduce((a, b) -> a + ",\n" + b).orElse("")}

                        \{queryBuilder.from}

                        \{queryBuilder.joins.stream().sorted(Comparator.comparing(String::length)).reduce((a, b) -> a + "\n" + b).orElse("")}

                        \{queryBuilder.condition};
                        """;
                try {
                    return CompletableFuture.completedFuture(TestController.this.jdbi.withHandle((handle -> {
                        final var test = handle.select(sql)
                            .bind("projectBySlug_slug", "Test") // todo: fix binds
                            .mapToMap()
                            .collectIntoList()
                            .stream().map(inputMap -> {
                                // first put in static fields
                                final Map<String, Object> outputMap = new HashMap<>(queryBuilder.staticFields);
                                Map<String, Object> currentMap = outputMap;

                                // then run the resolvers
                                for (final var entry : queryBuilder.resolver.entrySet()) {
                                    outputMap.put(entry.getKey(), entry.getValue().apply(inputMap));
                                }

                                // then clean it up into a tree
                                for (final String key : inputMap.keySet()) {
                                    final String[] parts = key.split("_");
                                    for (int i = 0; i < parts.length - 1; i++) {
                                        final String part = parts[i];
                                        if (!currentMap.containsKey(part)) {
                                            currentMap.put(part, new HashMap<String, Object>());
                                        }
                                        currentMap = (Map<String, Object>) currentMap.get(part);
                                    }
                                    currentMap.put(parts[parts.length - 1], inputMap.get(key));
                                    currentMap = outputMap;
                                }

                                return outputMap;
                            }).toList();

                        final var ext = Map.<Object, Object>of(
                            "sql", sql.split("\n"),
                            "sql2", sql.replace("\n", " ")
                        );
                        return ExecutionResult.newExecutionResult().data(test).extensions(ext).build();
                    })));
                } catch (final Throwable e) {
                    // e.printStackTrace();
                    final var error = Map.<String, Object>of(
                        "message", e.getMessage().split("\n"),
                        "sql", sql.split("\n"),
                        "sql2", sql.replace("\n", " ")
                    );
                    return CompletableFuture.completedFuture(ExecutionResult.newExecutionResult()
                        .addError(GraphQLError.newError().message("Dum").extensions(error).build())
                        .build());
                }
            }
        };
    }

    // TODO we need one query builder per query mapping or use subqueries
    class QueryBuilder {
        String rootTable = "";
        String from = "";
        String condition = "";
        Set<String> fields = new HashSet<>();
        Set<String> joins = new HashSet<>();
        Map<String, Function<Map<String, Object>, Object>> resolver = new HashMap<>();
        Map<String, Object> staticFields = new HashMap<>();
    }
}
