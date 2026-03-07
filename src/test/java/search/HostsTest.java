package search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import search.store.HostDAO;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Hosts}. Uses an instantiated {@link Hosts} with a temp directory
 * where possible so tests don't touch the real data dir.
 */
class HostsTest {

    // --- cleanHostUrl (package-private, tested via same package) ---

    @Test
    void cleanHostUrl_stripsPathAndQuery() {
        assertEquals("https://example.com", Hosts.cleanHostUrl("https://example.com/foo/bar?q=1"));
    }

    @Test
    void cleanHostUrl_omitsDefaultHttpsPort() {
        assertEquals("https://example.com", Hosts.cleanHostUrl("https://example.com:443/path"));
    }

    @Test
    void cleanHostUrl_omitsDefaultHttpPort() {
        assertEquals("http://example.com", Hosts.cleanHostUrl("http://example.com:80/path"));
    }

    @Test
    void cleanHostUrl_keepsNonDefaultPort() {
        assertEquals("https://example.com:8443", Hosts.cleanHostUrl("https://example.com:8443/"));
        assertEquals("http://example.com:8080", Hosts.cleanHostUrl("http://example.com:8080"));
    }

    @Test
    void cleanHostUrl_simpleUrl() {
        assertEquals("https://example.com", Hosts.cleanHostUrl("https://example.com"));
    }

    // --- addToUnknownHosts (with recording store) ---

    @Test
    void addToUnknownHosts_callsStoreWithCleanedUrl() {
        Hosts hosts = new Hosts();
        RecordingHostDAO store = new RecordingHostDAO();
        store.init();
        ContextMap ctx = new ContextMap();
        ctx.currentUrl = "https://example.com/page?x=1";
        ctx.hostDAO = store;

        hosts.addToUnknownHosts(ctx);

        assertEquals(List.of("https://example.com"), store.addedUrls);
    }

    @Test
    void addToUnknownHosts_onMalformedUrl_addsParsedForm() {
        // URI.create("not-a-valid-uri:::") parses as scheme "not-a-valid-uri", host null → "not-a-valid-uri://null"
        Hosts hosts = new Hosts();
        RecordingHostDAO store = new RecordingHostDAO();
        store.init();
        ContextMap ctx = new ContextMap();
        ctx.currentUrl = "not-a-valid-uri:::";
        ctx.hostDAO = store;

        hosts.addToUnknownHosts(ctx);

        assertEquals(List.of("not-a-valid-uri://null"), store.addedUrls);
    }

    // --- loadHosts (real CsvHostStore in temp dir) ---

    @Test
    void loadHosts_setsHostStoreAndCreatesDataDir(@TempDir Path tempDir) {
        Hosts hosts = new Hosts(tempDir.toString());
        ContextMap ctx = new ContextMap();
        ctx.useCsv = true;

        hosts.loadHosts(ctx);

        assertNotNull(ctx.hostDAO);
        ctx.hostDAO.addHost("https://test.example.com");
        assertTrue(Files.exists(tempDir.resolve("hosts.csv")));
    }

    @Test
    void loadHosts_withSqlite_setsSqliteStore() throws Exception {
        // Use build dir: SQLite keeps DB open so @TempDir cleanup would fail on Windows
        Path dir = Path.of("build", "test-data", "hosts-sqlite-" + UUID.randomUUID());
        Files.createDirectories(dir);
        Hosts hosts = new Hosts(dir.toString());
        ContextMap ctx = new ContextMap();
        ctx.useCsv = false;

        hosts.loadHosts(ctx);

        assertNotNull(ctx.hostDAO);
        ctx.hostDAO.addHost("https://sqlite.example.com");
        assertTrue(Files.exists(dir.resolve("hosts.db")));
    }

    // --- createHostSpecificIndex (real CsvIndexStore in temp dir) ---

    @Test
    void createHostSpecificIndex_setsIndexStoreAndCreatesFile(@TempDir Path tempDir) {
        Hosts hosts = new Hosts(tempDir.toString());
        ContextMap ctx = new ContextMap();
        ctx.useCsv = true;
        ctx.currentHost = "myhost";

        hosts.createHostSpecificIndex(ctx);

        assertNotNull(ctx.indexDAO);
        ctx.indexDAO.addPage("https://myhost.com/p1", "etag1", "hello");
        assertTrue(Files.exists(tempDir.resolve("myhost.csv")));
    }

    @Test
    void createHostSpecificIndex_withSqlite_setsSqliteIndexStore() throws Exception {
        Path dir = Path.of("build", "test-data", "index-sqlite-" + UUID.randomUUID());
        Files.createDirectories(dir);
        Hosts hosts = new Hosts(dir.toString());
        ContextMap ctx = new ContextMap();
        ctx.useCsv = false;
        ctx.currentHost = "otherhost";

        hosts.createHostSpecificIndex(ctx);

        assertNotNull(ctx.indexDAO);
        ctx.indexDAO.addPage("https://otherhost.com/p1", "e1", "text");
    }

    @Test
    void defaultConstructor_loadHosts_works() {
        Hosts hosts = new Hosts();
        ContextMap ctx = new ContextMap();
        ctx.useCsv = true;
        hosts.loadHosts(ctx);
        assertNotNull(ctx.hostDAO);
    }

    /** In-memory HostStore that records every URL passed to addHost. */
    private static final class RecordingHostDAO implements HostDAO {
        final List<String> addedUrls = new ArrayList<>();

        @Override
        public void init() {}

        @Override
        public void addHost(String url) {
            addedUrls.add(url);
        }
    }
}
