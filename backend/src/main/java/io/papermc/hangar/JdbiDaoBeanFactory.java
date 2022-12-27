package io.papermc.hangar;

import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

// https://stackoverflow.com/questions/61526870/spring-boot-custom-bean-loader
public class JdbiDaoBeanFactory implements FactoryBean<Object>, InitializingBean {

    private final Jdbi jdbi;
    private final Class<?> jdbiDaoClass;
    private volatile Object jdbiDaoBean;

    public JdbiDaoBeanFactory(final Jdbi jdbi, final Class<?> jdbiDaoClass) {
        this.jdbi = jdbi;
        this.jdbiDaoClass = jdbiDaoClass;
    }

    @Override
    public Object getObject() throws Exception {
        return this.jdbiDaoBean;
    }

    @Override
    public Class<?> getObjectType() {
        return this.jdbiDaoClass;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.jdbiDaoBean = this.jdbi.onDemand(this.jdbiDaoClass);
    }
}
