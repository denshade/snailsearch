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

    @Override
    public void startScanIndex(String indexName) {
        try {
            try (var ps = connection.prepareStatement("DELETE FROM indices WHERE index_name = ?")) {
                ps.setString(1, indexName);
                ps.executeUpdate();
            }
            try (var ps = connection.prepareStatement(
                    "INSERT INTO indices(index_name, UPDATE_START, PCT_COMPLETE) VALUES(?, datetime('now'), 0)")) {
                ps.setString(1, indexName);
                ps.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stopScanIndex(String indexName) {
        try {
            try (var ps = connection.prepareStatement(
                    "UPDATE indices SET UPDATE_STOP = datetime('now'), PCT_COMPLETE = 100 WHERE index_name = ?")) {
                ps.setString(1, indexName);
                ps.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateScanIndexProgress(String indexName, double pctComplete) {
        try {
            try (var ps = connection.prepareStatement(
                    "UPDATE indices SET UPDATE_STOP = NULL, PCT_COMPLETE = ? WHERE index_name = ?")) {
                ps.setDouble(1, pctComplete);
                ps.setString(2, indexName);
                ps.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
