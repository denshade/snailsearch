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
 */
public final class Hosts {

    private static final String DATA_DIR = "data";

    private Hosts() {
    }

    public static void addToUnknownHosts(ContextMap contextmap) {
        try {
            String cleanUrl = cleanHostUrl(contextmap.currentUrl);
            System.out.println("skipped " + cleanUrl);
            contextmap.hostStore.addHost(cleanUrl);
        } catch (Exception e) {
            System.out.println("skipped " + contextmap.currentUrl);
            contextmap.hostStore.addHost(contextmap.currentUrl);
        }
    }

    private static String cleanHostUrl(String url) {
        java.net.URI uri = java.net.URI.create(url);
        String clean = uri.getScheme() + "://" + uri.getHost();
        if (uri.getPort() > 0 && uri.getPort() != ("https".equals(uri.getScheme()) ? 443 : 80)) {
            clean += ":" + uri.getPort();
        }
        return clean;
    }

    public static ContextMap loadHosts(ContextMap contextmap) {
        ensureDataDir();
        HostStore store = contextmap.useCsv
                ? new CsvHostStore(DATA_DIR)
                : new SqliteHostStore(DATA_DIR);
        store.init();
        contextmap.hostStore = store;
        return contextmap;
    }

    public static ContextMap createHostSpecificIndex(ContextMap contextmap) {
        ensureDataDir();
        String hostname = contextmap.currentHost;
        IndexStore store = contextmap.useCsv
                ? new CsvIndexStore(DATA_DIR, hostname)
                : new SqliteIndexStore(DATA_DIR, hostname);
        store.init();
        contextmap.indexStore = store;
        return contextmap;
    }

    private static void ensureDataDir() {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }
}
