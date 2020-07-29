package me.minidigger.hangar.config;

import me.minidigger.hangar.db.customtypes.RoleCategory;
import me.minidigger.hangar.db.dao.HangarDao;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.spi.JdbiPlugin;
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
    public Jdbi jdbi(DataSource dataSource, List<JdbiPlugin> jdbiPlugins) {
        TransactionAwareDataSourceProxy dataSourceProxy = new TransactionAwareDataSourceProxy(dataSource);
        Jdbi jdbi = Jdbi.create(dataSourceProxy);
        jdbiPlugins.forEach(jdbi::installPlugin);
        jdbi.configure(PostgresTypes.class, pt -> pt.registerCustomType(RoleCategory.class, "role_category"));
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
