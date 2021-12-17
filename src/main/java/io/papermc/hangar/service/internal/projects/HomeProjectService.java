package io.papermc.hangar.service.internal.projects;

import com.impossibl.postgres.jdbc.PGDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;

@Service
public class HomeProjectService {

    private static final Logger logger = LoggerFactory.getLogger(HomeProjectService.class);

    private final String url;
    private final String username;
    private final String password;

    public HomeProjectService(@Value("${spring.datasource.url}") String url, @Value("${spring.datasource.username}") String username, @Value("${spring.datasource.password}") String password) {
        this.url = url.replace("postgresql", "pgsql");
        this.username = username;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        // force using pgjdbc-ng since that doesn't do dum begin queries that break cockroach
        PGDataSource ds = new PGDataSource();
        ds.setDatabaseUrl(url);
//        ds.setSqlTrace(true);
        return ds.getConnection(username, password);
    }

    // if it works, it's not wrong, right?
    public void refreshHomeProjects() {
        try (var con = getConnection();
             var stmt =  con.createStatement()) {
            stmt.execute("REFRESH MATERIALIZED VIEW home_projects");
        } catch (SQLException e) {
            if (e.getMessage().contains("Unsupported database URL")) {
                logger.warn("Unsupported database URL: {}", url);
            } else {
                e.printStackTrace();
            }
        }
    }
}
