package search;

import java.sql.SQLException;

/**
 * Port of abstr2.index.
 */
public final class Index {

    private Index() {
    }

    public static void addToIndex(ContextMap contextmap, ProcessResult processResult) {
        String text = String.join(",", processResult.getWordlistSet());
        String etag = processResult.getEtag();
        try {
            try (var ps = contextmap.indexConnection.prepareStatement("DELETE from site where url = ?")) {
                ps.setString(1, contextmap.currentUrl);
                ps.executeUpdate();
            }
            try (var ps = contextmap.indexConnection.prepareStatement("INSERT INTO site(url, etag, text) values(?, ?, ?)")) {
                ps.setString(1, contextmap.currentUrl);
                ps.setString(2, etag);
                ps.setString(3, text);
                ps.executeUpdate();
            }
            contextmap.indexConnection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isEtagInIndex(String url, String etag, ContextMap contextmap) {
        if (etag == null || etag.isEmpty()) {
            return false;
        }
        try {
            String sql = "SELECT count(1) from site where url = ? and etag = ?";
            try (var ps = contextmap.indexConnection.prepareStatement(sql)) {
                ps.setString(1, url);
                ps.setString(2, etag);
                var rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println(etag);
        }
        return false;
    }
}
