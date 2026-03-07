package search.store;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * SQLite-backed host store (hosts.db, host table).
 */
public class SqliteHostDAO implements HostDAO {

    private final String dbPath;
    private Connection connection;

    public SqliteHostDAO(String dataDir) {
        File data = new File(dataDir, "hosts.db");
        this.dbPath = data.getAbsolutePath();
    }

    @Override
    public void init() {
        try {
            File parent = new File(dbPath).getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            connection.setAutoCommit(false);
            try (var st = connection.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS indices(index_name varchar, UPDATE_START timestamp, UPDATE_STOP timestamp, PCT_COMPLETE DOUBLE, UNIQUE(index_name))");
                st.executeUpdate("CREATE TABLE IF NOT EXISTS host(URL, LAST_UPDATE, UNIQUE(URL))");
            }
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addHost(String url) {
        try {
            try (var ps = connection.prepareStatement("INSERT OR IGNORE INTO host(url) values(?)")) {
                ps.setString(1, url);
                ps.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
