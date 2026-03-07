package search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import search.store.CsvIndexDAO;
import search.store.IndexDAO;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Index}. Uses an instantiated {@link Index} with a recording store
 * or real {@link CsvIndexDAO} in a temp dir.
 */
class IndexTest {

    @Test
    void addToIndex_callsStoreWithCurrentUrlEtagAndJoinedWordlist() {
        Index index = new Index();
        RecordingIndexDAO store = new RecordingIndexDAO();
        store.init();
        ContextMap ctx = new ContextMap();
        ctx.currentUrl = "https://example.com/page";
        ctx.indexDAO = store;
        ProcessResult result = new ProcessResult(List.of("only"), List.of(), "etag-123");

        index.addToIndex(ctx, result);

        assertEquals(1, store.pages.size());
        assertEquals("https://example.com/page", store.pages.get(0).url);
        assertEquals("etag-123", store.pages.get(0).etag);
        assertEquals("only", store.pages.get(0).text);
    }

    @Test
    void addToIndex_emptyWordlist_joinsToEmptyString() {
        Index index = new Index();
        RecordingIndexDAO store = new RecordingIndexDAO();
        store.init();
        ContextMap ctx = new ContextMap();
        ctx.currentUrl = "https://example.com/empty";
        ctx.indexDAO = store;
        ProcessResult result = new ProcessResult(List.of(), List.of(), "e1");

        index.addToIndex(ctx, result);

        assertEquals(1, store.pages.size());
        assertEquals("", store.pages.get(0).text);
    }

    @Test
    void addToIndex_nullEtag_storedAsEmpty() {
        Index index = new Index();
        RecordingIndexDAO store = new RecordingIndexDAO();
        store.init();
        ContextMap ctx = new ContextMap();
        ctx.currentUrl = "https://example.com/x";
        ctx.indexDAO = store;
        ProcessResult result = new ProcessResult(List.of("a"), List.of(), null);

        index.addToIndex(ctx, result);

        assertEquals(1, store.pages.size());
        assertEquals("", store.pages.get(0).etag);
    }

    @Test
    void isEtagInIndex_delegatesToStore() {
        Index index = new Index();
        RecordingIndexDAO store = new RecordingIndexDAO();
        store.init();
        store.pages.add(new RecordingIndexDAO.Page("https://example.com/p", "etag1", "text"));
        ContextMap ctx = new ContextMap();
        ctx.indexDAO = store;

        assertTrue(index.isEtagInIndex("https://example.com/p", "etag1", ctx));
        assertFalse(index.isEtagInIndex("https://example.com/p", "other", ctx));
        assertFalse(index.isEtagInIndex("https://other.com/p", "etag1", ctx));
    }

    @Test
    void addToIndex_and_isEtagInIndex_withRealCsvStore(@TempDir Path tempDir) throws Exception {
        String dataDir = tempDir.toString();
        Index index = new Index();
        ContextMap ctx = new ContextMap();
        ctx.indexDAO = new CsvIndexDAO(dataDir, "testhost");
        ctx.indexDAO.init();
        ctx.currentUrl = "https://testhost.com/doc";

        ProcessResult result = new ProcessResult(List.of("foo", "bar"), List.of(), "my-etag");
        index.addToIndex(ctx, result);

        assertTrue(index.isEtagInIndex("https://testhost.com/doc", "my-etag", ctx));
        assertFalse(index.isEtagInIndex("https://testhost.com/doc", "wrong-etag", ctx));

        assertTrue(Files.exists(tempDir.resolve("testhost.csv")));
    }

    /** In-memory IndexStore that records addPage and answers isEtagInIndex from recorded pages. */
    private static final class RecordingIndexDAO implements IndexDAO {
        final List<Page> pages = new ArrayList<>();

        static final class Page {
            final String url, etag, text;

            Page(String url, String etag, String text) {
                this.url = url;
                this.etag = etag;
                this.text = text;
            }
        }

        @Override
        public void init() {}

        @Override
        public void addPage(String url, String etag, String text) {
            pages.add(new Page(url, etag != null ? etag : "", text != null ? text : ""));
        }

        @Override
        public boolean isEtagInIndex(String url, String etag) {
            if (etag == null || etag.isEmpty()) return false;
            for (Page p : pages) {
                if (p.url.equals(url) && p.etag.equals(etag)) return true;
            }
            return false;
        }

        @Override
        public List<String> getUrls() {
            return List.of();
        }
    }
}
