package io.papermc.hangar.service.internal.projects;

import com.impossibl.postgres.jdbc.PGDataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;

@Service
public class HomeProjectService {

    private final String url;
    private final String username;
    private final String password;

    public HomeProjectService(@Value("${spring.datasource.url}") String url, @Value("${spring.datasource.username}") String username, @Value("${spring.datasource.password}") String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        // force using pgjdbc-ng since that doesn't do dum begin queries that break cockroach
        PGDataSource ds = new PGDataSource();
        ds.setDatabaseUrl(url.replace("postgresql", "pgsql"));
//        ds.setSqlTrace(true);
        return ds.getConnection(username, password);
    }

    // if it works, it's not wrong, right?
    public void refreshHomeProjects() {
        System.out.println("start refresh");
        try (var con = getConnection();
             var stmt =  con.createStatement()) {
            System.out.println("start?");
            stmt.execute("REFRESH MATERIALIZED VIEW home_projects");
            System.out.println("done?");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("refreshed");
    }
}
