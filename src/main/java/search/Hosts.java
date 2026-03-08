package search;

import search.store.CsvHostDAO;
import search.store.HostDAO;
import search.store.IndexDAO;
import search.store.SqliteHostDAO;
import search.store.SqliteIndexDAO;
import search.store.CsvIndexDAO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Port of abstr2.hosts. Delegates to HostStore / IndexStore implementations.
 * Can be instantiated with a custom data directory (e.g. for tests).
 */
public final class Hosts {

    private static final String DEFAULT_DATA_DIR = "data";
    private static final String HOSTS_CSV = "hosts.csv";
    private static final String HOSTS_DB = "hosts.db";

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
            contextmap.hostDAO.addHost(cleanUrl);
        } catch (Exception e) {
            System.out.println("skipped " + contextmap.currentUrl);
            contextmap.hostDAO.addHost(contextmap.currentUrl);
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
        HostDAO store = new CsvHostDAO(dataDir);
        store.init();
        contextmap.hostDAO = store;
        return contextmap;
    }

    public ContextMap createHostSpecificIndex(ContextMap contextmap) {
        ensureDataDir();
        String hostname = contextmap.currentHost;
        var store = new CsvIndexDAO(dataDir, hostname);
        store.init();
        contextmap.indexDAO = store;
        return contextmap;
    }

    private void ensureDataDir() {
        File dir = new File(dataDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Port of abstr1.hosts.get_hosts. Lists host index files in data dir (excluding the host store)
     * and returns hostnames (filename without extension).
     */
    public List<String> getHosts() {
        ensureDataDir();
        File dir = new File(dataDir);
        File[] files = dir.listFiles();
        List<String> hostnames = new ArrayList<>();
        if (files == null) return hostnames;
        String suffix = ".csv";
        for (File f : files) {
            if (!f.isFile()) continue;
            String name = f.getName();
            if (name.equals(HOSTS_CSV)) continue;
            if (name.endsWith(suffix)) {
                hostnames.add(name.substring(0, name.length() - suffix.length()));
            }
        }
        return hostnames;
    }
}
