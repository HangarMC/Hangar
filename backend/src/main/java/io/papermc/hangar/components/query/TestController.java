package io.papermc.hangar.components.query;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import graphql.execution.instrumentation.parameters.InstrumentationFieldFetchParameters;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.PropertyDataFetcher;
import io.papermc.hangar.components.images.service.AvatarService;
import io.papermc.hangar.service.internal.file.FileService;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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

        // TODO fix me
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
        final String parentTable = PrefixUtil.getParentTable(environment.getExecutionStepInfo(), queryBuilder);
        final String parentAlias = PrefixUtil.getParentAlias(environment.getExecutionStepInfo(), queryBuilder);
        queryBuilder.joins.add(STR."JOIN projects \{parentAlias}projects ON \{parentAlias}projects.owner_id = \{parentTable}id");
        return List.of(EMPTY);
    }

    @SchemaMapping(typeName = "Project", field = "owner")
    public Object projectOwner(final DataFetchingEnvironment environment) {
        final QueryBuilder queryBuilder = environment.getGraphQlContext().get("queryBuilder");
        final String parentTable = PrefixUtil.getParentTable(environment.getExecutionStepInfo(), queryBuilder);
        final String parentAlias = PrefixUtil.getParentAlias(environment.getExecutionStepInfo(), queryBuilder);
        queryBuilder.joins.add(STR."JOIN users \{parentAlias}owner ON \{parentAlias}owner.id = \{parentTable}owner_id");
        return EMPTY;
    }

    @SchemaMapping(typeName = "Project", field = "avatarUrl")
    public Object projectAvatarUrl(final DataFetchingEnvironment environment) {
        final QueryBuilder queryBuilder = environment.getGraphQlContext().get("queryBuilder");
        final String parentTable = PrefixUtil.getParentTable(environment.getExecutionStepInfo(), queryBuilder);
        final String parentAlias = PrefixUtil.getParentAlias(environment.getExecutionStepInfo(), queryBuilder);
        final String avatarVersion = STR."ext_\{parentAlias.replace("_", "")}avatarversion";
        final String projectId = STR."ext_\{parentAlias.replace("_", "")}projectid";
        queryBuilder.fields.add(STR."\{parentAlias}avatar.version AS \{avatarVersion}");
        queryBuilder.fields.add(STR."\{parentTable}id AS \{projectId}");
        queryBuilder.joins.add(STR."JOIN avatars \{parentAlias}avatar ON \{parentAlias}avatar.type = 'project' AND \{parentAlias}avatar.subject = \{parentTable}id::varchar");
        queryBuilder.resolver.put(parentAlias + "avatarUrl", (r) -> this.fileService.getAvatarUrl(AvatarService.PROJECT, String.valueOf(r.get(projectId)), (Integer) r.get(avatarVersion)));
        return EMPTY;
    }

    @SchemaMapping(typeName = "Project", field = "namespace")
    public Object projectNamespace(final DataFetchingEnvironment environment) {
        final QueryBuilder queryBuilder = environment.getGraphQlContext().get("queryBuilder");
        final String parentAlias = PrefixUtil.getParentAlias(environment.getExecutionStepInfo(), queryBuilder);
        final String parentTable = PrefixUtil.getParentTable(environment.getExecutionStepInfo(), queryBuilder);
        if (environment.getSelectionSet().contains("owner")) {
            queryBuilder.fields.add(STR."\{parentTable}owner_name AS \{parentAlias}namespace_owner");
        }
        if (environment.getSelectionSet().contains("slug")) {
            queryBuilder.fields.add(STR."\{parentTable}slug AS \{parentAlias}namespace_slug");
        }
        return null; // no need to dig deeper
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
                parameters.getGraphQLContext().put("startTime", LocalDateTime.now());
                parameters.getGraphQLContext().put("queryBuilder", new QueryBuilder());
                return Instrumentation.super.instrumentExecutionInput(executionInput, parameters, state);
            }

            @Override
            public @NotNull DataFetcher<?> instrumentDataFetcher(final DataFetcher<?> dataFetcher, final InstrumentationFieldFetchParameters parameters, final InstrumentationState state) {
                System.out.println(STR."fetch \{parameters.getEnvironment().getField().getName()} using \{dataFetcher.getClass().getName()} \{parameters.getEnvironment().getExecutionStepInfo().getPath()}");
                // replace the default property data fetcher with our own
                if (dataFetcher instanceof final PropertyDataFetcher<?> propertyDataFetcher) {
                    final QueryBuilder queryBuilder = parameters.getEnvironment().getGraphQlContext().get("queryBuilder");
                    final String parentAlias = PrefixUtil.getParentAlias(parameters.getEnvironment().getExecutionStepInfo(), queryBuilder);
                    final String parentTable = PrefixUtil.getParentTable(parameters.getEnvironment().getExecutionStepInfo(), queryBuilder);
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

                final String sql = queryBuilder.buildSql();

                final LocalDateTime parseTime = LocalDateTime.now();

                try {
                    return CompletableFuture.completedFuture(TestController.this.jdbi.withHandle((handle -> {
                        final var result = queryBuilder.execute(handle, sql);

                        final LocalDateTime startTime = parameters.getGraphQLContext().get("startTime");
                        final LocalDateTime endTime = LocalDateTime.now();
                        final var ext = Map.<Object, Object>of(
                            "sql", sql.split("\n"),
                            "sql2", sql.replace("\n", " "),
                            "parseTime", Duration.between(startTime, parseTime).toMillis() + "ms",
                            "executionTime", Duration.between(parseTime, endTime).toMillis() + "ms",
                            "totalTime", Duration.between(startTime, endTime).toMillis()+ "ms"
                        );

                        return ExecutionResult.newExecutionResult().data(result).extensions(ext).build();
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

}
