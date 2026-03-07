package search;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Urls}. Uses an instantiated {@link Urls} and in-process
 * implementations of {@link RobotRules} / {@link URLFilter} (no external library).
 */
class UrlsTest {

    private final Urls urls = new Urls();

    @Test
    void isSupportedSite_trueForHttps() {
        ContextMap ctx = new ContextMap();
        ctx.currentUrl = "https://example.com/page";
        assertTrue(urls.isSupportedSite(ctx));
    }

    @Test
    void isSupportedSite_trueForHttp() {
        ContextMap ctx = new ContextMap();
        ctx.currentUrl = "http://example.com";
        assertTrue(urls.isSupportedSite(ctx));
    }

    @Test
    void isSupportedSite_falseForNull() {
        ContextMap ctx = new ContextMap();
        ctx.currentUrl = null;
        assertFalse(urls.isSupportedSite(ctx));
    }

    @Test
    void isSupportedSite_falseForFtp() {
        ContextMap ctx = new ContextMap();
        ctx.currentUrl = "ftp://files.example.com";
        assertFalse(urls.isSupportedSite(ctx));
    }

    @Test
    void isSupportedSite_falseForPlainHost() {
        ContextMap ctx = new ContextMap();
        ctx.currentUrl = "example.com";
        assertFalse(urls.isSupportedSite(ctx));
    }

    @Test
    void needsBeIndexed_falseWhenNotSupported() {
        ContextMap ctx = new ContextMap();
        ctx.currentUrl = "ftp://x.com";
        ctx.urlFilter = new URLFilter("x", List.of());
        ctx.robotRules = null;
        assertFalse(urls.needsBeIndexed(ctx));
    }

    @Test
    void needsBeIndexed_falseWhenUrlFilterNull() {
        ContextMap ctx = new ContextMap();
        ctx.currentUrl = "https://example.com";
        ctx.urlFilter = null;
        ctx.robotRules = null;
        assertFalse(urls.needsBeIndexed(ctx));
    }

    @Test
    void needsBeIndexed_falseWhenUrlFilterDoesNotMatch() {
        ContextMap ctx = new ContextMap();
        ctx.currentUrl = "https://other.com";
        ctx.urlFilter = new URLFilter("example.com", List.of());
        ctx.robotRules = null;
        assertFalse(urls.needsBeIndexed(ctx));
    }

    @Test
    void needsBeIndexed_trueWhenSupportedFilterMatchesAndNoRobotRules() {
        ContextMap ctx = new ContextMap();
        ctx.currentUrl = "https://example.com/page";
        ctx.urlFilter = new URLFilter("example.com", List.of());
        ctx.robotRules = null;
        assertTrue(urls.needsBeIndexed(ctx));
    }

    @Test
    void needsBeIndexed_trueWhenRobotRulesAllows() {
        ContextMap ctx = new ContextMap();
        ctx.currentUrl = "https://example.com/page";
        ctx.urlFilter = new URLFilter("example.com", List.of());
        ctx.robotRules = allowAll();
        assertTrue(urls.needsBeIndexed(ctx));
    }

    @Test
    void needsBeIndexed_falseWhenRobotRulesDisallows() {
        ContextMap ctx = new ContextMap();
        ctx.currentUrl = "https://example.com/page";
        ctx.urlFilter = new URLFilter("example.com", List.of());
        ctx.robotRules = allowNone();
        assertFalse(urls.needsBeIndexed(ctx));
    }

    @Test
    void needsBeIndexed_falseWhenUrlFilterMustNotContainMatches() {
        ContextMap ctx = new ContextMap();
        ctx.currentUrl = "https://example.com/skip";
        ctx.urlFilter = new URLFilter("example.com", List.of("skip"));
        ctx.robotRules = null;
        assertFalse(urls.needsBeIndexed(ctx));
    }

    private static RobotRules allowAll() {
        return new RobotRules() {
            @Override
            public boolean isAllowed(String url) {
                return true;
            }

            @Override
            public long getCrawlDelay() {
                return 0;
            }
        };
    }

    private static RobotRules allowNone() {
        return new RobotRules() {
            @Override
            public boolean isAllowed(String url) {
                return false;
            }

            @Override
            public long getCrawlDelay() {
                return 0;
            }
        };
    }
}
