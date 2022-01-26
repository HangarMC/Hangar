package io.papermc.hangar.config;

import com.zaxxer.hikari.HikariDataSource;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.db.customtypes.JobState;
import io.papermc.hangar.db.customtypes.PGLoggedAction;
import io.papermc.hangar.db.customtypes.RoleCategory;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.RowMapperFactory;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.postgres.PostgresTypes;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Configuration
public class JDBIConfig {

    @Bean
    public JdbiPlugin sqlObjectPlugin() {
        return new SqlObjectPlugin();
    }

    @Bean
    public JdbiPlugin postgresPlugin() {
        return new PostgresPlugin();
    }

    @Bean
    DataSource dataSource(@Value("${spring.datasource.url}") String url, @Value("${spring.datasource.username}") String username, @Value("${spring.datasource.password}") String password) {
        HikariDataSource dataSource = DataSourceBuilder.create().type(HikariDataSource.class).url(url).username(username).password(password).driverClassName("org.postgresql.Driver").build();
        dataSource.setPoolName(UUID.randomUUID().toString());
        return dataSource;
    }

    @Bean
    public Jdbi jdbi(DataSource dataSource, List<JdbiPlugin> jdbiPlugins, List<RowMapper<?>> rowMappers, List<RowMapperFactory> rowMapperFactories, List<ColumnMapper<?>> columnMappers) {
        SqlLogger myLogger = new SqlLogger() {
            @Override
            public void logException(StatementContext context, SQLException ex) {
                Logger.getLogger("sql").info("sql: " + context.getRenderedSql());
            }
            @Override
            public void logAfterExecution(StatementContext context) {
                Logger.getLogger("sql").info("sql ae: " + context.getRenderedSql());
            }
        };
        TransactionAwareDataSourceProxy dataSourceProxy = new TransactionAwareDataSourceProxy(dataSource);
        Jdbi jdbi = Jdbi.create(dataSourceProxy);
        jdbi.setSqlLogger(myLogger); // for debugging sql statements
        PostgresTypes config = jdbi.getConfig(PostgresTypes.class);

        jdbiPlugins.forEach(jdbi::installPlugin);
        rowMappers.forEach(jdbi::registerRowMapper);
        rowMapperFactories.forEach(jdbi::registerRowMapper);
        columnMappers.forEach(jdbi::registerColumnMapper);

        config.registerCustomType(PGLoggedAction.class, "logged_action_type");
        config.registerCustomType(RoleCategory.class, "role_category");
        config.registerCustomType(JobState.class, "job_state");
        config.registerCustomType(JSONB.class, "jsonb");

        return jdbi;
    }
}
