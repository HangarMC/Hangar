package io.papermc.hangar.config;

import io.papermc.hangar.db.customtypes.JSONB;
import io.papermc.hangar.db.customtypes.JobState;
import io.papermc.hangar.db.customtypes.LoggedAction;
import io.papermc.hangar.db.customtypes.RoleCategory;
import io.papermc.hangar.db.dao.HangarDao;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.postgres.PostgresTypes;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;
import java.util.List;
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
    public Jdbi jdbi(DataSource dataSource, List<JdbiPlugin> jdbiPlugins, List<RowMapper> rowMappers) {
        SqlLogger myLogger = new SqlLogger() {
            @Override
            public void logAfterExecution(StatementContext context) {
                Logger.getLogger("sql").info("sql: " + context.getRenderedSql());
            }
        };
        TransactionAwareDataSourceProxy dataSourceProxy = new TransactionAwareDataSourceProxy(dataSource);
        Jdbi jdbi = Jdbi.create(dataSourceProxy);
//        jdbi.setSqlLogger(myLogger); // TODO for debugging sql statements
        PostgresTypes config = jdbi.getConfig(PostgresTypes.class);

        jdbiPlugins.forEach(jdbi::installPlugin);
        rowMappers.forEach(jdbi::registerRowMapper);

        config.registerCustomType(LoggedAction.class, "logged_action_type");
        config.registerCustomType(RoleCategory.class, "role_category");
        config.registerCustomType(JobState.class, "job_state");
        config.registerCustomType(JSONB.class, "jsonb");

        return jdbi;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public <T> HangarDao<T> hangarDao(Jdbi jdbi, InjectionPoint injectionPoint) {
        if (injectionPoint instanceof DependencyDescriptor) {
            DependencyDescriptor descriptor = (DependencyDescriptor) injectionPoint;
            //noinspection unchecked
            return new HangarDao<>((T) jdbi.onDemand(descriptor.getResolvableType().getGeneric(0).getRawClass()));
        }
        return null;
    }
}
