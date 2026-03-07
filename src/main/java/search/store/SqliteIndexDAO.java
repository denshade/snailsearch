package search.store;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite-backed index store (one db per hostname, site table).
 */
public class SqliteIndexDAO implements IndexDAO {

    private final String dbPath;
    private Connection connection;

    public SqliteIndexDAO(String dataDir, String hostname) {
        File data = new File(dataDir, hostname + ".db");
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
                st.executeUpdate("CREATE TABLE IF NOT EXISTS site(URL, etag, text)");
            }
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addPage(String url, String etag, String text) {
        try {
            try (var ps = connection.prepareStatement("DELETE from site where url = ?")) {
                ps.setString(1, url);
                ps.executeUpdate();
            }
            try (var ps = connection.prepareStatement("INSERT INTO site(url, etag, text) values(?, ?, ?)")) {
                ps.setString(1, url);
                ps.setString(2, etag);
                ps.setString(3, text);
                ps.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isEtagInIndex(String url, String etag) {
        if (etag == null || etag.isEmpty()) {
            return false;
        }
        try {
            try (var ps = connection.prepareStatement("SELECT count(1) from site where url = ? and etag = ?")) {
                ps.setString(1, url);
                ps.setString(2, etag);
                var rs = ps.executeQuery();
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public List<String> getUrls() {
        try {
            List<String> urls = new ArrayList<>();
            try (var st = connection.createStatement();
                 ResultSet rs = st.executeQuery("SELECT url FROM site")) {
                while (rs.next()) {
                    urls.add(rs.getString(1));
                }
            }
            return urls;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
