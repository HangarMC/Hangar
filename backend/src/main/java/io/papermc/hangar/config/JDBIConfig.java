package io.papermc.hangar.config;

import com.zaxxer.hikari.HikariDataSource;
import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.db.customtypes.JobState;
import io.papermc.hangar.db.customtypes.PGLoggedAction;
import io.papermc.hangar.db.customtypes.RoleCategory;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.RowMapperFactory;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.postgres.PostgresTypes;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

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
    DataSource dataSource(@Value("${spring.datasource.url}") final String url, @Value("${spring.datasource.username}") final String username, @Value("${spring.datasource.password}") final String password) {
        final HikariDataSource dataSource = DataSourceBuilder.create().type(HikariDataSource.class).url(url).username(username).password(password).build();
        dataSource.setPoolName(UUID.randomUUID().toString());
        return dataSource;
    }

    @Bean
    public Jdbi jdbi(final DataSource dataSource, final List<JdbiPlugin> jdbiPlugins, final List<RowMapper<?>> rowMappers, final List<RowMapperFactory> rowMapperFactories, final List<ColumnMapper<?>> columnMappers) {
        final TransactionAwareDataSourceProxy dataSourceProxy = new TransactionAwareDataSourceProxy(dataSource);
        final Jdbi jdbi = Jdbi.create(dataSourceProxy);
        final PostgresTypes config = jdbi.getConfig(PostgresTypes.class);

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
