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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.jdbi.v3.core.Jdbi;
import org.jetbrains.annotations.NotNull;
import org.postgresql.jdbc.PgArray;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                    return CompletableFuture.completedFuture(QueryConfig.this.jdbi.withHandle((handle -> {
                        final var resultList = queryBuilder.execute(handle, sql, parameters.getVariables());

                        final LocalDateTime executionTime = LocalDateTime.now();

                        // handle resolvers
                        queryBuilder.handleResolvers(resultList);

                        // merge the result
                        final var result = QueryMerger.merge(resultList);

                        final LocalDateTime startTime = parameters.getGraphQLContext().get("startTime");
                        final LocalDateTime endTime = LocalDateTime.now();

                        final var ext = LinkedHashMap.newLinkedHashMap(5);
                        ext.put("sql", sql.split("\n"));
                        ext.put("sql2", sql.replace("\n", " "));
                        ext.put("parseTime", Duration.between(startTime, parseTime).toMillis() + "ms");
                        ext.put("executionTime", Duration.between(parseTime, executionTime).toMillis() + "ms");
                        ext.put("resolveTime", Duration.between(executionTime, endTime).toMillis() + "ms");
                        ext.put("totalTime", Duration.between(startTime, endTime).toMillis() + "ms");

                        return ExecutionResult.newExecutionResult().data(result).extensions(ext).build();
                    })));
                } catch (final Throwable e) {
                    e.printStackTrace();

                    final var error = LinkedHashMap.<String, Object>newLinkedHashMap(3);
                    error.put("message", e.getMessage() != null ? e.getMessage().split("\n") : "<null>");
                    error.put("sql", sql.split("\n"));
                    error.put("sql2", sql.replace("\n", " "));

                    return CompletableFuture.completedFuture(ExecutionResult.newExecutionResult()
                        .addError(GraphQLError.newError().message("Dum").extensions(error).build())
                        .build());
                }
            }
        };
    }

    @Bean
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
