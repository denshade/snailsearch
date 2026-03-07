package search.store;

/**
 * Abstraction for storing known/skipped hosts.
 */
public interface HostDAO {

    /** Create or open the store. */
    void init();

    /** Record a host URL (e.g. skipped). Idempotent. */
    void addHost(String url);
}
