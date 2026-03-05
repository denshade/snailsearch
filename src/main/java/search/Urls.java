package search;

import crawlercommons.robots.BaseRobotRules;

/**
 * Port of abstr2.urls.
 */
public final class Urls {

    private Urls() {
    }

    public static boolean isSupportedSite(ContextMap contextmap) {
        String url = contextmap.currentUrl;
        return url != null && (url.startsWith("https://") || url.startsWith("http://"));
    }

    public static boolean needsBeIndexed(ContextMap contextmap) {
        String url = contextmap.currentUrl;
        URLFilter urlfilter = contextmap.urlFilter;
        BaseRobotRules rp = contextmap.robotRules;
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
