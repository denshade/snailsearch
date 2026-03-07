package search.store;

import java.util.List;

/**
 * Abstraction for storing the per-host page index (url, etag, text).
 */
public interface IndexDAO {

    /** Create or open the store for this host. */
    void init();

    /** Upsert a page: replace existing row for url with new etag and text. */
    void addPage(String url, String etag, String text);

    /** True if the store already has this url with this etag. */
    boolean isEtagInIndex(String url, String etag);

    /** All URLs currently in the index (for update_site iteration). */
    List<String> getUrls();
}
