package search;

/**
 * Abstraction for robots.txt rules. Allows using custom implementations in tests
 * without depending on external libraries.
 */
public interface RobotRules {

    /** True if the URL is allowed to be crawled. */
    boolean isAllowed(String url);

    /** Crawl delay in milliseconds; 0 means no delay. */
    long getCrawlDelay();
}
