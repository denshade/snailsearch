package search;

/**
 * Port of abstr2.urls.
 * Can be instantiated for testing.
 */
public final class Urls {

    public Urls() {
    }

    public boolean isSupportedSite(ContextMap contextmap) {
        String url = contextmap.currentUrl;
        return url != null && (url.startsWith("https://") || url.startsWith("http://"));
    }

    public boolean needsBeIndexed(ContextMap contextmap) {
        String url = contextmap.currentUrl;
        URLFilter urlfilter = contextmap.urlFilter;
        RobotRules rp = contextmap.robotRules;
        if (!isSupportedSite(contextmap)) {
            return false;
        }
        if (urlfilter == null || !urlfilter.matches(url)) {
            return false;
        }
        if (rp != null && !rp.isAllowed(url)) {
            return false;
        }
        return true;
    }
}
