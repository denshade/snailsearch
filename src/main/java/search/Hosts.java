package search;

import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Port of abstr2.hosts.
 */
public final class Hosts {

    private Hosts() {
    }

    public static void addToUnknownHosts(ContextMap contextmap) {
        try {
            URI uri = URI.create(contextmap.currentUrl);
            String cleanUrl = uri.getScheme() + "://" + uri.getHost();
            if (uri.getPort() > 0 && uri.getPort() != ("https".equals(uri.getScheme()) ? 443 : 80)) {
                cleanUrl += ":" + uri.getPort();
            }
            System.out.println("skipped " + cleanUrl);
            addToHosts(cleanUrl, contextmap.hostStatement, contextmap.hostConnection);
        } catch (Exception e) {
            System.out.println("skipped " + contextmap.currentUrl);
            addToHosts(contextmap.currentUrl, contextmap.hostStatement, contextmap.hostConnection);
        }
    }

    private static void addToHosts(String host, Statement cur, Connection con) {
        try {
            try (var ps = con.prepareStatement("INSERT OR IGNORE INTO host(url) values(?)")) {
                ps.setString(1, host);
                ps.executeUpdate();
            }
            con.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static ContextMap loadHosts(ContextMap contextmap) {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        String dbPath = new File(dataDir, "hosts.db").getAbsolutePath();
        try {
            Connection hostcon = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            hostcon.setAutoCommit(false);
            Statement hostcur = hostcon.createStatement();
            contextmap.hostConnection = hostcon;
            contextmap.hostStatement = hostcur;
            initHostsIndex(contextmap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return contextmap;
    }

    private static void initHostsIndex(ContextMap contextmap) throws SQLException {
        contextmap.hostStatement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS indices(index_name varchar, UPDATE_START timestamp, UPDATE_STOP timestamp, PCT_COMPLETE DOUBLE, UNIQUE(index_name))");
        contextmap.hostStatement.executeUpdate("CREATE TABLE IF NOT EXISTS host(URL, LAST_UPDATE, UNIQUE(URL))");
    }

    public static ContextMap createHostSpecificIndex(ContextMap contextmap) {
        String hostname = contextmap.currentHost;
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        String dbPath = new File(dataDir, hostname + ".db").getAbsolutePath();
        try {
            Connection con = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            con.setAutoCommit(false);
            Statement cur = con.createStatement();
            cur.executeUpdate("CREATE TABLE IF NOT EXISTS site(URL, etag, text)");
            contextmap.indexConnection = con;
            contextmap.indexStatement = cur;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return contextmap;
    }
}
