package search.store;

import search.IndexRecord;

import java.util.List;

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

    /** All index records (index_name, update start/end, pct_complete). Empty for CSV store. */
    List<IndexRecord> getIndices();
}
