package search;

import search.store.CsvHostStore;
import search.store.HostStore;
import search.store.IndexStore;
import search.store.SqliteHostStore;
import search.store.SqliteIndexStore;
import search.store.CsvIndexStore;

import java.io.File;

/**
 * Port of abstr2.hosts. Delegates to HostStore / IndexStore implementations.
 * Can be instantiated with a custom data directory (e.g. for tests).
 */
public final class Hosts {

    private static final String DEFAULT_DATA_DIR = "data";

    private final String dataDir;

    /** Uses default data directory "data". */
    public Hosts() {
        this(DEFAULT_DATA_DIR);
    }

    /** @param dataDir directory for host/index store files */
    public Hosts(String dataDir) {
        this.dataDir = dataDir;
    }

    public void addToUnknownHosts(ContextMap contextmap) {
        try {
            String cleanUrl = cleanHostUrl(contextmap.currentUrl);
            System.out.println("skipped " + cleanUrl);
            contextmap.hostStore.addHost(cleanUrl);
        } catch (Exception e) {
            System.out.println("skipped " + contextmap.currentUrl);
            contextmap.hostStore.addHost(contextmap.currentUrl);
        }
    }

    /** Normalizes a URL to scheme://host[:port] (port only if non-default). Package visibility for tests. */
    static String cleanHostUrl(String url) {
        java.net.URI uri = java.net.URI.create(url);
        String clean = uri.getScheme() + "://" + uri.getHost();
        if (uri.getPort() > 0 && uri.getPort() != ("https".equals(uri.getScheme()) ? 443 : 80)) {
            clean += ":" + uri.getPort();
        }
        return clean;
    }

    public ContextMap loadHosts(ContextMap contextmap) {
        ensureDataDir();
        HostStore store = contextmap.useCsv
                ? new CsvHostStore(dataDir)
                : new SqliteHostStore(dataDir);
        store.init();
        contextmap.hostStore = store;
        return contextmap;
    }

    public ContextMap createHostSpecificIndex(ContextMap contextmap) {
        ensureDataDir();
        String hostname = contextmap.currentHost;
        IndexStore store = contextmap.useCsv
                ? new CsvIndexStore(dataDir, hostname)
                : new SqliteIndexStore(dataDir, hostname);
        store.init();
        contextmap.indexStore = store;
        return contextmap;
    }

    private void ensureDataDir() {
        File dir = new File(dataDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}
