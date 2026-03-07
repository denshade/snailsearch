package search.store;

/**
 * Abstraction for storing known/skipped hosts.
 */
public interface HostDAO {

    /** Create or open the store. */
    void init();

    /** Record a host URL (e.g. skipped). Idempotent. */
    void addHost(String url);

    /** Start a scan record for update_site (index_name = start URL). No-op for CSV. */
    void startScanIndex(String indexName);

    /** Mark scan complete. No-op for CSV. */
    void stopScanIndex(String indexName);

    /** Update scan progress percentage. No-op for CSV. */
    void updateScanIndexProgress(String indexName, double pctComplete);
}
