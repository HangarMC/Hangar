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
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.PropertyDataFetcher;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.jdbi.v3.core.Jdbi;
import org.jetbrains.annotations.NotNull;
import org.postgresql.jdbc.PgArray;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.papermc.hangar.components.query.QueryBuilder.getActiveQueryBuilder;
import static io.papermc.hangar.components.query.QueryBuilder.getAllQueryBuilders;
import static io.papermc.hangar.components.query.QueryHelper.EMPTY;

@Configuration
public class QueryConfig {

    private final Jdbi jdbi;

    public QueryConfig(final Jdbi jdbi) {
        this.jdbi = jdbi;
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
                return Instrumentation.super.instrumentExecutionInput(executionInput, parameters, state);
            }

            @Override
            public @NotNull DataFetcher<?> instrumentDataFetcher(final DataFetcher<?> dataFetcher, final InstrumentationFieldFetchParameters parameters, final InstrumentationState state) {
                System.out.println(STR."fetch \{parameters.getEnvironment().getField().getName()} using \{dataFetcher.getClass().getName()} \{parameters.getEnvironment().getExecutionStepInfo().getPath()}");
                // replace the default property data fetcher with our own
                if (dataFetcher instanceof final PropertyDataFetcher<?> propertyDataFetcher) {
                    final QueryBuilder queryBuilder = getActiveQueryBuilder(parameters.getEnvironment().getGraphQlContext());
                    final String parentAlias = PrefixUtil.getParentAlias(parameters.getEnvironment().getExecutionStepInfo(), queryBuilder);
                    final String parentTable = PrefixUtil.getParentTable(parameters.getEnvironment().getExecutionStepInfo(), queryBuilder);
                    queryBuilder.fields.add(STR."\{parentTable}\{propertyDataFetcher.getPropertyName()} AS \{parentAlias}\{parameters.getExecutionStepInfo().getPath().getSegmentName()}");

                    // find return type
                    if (parameters.getField().getType() instanceof final GraphQLScalarType scalarType) {
                        if (scalarType.getName().equals("Int")) {
                            return (DataFetcher<Integer>) dum -> 0;
                        }
                    }
                    return (DataFetcher<Object>) dum -> EMPTY;
                }
                return Instrumentation.super.instrumentDataFetcher(dataFetcher, parameters, state);
            }

            @Override
            public @NotNull CompletableFuture<ExecutionResult> instrumentExecutionResult(final ExecutionResult executionResult, final InstrumentationExecutionParameters parameters, final InstrumentationState state) {
                final List<QueryBuilder> queryBuilders = getAllQueryBuilders(parameters.getGraphQLContext());
                final QueryMerger merger = new QueryMerger(parameters.getSchema());

                // (parsing) error? -> return
                if (!executionResult.getErrors().isEmpty()) {
                    return CompletableFuture.completedFuture(executionResult);
                }

                // introspection query? -> return
                if (parameters.getOperation() != null && parameters.getOperation().equals("IntrospectionQuery")) {
                    return CompletableFuture.completedFuture(executionResult);
                }

                final Map<String, Object> totalResult = new HashMap<>();
                final Map<Object, Object> totalExt = LinkedHashMap.newLinkedHashMap(queryBuilders.size());
                final List<GraphQLError> errors = new ArrayList<>();
                // TODO we can run these concurrently
                for (final QueryBuilder queryBuilder : queryBuilders) {
                    final String sql = queryBuilder.buildSql();
                    final LocalDateTime parseTime = LocalDateTime.now();

                    try {
                        QueryConfig.this.jdbi.useHandle((handle -> {
                            // run the query
                            final var resultList = queryBuilder.execute(handle, sql);

                            final LocalDateTime executionTime = LocalDateTime.now();

                            // handle resolvers
                            queryBuilder.handleResolvers(resultList);

                            // merge the result
                            final var result = merger.merge(resultList);

                            // collect some data
                            final LocalDateTime startTime = parameters.getGraphQLContext().get("startTime");
                            final LocalDateTime endTime = LocalDateTime.now();

                            final var ext = LinkedHashMap.newLinkedHashMap(5);
                            ext.put("sql", sql.split("\n"));
                            ext.put("sql2", sql.replace("\n", " "));
                            ext.put("parseTime", Duration.between(startTime, parseTime).toMillis() + "ms");
                            ext.put("executionTime", Duration.between(parseTime, executionTime).toMillis() + "ms");
                            ext.put("resolveTime", Duration.between(executionTime, endTime).toMillis() + "ms");
                            ext.put("totalTime", Duration.between(startTime, endTime).toMillis() + "ms");

                            // store the result
                            totalResult.putAll(result);
                            totalExt.put(queryBuilder.rootTable, ext);
                        }));
                    } catch (Exception ex) {
                        final var error = LinkedHashMap.<String, Object>newLinkedHashMap(3);
                        error.put("message", ex.getMessage() != null ? ex.getMessage().split("\n") : "<null>");
                        error.put("sql", sql.split("\n"));
                        error.put("sql2", sql.replace("\n", " "));
                        errors.add(GraphQLError.newError().message("Dum").extensions(error).build());
                    }
                }

                return CompletableFuture.completedFuture(ExecutionResult.newExecutionResult()
                    .data(totalResult)
                    .extensions(totalExt)
                    .errors(errors)
                    .build());
            }
        };
    }

    @Bean // TODO remove again eventually
    public SimpleModule queryPostgresSerializer() {
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
}
